package com.parship.roperty.persistence.jpa;

import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

public class RopertyValueDAO {

    private QueryBuilderDelegate<RopertyValue> queryBuilderDelegate;

    public Long getNumberOfValues(RopertyKey ropertyKey) {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        EntityManager entityManager = queryBuilderDelegate.createEntityManager();
        Validate.notNull(entityManager, "Entity manager must not be null");
        TypedQuery<Long> typedQuery = queryBuilderDelegate.count(ropertyKey);
        if (typedQuery == null) {
            entityManager.close();
            throw new RopertyPersistenceException(String.format("Typed query for counting of key '%s' must not be null", ropertyKey.getId()));
        }
        Long result = typedQuery.getSingleResult();
        if (result == null) {
            throw new RopertyPersistenceException("Single result of query must not be null");
        }
        entityManager.close();
        return result;
    }

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

    public RopertyValue loadRopertyValue(RopertyKey ropertyKey, String pattern) {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        EntityManager entityManager = queryBuilderDelegate.createEntityManager();
        Validate.notNull(entityManager, "Entity manager must not be null");

        EqualsCriterion<RopertyKey> keyCriterion = new EqualsCriterion<RopertyKey>()
                .withAttributeName("key")
                .withComparison(ropertyKey);

        EqualsCriterion<String> patternCriterion = new EqualsCriterion<String>()
                .withAttributeName("pattern")
                .withComparison(pattern);

        TypedQuery<RopertyValue> typedQuery = queryBuilderDelegate.equality(keyCriterion, patternCriterion);
        if (typedQuery == null) {
            entityManager.close();
            throw new RopertyPersistenceException(String.format("Typed query for equality of key '%s' must not be null", ropertyKey.getId()));
        }

        List<RopertyValue> ropertyValues = typedQuery.getResultList();
        int numValues = ropertyValues.size();
        RopertyValue ropertyValue;
        if (numValues == 0) {
            ropertyValue = null;
        } else if (numValues == 1) {
            ropertyValue = ropertyValues.get(0);
        } else {
            throw new RopertyPersistenceException(String.format("More than one database entry found for key '%s' and pattern '%s'", ropertyKey, pattern));
        }

        entityManager.close();

        return ropertyValue;
    }

    public void setQueryBuilderDelegate(QueryBuilderDelegate<RopertyValue> queryBuilderDelegate) {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        this.queryBuilderDelegate = queryBuilderDelegate;
    }
}
