package com.parship.roperty.persistence;

import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

public class RopertyValueDAO {

    private QueryBuilderDelegate<RopertyValue> queryBuilderDelegate;

    public List<RopertyValue> loadRopertyValues(RopertyKey ropertyKey) {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        EntityManager entityManager = queryBuilderDelegate.createEntityManager();
        Validate.notNull(entityManager, "Entity manager must not be null");
        EqualsCriterion<RopertyKey> equalsCriterion = new EqualsCriterion<RopertyKey>()
                .withAttributeName("key")
                .withComparison(ropertyKey);

        TypedQuery<RopertyValue> typedQuery = queryBuilderDelegate.equality(equalsCriterion);
        if (typedQuery == null) {
            entityManager.close();
            throw new RopertyPersistenceException(String.format("Typed query for equality of key '%s' must not be null", ropertyKey.getId()));
        }

        List<RopertyValue> ropertyValues = typedQuery.getResultList();
        entityManager.close();

        Validate.notNull(ropertyValues, "Result list of Roperty values for key '%s' was null", ropertyKey.getId());

        return Collections.unmodifiableList(ropertyValues);
    }

    public RopertyValue loadRopertyValue(RopertyKey ropertyKey, String pattern, Object value) {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        EntityManager entityManager = queryBuilderDelegate.createEntityManager();
        Validate.notNull(entityManager, "Entity manager must not be null");

        EqualsCriterion<RopertyKey> keyCriterion = new EqualsCriterion<RopertyKey>()
                .withAttributeName("key")
                .withComparison(ropertyKey);

        EqualsCriterion<String> patternCriterion = new EqualsCriterion<String>()
                .withAttributeName("pattern")
                .withComparison(pattern);

        EqualsCriterion<Object> valueCriterion = new EqualsCriterion<>()
                .withAttributeName("value")
                .withComparison(value);

        TypedQuery<RopertyValue> typedQuery = queryBuilderDelegate.equality(keyCriterion, patternCriterion, valueCriterion);
        if (typedQuery == null) {
            entityManager.close();
            throw new RopertyPersistenceException(String.format("Typed query for equality of key '%s' must not be null", ropertyKey.getId()));
        }

        RopertyValue ropertyValue = typedQuery.getSingleResult();
        if (ropertyValue != null) {
            entityManager.detach(ropertyValue);
        }
        entityManager.close();

        return ropertyValue;
    }

    public void setQueryBuilderDelegate(QueryBuilderDelegate<RopertyValue> queryBuilderDelegate) {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        this.queryBuilderDelegate = queryBuilderDelegate;
    }
}
