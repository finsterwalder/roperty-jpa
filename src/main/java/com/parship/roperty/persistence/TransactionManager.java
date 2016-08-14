package com.parship.roperty.persistence;

import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class TransactionManager {

    private EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    private EntityTransaction transaction;

    private boolean transactionStarted;

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        Validate.notNull(entityManagerFactory, "Entity manager factory must not be null");
        this.entityManagerFactory = entityManagerFactory;
    }

    public void begin() {
        Validate.notNull(entityManagerFactory, "Entity manager factory must not be null");
        entityManager = entityManagerFactory.createEntityManager();
        Validate.notNull(entityManager, "Entity manager must not be null");
        transaction = entityManager.getTransaction();
        Validate.notNull(transaction, "Entity manager didn't return a transaction");
        transaction.begin();
        transactionStarted = true;
    }

    public void end() {
        Validate.isTrue(transactionStarted, "No transaction started yet. You need to call begin first");
        Validate.notNull(transaction, "Transaction must not be null");
        Validate.notNull(entityManager, "Entity manager must not be null");
        transaction.commit();
        entityManager.close();
        transactionStarted = false;
    }

    public void merge(Object object) {
        Validate.isTrue(transactionStarted, "No transaction started yet. You need to call begin first");
        Validate.notNull(entityManager, "Entity manager must not be null");
        Validate.notNull(object, "Object must not be null");
        entityManager.merge(object);
    }

    public boolean contains(Object object) {
        Validate.isTrue(transactionStarted, "No transaction started yet. You need to call begin first");
        Validate.notNull(entityManager, "Entity manager must not be null");
        Validate.notNull(object, "Object must not be null");
        return entityManager.contains(object);
    }

    public void persist(Object object) {
        Validate.isTrue(transactionStarted, "No transaction started yet. You need to call begin first");
        Validate.notNull(entityManager, "Entity manager must not be null");
        Validate.notNull(object, "Object must not be null");
        entityManager.persist(object);
    }

    public void remove(Object object) {
        Validate.isTrue(transactionStarted, "No transaction started yet. You need to call begin first");
        Validate.notNull(entityManager, "Entity manager must not be null");
        Validate.notNull(object, "Object must not be null");
        Object attachedEntity;
        if (entityManager.contains(object)) {
            attachedEntity = object;
        } else {
            attachedEntity = entityManager.merge(object);
        }
        entityManager.remove(attachedEntity);
    }
}
