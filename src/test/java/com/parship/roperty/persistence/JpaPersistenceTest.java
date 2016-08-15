package com.parship.roperty.persistence;

import com.parship.roperty.DomainSpecificValue;
import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JpaPersistenceTest {

    public static final String KEY = "key";
    public static final String CHANGE_SET = "changeSet";
    public static final String DOMAIN_KEY_PART_1 = "domainKeyPart1";
    public static final String DOMAIN_KEY_PART_2 = "domainKeyPart2";
    public static final String PATTERN = DOMAIN_KEY_PART_1 + '|' + DOMAIN_KEY_PART_2;
    public static final String DESCRIPTION = "description";

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
        assertThat(keyValues, nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadShouldFailNullIfNoRopertyValuesFound() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        KeyValues keyValues = jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
        assertThat(keyValues, nullValue());
    }

    @Test(expected = NullPointerException.class)
    public void failIfRopertyValuePatternIsNull() throws Exception {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));

        jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
    }

    @Test(expected = NullPointerException.class)
    public void failIfKeyValuesIsNull() throws Exception {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));
        when(ropertyValue.getPattern()).thenReturn("pattern");

        jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
    }

    @Test(expected = NullPointerException.class)
    public void failIfRopertyValueHasNoKey() throws Exception {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));
        when(ropertyValue.getPattern()).thenReturn("pattern");
        when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);

        jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
    }

    @Test
    public void loadShouldReturnKeyValues() throws Exception {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));
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
        assertThat(result, is(keyValues));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failIfLoadAllAndNotKeyGiven() throws Exception {
        when(ropertyKeyDAO.loadAllRopertyKeys()).thenReturn(Collections.singletonList(ropertyKey));
        jpaPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failIfNoValuesFound() throws Exception {
        when(ropertyKeyDAO.loadAllRopertyKeys()).thenReturn(Collections.singletonList(ropertyKey));
        when(ropertyKey.getId()).thenReturn(KEY);

        jpaPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);
    }

    @Test
    public void loadAll() throws Exception {
        when(ropertyKeyDAO.loadAllRopertyKeys()).thenReturn(Collections.singletonList(ropertyKey));
        when(ropertyKey.getId()).thenReturn(KEY);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));
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
        assertThat(result.get(KEY), is(keyValues));
        assertThat(result.size(), is(1));
    }

    @Test
    public void reloadWithEmptyMapReturnsEmptyMap() {
        Map<String, KeyValues> keyValuesMap = new HashMap<>();

        Map<String, KeyValues> result = jpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);

        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void doNotIncludeEntryIfKeyDoesNoLongerExist() {
        Map<String, KeyValues> keyValuesMap = new HashMap<>();
        keyValuesMap.put(KEY, keyValues);

        Map<String, KeyValues> result = jpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);

        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void reloadingReplacesOldValueWithNewValue() {
        KeyValues oldKeyValues = mock(KeyValues.class);
        Map<String, KeyValues> keyValuesMap = new HashMap<>();
        keyValuesMap.put(KEY, oldKeyValues);
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Arrays.asList(ropertyValue));
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
        assertThat(result.get(KEY), is(keyValues));
        assertThat(result.get(KEY), not(is(oldKeyValues)));
        assertThat(result.size(), is(1));
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfKeyWithoutValuesShouldBeStored() {
        jpaPersistence.store(KEY, keyValues, CHANGE_SET);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfValueIsNull() {
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue)));

        jpaPersistence.store(KEY, keyValues, CHANGE_SET);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void valueMustBeSerializable() {
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue)));
        when(domainSpecificValue.getValue()).thenReturn(new Object());

        jpaPersistence.store(KEY, keyValues, CHANGE_SET);
    }

    @Test
    public void storeShouldPersistValues() {
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue)));
        when(domainSpecificValue.getValue()).thenReturn(value);
        when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);

        jpaPersistence.store(KEY, keyValues, CHANGE_SET);

        verify(keyValues).getDescription();
        verify(keyValues).getDomainSpecificValues();
        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(transactionManager).begin();
        verify(transactionManager, times(2)).persist(any(RopertyKey.class));
        verify(transactionManager, times(2)).persist(any(RopertyValue.class));
        verify(transactionManager).end();
        verify(domainSpecificValue).getValue();
        verify(domainSpecificValue).getPatternStr();
    }

    @Test
    public void removeNonExistingKeyShouldDoNothing() {
        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void removingKeyWithoutValuesShouldNotHappen() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);

        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfNoDomainSpecificValuesOnRemoval() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Arrays.asList(ropertyValue));

        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);
    }

    @Test
    public void removeNothingIfNoValueFound() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Arrays.asList(ropertyValue));
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue)));

        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        verify(keyValues).getDomainSpecificValues();
        verify(ropertyValue).equals(domainSpecificValue);
    }

    @Test
    public void removingKeyWithOnlyOneValueShouldRemoveKeyAsWell() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Arrays.asList(ropertyValue));
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue)));
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
    }

    @Test
    public void removingKeyWithMultipleValuesAndJustOneValueRemovedShouldOnlyRemoveValues() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        RopertyValue ropertyValue2 = mock(RopertyValue.class);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Arrays.asList(ropertyValue, ropertyValue2));
        DomainSpecificValue domainSpecificValue2 = mock(DomainSpecificValue.class);
        when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue, domainSpecificValue2)));
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
    }

    @Test
    public void shouldDoNothingIfKeyNotFoundOnRemoval() {
        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(transactionManager).begin();
        verify(transactionManager).end();
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfRemovalOfValueWithoutPattern() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfValueIsNullOnRemoval() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);
    }

    @Test
    public void doNothingIfNotRopertyValueFoundForExistingKey() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);
        when(domainSpecificValue.getValue()).thenReturn(value);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(ropertyValueDAO).loadRopertyValue(ropertyKey, PATTERN);
        verify(transactionManager).begin();
        verify(transactionManager).end();
        verify(domainSpecificValue).getPatternStr();
        verify(domainSpecificValue).getValue();
    }

    @Test
    public void removeExistingRopertyValue() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);
        when(domainSpecificValue.getValue()).thenReturn(value);
        when(ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN)).thenReturn(ropertyValue);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);

        verify(ropertyKeyDAO).loadRopertyKey(KEY);
        verify(ropertyValueDAO).loadRopertyValue(ropertyKey, PATTERN);
        verify(transactionManager).begin();
        verify(transactionManager).remove(ropertyValue);
        verify(transactionManager).end();
        verify(domainSpecificValue).getPatternStr();
        verify(domainSpecificValue).getValue();
    }

}