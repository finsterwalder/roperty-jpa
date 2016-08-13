package com.parship.roperty.persistence;

import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class RelationalTransactionManager {

    private EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    private EntityTransaction transaction;

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        Validate.notNull(entityManagerFactory, "Entity manager factory must not be null");
        this.entityManagerFactory = entityManagerFactory;
    }

    public void begin() {
        entityManager = entityManagerFactory.createEntityManager();
        transaction = entityManager.getTransaction();
        Validate.notNull(transaction, "Entity manager didn't return a transaction");
        transaction.begin();
    }

    public void end() {
        transaction.commit();
        entityManager.close();
    }

    public void persist(Object object) {
        Validate.notNull(object, "Object must not be null");
        entityManager.persist(object);
    }

    public void remove(Object entity) {
        Object attachedEntity;
        if (entityManager.contains(entity)) {
            attachedEntity = entity;
        } else {
            attachedEntity = entityManager.merge(entity);
        }

        entityManager.remove(attachedEntity);
    }
}
