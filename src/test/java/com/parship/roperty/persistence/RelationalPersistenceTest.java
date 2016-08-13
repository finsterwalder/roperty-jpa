package com.parship.roperty.persistence;

import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.transaction.TransactionManager;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RelationalPersistenceTest {

    public static final String KEY = "key";
    public static final String CHANGE_SET = "changeSet";
    public static final String DOMAIN_KEY_PART_1 = "domainKeyPart1";
    public static final String DOMAIN_KEY_PART_2 = "domainKeyPart2";
    public static final String PATTERN = DOMAIN_KEY_PART_1 + '|' + DOMAIN_KEY_PART_2;
    public static final String DESCRIPTION = "description";
    @InjectMocks
    private RelationalPersistence relationalPersistence;

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
    private Object value;

    @Before
    public void prepareMocks() {

    }

    @Test
    public void loadShouldReturnNullIfNoRopertyKeyFound() {
        KeyValues keyValues = relationalPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
        assertThat(keyValues, nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadShouldFailNullIfNoRopertyValuesFound() {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        KeyValues keyValues = relationalPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
        assertThat(keyValues, nullValue());
    }

    @Test(expected = NullPointerException.class)
    public void failIfRopertyValuePatternIsNull() throws Exception {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));

        relationalPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
    }

    @Test(expected = NullPointerException.class)
    public void failIfKeyValuesIsNull() throws Exception {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));
        when(ropertyValue.getPattern()).thenReturn("pattern");

        relationalPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
    }

    @Test(expected = NullPointerException.class)
    public void failIfRopertyValueHasNoKey() throws Exception {
        when(ropertyKeyDAO.loadRopertyKey(KEY)).thenReturn(ropertyKey);
        when(ropertyValueDAO.loadRopertyValues(ropertyKey)).thenReturn(Collections.singletonList(ropertyValue));
        when(ropertyValue.getPattern()).thenReturn("pattern");
        when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);

        relationalPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);
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

        KeyValues result = relationalPersistence.load(KEY, keyValuesFactory, domainSpecificValueFactory);

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
        relationalPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failIfNoValuesFound() throws Exception {
        when(ropertyKeyDAO.loadAllRopertyKeys()).thenReturn(Collections.singletonList(ropertyKey));
        when(ropertyKey.getId()).thenReturn(KEY);

        relationalPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);
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

        Map<String, KeyValues> result = relationalPersistence.loadAll(keyValuesFactory, domainSpecificValueFactory);

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

    }

}