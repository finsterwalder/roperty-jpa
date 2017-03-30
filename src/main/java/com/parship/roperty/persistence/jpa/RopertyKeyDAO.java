package com.parship.roperty.persistence.jpa;

import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RopertyKeyDAO {

    private QueryBuilderDelegate<RopertyKey> queryBuilderDelegate;

    RopertyKey loadRopertyKey(String key) {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        EntityManager entityManager = queryBuilderDelegate.createEntityManager();
        Validate.notNull(entityManager, "Entity manager must not be null");
        RopertyKey ropertyKey = entityManager.find(RopertyKey.class, key);
        entityManager.close();
        return ropertyKey;
    }

    List<RopertyKey> loadAllRopertyKeys() {
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

    public void setQueryBuilderDelegate(QueryBuilderDelegate<RopertyKey> queryBuilderDelegate) {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        this.queryBuilderDelegate = queryBuilderDelegate;
    }

    List<String> findKeys(String substring) {
        Validate.notNull(queryBuilderDelegate, "Query builder delegate must not be null");
        EntityManager entityManager = queryBuilderDelegate.createEntityManager();
        Validate.notNull(entityManager, "Entity manager must not be null");

        LikeCriterion idCriterion = new LikeCriterion()
                .withAttributeName("id")
                .withExpression("%" + substring + "%");

        TypedQuery<RopertyKey> typedQuery = queryBuilderDelegate.likeliness(idCriterion);

        if (typedQuery == null) {
            entityManager.close();
            throw new RopertyPersistenceException("Typed query must not be null");
        }

        List<RopertyKey> ropertyKeys = typedQuery.getResultList();
        List<String> result = new ArrayList<>(ropertyKeys.size());
        ropertyKeys.forEach((key) -> result.add(key.getId()));
        entityManager.close();
        return Collections.unmodifiableList(result);
    }

}
