package ru.nsu.fit.endpoint.service.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import ru.nsu.fit.endpoint.service.database.DBService;
import ru.nsu.fit.endpoint.service.database.data.Customer;
import ru.nsu.fit.endpoint.service.database.data.Plan;
import ru.nsu.fit.endpoint.service.database.data.Subscription;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SubscriptionManagerTest {
    private DBService dbService;
    private Logger logger;
    private SubscriptionManager subscriptionManager;

    private Subscription subscriptionBeforeCreateMethod;
    private Subscription subscriptionAfterCreateMethod;
    private Customer customer;
    private Plan plan;

    @BeforeEach
    public void before() {
        // create stubs for the test's class
        dbService = Mockito.mock(DBService.class);
        logger = Mockito.mock(Logger.class);

        subscriptionBeforeCreateMethod = Subscription.builder()
                .id(null)
                .customerId(UUID.randomUUID())
                .planId(UUID.randomUUID())
                .build();
        subscriptionAfterCreateMethod = subscriptionBeforeCreateMethod.clone();
        subscriptionAfterCreateMethod.setId(UUID.randomUUID());

        customer = Customer.builder()
                .balance(1000)
                .build();

        plan = Plan.builder()
                .fee(42)
                .build();

        Mockito.when(dbService.createSubscription(subscriptionBeforeCreateMethod)).thenReturn(subscriptionAfterCreateMethod);

        // create the test's class
        subscriptionManager = new SubscriptionManager(dbService, logger);
    }

    @Test
    void testCreateSubscription() {
        when(dbService.getCustomerById(subscriptionBeforeCreateMethod.getCustomerId())).thenReturn(customer);
        when(dbService.getPlanById(subscriptionBeforeCreateMethod.getPlanId())).thenReturn(plan);

        // Вызываем метод, который хотим протестировать
        Subscription subscription = subscriptionManager.createSubscription(subscriptionBeforeCreateMethod);

        // Проверяем результат выполенния метода
        assertEquals(subscription.getId(), subscriptionAfterCreateMethod.getId());

        // Проверяем, что метод мока базы данных был вызван 1 раз
        verify(dbService, times(1)).createSubscription(subscriptionBeforeCreateMethod);
    }




}
