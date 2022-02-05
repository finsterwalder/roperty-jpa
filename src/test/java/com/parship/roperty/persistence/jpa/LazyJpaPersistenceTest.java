package com.parship.roperty.persistence.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
        verifyNoInteractions(keyValuesFactory, domainSpecificValueFactory);
    }

    @Test
    public void loadAllResultShouldBeReadOnly() {
        Map<String, KeyValues> result = lazyJpaPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);
        assertThrows(UnsupportedOperationException.class, () -> result.put(KEY, keyValues));
    }

    @Test
    public void reloadShouldReturnEmptyMap() {
        Map<String, KeyValues> keyValuesMap = new HashMap<>(1);
        keyValuesMap.put(KEY, keyValues);
        Map<String, KeyValues> result = lazyJpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);
        assertThat(result.isEmpty(), is(true));
        assertThat(keyValuesMap.size(), is(1));
        assertThat(keyValuesMap.get(KEY), is(keyValues));
        verifyNoInteractions(keyValuesFactory, domainSpecificValueFactory);
    }

    @Test
    public void reloadResultShouldBeReadOnly() {
        Map<String, KeyValues> keyValuesMap = new HashMap<>(0);
        Map<String, KeyValues> result = lazyJpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);
        assertThrows(UnsupportedOperationException.class, () -> result.put(KEY, keyValues));
    }

}
