package com.parship.roperty.persistence;

import com.parship.roperty.DomainSpecificValue;
import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import com.parship.roperty.Persistence;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class JpaPersistence implements Persistence {

    private RopertyKeyDAO ropertyKeyDAO;

    private RopertyValueDAO ropertyValueDAO;

    private TransactionManager transactionManager;

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
        Validate.notNull(keyValuesFactory, "Key values factory must no be null");
        Validate.notNull(domainSpecificValueFactory, "Domain specific value factory must not be null");

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
            if (keyValues != null) {
                result.put(key, keyValues);
            }
        }

        return result;
    }

    @Override
    public void store(String key, KeyValues keyValues, String changeSet) {
        Validate.notBlank(key, "Key must not be empty");
        Validate.notNull(keyValues, "Key values must not be null");
        Validate.notNull(transactionManager, "Transaction manager must no be null");

        transactionManager.begin();

        RopertyKey ropertyKey = ropertyKeyDAO.loadRopertyKey(key);
        String description = keyValues.getDescription();
        if (ropertyKey == null) {
            ropertyKey = new RopertyKey();
            ropertyKey.setId(key);
            ropertyKey.setDescription(description);
            transactionManager.persist(ropertyKey);
        }

        Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        if (domainSpecificValues == null) {
            transactionManager.end();
            throw new RopertyPersistenceException(String.format("Domain specific values were null for key values with description '%s'", description));
        }
        if (domainSpecificValues.isEmpty()) {
            transactionManager.end();
            throw new RopertyPersistenceException(String.format("Domain specific values were empty for key values with description '%s'", description));
        }

        for (DomainSpecificValue domainSpecificValue : domainSpecificValues) {

            Object rawValue = domainSpecificValue.getValue();
            String patternStr = domainSpecificValue.getPatternStr();
            if (patternStr == null) {
                transactionManager.end();
                throw new RopertyPersistenceException(String.format("Pattern for key '%s' must not be null", key));
            }
            RopertyValue ropertyValue = ropertyValueDAO.loadRopertyValue(ropertyKey, patternStr);
            if (ropertyValue == null) {
                ropertyValue = new RopertyValue();
                ropertyValue.setKey(ropertyKey);
                if (rawValue != null && Serializable.class.isAssignableFrom(rawValue.getClass())) {
                    Serializable value = (Serializable) rawValue;
                    ropertyValue.setValue(value);
                } else {
                    throw new RopertyPersistenceException(String.format("Cannot serialize value '%s'", rawValue));
                }
                ropertyValue.setPattern(patternStr);
                ropertyValue.setChangeSet(nullWhenEmpty(changeSet));
                transactionManager.persist(ropertyValue);
            } else {
                boolean merge = false;
                if (!Objects.equals(ropertyValue.getChangeSet(), changeSet)) {
                    ropertyValue.setChangeSet(nullWhenEmpty(changeSet));
                    merge = true;
                }
                if (!Objects.equals(ropertyValue.getValue(), rawValue)) {
                    if (rawValue == null) {
                        ropertyValue.setValue(null);
                        merge = true;
                    } else if (Serializable.class.isAssignableFrom(rawValue.getClass())) {
                        Serializable value = (Serializable) rawValue;
                        ropertyValue.setValue(value);
                        merge = true;
                    } else {
                        throw new RopertyPersistenceException(String.format("Cannot serialize value '%s'", rawValue));
                    }
                }
                if (merge) {
                    transactionManager.merge(ropertyValue);
                }
            }
        }

        transactionManager.end();
    }

    private static String nullWhenEmpty(String changeSet) {
        if (StringUtils.isEmpty(changeSet)) {
            return null;
        }

        return changeSet;
    }

    @Override
    public void remove(String key, KeyValues keyValues, String changeSet) {
        Validate.notBlank(key, "Key must not be empty");
        Validate.notNull(keyValues, "Key values must not be null");

        transactionManager.begin();

        RopertyKey ropertyKey = ropertyKeyDAO.loadRopertyKey(key);
        if (ropertyKey == null) {
            transactionManager.end();
            return;
        }

        List<RopertyValue> ropertyValues = new LinkedList<>(ropertyValueDAO.loadRopertyValues(ropertyKey));
        if (ropertyValues.isEmpty()) {
            transactionManager.end();
            throw new RopertyPersistenceException(String.format("Could not find any values for key '%s'. This is an inconsistency that should not happen.", key));
        }

        Set<DomainSpecificValue> domainSpecificValues = new HashSet<>(keyValues.getDomainSpecificValues());
        int numDomainSpecificValues = domainSpecificValues.size();
        if (numDomainSpecificValues == 0) {
            transactionManager.end();
            throw new RopertyPersistenceException(String.format("Key values for key '%s' must contain domain specific values", key));
        }

        int numRemovedValues = 0;

        for (Iterator<DomainSpecificValue> itDomainSpecificValue = domainSpecificValues.iterator(); itDomainSpecificValue.hasNext(); ) {
            DomainSpecificValue domainSpecificValue = itDomainSpecificValue.next();
            for (Iterator<RopertyValue> itRopertyValue = ropertyValues.iterator(); itRopertyValue.hasNext(); ) {
                RopertyValue ropertyValue = itRopertyValue.next();
                if (ropertyValue.equals(domainSpecificValue)) {
                    transactionManager.remove(ropertyValue);
                    itRopertyValue.remove();
                    itDomainSpecificValue.remove();
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

        transactionManager.begin();

        RopertyKey ropertyKey = ropertyKeyDAO.loadRopertyKey(key);
        if (ropertyKey == null) {
            transactionManager.end();
            return;
        }

        long numValues = ropertyValueDAO.getNumberOfValues(ropertyKey);

        String patternStr = domainSpecificValue.getPatternStr();

        Object value = domainSpecificValue.getValue();
        if (value == null) {
            transactionManager.end();
            throw new RopertyPersistenceException(String.format("Value for key '%s' must not be null", key));
        }
        if (!Serializable.class.isAssignableFrom(value.getClass())) {
            transactionManager.end();
            throw new RopertyPersistenceException(String.format("Domain specific value '%s' for key '%s' must be serializable", value, key));
        }

        RopertyValue ropertyValue = ropertyValueDAO.loadRopertyValue(ropertyKey, patternStr);
        if (ropertyValue == null) {
            transactionManager.end();
            return;
        }

        transactionManager.remove(ropertyValue);

        if (numValues == 1) {
            transactionManager.remove(ropertyKey);
        }

        transactionManager.end();
    }

    public void setTransactionManager(TransactionManager transactionManager) {
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
