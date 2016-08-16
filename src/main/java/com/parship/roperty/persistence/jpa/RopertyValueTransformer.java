package com.parship.roperty.persistence.jpa;

import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;

public class RopertyValueTransformer {

    private KeyValuesFactory keyValuesFactory;
    private DomainSpecificValueFactory domainSpecificValueFactory;

    public KeyValues transformValues(List<RopertyValue> ropertyValues) {
        if (ropertyValues.isEmpty()) {
            return null;
        }

        KeyValues keyValues = keyValuesFactory.create(domainSpecificValueFactory);
        Validate.notNull(keyValues, "Key values must not be null");

        for (RopertyValue ropertyValue : ropertyValues) {
            String pattern = ropertyValue.getPattern();
            RopertyKey key = ropertyValue.getKey();
            Validate.notNull(pattern, "Pattern of value with key '%s' may not be null", key);
            String[] domainKeyParts;
            if (StringUtils.isEmpty(pattern)) {
                domainKeyParts = new String[0];
            } else {
                domainKeyParts = pattern.split("\\|");
            }
            Object value = ropertyValue.getValue();
            String changeSet = ropertyValue.getChangeSet();
            if (changeSet == null) {
                keyValues.put(value, domainKeyParts);
            } else {
                keyValues.putWithChangeSet(changeSet, value, domainKeyParts);
            }

            Validate.notNull(key, "Key of value '%s' for pattern '%s' may not be null", value, pattern);
            keyValues.setDescription(key.getDescription());
        }

        return keyValues;
    }


    public RopertyValueTransformer withKeyValuesFactory(KeyValuesFactory keyValuesFactory) {
        this.keyValuesFactory = keyValuesFactory;
        return this;
    }

    public RopertyValueTransformer withDomainSpecificValueFactory(DomainSpecificValueFactory domainSpecificValueFactory) {
        this.domainSpecificValueFactory = domainSpecificValueFactory;
        return this;
    }
}
