package com.parship.roperty.persistence.jpa;

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

    TypedQuery<X> equality(EqualsCriterion<?>... equalsCriteria) {
        Validate.notNull(entityManager, "Entity manager must not be null");
        Validate.notNull(resultClass, "Result class must not be null");
        Validate.notEmpty(equalsCriteria, "At least one equals criterion should be given");

        for (EqualsCriterion<?> equalsCriterion : equalsCriteria) {
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
            EqualsCriterion<?> equalsCriterion = equalsCriteria[i];
            String attributeName = equalsCriterion.getAttributeName();
            SingularAttribute<? super X, ?> singularAttribute = entityType.getSingularAttribute(attributeName);
            Path<?> path = root.get(singularAttribute);
            Predicate restriction = criteriaBuilder.equal(path, equalsCriterion.getComparison());
            restrictions[i] = restriction;
        }
        query.where(criteriaBuilder.and(restrictions));
        return entityManager.createQuery(query);
    }

    TypedQuery<X> all() {
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

    void setResultClass(Class<X> resultClass) {
        Validate.notNull(resultClass, "Result class must not be null");
        this.resultClass = resultClass;
    }

    void withEntityManager(EntityManager entityManager) {
        Validate.notNull(entityManager, "Entity manager must not be null");
        this.entityManager = entityManager;
    }

    TypedQuery<Long> count(RopertyKey ropertyKey) {
        Validate.notNull(entityManager, "Entity manager must not be null");
        Validate.notNull(resultClass, "Result class must not be null");

        Metamodel metamodel = entityManager.getMetamodel();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        EntityType<X> entityType = metamodel.entity(resultClass);
        Root<X> root = query.from(entityType);

        query.select(criteriaBuilder.count(root));
        query.where(criteriaBuilder.equal(root.get(entityType.getSingularAttribute("key")), ropertyKey));

        return entityManager.createQuery(query);
    }

    TypedQuery<X> likeliness(LikeCriterion... criteria) {
        Validate.notNull(entityManager, "Entity manager must not be null");
        Validate.notNull(resultClass, "Result class must not be null");
        Validate.notEmpty(criteria, "At least one like criterion should be given");

        for (LikeCriterion criterion : criteria) {
            Validate.notEmpty(criterion.getAttributeName(), "Attribute name of equals criterion must no be blank");
            Validate.notNull(criterion.getExpression(), "An expression must exist. It is currently null");
        }

        Metamodel metamodel = entityManager.getMetamodel();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<X> query = criteriaBuilder.createQuery(resultClass);
        EntityType<X> entityType = metamodel.entity(resultClass);
        Root<X> root = query.from(entityType);
        int numRestrictions = criteria.length;
        Predicate[] restrictions = new Predicate[numRestrictions];
        for (int i = 0; i < numRestrictions; i++) {
            LikeCriterion criterion = criteria[i];
            String attributeName = criterion.getAttributeName();
            SingularAttribute<? super X, String> singularAttribute = entityType.getSingularAttribute(attributeName, String.class);
            Path<String> path = root.get(singularAttribute);
            String expression = criterion.getExpression();
            Predicate restriction = criteriaBuilder.like(criteriaBuilder.lower(path), expression.toLowerCase());
            restrictions[i] = restriction;
        }
        query.where(criteriaBuilder.and(restrictions));
        return entityManager.createQuery(query);
    }
}
