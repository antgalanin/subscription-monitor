package com.subscriptionmonitor.scheduler;

import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.model.enums.Currency;
import com.subscriptionmonitor.model.enums.NotificationType;
import com.subscriptionmonitor.repository.NotificationRepository;
import com.subscriptionmonitor.repository.PaymentRepository;
import com.subscriptionmonitor.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentProcessor Tests")
class PaymentProcessorTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private PaymentProcessor paymentProcessor;

    private Subscription overdueSubscription(LocalDate nextBillingDate, int periodDays) {
        Payment payment = new Payment(new BigDecimal("100.00"), Currency.RUB, periodDays, nextBillingDate);
        payment.setId(UUID.randomUUID());

        Subscription subscription = new Subscription();
        subscription.setId(UUID.randomUUID());
        subscription.setUserId(UUID.randomUUID());
        subscription.setCategoryId(UUID.randomUUID());
        subscription.setName("Overdue");
        subscription.setIsActive(true);
        subscription.setPayment(payment);
        return subscription;
    }

    @Test
    @DisplayName("За каждый пропущенный период создаётся отдельное уведомление")
    void testNotificationPerMissedPeriod() {
        LocalDate start = LocalDate.now().minusDays(65);
        Subscription subscription = overdueSubscription(start, 30);
        when(subscriptionRepository.findActiveWithPayment()).thenReturn(List.of(subscription));

        paymentProcessor.processOverduePayments();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(3)).save(captor.capture());

        List<LocalDateTime> dates = captor.getAllValues().stream()
                .map(Notification::getNotificationDate)
                .toList();
        assertEquals(List.of(
                start.atStartOfDay(),
                start.plusDays(30).atStartOfDay(),
                start.plusDays(60).atStartOfDay()
        ), dates);
    }

    @Test
    @DisplayName("Дата следующего списания сдвигается в будущее")
    void testNextBillingDateMovesToFuture() {
        LocalDate start = LocalDate.now().minusDays(65);
        Subscription subscription = overdueSubscription(start, 30);
        when(subscriptionRepository.findActiveWithPayment()).thenReturn(List.of(subscription));

        paymentProcessor.processOverduePayments();

        LocalDate updated = subscription.getPayment().getNextBillingDate();
        assertEquals(start.plusDays(90), updated);
        assertTrue(updated.isAfter(LocalDate.now()));
        verify(paymentRepository).save(subscription.getPayment());
    }

    @Test
    @DisplayName("Уже существующее уведомление за дату не дублируется")
    void testExistingNotificationIsNotDuplicated() {
        LocalDate start = LocalDate.now().minusDays(35);
        Subscription subscription = overdueSubscription(start, 30);
        when(subscriptionRepository.findActiveWithPayment()).thenReturn(List.of(subscription));
        when(notificationRepository.findTopBySubscriptionIdAndTypeAndNotificationDateBetweenOrderByCreatedAtDesc(
                eq(subscription.getId()),
                eq(NotificationType.PAYMENT_SUCCESSFUL),
                eq(start.atStartOfDay()),
                any(LocalDateTime.class)))
                .thenReturn(new Notification());

        paymentProcessor.processOverduePayments();

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Подписка с датой в будущем не обрабатывается")
    void testFutureSubscriptionIsUntouched() {
        Subscription subscription = overdueSubscription(LocalDate.now().plusDays(5), 30);
        when(subscriptionRepository.findActiveWithPayment()).thenReturn(List.of(subscription));

        paymentProcessor.processOverduePayments();

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}
