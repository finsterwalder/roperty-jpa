package com.parship.roperty.persistence.jpa;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.parship.roperty.DomainSpecificValue;
import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JpaPersistenceTest {

    private static final String KEY = "key";
    private static final String CHANGE_SET = "changeSet";
    private static final String DOMAIN_KEY_PART_1 = "domainKeyPart1";
    private static final String DOMAIN_KEY_PART_2 = "domainKeyPart2";
    private static final String PATTERN = DOMAIN_KEY_PART_1 + '|' + DOMAIN_KEY_PART_2;
    private static final String DESCRIPTION = "description";

    @InjectMocks
    private JpaPersistence jpaPersistence;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private RopertyKeyDAO ropertyKeyDAO;

    @Mock
    private RopertyValueDAO ropertyValueDAO;

    @Mock
    private KeyValuesFactory keyValuesFactory;

    @Mock
    private DomainSpecificValueFactory domainSpecificValueFactory;

    @Mock
    private RopertyKey ropertyKey;

    @Mock
    private RopertyValue ropertyValue;

    @Mock
    private KeyValues keyValues;

    @Mock
    private Serializable value;

    @Mock
    private DomainSpecificValue domainSpecificValue;

    @Test
    public void loadShouldReturnNullIfNoRopertyKeyFound() {
        KeyValues keyValues = jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
        assertThat(keyValues, Matchers.nullValue());
    }

    @Test
    public void loadShouldFailNullIfNoRopertyValuesFound() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        assertThrows(IllegalArgumentException.class, () -> jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory));
    }

    @Test
    public void failIfRopertyValuePatternIsNull() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(singletonList(ropertyValue));

        assertThrows(NullPointerException.class, () -> jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory));
    }

    @Test
    public void failIfKeyValuesIsNull() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(singletonList(ropertyValue));

        assertThrows(NullPointerException.class, () -> jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory));
    }

    @Test
    public void failIfRopertyValueHasNoKey() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(singletonList(ropertyValue));
        when(ropertyValue.getPattern()).thenReturn("pattern");
        when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);

        assertThrows(NullPointerException.class, () -> jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory));
    }

    @Test
    public void loadShouldReturnKeyValues() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(singletonList(ropertyValue));
        when(ropertyValue.getPattern()).thenReturn(PATTERN);
        when(ropertyValue.getKey()).thenReturn(ropertyKey);
        when(ropertyValue.getChangeSet()).thenReturn(CHANGE_SET);
        when(ropertyValue.getValue()).thenReturn(value);
        when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);
        when(ropertyKey.getDescription()).thenReturn(DESCRIPTION);

        KeyValues result = jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        verify(keyValuesFactory).create(domainSpecificValueFactory);
        verify(ropertyKey).getDescription();
        verify(ropertyValue).getPattern();
        verify(ropertyValue).getValue();
        verify(ropertyValue).getKey();
        verify(ropertyValue).getChangeSet();
        verify(keyValues).putWithChangeSet(CHANGE_SET, value, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2);
        verify(keyValues).setDescription(DESCRIPTION);
        assertThat(result, Matchers.is(keyValues));
    }

    @Test
    public void failIfLoadAllAndNotKeyGiven() {
        when(ropertyKeyDAO.loadAllRopertyKeys()).thenReturn(singletonList(ropertyKey));
        assertThrows(IllegalArgumentException.class, () -> jpaPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory));
    }

    @Test
    public void failIfNoValuesFound() {
        when(ropertyKeyDAO.loadAllRopertyKeys()).thenReturn(singletonList(ropertyKey));
        when(ropertyKey.getId()).thenReturn(KEY);

        assertThrows(IllegalArgumentException.class, () -> jpaPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory));
    }

    @Test
    public void loadAll() {
        when(ropertyKeyDAO.loadAllRopertyKeys()).thenReturn(singletonList(ropertyKey));
        when(ropertyKey.getId()).thenReturn(KEY);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(singletonList(ropertyValue));
        when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);
        when(ropertyValue.getPattern()).thenReturn(PATTERN);
        when(ropertyValue.getKey()).thenReturn(ropertyKey);
        when(ropertyValue.getChangeSet()).thenReturn(CHANGE_SET);
        when(ropertyValue.getValue()).thenReturn(value);
        when(ropertyKey.getDescription()).thenReturn(DESCRIPTION);

        Map<String, KeyValues> result = jpaPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);

        verify(ropertyKeyDAO).loadAllRopertyKeys();
        verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        verify(keyValuesFactory).create(domainSpecificValueFactory);
        verify(ropertyKey, times(2)).getId();
        verify(ropertyKey).getDescription();
        verify(ropertyValue).getPattern();
        verify(ropertyValue).getKey();
        verify(ropertyValue).getValue();
        verify(ropertyValue).getChangeSet();
        verify(ropertyValue).getValue();
        verify(keyValues).putWithChangeSet(CHANGE_SET, value, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2);
        verify(keyValues).setDescription(DESCRIPTION);
        assertThat(result.get(KEY), Matchers.is(keyValues));
        assertThat(result.size(), Matchers.is(1));
    }

    @Test
    public void reloadWithEmptyMapReturnsEmptyMap() {
        Map<String, KeyValues> keyValuesMap = new HashMap<>();

        Map<String, KeyValues> result = jpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);

        assertThat(result.isEmpty(), Matchers.is(true));
    }

    @Test
    public void doNotIncludeEntryIfKeyDoesNoLongerExist() {
        Map<String, KeyValues> keyValuesMap = new HashMap<>();
        keyValuesMap.put(KEY, keyValues);

        Map<String, KeyValues> result = jpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);

        assertThat(result.isEmpty(), Matchers.is(true));
    }

    @Test
    public void reloadingReplacesOldValueWithNewValue() {
        KeyValues oldKeyValues = mock(KeyValues.class);
        Map<String, KeyValues> keyValuesMap = new HashMap<>();
        keyValuesMap.put(KEY, oldKeyValues);
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(singletonList(ropertyValue));
        when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);
        when(ropertyValue.getKey()).thenReturn(ropertyKey);
        when(ropertyValue.getPattern()).thenReturn(PATTERN);
        when(ropertyValue.getChangeSet()).thenReturn(CHANGE_SET);
        when(ropertyValue.getValue()).thenReturn(value);
        when(ropertyKey.getDescription()).thenReturn(DESCRIPTION);
        when(ropertyKey.getId()).thenReturn(KEY);

        Map<String, KeyValues> result = jpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        verify(keyValuesFactory).create(domainSpecificValueFactory);
        verify(ropertyValue).getKey();
        verify(ropertyValue).getPattern();
        verify(ropertyValue).getChangeSet();
        verify(ropertyValue).getValue();
        verify(ropertyKey).getDescription();
        verify(ropertyKey).getId();
        verify(keyValues).putWithChangeSet(CHANGE_SET, value, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2);
        verify(keyValues).setDescription(DESCRIPTION);
        assertThat(result.get(KEY), Matchers.is(keyValues));
        assertThat(result.get(KEY), Matchers.not(Matchers.is(oldKeyValues)));
        assertThat(result.size(), Matchers.is(1));
    }

    @Test
    public void failIfKeyWithoutValuesShouldBeStored() {
        assertThrows(RopertyPersistenceException.class, () -> jpaPersistence.store(KEY, keyValues, CHANGE_SET));
    }

    @Test
    public void failIfValueIsNull() {
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(singletonList(domainSpecificValue)));

        assertThrows(RopertyPersistenceException.class, () -> jpaPersistence.store(KEY, keyValues, CHANGE_SET));
    }

    @Test
    public void valueMustBeSerializable() {
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(singletonList(domainSpecificValue)));
        when(domainSpecificValue.getValue()).thenReturn(new Object());

        assertThrows(RopertyPersistenceException.class, () -> jpaPersistence.store(KEY, keyValues, CHANGE_SET));
    }

    @Test
    public void storeShouldPersistValues() {
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(singletonList(domainSpecificValue)));
        when(domainSpecificValue.getValue()).thenReturn(value);
        when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);
        when(domainSpecificValue.changeSetIs(CHANGE_SET)).thenReturn(true);

        jpaPersistence.store(KEY, keyValues, CHANGE_SET);

        verify(keyValues).getDescription();
        verify(keyValues).getDomainSpecificValues();
        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(transactionManager).begin();
        verify(transactionManager).persist(any(RopertyKey.class));
        verify(transactionManager).persist(any(RopertyValue.class));
        verify(transactionManager).end();
        verify(domainSpecificValue).getValue();
        verify(domainSpecificValue).getPatternStr();

        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void failIfNullDomainSpecificValues() {
        when(keyValues.getDomainSpecificValues()).thenReturn(null);
        assertThrows(RopertyPersistenceException.class, () -> jpaPersistence.store(KEY, keyValues, CHANGE_SET));
    }

    @Test
    public void storeWithNullChangeSetShouldPersistValues() {
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(singletonList(domainSpecificValue)));
        when(domainSpecificValue.getValue()).thenReturn(value);
        when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);
        when(domainSpecificValue.changeSetIs(null)).thenReturn(true);

        jpaPersistence.store(KEY, keyValues, null);

        verify(keyValues).getDescription();
        verify(keyValues).getDomainSpecificValues();
        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(transactionManager).begin();
        verify(transactionManager).persist(any(RopertyKey.class));
        verify(transactionManager).persist(any(RopertyValue.class));
        verify(transactionManager).end();
        verify(domainSpecificValue).getValue();
        verify(domainSpecificValue).getPatternStr();

        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void removeNonExistingKeyShouldDoNothing() {
        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
    }

    @Test
    public void removingKeyWithoutValuesShouldNotHappen() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);

        assertThrows(RopertyPersistenceException.class, () -> jpaPersistence.remove(KEY, keyValues, CHANGE_SET));
    }

    @Test
    public void failIfNoDomainSpecificValuesOnRemoval() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(singletonList(ropertyValue));

        assertThrows(RopertyPersistenceException.class, () -> jpaPersistence.remove(KEY, keyValues, CHANGE_SET));
    }

    @Test
    public void removeNothingIfNoValueFound() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(singletonList(ropertyValue));
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(singletonList(domainSpecificValue)));

        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        verify(keyValues).getDomainSpecificValues();
        verify(ropertyValue).equals(domainSpecificValue);
    }

    @Test
    public void removingKeyWithOnlyOneValueShouldRemoveKeyAsWell() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(singletonList(ropertyValue));
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(singletonList(domainSpecificValue)));
        when(ropertyValue.equals(domainSpecificValue)).thenReturn(true);

        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        verify(keyValues).getDomainSpecificValues();
        verify(ropertyValue).equals(domainSpecificValue);
        verify(transactionManager).begin();
        verify(transactionManager).remove(ropertyValue);
        verify(transactionManager).remove(ropertyKey);
        verify(transactionManager).end();

        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void removingKeyWithMultipleValuesAndJustOneValueRemovedShouldOnlyRemoveValues() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        RopertyValue ropertyValue2 = mock(RopertyValue.class);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(asList(ropertyValue, ropertyValue2));
        DomainSpecificValue domainSpecificValue2 = mock(DomainSpecificValue.class);
        final Set<DomainSpecificValue> value = new TreeSet<>(asList(domainSpecificValue, domainSpecificValue2));
        when(keyValues.getDomainSpecificValues()).thenReturn(value);
        when(ropertyValue.equals(domainSpecificValue)).thenReturn(true);

        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        verify(keyValues).getDomainSpecificValues();
        verify(ropertyValue).equals(domainSpecificValue);
        verify(ropertyValue2).equals(domainSpecificValue);
        verify(ropertyValue2).equals(domainSpecificValue2);
        verify(transactionManager).begin();
        verify(transactionManager).remove(ropertyValue);
        verify(transactionManager).end();

        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void shouldDoNothingIfKeyNotFoundOnRemoval() {
        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(transactionManager).begin();
        verify(transactionManager).end();

        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void failIfRemovalOfValueWithoutPattern() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);

        assertThrows(RopertyPersistenceException.class, () -> jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET));
    }

    @Test
    public void failIfValueIsNullOnRemoval() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);

        assertThrows(RopertyPersistenceException.class, () -> jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET));
    }

    @Test
    public void doNothingIfNotRopertyValueFoundForExistingKey() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);
        when(domainSpecificValue.getValue()).thenReturn(value);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(ropertyValueDAO).loadRopertyValue(ropertyKey, PATTERN, CHANGE_SET);
        verify(transactionManager).begin();
        verify(transactionManager).end();
        verify(domainSpecificValue).getPatternStr();
        verify(domainSpecificValue).getValue();

        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void removeExistingRopertyValue() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);
        when(domainSpecificValue.getValue()).thenReturn(value);
        when(ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN, CHANGE_SET)).thenReturn(ropertyValue);
        when(ropertyValueDAO.getNumberOfValues(ropertyKey)).thenReturn(2L);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(ropertyValueDAO).loadRopertyValue(ropertyKey, PATTERN, CHANGE_SET);
        verify(transactionManager).begin();
        verify(transactionManager).remove(ropertyValue);
        verify(transactionManager).end();
        verify(domainSpecificValue).getPatternStr();
        verify(domainSpecificValue).getValue();

        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void removeExistingRopertyValueAndKey() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);
        when(domainSpecificValue.getValue()).thenReturn(value);
        when(ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN, CHANGE_SET)).thenReturn(ropertyValue);
        when(ropertyValueDAO.getNumberOfValues(ropertyKey)).thenReturn(1L);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(ropertyValueDAO).loadRopertyValue(ropertyKey, PATTERN, CHANGE_SET);
        verify(transactionManager).begin();
        verify(transactionManager).remove(ropertyValue);
        verify(transactionManager).remove(ropertyKey);
        verify(transactionManager).end();
        verify(domainSpecificValue).getPatternStr();
        verify(domainSpecificValue).getValue();

        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void returnsAllKeys() {
        when(ropertyKey.getId()).thenReturn(KEY);
        when(ropertyKeyDAO.loadAllRopertyKeys()).thenReturn(singletonList(ropertyKey));
        List<String> allKeys = jpaPersistence.getAllKeys();
        assertThat(allKeys, contains(KEY));
    }

}
