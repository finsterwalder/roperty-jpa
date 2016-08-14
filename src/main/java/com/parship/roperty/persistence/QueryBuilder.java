package com.parship.roperty.persistence;

import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

public class QueryBuilder<X> {

    private Class<X> resultClass;

    private EntityManager entityManager;

    public <Y> TypedQuery<X> equality(EqualsCriterion... equalsCriteria) {
        Validate.notNull(entityManager, "Entity manager must not be null");
        Validate.notNull(resultClass, "Result class must not be null");
        Validate.notEmpty(equalsCriteria, "At least one equals criterion should be given");

        for (EqualsCriterion<Y> equalsCriterion : equalsCriteria) {
            Validate.notEmpty(equalsCriterion.getAttributeName(), "Attribute name of equals criterion must no be blank");
            Validate.notNull(equalsCriterion.getComparison(), "A comparison value must exist. It is currently null");
        }

        Metamodel metamodel = entityManager.getMetamodel();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<X> query = criteriaBuilder.createQuery(resultClass);
        EntityType<X> entityType = metamodel.entity(resultClass);
        Root<X> root = query.from(entityType);
        int numRestrictions = equalsCriteria.length;
        Predicate[] restrictions = new Predicate[numRestrictions];
        for (int i = 0; i < numRestrictions; i++) {
            EqualsCriterion<Y> equalsCriterion = equalsCriteria[i];
            String attributeName = equalsCriterion.getAttributeName();
            SingularAttribute<? super X, ?> singularAttribute = entityType.getSingularAttribute(attributeName);
            Path<?> path = root.get(singularAttribute);
            Y comparison = equalsCriterion.getComparison();
            Predicate restriction = criteriaBuilder.equal(path, comparison);
            restrictions[i] = restriction;
        }
        query.where(criteriaBuilder.and(restrictions));
        return entityManager.createQuery(query);
    }

    public TypedQuery<X> all() {
        Validate.notNull(entityManager, "Entity manager must not be null");
        Validate.notNull(resultClass, "Result class must not be null");

        Metamodel metamodel = entityManager.getMetamodel();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<X> query = criteriaBuilder.createQuery(resultClass);
        EntityType<X> entityType = metamodel.entity(resultClass);
        Root<X> root = query.from(entityType);
        query.select(root);
        return entityManager.createQuery(query);
    }

    public QueryBuilder<X> withResultClass(Class<X> resultClass) {
        Validate.notNull(resultClass, "Result class must not be null");
        this.resultClass = resultClass;
        return this;
    }

    public QueryBuilder<X> withEntityManager(EntityManager entityManager) {
        Validate.notNull(entityManager, "Entity manager must not be null");
        this.entityManager = entityManager;
        return this;
    }

}
