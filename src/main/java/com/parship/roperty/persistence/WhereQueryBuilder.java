package com.parship.roperty.persistence;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

public class WhereQueryBuilder<T> {

    private Class<T> resultClass;

    private Object comparison;

    private EntityManager entityManager;

    private String attributeName;

    public TypedQuery<T> build() {
        Metamodel metamodel = entityManager.getMetamodel();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(resultClass);

        EntityType<T> entityType = metamodel.entity(resultClass);
        Root<T> root = query.from(entityType);

        query.where(criteriaBuilder.equal(root.get(entityType.getSingularAttribute(attributeName)), comparison));
        return entityManager.createQuery(query);
    }

    public void setResultClass(Class<T> resultClass) {
        this.resultClass = resultClass;
    }

    public void setComparison(Object comparison) {
        this.comparison = comparison;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
}
