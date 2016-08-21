package com.parship.roperty.persistence.jpa;


import com.parship.roperty.DomainResolver;
import com.parship.roperty.KeyValues;
import com.parship.roperty.MapBackedDomainResolver;
import com.parship.roperty.Roperty;
import com.parship.roperty.RopertyImpl;
import com.parship.roperty.RopertyWithResolver;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class JpaIntegrationTest {

    private JpaPersistence jpaPersistence;
    private Roperty roperty;
    private RopertyWithResolver ropertyWithResolver;
    private MapBackedDomainResolver resolver;

    @Mock
    private DomainResolver resolverMock;

    private String persistenceUnitName;

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        final JpaPersistence[] jpaPersistences = {new JpaPersistence(), new LazyJpaPersistence()};
        final String[] persistenceUnits = {
                "h2"
                ,"hsqldb"
        };
        List<Object[]> result = new ArrayList<>(jpaPersistences.length);

        for (JpaPersistence jpaPersistence : jpaPersistences) {
            for (String persistenceUnit : persistenceUnits) {
                result.add(new Object[]{jpaPersistence, persistenceUnit});
            }
        }

        return result;
    }

    public JpaIntegrationTest(JpaPersistence jpaPersistence, String persistenceUnitName) {
        this.jpaPersistence = jpaPersistence;
        this.persistenceUnitName = persistenceUnitName;
    }

    @Before
    public void initializeRelationPersistence() {
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
        valueQueryBuilderDelegate.setQueryBuilder(new QueryBuilder<RopertyValue>());
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

        when(resolverMock.getActiveChangeSets()).thenReturn(new ArrayList<String>());
        when(resolverMock.getDomainValue(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                return (String) invocationOnMock.getArguments()[0];
            }
        });

        ropertyWithResolver = new RopertyWithResolver(roperty, resolverMock);
    }

    @Test
    public void keyAndStringValueShouldBePersisted() {
        roperty.addDomains("domain1", "domain2");
        roperty.set("key_keyAndStringValueShouldBePersisted", "value_keyAndStringValueShouldBePersisted", "description_keyAndStringValueShouldBePersisted", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_keyAndStringValueShouldBePersisted", resolver), Matchers.<Object>is("value_keyAndStringValueShouldBePersisted"));
        KeyValues keyValues = roperty.getKeyValues("key_keyAndStringValueShouldBePersisted");
        assertThat(keyValues.getDescription(), is("description_keyAndStringValueShouldBePersisted"));
    }

    @Test
    public void keyAndDateValueShouldBePersisted() {
        roperty.addDomains("domain1", "domain2");
        Date dateValue = new Date(123456789101112L);
        roperty.set("key_keyAndDateValueShouldBePersisted", dateValue, "description_keyAndDateValueShouldBePersisted", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_keyAndDateValueShouldBePersisted", resolver), Matchers.<Object>is(dateValue));
    }

    @Test
    public void changeSetShouldBeRemoved() {
        roperty.addDomains("domain1", "domain2");
        roperty.setWithChangeSet("key_changeSetShouldBeRemoved", "value_changeSetShouldBeRemoved", "description_changeSetShouldBeRemoved", "changeSet_changeSetShouldBeRemoved", "domainValue1", "domainValue2");
        roperty.reload();
        resolver.addActiveChangeSets("changeSet_changeSetShouldBeRemoved");
        assertThat(roperty.get("key_changeSetShouldBeRemoved", resolver), Matchers.<Object>is("value_changeSetShouldBeRemoved"));
        roperty.removeChangeSet("changeSet_changeSetShouldBeRemoved");
        assertThat(roperty.get("key_changeSetShouldBeRemoved", resolver), nullValue());
    }

    @Test
    public void keyAndValueShouldBeRemovedWithChangeSet() {
        roperty.addDomains("domain1", "domain2");
        roperty.setWithChangeSet("key_keyAndValueShouldBeRemovedWithChangeSet", "value_keyAndValueShouldBeRemovedWithChangeSet", "description_keyAndValueShouldBeRemovedWithChangeSet", "changeSet_keyAndValueShouldBeRemovedWithChangeSet", "domainValue1", "domainValue2");
        roperty.reload();
        resolver.addActiveChangeSets("changeSet_keyAndValueShouldBeRemovedWithChangeSet");
        assertThat(roperty.get("key_keyAndValueShouldBeRemovedWithChangeSet", resolver), Matchers.<Object>is("value_keyAndValueShouldBeRemovedWithChangeSet"));
        roperty.removeWithChangeSet("key_keyAndValueShouldBeRemovedWithChangeSet", "changeSet_keyAndValueShouldBeRemovedWithChangeSet", "domainValue1", "domainValue2");
        assertThat(roperty.get("key_keyAndValueShouldBeRemovedWithChangeSet", resolver), nullValue());
    }

    @Test
    public void keyAndValueShouldBeRemoved() {
        roperty.addDomains("domain1", "domain2");
        roperty.set("key_keyAndValueShouldBeRemoved", "value_keyAndValueShouldBeRemoved", "description_keyAndValueShouldBeRemoved", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_keyAndValueShouldBeRemoved", resolver), Matchers.<Object>is("value_keyAndValueShouldBeRemoved"));
        roperty.remove("key_keyAndValueShouldBeRemoved", "domainValue1", "domainValue2");
        assertThat(roperty.get("key_keyAndValueShouldBeRemoved", resolver), nullValue());
    }

    @Test
    public void removingKeyRemovesAllValues() {
        roperty.addDomains("domain1", "domain2");
        roperty.set("key_removingKeyRemovesAllValues", "value_removingKeyRemovesAllValues", "description_removingKeyRemovesAllValues", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_removingKeyRemovesAllValues", resolver), Matchers.<Object>is("value_removingKeyRemovesAllValues"));
        roperty.removeKey("key_removingKeyRemovesAllValues");
        assertThat(roperty.get("key_removingKeyRemovesAllValues", resolver), nullValue());
    }

    @Test
    public void gettingAPropertyThatDoesNotExistGivesNull() {
        String value = ropertyWithResolver.get("key");
        assertThat(value, nullValue());
    }

    @Test
    public void gettingAPropertyThatDoesNotExistGivesDefaultValue() {
        String text = "default";
        String value = ropertyWithResolver.get("key", text);
        assertThat(value, is(text));
    }

    @Test
    public void settingNullAsValue() {
        ropertyWithResolver.set("key", "value", null);
        assertThat((String) ropertyWithResolver.get("key"), is("value"));
        ropertyWithResolver.set("key", null, null);
        assertThat(ropertyWithResolver.get("key"), nullValue());
    }

    @Test
    public void settingAnEmptyString() {
        ropertyWithResolver.set("key", "", null);
        assertThat((String) ropertyWithResolver.get("key"), is(""));
    }

    @Test
    public void definingAndGettingAStringValue() {
        String key = "key";
        String text = "some Value";
        ropertyWithResolver.set(key, text, null);
        String value = ropertyWithResolver.get(key, "default");
        assertThat(value, is(text));
    }

    @Test
    public void gettingAValueWithoutAGivenDefaultGivesValue() {
        String text = "value";
        ropertyWithResolver.set("key", text, null);
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(text));
    }

    @Test
    public void changingAStringValue() {
        ropertyWithResolver.set("key", "first", null);
        ropertyWithResolver.set("key", "other", null);
        String value = ropertyWithResolver.get("key", "default");
        assertThat(value, is("other"));
    }

    @Test
    public void gettingAnIntValueThatDoesNotExistGivesDefault() {
        int value = ropertyWithResolver.get("key", 3);
        assertThat(value, is(3));
    }

    @Test
    public void settingAndGettingAnIntValueWithDefaultGivesStoredValue() {
        ropertyWithResolver.set("key", 7, null);
        int value = ropertyWithResolver.get("key", 3);
        assertThat(value, is(7));
    }

    @Test
    public void getOrDefineSetsAValueWithTheGivenDefault() {
        String text = "text";
        String value = ropertyWithResolver.getOrDefine("key", text, "descr");
        assertThat(value, is(text));
        value = ropertyWithResolver.getOrDefine("key", "other default");
        assertThat(value, is(text));
    }

    @Test
    public void getOverriddenValue() {
        roperty.addDomains("domain1");
        ropertyWithResolver = new RopertyWithResolver(roperty, resolverMock);
        String defaultValue = "default value";
        String overriddenValue = "overridden value";
        ropertyWithResolver.set("key", defaultValue, null);
        ropertyWithResolver.set("key", overriddenValue, null, "domain1");
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(overriddenValue));
    }

    @Test
    public void whenAKeyForASubdomainIsSetTheRootKeyGetsANullValue() {
        ropertyWithResolver.set("key", "value", "descr", "subdomain");
        assertThat(ropertyWithResolver.get("key"), nullValue());
    }

    @Test
    public void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExist() {
        roperty.addDomains("domain1");
        ropertyWithResolver = new RopertyWithResolver(roperty, resolverMock);
        String overriddenValue = "overridden value";
        ropertyWithResolver.set("key", "other value", null, "other");
        ropertyWithResolver.set("key", overriddenValue, null, "domain1");
        ropertyWithResolver.set("key", "yet another value", null, "yet another");
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(overriddenValue));
    }

    @Test
    public void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExistWithTwoDomains() {
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

    @Test
    public void getOverriddenValueTwoDomainsOnlyFirstDomainIsOverridden() {
        roperty.addDomains("domain1", "domain2");
        ropertyWithResolver = new RopertyWithResolver(roperty, resolverMock);
        String defaultValue = "default value";
        String overriddenValue1 = "overridden value domain1";
        ropertyWithResolver.set("key", defaultValue, null);
        ropertyWithResolver.set("key", overriddenValue1, null, "domain1");
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(overriddenValue1));
    }

    @Test
    public void domainValuesAreRequestedFromAResolver() {
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

    @Test
    public void noDomainValuesAreRequestedWhenAKeyDoesNotExist() {
        roperty.addDomains("domain1", "domain2");
        DomainResolver mockResolver = mock(DomainResolver.class);
        ropertyWithResolver = new RopertyWithResolver(roperty, mockResolver);
        ropertyWithResolver.get("key");
        verifyNoMoreInteractions(mockResolver);
    }

    @Test
    public void wildcardIsResolvedWhenOtherDomainsMatch() {
        roperty.addDomains("domain1", "domain2");
        ropertyWithResolver = new RopertyWithResolver(roperty, resolverMock);
        String value = "overridden value";
        ropertyWithResolver.set("key", value, null, "*", "domain2");
        assertThat((String) ropertyWithResolver.get("key"), is(value));
    }

    @Test
    public void domainsThatAreInitializedArePresent() {
        RopertyImpl roperty = new RopertyImpl("domain1", "domain2");
        assertThat(roperty.dump().toString(), is("Roperty{domains=[domain1, domain2]\n}"));
    }

    @Test
    public void ropertyWithResolverToString() {
        assertThat(ropertyWithResolver.toString(), is("RopertyWithResolver{roperty=Roperty{domains=[]}}"));
    }

    @Test
    public void toStringEmptyRoperty() {
        assertThat(roperty.dump().toString(), is("Roperty{domains=[]\n}"));
        roperty.addDomains("domain");
        assertThat(roperty.dump().toString(), is("Roperty{domains=[domain]\n}"));
    }

    @Test
    public void domainResolverToNullIsIgnored() {
        DomainResolver domainResolver = new MapBackedDomainResolver().set("dom", "domVal");
        roperty.addDomains("dom", "dom2", "dom3");
        roperty.get("key", domainResolver);
        roperty.set("key", "value", "desc");
        roperty.set("key", "valueDom", "desc", "domVal");
        roperty.set("key", "valueDom2", "desc", "domVal", "dom2");
        roperty.set("key", "valueDom3", "desc", "domVal", "dom2", "dom3");
        roperty.reload();
        assertThat(roperty.<String>get("key", domainResolver), is("valueDom"));
    }

    @Test
    public void removeDefaultValue() {

        roperty.addDomains("dom1");
        roperty.set("key", "value", "desc");
        roperty.set("key", "domValue", "desc", "dom1");
        roperty.reload();

        roperty.remove("key");

        assertThat(roperty.get("key", mock(DomainResolver.class)), nullValue());
        assertThat(roperty.<String>get("key", resolverMock), is("domValue"));
    }

    @Test
    public void removeDomainSpecificValue() {
        roperty.addDomains("dom1", "dom2");
        roperty.set("key", "value", "desc");
        roperty.set("key", "domValue1", "desc", "dom1");
        roperty.set("key", "domValue2", "desc", "dom1", "dom2");
        roperty.reload();
        roperty.remove("key", "dom1");

        assertThat(roperty.<String>get("key", mock(DomainResolver.class)), is("value"));
        assertThat(roperty.<String>get("key", resolverMock), is("domValue2"));
    }

    @Test
    public void removeACompleteKey() {
        roperty.set("key", "value", "desc");
        roperty.set("key", "domValue1", "desc", "dom1");
        roperty.reload();
        roperty.removeKey("key");
        assertThat(roperty.get("key", resolverMock), nullValue());
    }

    @Test
    public void removeKeyFromChangeSet() {
        roperty.set("key", "value", "descr");
        roperty.setWithChangeSet("key", "valueChangeSet", "descr", "changeSet");
        roperty.reload();
        DomainResolver resolver = new MapBackedDomainResolver().addActiveChangeSets("changeSet");
        assertThat(roperty.<String>get("key", resolver), is("valueChangeSet"));
        roperty.removeWithChangeSet("key", "changeSet");
        assertThat(roperty.<String>get("key", resolver), is("value"));
    }

    @Test
    public void removeAChangeSet() {
        roperty.set("key", "value", "descr");
        roperty.setWithChangeSet("key", "valueChangeSet", "descr", "changeSet");
        roperty.setWithChangeSet("otherKey", "otherValueChangeSet", "descr", "changeSet");
        roperty.reload();
        DomainResolver resolver = new MapBackedDomainResolver().addActiveChangeSets("changeSet");
        assertThat(roperty.<String>get("key", resolver), is("valueChangeSet"));
        assertThat(roperty.<String>get("otherKey", resolver), is("otherValueChangeSet"));
        roperty.reload();
        roperty.removeChangeSet("changeSet");
        roperty.reload();
        assertThat(roperty.<String>get("key", resolver), is("value"));
        assertThat(roperty.<String>get("otherKey", resolver), nullValue());
    }


}
