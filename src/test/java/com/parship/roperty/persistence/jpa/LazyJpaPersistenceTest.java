package com.parship.roperty.persistence.jpa;

import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class LazyJpaPersistenceTest {

    public static final String KEY = "key";

    @InjectMocks
    private LazyJpaPersistence lazyJpaPersistence = new LazyJpaPersistence();

    @Mock
    private KeyValuesFactory keyValuesFactory;

    @Mock
    private DomainSpecificValueFactory domainSpecificValueFactory;

    @Mock
    private KeyValues keyValues;

    @Test
    public void loadAllShouldReturnEmptyMap() {
        Map<String, KeyValues> result = lazyJpaPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);
        assertThat(result.isEmpty(), is(true));
        verifyZeroInteractions(keyValuesFactory, domainSpecificValueFactory);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void loadAllResultShouldBeReadOnly() {
        Map<String, KeyValues> result = lazyJpaPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);
        result.put(KEY, keyValues);
    }

    @Test
    public void reloadShouldReturnEmptyMap() {
        Map<String, KeyValues> keyValuesMap = new HashMap<>(1);
        keyValuesMap.put(KEY, keyValues);
        Map<String, KeyValues> result = lazyJpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);
        assertThat(result.isEmpty(), is(true));
        assertThat(keyValuesMap.size(), is(1));
        assertThat(keyValuesMap.get(KEY), is(keyValues));
        verifyZeroInteractions(keyValuesFactory, domainSpecificValueFactory);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void reloadResultShouldBeReadOnly() {
        Map<String, KeyValues> keyValuesMap = new HashMap<>(0);
        Map<String, KeyValues> result = lazyJpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);
        result.put(KEY, keyValues);
    }

}
