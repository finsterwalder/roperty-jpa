package com.parship.roperty.persistence;

import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;

import java.util.Collections;
import java.util.Map;

public class LazyJpaPersistence extends JpaPersistence {

    @Override
    public Map<String, KeyValues> loadAll(KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, KeyValues> reload(Map<String, KeyValues> keyValuesMap, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory) {
        return Collections.emptyMap();
    }

}
