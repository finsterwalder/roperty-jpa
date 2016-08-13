package com.parship.roperty.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;

public class RopertyValueDAO {

    private EntityManagerFactory entityManagerFactory;

    public List<RopertyValue> loadRopertyValues(RopertyKey ropertyKey) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EqualsCriterion<RopertyKey> equalsCriterion = new EqualsCriterion<RopertyKey>()
                .withAttributeName("key")
                .withComparison(ropertyKey);

        TypedQuery<RopertyValue> typedQuery = new QueryBuilder<RopertyValue>()
                .withEntityManager(entityManager)
                .withResultClass(RopertyValue.class)
                .equality(equalsCriterion);
        List<RopertyValue> ropertyValues = typedQuery.getResultList();
        entityManager.close();

        return ropertyValues;
    }


    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public RopertyValue loadRopertyValue(RopertyKey ropertyKey, String pattern, Object value) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        EqualsCriterion<RopertyKey> keyCriterion = new EqualsCriterion<RopertyKey>()
                .withAttributeName("key")
                .withComparison(ropertyKey);

        EqualsCriterion<String> patternCriterion = new EqualsCriterion<String>()
                .withAttributeName("pattern")
                .withComparison(pattern);

        EqualsCriterion<Object> valueCriterion = new EqualsCriterion<>()
                .withAttributeName("value")
                .withComparison(value);

        TypedQuery<RopertyValue> typedQuery = new QueryBuilder<RopertyValue>()
                .withEntityManager(entityManager)
                .withResultClass(RopertyValue.class)
                .equality(keyCriterion, patternCriterion, valueCriterion);

        RopertyValue ropertyValue = typedQuery.getSingleResult();
        entityManager.close();

        return ropertyValue;
    }
}
