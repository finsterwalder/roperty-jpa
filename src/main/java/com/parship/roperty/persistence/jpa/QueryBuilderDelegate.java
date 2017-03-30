package com.parship.roperty.persistence.jpa;

import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

public class QueryBuilderDelegate<T> {

    private EntityManagerFactory entityManagerFactory;

    private QueryBuilder<T> queryBuilder;

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        Validate.notNull(entityManagerFactory, "Entity manager factory must not be null");
        this.entityManagerFactory = entityManagerFactory;
    }

    public void setQueryBuilder(QueryBuilder<T> queryBuilder) {
        Validate.notNull(queryBuilder, "Query builder must not be null");
        this.queryBuilder = queryBuilder;
    }

    EntityManager createEntityManager() {
        Validate.notNull(entityManagerFactory, "Entity manager factory must not be null");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        queryBuilder.withEntityManager(entityManager);
        return entityManager;
    }

    TypedQuery<T> equality(EqualsCriterion<?>... equalsCriteria) {
        Validate.notEmpty(equalsCriteria, "Equals criteria must not be empty");
        return queryBuilder.equality(equalsCriteria);
    }

    TypedQuery<T> all() {
        Validate.notNull(queryBuilder, "Query builder must not be null");
        return queryBuilder.all();
    }

    public void setResultClass(Class<T> resultClass) {
        Validate.notNull(resultClass, "Result class must not be null");
        queryBuilder.setResultClass(resultClass);
    }

    TypedQuery<Long> count(RopertyKey ropertyKey) {
        Validate.notNull(ropertyKey, "Roperty key must not be null");
        Validate.notNull(queryBuilder, "Query builder must not be null");
        return queryBuilder.count(ropertyKey);
    }

    TypedQuery<T> likeliness(LikeCriterion... criteria) {
        Validate.notEmpty(criteria, "Like criteria must not be empty");
        return queryBuilder.likeliness(criteria);
    }
}
