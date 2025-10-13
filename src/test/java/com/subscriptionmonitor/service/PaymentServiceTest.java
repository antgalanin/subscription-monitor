package com.subscriptionmonitor.service;

import com.subscriptionmonitor.exception.PaymentNotFoundException;
import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.enums.Currency;
import com.subscriptionmonitor.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment testPayment;
    private UUID testPaymentId;
    private UUID payment1Id;
    private UUID payment2Id;

    @BeforeEach
    void setUp() {
        testPaymentId = UUID.randomUUID();
        payment1Id = UUID.randomUUID();
        payment2Id = UUID.randomUUID();

        testPayment = new Payment(new BigDecimal("999.99"), Currency.RUB, 30, LocalDate.now().plusDays(30));
        testPayment.setId(testPaymentId);
    }

    @Test
    @DisplayName("Создание платежа с корректными данными")
    void testCreatePayment_Success() throws Exception {
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        Payment created = paymentService.create(testPayment);

        assertNotNull(created);
        assertEquals(testPaymentId, created.getId());
        assertEquals(new BigDecimal("999.99"), created.getCost());
        assertEquals(Currency.RUB, created.getCurrency());
        assertEquals(30, created.getBillingPeriodDays());

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Поиск платежа по ID")
    void testFindById_Success() throws Exception {
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));

        Payment found = paymentService.findById(testPaymentId);

        assertNotNull(found);
        assertEquals(testPaymentId, found.getId());
        assertEquals(new BigDecimal("999.99"), found.getCost());

        verify(paymentRepository, times(1)).findById(testPaymentId);
    }

    @Test
    @DisplayName("Поиск платежа по несуществующему ID")
    void testFindById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(paymentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> {
            paymentService.findById(nonExistentId);
        });

        verify(paymentRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Получение всех платежей")
    void testFindAll() {
        Payment payment1 = new Payment(new BigDecimal("100.00"), Currency.RUB, 30, LocalDate.now());
        payment1.setId(payment1Id);
        Payment payment2 = new Payment(new BigDecimal("50.00"), Currency.USD, 7, LocalDate.now());
        payment2.setId(payment2Id);

        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment1, payment2));

        List<Payment> payments = paymentService.findAll();

        assertEquals(2, payments.size());
        assertTrue(payments.stream().anyMatch(p -> p.getCost().equals(new BigDecimal("100.00"))));
        assertTrue(payments.stream().anyMatch(p -> p.getCost().equals(new BigDecimal("50.00"))));

        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Поиск платежей по валюте")
    void testFindByCurrency() {
        Payment payment1 = new Payment(new BigDecimal("100.00"), Currency.RUB, 30, LocalDate.now());
        payment1.setId(payment1Id);
        Payment payment2 = new Payment(new BigDecimal("75.00"), Currency.RUB, 30, LocalDate.now());
        payment2.setId(payment2Id);

        when(paymentRepository.findByCurrency(Currency.RUB))
                .thenReturn(Arrays.asList(payment1, payment2));

        List<Payment> rubPayments = paymentService.findByCurrency(Currency.RUB);

        assertEquals(2, rubPayments.size());
        assertTrue(rubPayments.stream().allMatch(p -> p.getCurrency() == Currency.RUB));

        verify(paymentRepository, times(1)).findByCurrency(Currency.RUB);
    }

    @Test
    @DisplayName("Поиск платежей по диапазону дат следующего списания")
    void testFindByNextBillingDateBetween() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        Payment payment1 = new Payment(new BigDecimal("100.00"), Currency.RUB, 30, startDate.plusDays(3));
        payment1.setId(payment1Id);

        when(paymentRepository.findByNextBillingDateBetween(startDate, endDate))
                .thenReturn(Arrays.asList(payment1));

        List<Payment> payments = paymentService.findByNextBillingDateBetween(startDate, endDate);

        assertEquals(1, payments.size());
        assertTrue(payments.get(0).getNextBillingDate().isAfter(startDate.minusDays(1)));
        assertTrue(payments.get(0).getNextBillingDate().isBefore(endDate.plusDays(1)));

        verify(paymentRepository, times(1)).findByNextBillingDateBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Поиск платежей до определенной даты")
    void testFindByNextBillingDateBefore() {
        LocalDate date = LocalDate.now().plusDays(7);

        Payment payment1 = new Payment(new BigDecimal("100.00"), Currency.RUB, 30, LocalDate.now().plusDays(3));
        payment1.setId(payment1Id);

        when(paymentRepository.findByNextBillingDateBefore(date))
                .thenReturn(Arrays.asList(payment1));

        List<Payment> payments = paymentService.findByNextBillingDateBefore(date);

        assertEquals(1, payments.size());
        assertTrue(payments.get(0).getNextBillingDate().isBefore(date));

        verify(paymentRepository, times(1)).findByNextBillingDateBefore(date);
    }

    @Test
    @DisplayName("Обновление платежа")
    void testUpdatePayment_Success() throws Exception {
        Payment updatedPayment = new Payment(new BigDecimal("1500.00"), Currency.USD, 30, LocalDate.now().plusDays(30));
        updatedPayment.setId(testPaymentId);

        when(paymentRepository.existsById(testPaymentId)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

        Payment updated = paymentService.update(updatedPayment);

        assertEquals(new BigDecimal("1500.00"), updated.getCost());
        assertEquals(Currency.USD, updated.getCurrency());
        assertEquals(testPaymentId, updated.getId());

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Удаление платежа")
    void testDeletePayment_Success() throws Exception {
        when(paymentRepository.existsById(testPaymentId)).thenReturn(true);
        doNothing().when(paymentRepository).deleteById(testPaymentId);

        paymentService.delete(testPaymentId);

        verify(paymentRepository, times(1)).deleteById(testPaymentId);
    }

    @Test
    @DisplayName("Удаление всех платежей")
    void testDeleteAll() {
        doNothing().when(paymentRepository).deleteAll();

        paymentService.deleteAll();

        verify(paymentRepository, times(1)).deleteAll();
    }
}
