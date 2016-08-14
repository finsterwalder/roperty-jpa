package com.parship.roperty.persistence;

import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

public class RopertyKeyDAO {

    private QueryBuilderDelegate<RopertyKey> queryBuilderDelegate;

    public RopertyKey loadRopertyKey(String key) {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        EqualsCriterion<String> equalsCriterion = new EqualsCriterion<String>()
                .withAttributeName("id")
                .withComparison(key);

        EntityManager entityManager = queryBuilderDelegate.createEntityManager();
        Validate.notNull(entityManager, "Entity manager must not be null");
        TypedQuery<RopertyKey> typedQuery = queryBuilderDelegate.equality(equalsCriterion);
        if (typedQuery == null) {
            entityManager.close();
            throw new RopertyPersistenceException(String.format("Typed query for equality of key '%s' must not be null", key));
        }

        List<RopertyKey> ropertyKeys = typedQuery.getResultList();

        Validate.notNull(ropertyKeys, "Result list of Roperty keys for identifier '%s' was null", key);

        int numResults = ropertyKeys.size();

        RopertyKey ropertyKey;
        if (numResults == 0) {
            ropertyKey = null;
        } else if (numResults == 1) {
            ropertyKey = ropertyKeys.get(0);
            entityManager.detach(ropertyKey);
        } else {
            entityManager.close();
            throw new RopertyPersistenceException("More than one Roperty key was found in database.");
        }

        entityManager.close();
        return ropertyKey;
    }

    public List<RopertyKey> loadAllRopertyKeys() {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        EntityManager entityManager = queryBuilderDelegate.createEntityManager();
        Validate.notNull(entityManager, "Entity manager must not be null");
        TypedQuery<RopertyKey> typedQuery = queryBuilderDelegate.all();

        if (typedQuery == null) {
            entityManager.close();
            throw new RopertyPersistenceException("Typed query must not be null");
        }

        List<RopertyKey> ropertyKeys = typedQuery.getResultList();
        entityManager.close();
        return Collections.unmodifiableList(ropertyKeys);
    }

    public void setQueryBuilderDelegate(QueryBuilderDelegate queryBuilderDelegate) {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        this.queryBuilderDelegate = queryBuilderDelegate;
    }
}
