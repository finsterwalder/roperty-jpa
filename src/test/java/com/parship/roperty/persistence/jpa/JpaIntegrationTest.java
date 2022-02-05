package com.parship.roperty.persistence.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.parship.roperty.DomainResolver;
import com.parship.roperty.KeyValues;
import com.parship.roperty.MapBackedDomainResolver;
import com.parship.roperty.Roperty;
import com.parship.roperty.RopertyImpl;
import com.parship.roperty.RopertyWithResolver;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JpaIntegrationTest {

    private Roperty roperty;
    private RopertyWithResolver ropertyWithResolver;
    private MapBackedDomainResolver resolver;

    @Mock
    private DomainResolver resolverMock;

    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(new JpaPersistence(), "hsqldb"),
            Arguments.of(new LazyJpaPersistence(), "hsqldb")
        );
    }

    public void initPersistence(JpaPersistence jpaPersistence, String persistenceUnitName) {
        MockitoAnnotations.initMocks(this);
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);

        TransactionManager transactionManager = new TransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);

        QueryBuilderDelegate<RopertyKey> keyQueryBuilderDelegate = new QueryBuilderDelegate<>();
        keyQueryBuilderDelegate.setEntityManagerFactory(entityManagerFactory);
        keyQueryBuilderDelegate.setQueryBuilder(new QueryBuilder<RopertyKey>());
        keyQueryBuilderDelegate.setResultClass(RopertyKey.class);
        RopertyKeyDAO ropertyKeyDAO = new RopertyKeyDAO();
        ropertyKeyDAO.setQueryBuilderDelegate(keyQueryBuilderDelegate);

        QueryBuilderDelegate<RopertyValue> valueQueryBuilderDelegate = new QueryBuilderDelegate<>();
        valueQueryBuilderDelegate.setEntityManagerFactory(entityManagerFactory);
        valueQueryBuilderDelegate.setQueryBuilder(new QueryBuilder<>());
        valueQueryBuilderDelegate.setResultClass(RopertyValue.class);
        RopertyValueDAO ropertyValueDAO = new RopertyValueDAO();
        ropertyValueDAO.setQueryBuilderDelegate(valueQueryBuilderDelegate);

        jpaPersistence.setTransactionManager(transactionManager);
        jpaPersistence.setRopertyKeyDAO(ropertyKeyDAO);
        jpaPersistence.setRopertyValueDAO(ropertyValueDAO);

        roperty = new RopertyImpl(jpaPersistence);
        resolver = new MapBackedDomainResolver()
                .set("domain1", "domainValue1")
                .set("domain2", "domainValue2");

        when(resolverMock.getActiveChangeSets()).thenReturn(new ArrayList<>());
        when(resolverMock.getDomainValue(anyString())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        ropertyWithResolver = new RopertyWithResolver(roperty, resolverMock);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void keyAndStringValueShouldBePersisted(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1", "domain2");
        roperty.set("key_keyAndStringValueShouldBePersisted", "value_keyAndStringValueShouldBePersisted", "description_keyAndStringValueShouldBePersisted", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_keyAndStringValueShouldBePersisted", resolver), Matchers.<Object>is("value_keyAndStringValueShouldBePersisted"));
        KeyValues keyValues = roperty.getKeyValues("key_keyAndStringValueShouldBePersisted");
        assertThat(keyValues.getDescription(), is("description_keyAndStringValueShouldBePersisted"));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void keyAndDateValueShouldBePersisted(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1", "domain2");
        Date dateValue = new Date(123456789101112L);
        roperty.set("key_keyAndDateValueShouldBePersisted", dateValue, "description_keyAndDateValueShouldBePersisted", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_keyAndDateValueShouldBePersisted", resolver), Matchers.<Object>is(dateValue));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void changeSetShouldBeRemoved(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1", "domain2");
        roperty.setWithChangeSet("key_changeSetShouldBeRemoved", "value_changeSetShouldBeRemoved", "description_changeSetShouldBeRemoved", "changeSet_changeSetShouldBeRemoved", "domainValue1", "domainValue2");
        roperty.reload();
        resolver.addActiveChangeSets("changeSet_changeSetShouldBeRemoved");
        assertThat(roperty.get("key_changeSetShouldBeRemoved", resolver), Matchers.<Object>is("value_changeSetShouldBeRemoved"));
        roperty.removeChangeSet("changeSet_changeSetShouldBeRemoved");
        assertThat(roperty.get("key_changeSetShouldBeRemoved", resolver), nullValue());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void keyAndValueShouldBeRemovedWithChangeSet(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1", "domain2");
        roperty.setWithChangeSet("key_keyAndValueShouldBeRemovedWithChangeSet", "value_keyAndValueShouldBeRemovedWithChangeSet", "description_keyAndValueShouldBeRemovedWithChangeSet", "changeSet_keyAndValueShouldBeRemovedWithChangeSet", "domainValue1", "domainValue2");
        roperty.reload();
        resolver.addActiveChangeSets("changeSet_keyAndValueShouldBeRemovedWithChangeSet");
        assertThat(roperty.get("key_keyAndValueShouldBeRemovedWithChangeSet", resolver), Matchers.<Object>is("value_keyAndValueShouldBeRemovedWithChangeSet"));
        roperty.removeWithChangeSet("key_keyAndValueShouldBeRemovedWithChangeSet", "changeSet_keyAndValueShouldBeRemovedWithChangeSet", "domainValue1", "domainValue2");
        assertThat(roperty.get("key_keyAndValueShouldBeRemovedWithChangeSet", resolver), nullValue());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void keyAndValueShouldBeRemoved(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1", "domain2");
        roperty.set("key_keyAndValueShouldBeRemoved", "value_keyAndValueShouldBeRemoved", "description_keyAndValueShouldBeRemoved", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_keyAndValueShouldBeRemoved", resolver), Matchers.<Object>is("value_keyAndValueShouldBeRemoved"));
        roperty.remove("key_keyAndValueShouldBeRemoved", "domainValue1", "domainValue2");
        assertThat(roperty.get("key_keyAndValueShouldBeRemoved", resolver), nullValue());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void removingKeyRemovesAllValues(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1", "domain2");
        roperty.set("key_removingKeyRemovesAllValues", "value_removingKeyRemovesAllValues", "description_removingKeyRemovesAllValues", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_removingKeyRemovesAllValues", resolver), Matchers.<Object>is("value_removingKeyRemovesAllValues"));
        roperty.removeKey("key_removingKeyRemovesAllValues");
        assertThat(roperty.get("key_removingKeyRemovesAllValues", resolver), nullValue());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void gettingAPropertyThatDoesNotExistGivesNull(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        String value = ropertyWithResolver.get("key");
        assertThat(value, nullValue());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void gettingAPropertyThatDoesNotExistGivesDefaultValue(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        String text = "default";
        String value = ropertyWithResolver.get("key", text);
        assertThat(value, is(text));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void settingNullAsValue(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        ropertyWithResolver.set("key", "value", null);
        assertThat(ropertyWithResolver.get("key"), is("value"));
        ropertyWithResolver.set("key", null, null);
        assertThat(ropertyWithResolver.get("key"), nullValue());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void settingAnEmptyString(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        ropertyWithResolver.set("key", "", null);
        assertThat(ropertyWithResolver.get("key"), is(""));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void definingAndGettingAStringValue(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        String key = "key";
        String text = "some Value";
        ropertyWithResolver.set(key, text, null);
        String value = ropertyWithResolver.get(key, "default");
        assertThat(value, is(text));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void gettingAValueWithoutAGivenDefaultGivesValue(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        String text = "value";
        ropertyWithResolver.set("key", text, null);
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(text));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void changingAStringValue(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        ropertyWithResolver.set("key", "first", null);
        ropertyWithResolver.set("key", "other", null);
        String value = ropertyWithResolver.get("key", "default");
        assertThat(value, is("other"));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void gettingAnIntValueThatDoesNotExistGivesDefault(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        int value = ropertyWithResolver.get("key", 3);
        assertThat(value, is(3));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void settingAndGettingAnIntValueWithDefaultGivesStoredValue(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        ropertyWithResolver.set("key", 7, null);
        int value = ropertyWithResolver.get("key", 3);
        assertThat(value, is(7));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void getOrDefineSetsAValueWithTheGivenDefault(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        String text = "text";
        String value = ropertyWithResolver.getOrDefine("key", text, "descr");
        assertThat(value, is(text));
        value = ropertyWithResolver.getOrDefine("key", "other default");
        assertThat(value, is(text));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void getOverriddenValue(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1");
        ropertyWithResolver = new RopertyWithResolver(roperty, resolverMock);
        String defaultValue = "default value";
        String overriddenValue = "overridden value";
        ropertyWithResolver.set("key", defaultValue, null);
        ropertyWithResolver.set("key", overriddenValue, null, "domain1");
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(overriddenValue));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void whenAKeyForASubdomainIsSetTheRootKeyGetsANullValue(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        ropertyWithResolver.set("key", "value", "descr", "subdomain");
        assertThat(ropertyWithResolver.get("key"), nullValue());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExist(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1");
        ropertyWithResolver = new RopertyWithResolver(roperty, resolverMock);
        String overriddenValue = "overridden value";
        ropertyWithResolver.set("key", "other value", null, "other");
        ropertyWithResolver.set("key", overriddenValue, null, "domain1");
        ropertyWithResolver.set("key", "yet another value", null, "yet another");
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(overriddenValue));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExistWithTwoDomains(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1", "domain2");
        DomainResolver mockResolver = mock(DomainResolver.class);
        when(mockResolver.getDomainValue("domain1")).thenReturn("domVal1");
        when(mockResolver.getDomainValue("domain2")).thenReturn("domVal2");
        ropertyWithResolver = new RopertyWithResolver(roperty, mockResolver);
        String overriddenValue = "overridden value";
        ropertyWithResolver.set("key", "other value", null, "other");
        ropertyWithResolver.set("key", "domVal1", null, "domVal1");
        ropertyWithResolver.set("key", overriddenValue, null, "domVal1", "domVal2");
        ropertyWithResolver.set("key", "yet another value", null, "domVal1", "other");
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(overriddenValue));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void getOverriddenValueTwoDomainsOnlyFirstDomainIsOverridden(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1", "domain2");
        ropertyWithResolver = new RopertyWithResolver(roperty, resolverMock);
        String defaultValue = "default value";
        String overriddenValue1 = "overridden value domain1";
        ropertyWithResolver.set("key", defaultValue, null);
        ropertyWithResolver.set("key", overriddenValue1, null, "domain1");
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(overriddenValue1));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void domainValuesAreRequestedFromAResolver(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        ((RopertyImpl) ropertyWithResolver.getRoperty()).addDomains("domain1", "domain2");
        DomainResolver mockResolver = mock(DomainResolver.class);
        ropertyWithResolver = new RopertyWithResolver(roperty, mockResolver);
        ropertyWithResolver.set("key", "value", null);
        ropertyWithResolver.get("key");
        verify(mockResolver).getDomainValue("domain1");
        verify(mockResolver).getDomainValue("domain2");
        verify(mockResolver).getActiveChangeSets();
        verifyNoMoreInteractions(mockResolver);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void noDomainValuesAreRequestedWhenAKeyDoesNotExist(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1", "domain2");
        DomainResolver mockResolver = mock(DomainResolver.class);
        ropertyWithResolver = new RopertyWithResolver(roperty, mockResolver);
        ropertyWithResolver.get("key");
        verifyNoMoreInteractions(mockResolver);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void wildcardIsResolvedWhenOtherDomainsMatch(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("domain1", "domain2");
        ropertyWithResolver = new RopertyWithResolver(roperty, resolverMock);
        String value = "overridden value";
        ropertyWithResolver.set("key", value, null, "*", "domain2");
        assertThat(ropertyWithResolver.get("key"), is(value));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void domainsThatAreInitializedArePresent(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        RopertyImpl roperty = new RopertyImpl("domain1", "domain2");
        assertThat(roperty.dump().toString(), is("Roperty{domains=[domain1, domain2]\n}"));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void ropertyWithResolverToString(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        assertThat(ropertyWithResolver.toString(), is("RopertyWithResolver{roperty=Roperty{domains=[]}}"));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void domainResolverToNullIsIgnored(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        DomainResolver domainResolver = new MapBackedDomainResolver().set("dom", "domVal");
        roperty.addDomains("dom", "dom2", "dom3");
        roperty.get("key", domainResolver);
        roperty.set("key", "value", "desc");
        roperty.set("key", "valueDom", "desc", "domVal");
        roperty.set("key", "valueDom2", "desc", "domVal", "dom2");
        roperty.set("key", "valueDom3", "desc", "domVal", "dom2", "dom3");
        roperty.reload();
        assertThat(roperty.get("key", domainResolver), is("valueDom"));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void removeDefaultValue(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);

        roperty.addDomains("dom1");
        roperty.set("key", "value", "desc");
        roperty.set("key", "domValue", "desc", "dom1");
        roperty.reload();

        roperty.remove("key");

        assertThat(roperty.get("key", mock(DomainResolver.class)), nullValue());
        assertThat(roperty.get("key", resolverMock), is("domValue"));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void removeDomainSpecificValue(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.addDomains("dom1", "dom2");
        roperty.set("key", "value", "desc");
        roperty.set("key", "domValue1", "desc", "dom1");
        roperty.set("key", "domValue2", "desc", "dom1", "dom2");
        roperty.reload();
        roperty.remove("key", "dom1");

        assertThat(roperty.get("key", mock(DomainResolver.class)), is("value"));
        assertThat(roperty.get("key", resolverMock), is("domValue2"));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void removeACompleteKey(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.set("key", "value", "desc");
        roperty.set("key", "domValue1", "desc", "dom1");
        roperty.reload();
        roperty.removeKey("key");
        assertThat(roperty.get("key", resolverMock), nullValue());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void removeKeyFromChangeSet(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.set("key", "value", "descr");
        roperty.setWithChangeSet("key", "valueChangeSet", "descr", "changeSet");
        roperty.reload();
        DomainResolver resolver = new MapBackedDomainResolver().addActiveChangeSets("changeSet");
        assertThat(roperty.get("key", resolver), is("valueChangeSet"));
        roperty.removeWithChangeSet("key", "changeSet");
        assertThat(roperty.get("key", resolver), is("value"));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void removeAChangeSet(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        roperty.set("key", "value", "descr");
        roperty.setWithChangeSet("key", "valueChangeSet", "descr", "changeSet");
        roperty.setWithChangeSet("otherKey", "otherValueChangeSet", "descr", "changeSet");
        roperty.reload();
        DomainResolver resolver = new MapBackedDomainResolver().addActiveChangeSets("changeSet");
        assertThat(roperty.get("key", resolver), is("valueChangeSet"));
        assertThat(roperty.get("otherKey", resolver), is("otherValueChangeSet"));
        roperty.reload();
        roperty.removeChangeSet("changeSet");
        roperty.reload();
        assertThat(roperty.get("key", resolver), is("value"));
        assertThat(roperty.<String>get("otherKey", resolver), nullValue());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void findsAKeyAccordingToSubstring(JpaPersistence jpaPersistence, String persistenceUnitName) {
        initPersistence(jpaPersistence, persistenceUnitName);
        String key = "somemultiwordkey";
        roperty.set(key, "value", "descr");
        List<String> keys = roperty.findKeys("MULTI");
        assertThat(keys, contains(key));
    }
}
