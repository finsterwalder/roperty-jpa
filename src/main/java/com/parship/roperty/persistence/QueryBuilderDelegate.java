package com.parship.roperty.persistence;

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

    public EntityManager createEntityManager() {
        Validate.notNull(entityManagerFactory, "Entity manager factory must not be null");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        queryBuilder.withEntityManager(entityManager);
        return entityManager;
    }

    public <Y> TypedQuery<T> equality(EqualsCriterion... equalsCriteria) {
        Validate.notEmpty(equalsCriteria, "Equals criteria must not be empty");
        return queryBuilder.equality(equalsCriteria);
    }

    public TypedQuery<T> all() {
        Validate.notNull(queryBuilder, "Query builder must not be null");
        return queryBuilder.all();
    }

    public void setResultClass(Class<T> resultClass) {
        Validate.notNull(resultClass, "Result class must not be null");
        queryBuilder.withResultClass(resultClass);
    }
}
