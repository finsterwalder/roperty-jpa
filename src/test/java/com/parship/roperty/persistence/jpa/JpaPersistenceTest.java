package com.parship.roperty.persistence.jpa;

import com.parship.roperty.DomainSpecificValue;
import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
        Assert.assertThat(keyValues, Matchers.nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadShouldFailNullIfNoRopertyValuesFound() {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        KeyValues keyValues = jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
        Assert.assertThat(keyValues, Matchers.nullValue());
    }

    @Test(expected = NullPointerException.class)
    public void failIfRopertyValuePatternIsNull() throws Exception {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));

        jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
    }

    @Test(expected = NullPointerException.class)
    public void failIfKeyValuesIsNull() throws Exception {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));
        Mockito.when(ropertyValue.getPattern()).thenReturn("pattern");

        jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
    }

    @Test(expected = NullPointerException.class)
    public void failIfRopertyValueHasNoKey() throws Exception {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));
        Mockito.when(ropertyValue.getPattern()).thenReturn("pattern");
        Mockito.when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);

        jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
    }

    @Test
    public void loadShouldReturnKeyValues() throws Exception {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));
        Mockito.when(ropertyValue.getPattern()).thenReturn(PATTERN);
        Mockito.when(ropertyValue.getKey()).thenReturn(ropertyKey);
        Mockito.when(ropertyValue.getChangeSet()).thenReturn(CHANGE_SET);
        Mockito.when(ropertyValue.getValue()).thenReturn(value);
        Mockito.when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);
        Mockito.when(ropertyKey.getDescription()).thenReturn(DESCRIPTION);

        KeyValues result = jpaPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);

        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);
        Mockito.verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        Mockito.verify(keyValuesFactory).create(domainSpecificValueFactory);
        Mockito.verify(ropertyKey).getDescription();
        Mockito.verify(ropertyValue).getPattern();
        Mockito.verify(ropertyValue).getValue();
        Mockito.verify(ropertyValue).getKey();
        Mockito.verify(ropertyValue).getChangeSet();
        Mockito.verify(keyValues).putWithChangeSet(CHANGE_SET, value, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2);
        Mockito.verify(keyValues).setDescription(DESCRIPTION);
        Assert.assertThat(result, Matchers.is(keyValues));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failIfLoadAllAndNotKeyGiven() throws Exception {
        Mockito.when(ropertyKeyDAO.loadAllRopertyKeys()).thenReturn(Collections.singletonList(ropertyKey));
        jpaPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failIfNoValuesFound() throws Exception {
        Mockito.when(ropertyKeyDAO.loadAllRopertyKeys()).thenReturn(Collections.singletonList(ropertyKey));
        Mockito.when(ropertyKey.getId()).thenReturn(KEY);

        jpaPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);
    }

    @Test
    public void loadAll() throws Exception {
        Mockito.when(ropertyKeyDAO.loadAllRopertyKeys()).thenReturn(Collections.singletonList(ropertyKey));
        Mockito.when(ropertyKey.getId()).thenReturn(KEY);
        Mockito.when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));
        Mockito.when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);
        Mockito.when(ropertyValue.getPattern()).thenReturn(PATTERN);
        Mockito.when(ropertyValue.getKey()).thenReturn(ropertyKey);
        Mockito.when(ropertyValue.getChangeSet()).thenReturn(CHANGE_SET);
        Mockito.when(ropertyValue.getValue()).thenReturn(value);
        Mockito.when(ropertyKey.getDescription()).thenReturn(DESCRIPTION);

        Map<String, KeyValues> result = jpaPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);

        Mockito.verify(ropertyKeyDAO).loadAllRopertyKeys();
        Mockito.verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        Mockito.verify(keyValuesFactory).create(domainSpecificValueFactory);
        Mockito.verify(ropertyKey, Mockito.times(2)).getId();
        Mockito.verify(ropertyKey).getDescription();
        Mockito.verify(ropertyValue).getPattern();
        Mockito.verify(ropertyValue).getKey();
        Mockito.verify(ropertyValue).getValue();
        Mockito.verify(ropertyValue).getChangeSet();
        Mockito.verify(ropertyValue).getValue();
        Mockito.verify(keyValues).putWithChangeSet(CHANGE_SET, value, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2);
        Mockito.verify(keyValues).setDescription(DESCRIPTION);
        Assert.assertThat(result.get(KEY), Matchers.is(keyValues));
        Assert.assertThat(result.size(), Matchers.is(1));
    }

    @Test
    public void reloadWithEmptyMapReturnsEmptyMap() {
        Map<String, KeyValues> keyValuesMap = new HashMap<>();

        Map<String, KeyValues> result = jpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);

        Assert.assertThat(result.isEmpty(), Matchers.is(true));
    }

    @Test
    public void doNotIncludeEntryIfKeyDoesNoLongerExist() {
        Map<String, KeyValues> keyValuesMap = new HashMap<>();
        keyValuesMap.put(KEY, keyValues);

        Map<String, KeyValues> result = jpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);

        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);

        Assert.assertThat(result.isEmpty(), Matchers.is(true));
    }

    @Test
    public void reloadingReplacesOldValueWithNewValue() {
        KeyValues oldKeyValues = Mockito.mock(KeyValues.class);
        Map<String, KeyValues> keyValuesMap = new HashMap<>();
        keyValuesMap.put(KEY, oldKeyValues);
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Arrays.asList(ropertyValue));
        Mockito.when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);
        Mockito.when(ropertyValue.getKey()).thenReturn(ropertyKey);
        Mockito.when(ropertyValue.getPattern()).thenReturn(PATTERN);
        Mockito.when(ropertyValue.getChangeSet()).thenReturn(CHANGE_SET);
        Mockito.when(ropertyValue.getValue()).thenReturn(value);
        Mockito.when(ropertyKey.getDescription()).thenReturn(DESCRIPTION);
        Mockito.when(ropertyKey.getId()).thenReturn(KEY);

        Map<String, KeyValues> result = jpaPersistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);

        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);
        Mockito.verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        Mockito.verify(keyValuesFactory).create(domainSpecificValueFactory);
        Mockito.verify(ropertyValue).getKey();
        Mockito.verify(ropertyValue).getPattern();
        Mockito.verify(ropertyValue).getChangeSet();
        Mockito.verify(ropertyValue).getValue();
        Mockito.verify(ropertyKey).getDescription();
        Mockito.verify(ropertyKey).getId();
        Mockito.verify(keyValues).putWithChangeSet(CHANGE_SET, value, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2);
        Mockito.verify(keyValues).setDescription(DESCRIPTION);
        Assert.assertThat(result.get(KEY), Matchers.is(keyValues));
        Assert.assertThat(result.get(KEY), Matchers.not(Matchers.is(oldKeyValues)));
        Assert.assertThat(result.size(), Matchers.is(1));
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfKeyWithoutValuesShouldBeStored() {
        jpaPersistence.store(KEY, keyValues, CHANGE_SET);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfValueIsNull() {
        Mockito.when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue)));

        jpaPersistence.store(KEY, keyValues, CHANGE_SET);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void valueMustBeSerializable() {
        Mockito.when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue)));
        Mockito.when(domainSpecificValue.getValue()).thenReturn(new Object());

        jpaPersistence.store(KEY, keyValues, CHANGE_SET);
    }

    @Test
    public void storeShouldPersistValues() {
        Mockito.when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue)));
        Mockito.when(domainSpecificValue.getValue()).thenReturn(value);
        Mockito.when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);

        jpaPersistence.store(KEY, keyValues, CHANGE_SET);

        Mockito.verify(keyValues).getDescription();
        Mockito.verify(keyValues).getDomainSpecificValues();
        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);
        Mockito.verify(transactionManager).begin();
        Mockito.verify(transactionManager, Mockito.times(2)).persist(Matchers.any(RopertyKey.class));
        Mockito.verify(transactionManager, Mockito.times(2)).persist(Matchers.any(RopertyValue.class));
        Mockito.verify(transactionManager).end();
        Mockito.verify(domainSpecificValue).getValue();
        Mockito.verify(domainSpecificValue).getPatternStr();

        Mockito.verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void removeNonExistingKeyShouldDoNothing() {
        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);

        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void removingKeyWithoutValuesShouldNotHappen() {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);

        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfNoDomainSpecificValuesOnRemoval() {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Arrays.asList(ropertyValue));

        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);
    }

    @Test
    public void removeNothingIfNoValueFound() {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Arrays.asList(ropertyValue));
        Mockito.when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue)));

        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);

        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);
        Mockito.verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        Mockito.verify(keyValues).getDomainSpecificValues();
        Mockito.verify(ropertyValue).equals(domainSpecificValue);
    }

    @Test
    public void removingKeyWithOnlyOneValueShouldRemoveKeyAsWell() {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Arrays.asList(ropertyValue));
        Mockito.when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue)));
        Mockito.when(ropertyValue.equals(domainSpecificValue)).thenReturn(true);

        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);

        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);
        Mockito.verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        Mockito.verify(keyValues).getDomainSpecificValues();
        Mockito.verify(ropertyValue).equals(domainSpecificValue);
        Mockito.verify(transactionManager).begin();
        Mockito.verify(transactionManager).remove(ropertyValue);
        Mockito.verify(transactionManager).remove(ropertyKey);
        Mockito.verify(transactionManager).end();

        Mockito.verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void removingKeyWithMultipleValuesAndJustOneValueRemovedShouldOnlyRemoveValues() {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        RopertyValue ropertyValue2 = Mockito.mock(RopertyValue.class);
        Mockito.when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Arrays.asList(ropertyValue, ropertyValue2));
        DomainSpecificValue domainSpecificValue2 = Mockito.mock(DomainSpecificValue.class);
        Mockito.when(keyValues.getDomainSpecificValues()).thenReturn(new HashSet<>(Arrays.asList(domainSpecificValue, domainSpecificValue2)));
        Mockito.when(ropertyValue.equals(domainSpecificValue)).thenReturn(true);

        jpaPersistence.remove(KEY, keyValues, CHANGE_SET);

        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);
        Mockito.verify(ropertyValueDAO).loadRopertyValues(ropertyKey);
        Mockito.verify(keyValues).getDomainSpecificValues();
        Mockito.verify(ropertyValue).equals(domainSpecificValue);
        Mockito.verify(ropertyValue2).equals(domainSpecificValue);
        Mockito.verify(ropertyValue2).equals(domainSpecificValue2);
        Mockito.verify(transactionManager).begin();
        Mockito.verify(transactionManager).remove(ropertyValue);
        Mockito.verify(transactionManager).end();

        Mockito.verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void shouldDoNothingIfKeyNotFoundOnRemoval() {
        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);

        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);
        Mockito.verify(transactionManager).begin();
        Mockito.verify(transactionManager).end();

        Mockito.verifyNoMoreInteractions(transactionManager);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfRemovalOfValueWithoutPattern() {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfValueIsNullOnRemoval() {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);
    }

    @Test
    public void doNothingIfNotRopertyValueFoundForExistingKey() {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);
        Mockito.when(domainSpecificValue.getValue()).thenReturn(value);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);

        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);
        Mockito.verify(ropertyValueDAO).loadRopertyValue(ropertyKey, PATTERN);
        Mockito.verify(transactionManager).begin();
        Mockito.verify(transactionManager).end();
        Mockito.verify(domainSpecificValue).getPatternStr();
        Mockito.verify(domainSpecificValue).getValue();

        Mockito.verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void removeExistingRopertyValue() {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);
        Mockito.when(domainSpecificValue.getValue()).thenReturn(value);
        Mockito.when(ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN)).thenReturn(ropertyValue);
        Mockito.when(ropertyValueDAO.getNumberOfValues(ropertyKey)).thenReturn(2L);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);

        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);
        Mockito.verify(ropertyValueDAO).loadRopertyValue(ropertyKey, PATTERN);
        Mockito.verify(transactionManager).begin();
        Mockito.verify(transactionManager).remove(ropertyValue);
        Mockito.verify(transactionManager).end();
        Mockito.verify(domainSpecificValue).getPatternStr();
        Mockito.verify(domainSpecificValue).getValue();

        Mockito.verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void removeExistingRopertyValueAndKey() {
        Mockito.when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        Mockito.when(domainSpecificValue.getPatternStr()).thenReturn(PATTERN);
        Mockito.when(domainSpecificValue.getValue()).thenReturn(value);
        Mockito.when(ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN)).thenReturn(ropertyValue);
        Mockito.when(ropertyValueDAO.getNumberOfValues(ropertyKey)).thenReturn(1L);

        jpaPersistence.remove(KEY, domainSpecificValue, CHANGE_SET);

        Mockito.verify(ropertyKeyDAO).loadRopertyKey(KEY);
        Mockito.verify(ropertyValueDAO).loadRopertyValue(ropertyKey, PATTERN);
        Mockito.verify(transactionManager).begin();
        Mockito.verify(transactionManager).remove(ropertyValue);
        Mockito.verify(transactionManager).remove(ropertyKey);
        Mockito.verify(transactionManager).end();
        Mockito.verify(domainSpecificValue).getPatternStr();
        Mockito.verify(domainSpecificValue).getValue();

        Mockito.verifyNoMoreInteractions(transactionManager);
    }

}