package com.parship.roperty.persistence.jpa;

import com.parship.roperty.DomainSpecificValue;
import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import com.parship.roperty.Persistence;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.HashMap;
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

        storeDomainSpecificValues(ropertyKey, domainSpecificValues, changeSet);

        transactionManager.end();
    }

    private void storeDomainSpecificValues(RopertyKey key, Iterable<DomainSpecificValue> domainSpecificValues, String changeSet) {
        String transformedChangeSet = emptyWhenNull(changeSet);
        for (DomainSpecificValue domainSpecificValue : domainSpecificValues) {
            Object rawValue = domainSpecificValue.getValue();
            String patternStr = domainSpecificValue.getPatternStr();
            if (patternStr == null) {
                transactionManager.end();
                throw new RopertyPersistenceException(String.format("Pattern for key '%s' must not be null", key.getId()));
            }
            if (domainSpecificValue.changeSetIs(nullWhenEmpty(changeSet))) {
                storeRopertyValue(key, patternStr, rawValue, transformedChangeSet);
            }
        }
    }

    private void storeRopertyValue(RopertyKey ropertyKey, String pattern, Object value, String changeSet) {
        RopertyValue ropertyValue = ropertyValueDAO.loadRopertyValue(ropertyKey, pattern, changeSet);
        if (ropertyValue == null) {
            RopertyValue newRopertyValue = createRopertyValue(ropertyKey, changeSet, value, pattern);
            transactionManager.persist(newRopertyValue);
        } else {
            boolean merge = mergeRopertyValue(ropertyValue, changeSet, value);
            if (merge) {
                transactionManager.merge(ropertyValue);
            }
        }
    }

    private static boolean mergeRopertyValue(RopertyValue original, String newChangeSet, Object newValue) {
        boolean merge = false;
        if (!Objects.equals(original.getChangeSet(), newChangeSet)) {
            original.setChangeSet(newChangeSet);
            merge = true;
        }
        if (!Objects.equals(original.getValue(), newValue)) {
            if (newValue == null) {
                original.setValue(null);
                merge = true;
            } else if (Serializable.class.isAssignableFrom(newValue.getClass())) {
                Serializable value = (Serializable) newValue;
                original.setValue(value);
                merge = true;
            } else {
                throw new RopertyPersistenceException(String.format("Cannot serialize value '%s'", newValue));
            }
        }
        return merge;
    }

    private static RopertyValue createRopertyValue(RopertyKey key, String changeSet, Object value, String pattern) {
        RopertyValue newRopertyValue = new RopertyValue();
        newRopertyValue.setKey(key);
        if (value != null && Serializable.class.isAssignableFrom(value.getClass())) {
            Serializable serializableValue = (Serializable) value;
            newRopertyValue.setValue(serializableValue);
        } else {
            throw new RopertyPersistenceException(String.format("Cannot serialize value '%s'", value));
        }
        newRopertyValue.setPattern(pattern);
        newRopertyValue.setChangeSet(changeSet);
        return newRopertyValue;
    }

    private static String nullWhenEmpty(String string) {
        if (StringUtils.isEmpty(string)) {
            return null;
        }

        return string;
    }

    private static String emptyWhenNull(String changeSet) {
        if (changeSet == null) {
            return "";
        }

        return changeSet;
    }

    @Override
    public void remove(String key, KeyValues keyValues, String changeSet) {
        Validate.notBlank(key, "Key must not be empty");

        transactionManager.begin();

        RopertyKey ropertyKey = ropertyKeyDAO.loadRopertyKey(key);
        if (ropertyKey == null) {
            transactionManager.end();
            return;
        }

        List<RopertyValue> ropertyValues = new LinkedList<>(ropertyValueDAO.loadRopertyValues(ropertyKey));
        if (ropertyValues.isEmpty()) {
            transactionManager.end();
            throw new RopertyPersistenceException(String.format("Could not find any values for key '%s'. This is an inconsistency that should not happen.", ropertyKey.getId()));
        }

        if (keyValues == null) {
            for (RopertyValue value : ropertyValues) {
                transactionManager.remove(value);
            }
            transactionManager.remove(ropertyKey);
        } else {
            removeKeyValues(ropertyKey, keyValues, ropertyValues);
        }

        transactionManager.end();
    }

    private void removeKeyValues(RopertyKey ropertyKey, KeyValues keyValues, List<RopertyValue> ropertyValues) {
        Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        int numDomainSpecificValues = domainSpecificValues.size();
        if (numDomainSpecificValues == 0) {
            transactionManager.end();
            throw new RopertyPersistenceException(String.format("Key values for key '%s' must contain domain specific values", ropertyKey.getId()));
        }

        int numRemovedValues = 0;

        for (DomainSpecificValue domainSpecificValue : domainSpecificValues) {
            for (Iterator<RopertyValue> itRopertyValue = ropertyValues.iterator(); itRopertyValue.hasNext(); ) {
                RopertyValue ropertyValue = itRopertyValue.next();
                if (ropertyValue.equals(domainSpecificValue)) {
                    transactionManager.remove(ropertyValue);
                    itRopertyValue.remove();
                    numRemovedValues++;
                }
            }
        }

        if (numDomainSpecificValues == numRemovedValues) {
            transactionManager.remove(ropertyKey);
        }
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

        RopertyValue ropertyValue = ropertyValueDAO.loadRopertyValue(ropertyKey, patternStr, emptyWhenNull(changeSet));
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
