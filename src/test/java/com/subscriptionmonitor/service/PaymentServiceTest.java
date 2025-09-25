package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.enums.Currency;
import com.subscriptionmonitor.storage.DataStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    private PaymentService paymentService;
    private DataStorage storage;

    @BeforeEach
    void setUp() {
        storage = DataStorage.getInstance();
        storage.clearAll();
        paymentService = new PaymentService();
    }

    @Test
    @DisplayName("Создание платежа с корректными данными")
    void testCreatePayment_Success() {
        Payment payment = new Payment(new BigDecimal("100"), Currency.USD, 30, LocalDate.now().plusDays(30));

        Payment created = paymentService.create(payment);

        assertNotNull(created);
        assertNotNull(created.getUuid());
        assertEquals(new BigDecimal("100"), created.getCost());
        assertEquals(Currency.USD, created.getCurrency());
        assertEquals(30, created.getBillingPeriodDays());
        assertEquals(1, paymentService.getTotalCount());
    }

    @Test
    @DisplayName("Создание платежа с null")
    void testCreatePayment_Null() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> paymentService.create(null));

        assertEquals("Payment cannot be null", exception.getMessage());
        assertEquals(0, paymentService.getTotalCount());
    }

    @Test
    @DisplayName("Создание платежа с отрицательной стоимостью")
    void testCreatePayment_NegativeCost() {
        Payment payment = new Payment(new BigDecimal("-100"), Currency.RUB, 30, LocalDate.now());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> paymentService.create(payment));

        assertTrue(exception.getMessage().contains("Cost cannot be null or negative"));
        assertEquals(0, paymentService.getTotalCount());
    }

    @Test
    @DisplayName("Поиск платежа по ID")
    void testFindById_Success() {
        Payment payment = new Payment(new BigDecimal("200"), Currency.EUR, 30, LocalDate.now());
        Payment created = paymentService.create(payment);

        Optional<Payment> found = paymentService.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals(new BigDecimal("200"), found.get().getCost());
    }

    @Test
    @DisplayName("Поиск платежа по UUID")
    void testFindByUuid_Success() {
        Payment payment = new Payment(new BigDecimal("200"), Currency.EUR, 30, LocalDate.now());
        Payment created = paymentService.create(payment);

        Optional<Payment> found = paymentService.findByUuid(created.getUuid());

        assertTrue(found.isPresent());
        assertEquals(created.getUuid(), found.get().getUuid());
        assertEquals(new BigDecimal("200"), found.get().getCost());
    }

    @Test
    @DisplayName("Поиск платежа по несуществующему ID")
    void testFindById_NotFound() {
        Optional<Payment> found = paymentService.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Поиск платежа по несуществующему UUID")
    void testFindByUuid_NotFound() {
        Optional<Payment> found = paymentService.findByUuid(UUID.randomUUID());

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Получение всех платежей")
    void testFindAll() {
        Payment payment1 = new Payment(new BigDecimal("100"), Currency.RUB, 30, LocalDate.now());
        Payment payment2 = new Payment(new BigDecimal("50"), Currency.USD, 7, LocalDate.now());

        paymentService.create(payment1);
        paymentService.create(payment2);

        List<Payment> payments = paymentService.findAll();

        assertEquals(2, payments.size());
    }

    @Test
    @DisplayName("Обновление платежа")
    void testUpdatePayment_Success() {
        Payment payment = new Payment(new BigDecimal("100"), Currency.RUB, 30, LocalDate.now());
        Payment created = paymentService.create(payment);

        created.setCost(new BigDecimal("150"));
        created.setCurrency(Currency.USD);

        Payment updated = paymentService.update(created);

        assertEquals(new BigDecimal("150"), updated.getCost());
        assertEquals(Currency.USD, updated.getCurrency());
        assertEquals(created.getUuid(), updated.getUuid());
    }

    @Test
    @DisplayName("Удаление платежа по ID")
    void testDeletePaymentById_Success() {
        Payment payment = new Payment(new BigDecimal("100"), Currency.RUB, 30, LocalDate.now());
        Payment created = paymentService.create(payment);

        boolean deleted = paymentService.deleteById(created.getId());

        assertTrue(deleted);
        assertEquals(0, paymentService.getTotalCount());
        assertFalse(paymentService.findById(created.getId()).isPresent());
    }

    @Test
    @DisplayName("Удаление платежа по UUID")
    void testDeletePaymentByUuid_Success() {
        Payment payment = new Payment(new BigDecimal("100"), Currency.RUB, 30, LocalDate.now());
        Payment created = paymentService.create(payment);

        boolean deleted = paymentService.deleteByUuid(created.getUuid());

        assertTrue(deleted);
        assertEquals(0, paymentService.getTotalCount());
        assertFalse(paymentService.findByUuid(created.getUuid()).isPresent());
    }

    @Test
    @DisplayName("Поиск платежей по валюте")
    void testFindByCurrency() {
        Payment payment1 = new Payment(new BigDecimal("100"), Currency.RUB, 30, LocalDate.now());
        Payment payment2 = new Payment(new BigDecimal("50"), Currency.USD, 30, LocalDate.now());
        Payment payment3 = new Payment(new BigDecimal("75"), Currency.RUB, 30, LocalDate.now());

        paymentService.create(payment1);
        paymentService.create(payment2);
        paymentService.create(payment3);

        List<Payment> rubPayments = paymentService.findByCurrency(Currency.RUB);
        List<Payment> usdPayments = paymentService.findByCurrency(Currency.USD);

        assertEquals(2, rubPayments.size());
        assertEquals(1, usdPayments.size());
    }

    @Test
    @DisplayName("Расчет общей суммы по валюте")
    void testCalculateTotalByCurrency() {
        Payment payment1 = new Payment(new BigDecimal("100"), Currency.RUB, 30, LocalDate.now());
        Payment payment2 = new Payment(new BigDecimal("50"), Currency.USD, 30, LocalDate.now());
        Payment payment3 = new Payment(new BigDecimal("75"), Currency.RUB, 30, LocalDate.now());

        paymentService.create(payment1);
        paymentService.create(payment2);
        paymentService.create(payment3);

        BigDecimal totalRub = paymentService.calculateTotalByCurrency(Currency.RUB);
        BigDecimal totalUsd = paymentService.calculateTotalByCurrency(Currency.USD);

        assertEquals(new BigDecimal("175"), totalRub);
        assertEquals(new BigDecimal("50"), totalUsd);
    }
}