package com.parship.roperty.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;

public class RopertyKeyDAO {

    private EntityManagerFactory entityManagerFactory;

    public RopertyKey loadRopertyKey(String key) {
        EqualsCriterion<String> equalsCriterion = new EqualsCriterion<String>()
                .withAttributeName("id")
                .withComparison(key);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<RopertyKey> typedQuery = new QueryBuilder<RopertyKey>()
                .withResultClass(RopertyKey.class)
                .withEntityManager(entityManager)
                .equality(equalsCriterion);

        List<RopertyKey> ropertyKeys = typedQuery.getResultList();
        int numResults = ropertyKeys.size();

        RopertyKey ropertyKey;
        if (numResults == 0) {
            ropertyKey = null;
        } else if (numResults == 1) {
            ropertyKey = ropertyKeys.get(0);
        } else {
            throw new RopertyPersistenceException("More than one Roperty key was found in database.");
        }

        entityManager.close();
        return ropertyKey;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public List<RopertyKey> loadAllRopertyKeys() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<RopertyKey> typedQuery = new QueryBuilder<RopertyKey>()
                .withEntityManager(entityManager)
                .withResultClass(RopertyKey.class)
                .all();
        List<RopertyKey> ropertyKeys = typedQuery.getResultList();
        entityManager.close();
        return ropertyKeys;
    }
}
