package com.parship.roperty.persistence;

import com.parship.roperty.DomainSpecificValue;
import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import com.parship.roperty.Persistence;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RelationalPersistence implements Persistence {

    private RopertyKeyDAO ropertyKeyDAO;

    private RopertyValueDAO ropertyValueDAO;

    private RelationalTransactionManager transactionManager;

    @Override
    public KeyValues load(String key, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory) {
        Validate.notBlank(key, "Key must not be empty");
        Validate.notNull(keyValuesFactory, "Key values factory must no be null");
        Validate.notNull(domainSpecificValueFactory, "Domain specific value factory must not be null");

        RopertyKey ropertyKey = ropertyKeyDAO.loadRopertyKey(key);
        if (ropertyKey == null) {
            return null;
        }

        return load(ropertyKey, keyValuesFactory, domainSpecificValueFactory);
    }

    private KeyValues load(RopertyKey ropertyKey, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory) {
        Validate.notNull(ropertyKey, "Roperty key must not be null");

        List<RopertyValue> ropertyValues = ropertyValueDAO.loadRopertyValues(ropertyKey);
        Validate.notEmpty(ropertyValues, "Could not find any values for key '%s'", ropertyKey.getId());

        return new RopertyValueTransformer()
                .withDomainSpecificValueFactory(domainSpecificValueFactory)
                .withKeyValuesFactory(keyValuesFactory)
                .transformValues(ropertyValues);
    }

    @Override
    public Map<String, KeyValues> loadAll(KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory) {
        Validate.notNull(keyValuesFactory, "Key values factory must no be null");
        Validate.notNull(domainSpecificValueFactory, "Domain specific value factory must not be null");

        List<RopertyKey> ropertyKeys = ropertyKeyDAO.loadAllRopertyKeys();
        Map<String, KeyValues> keyValuesMap = new HashMap<>(ropertyKeys.size());
        for (RopertyKey ropertyKey : ropertyKeys) {
            String key = ropertyKey.getId();
            KeyValues keyValues = load(ropertyKey, keyValuesFactory, domainSpecificValueFactory);
            Validate.notNull(keyValues, "No values found for key '%s'", key);
            keyValuesMap.put(key, keyValues);
        }

        return keyValuesMap;
    }

    @Override
    public Map<String, KeyValues> reload(Map<String, KeyValues> keyValuesMap, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory) {
        Validate.notNull(keyValuesFactory, "Key values factory must no be null");
        Validate.notNull(domainSpecificValueFactory, "Domain specific value factory must not be null");

        Map<String, KeyValues> result = new HashMap<>(keyValuesMap.size());

        for (String key : keyValuesMap.keySet()) {
            KeyValues keyValues = load(key, keyValuesFactory, domainSpecificValueFactory);
            result.put(key, keyValues);
        }

        return result;
    }

    @Override
    public void store(String key, KeyValues keyValues, String changeSet) {
        Validate.notBlank(key, "Key must not be empty");
        Validate.notNull(keyValues, "Key values must not be null");

        transactionManager.begin();

        RopertyKey ropertyKey = ropertyKeyDAO.loadRopertyKey(key);
        if (ropertyKey == null) {
            ropertyKey = new RopertyKey();
            ropertyKey.setId(key);
            ropertyKey.setDescription(keyValues.getDescription());
            transactionManager.persist(ropertyKey);
        }

        Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        for (DomainSpecificValue domainSpecificValue : domainSpecificValues) {
            RopertyValue ropertyValue = new RopertyValue();
            ropertyValue.setKey(ropertyKey);
            ropertyValue.setValue((Serializable) domainSpecificValue.getValue());
            ropertyValue.setChangeSet(changeSet);
            ropertyValue.setPattern(domainSpecificValue.getPatternStr());
            transactionManager.persist(ropertyValue);
        }

        transactionManager.end();
    }

    @Override
    public void remove(String key, KeyValues keyValues, String changeSet) {
        Validate.notBlank(key, "Key must not be empty");
        Validate.notNull(keyValues, "Key values must not be null");

        RopertyKey ropertyKey = ropertyKeyDAO.loadRopertyKey(key);
        if (ropertyKey == null) {
            return;
        }

        List<RopertyValue> ropertyValues = ropertyValueDAO.loadRopertyValues(ropertyKey);
        if (ropertyValues.isEmpty()) {
            return;
        }

        Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        int numDomainSpecificValues = domainSpecificValues.size();
        if (numDomainSpecificValues == 0) {
            return;
        }

        int numRemovedValues = 0;

        transactionManager.begin();

        for (DomainSpecificValue domainSpecificValue : domainSpecificValues) {
            for (RopertyValue ropertyValue : ropertyValues) {
                if (ropertyValue.equals(domainSpecificValue)) {
                    transactionManager.remove(ropertyValue);
                    numRemovedValues++;
                }
            }
        }

        if (numDomainSpecificValues == numRemovedValues) {
            transactionManager.remove(ropertyKey);
        }

        transactionManager.end();
    }

    @Override
    public void remove(String key, DomainSpecificValue domainSpecificValue, String changeSet) {
        Validate.notBlank(key, "Key must not be empty");
        Validate.notNull(domainSpecificValue, "Domain specific value must not be null");

        RopertyKey ropertyKey = ropertyKeyDAO.loadRopertyKey(key);
        if (ropertyKey == null) {
            return;
        }

        RopertyValue ropertyValue = ropertyValueDAO.loadRopertyValue(ropertyKey, domainSpecificValue.getPatternStr(), domainSpecificValue.getValue());
        if (ropertyValue == null) {
            return;
        }

        transactionManager.begin();
        transactionManager.remove(ropertyValue);
        transactionManager.end();
    }

    public void setTransactionManager(RelationalTransactionManager transactionManager) {
        Validate.notNull(transactionManager, "Transaction manager must not be null");
        this.transactionManager = transactionManager;
    }

    public void setRopertyKeyDAO(RopertyKeyDAO ropertyKeyDAO) {
        Validate.notNull(ropertyKeyDAO, "Roperty key DAO must not be null");
        this.ropertyKeyDAO = ropertyKeyDAO;
    }

    public void setRopertyValueDAO(RopertyValueDAO ropertyValueDAO) {
        Validate.notNull(ropertyValueDAO, "Roperty value DAO must no be null");
        this.ropertyValueDAO = ropertyValueDAO;
    }
}
