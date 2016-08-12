package com.parship.roperty.persistence;

import com.parship.roperty.DomainSpecificValue;
import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import com.parship.roperty.Persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RelationalPersistence implements Persistence {

    private EntityManagerFactory entityManagerFactory;

    @Override
    public KeyValues load(String key, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory) {
        RopertyKey ropertyKey = loadRopertyKey(key);

        List<RopertyValue> ropertyValues = loadRopertyValues(ropertyKey);
        KeyValues keyValues = keyValuesFactory.create(domainSpecificValueFactory);
        keyValues.setDescription(ropertyKey.getDescription());

        for (RopertyValue ropertyValue : ropertyValues) {
            String[] domainKeyParts = ropertyValue.getPattern().split("\\|");
            keyValues.putWithChangeSet(ropertyValue.getChangeSet(), ropertyValue.getValue(), domainKeyParts);
        }

        return keyValues;
    }

    private List<RopertyValue> loadRopertyValues(RopertyKey ropertyKey) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        WhereQueryBuilder<RopertyValue> whereQueryBuilder = new WhereQueryBuilder<>();
        whereQueryBuilder.setAttributeName("key");
        whereQueryBuilder.setComparison(ropertyKey);
        whereQueryBuilder.setEntityManager(entityManager);
        whereQueryBuilder.setResultClass(RopertyValue.class);

        TypedQuery<RopertyValue> typedQuery = whereQueryBuilder.build();
        List<RopertyValue> ropertyValues = typedQuery.getResultList();

        entityManager.close();
        return ropertyValues;
    }



    private RopertyKey loadRopertyKey(String key) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        WhereQueryBuilder<RopertyKey> whereQueryBuilder = new WhereQueryBuilder<>();
        whereQueryBuilder.setAttributeName("id");
        whereQueryBuilder.setComparison(key);
        whereQueryBuilder.setEntityManager(entityManager);
        whereQueryBuilder.setResultClass(RopertyKey.class);

        TypedQuery<RopertyKey> typedQuery = whereQueryBuilder.build();
        RopertyKey ropertyKey = typedQuery.getSingleResult();

        entityManager.close();
        return ropertyKey;
    }

    @Override
    public Map<String, KeyValues> loadAll(KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory) {
        return null;
    }

    @Override
    public Map<String, KeyValues> reload(Map<String, KeyValues> keyValuesMap, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory) {
        return null;
    }

    @Override
    public void store(String key, KeyValues keyValues, String changeSet) {
        RelationalTransactionManager transactionManager = createTransactionManager();
        EntityManager entityManager = transactionManager.begin();

        RopertyKey ropertyKey = new RopertyKey();
        ropertyKey.setId(key);
        ropertyKey.setDescription(keyValues.getDescription());

        entityManager.persist(ropertyKey);

        Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        for (DomainSpecificValue domainSpecificValue : domainSpecificValues) {
            RopertyValue ropertyValue = new RopertyValue();
            ropertyValue.setKey(ropertyKey);
            ropertyValue.setValue((Serializable) domainSpecificValue.getValue());
            ropertyValue.setChangeSet(changeSet);
            ropertyValue.setPattern(domainSpecificValue.getPatternStr());
            entityManager.persist(ropertyValue);
        }

        transactionManager.end();
    }

    private RelationalTransactionManager createTransactionManager() {
        RelationalTransactionManager transactionManager = new RelationalTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Override
    public void remove(String key, KeyValues keyValues, String changeSet) {

    }

    @Override
    public void remove(String key, DomainSpecificValue domainSpecificValue, String changeSet) {

    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
}
