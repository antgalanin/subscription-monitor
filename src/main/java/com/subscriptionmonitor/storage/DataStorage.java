package com.subscriptionmonitor.storage;

import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.entity.Subscription;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DataStorage {
    private static DataStorage instance;

    private final ConcurrentHashMap<Long, User> users;
    private final ConcurrentHashMap<Long, Category> categories;
    private final ConcurrentHashMap<Long, Subscription> subscriptions;

    private final AtomicLong userIdGenerator;
    private final AtomicLong categoryIdGenerator;
    private final AtomicLong subscriptionIdGenerator;

    private DataStorage() {
        this.users = new ConcurrentHashMap<>();
        this.categories = new ConcurrentHashMap<>();
        this.subscriptions = new ConcurrentHashMap<>();

        this.userIdGenerator = new AtomicLong(1);
        this.categoryIdGenerator = new AtomicLong(1);
        this.subscriptionIdGenerator = new AtomicLong(1);
    }

    public static DataStorage getInstance() {
        if (instance == null) {
            synchronized (DataStorage.class) {
                if (instance == null) {
                    instance = new DataStorage();
                }
            }
        }
        return instance;
    }

    public ConcurrentHashMap<Long, User> getUsers() {
        return users;
    }

    public ConcurrentHashMap<Long, Category> getCategories() {
        return categories;
    }

    public ConcurrentHashMap<Long, Subscription> getSubscriptions() {
        return subscriptions;
    }

    public Long generateUserId() {
        return userIdGenerator.getAndIncrement();
    }

    public Long generateCategoryId() {
        return categoryIdGenerator.getAndIncrement();
    }

    public Long generateSubscriptionId() {
        return subscriptionIdGenerator.getAndIncrement();
    }

    public void clearAll() {
        users.clear();
        categories.clear();
        subscriptions.clear();
        userIdGenerator.set(1);
        categoryIdGenerator.set(1);
        subscriptionIdGenerator.set(1);
    }

    public int getTotalUsersCount() {
        return users.size();
    }

    public int getTotalCategoriesCount() {
        return categories.size();
    }

    public int getTotalSubscriptionsCount() {
        return subscriptions.size();
    }
}