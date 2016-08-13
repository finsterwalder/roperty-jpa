package com.parship.roperty.persistence;


import com.parship.roperty.KeyValues;
import com.parship.roperty.MapBackedDomainResolver;
import com.parship.roperty.Roperty;
import com.parship.roperty.RopertyImpl;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@Ignore
public class H2IntegrationTest {

    private RelationalPersistence relationalPersistence = new RelationalPersistence();
    private Roperty roperty;
    private MapBackedDomainResolver resolver;

    @Before
    public void initializeRelationPersistence() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("com.parship.roperty.persistence");

        RelationalTransactionManager transactionManager = new RelationalTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);

        RopertyKeyDAO ropertyKeyDAO = new RopertyKeyDAO();
        ropertyKeyDAO.setEntityManagerFactory(entityManagerFactory);

        RopertyValueDAO ropertyValueDAO = new RopertyValueDAO();
        ropertyValueDAO.setEntityManagerFactory(entityManagerFactory);

        relationalPersistence.setTransactionManager(transactionManager);
        relationalPersistence.setRopertyKeyDAO(ropertyKeyDAO);
        relationalPersistence.setRopertyValueDAO(ropertyValueDAO);

        roperty = new RopertyImpl(relationalPersistence, "domain1", "domain2");
        resolver = new MapBackedDomainResolver()
                .set("domain1", "domainValue1")
                .set("domain2", "domainValue2");
    }

    @Test
    public void keyAndStringValueShouldBePersisted() {
        roperty.set("key_keyAndStringValueShouldBePersisted", "value_keyAndStringValueShouldBePersisted", "description_keyAndStringValueShouldBePersisted", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_keyAndStringValueShouldBePersisted", resolver), Matchers.<Object>is("value_keyAndStringValueShouldBePersisted"));
        roperty.reload();
        KeyValues keyValues = roperty.getKeyValues("key_keyAndStringValueShouldBePersisted");
        assertThat(keyValues.getDescription(), is("description_keyAndStringValueShouldBePersisted"));
    }

    @Test
    public void keyAndDateValueShouldBePersisted() {
        Date dateValue = new Date(123456789101112L);
        roperty.set("key_keyAndDateValueShouldBePersisted", dateValue, "description_keyAndDateValueShouldBePersisted", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_keyAndDateValueShouldBePersisted", resolver), Matchers.<Object>is(dateValue));
    }

    @Test
    public void changeSetShouldBeRemoved() {
        roperty.setWithChangeSet("key_changeSetShouldBeRemoved", "value_changeSetShouldBeRemoved", "description_changeSetShouldBeRemoved", "changeSet_changeSetShouldBeRemoved", "domainValue1", "domainValue2");
        roperty.reload();
        resolver.addActiveChangeSets("changeSet_changeSetShouldBeRemoved");
        assertThat(roperty.get("key_changeSetShouldBeRemoved", resolver), Matchers.<Object>is("value_changeSetShouldBeRemoved"));
        roperty.removeChangeSet("changeSet_changeSetShouldBeRemoved");
        assertThat(roperty.get("key_changeSetShouldBeRemoved", resolver), nullValue());
    }

    @Test
    public void keyAndValueShouldBeRemovedWithChangeSet() {
        roperty.setWithChangeSet("key_keyAndValueShouldBeRemovedWithChangeSet", "value_keyAndValueShouldBeRemovedWithChangeSet", "description_keyAndValueShouldBeRemovedWithChangeSet", "changeSet_keyAndValueShouldBeRemovedWithChangeSet", "domainValue1", "domainValue2");
        roperty.reload();
        resolver.addActiveChangeSets("changeSet_keyAndValueShouldBeRemovedWithChangeSet");
        assertThat(roperty.get("key_keyAndValueShouldBeRemovedWithChangeSet", resolver), Matchers.<Object>is("value_keyAndValueShouldBeRemovedWithChangeSet"));
        roperty.removeWithChangeSet("key_keyAndValueShouldBeRemovedWithChangeSet", "changeSet_keyAndValueShouldBeRemovedWithChangeSet", "domainValue1", "domainValue2");
        assertThat(roperty.get("key_keyAndValueShouldBeRemovedWithChangeSet", resolver), nullValue());
    }

    @Test
    public void keyAndValueShouldBeRemoved() {
        roperty.set("key_keyAndValueShouldBeRemoved", "value_keyAndValueShouldBeRemoved", "description_keyAndValueShouldBeRemoved", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_keyAndValueShouldBeRemoved", resolver), Matchers.<Object>is("value_keyAndValueShouldBeRemoved"));
        roperty.remove("key_keyAndValueShouldBeRemoved", "domainValue1", "domainValue2");
        assertThat(roperty.get("key_keyAndValueShouldBeRemoved", resolver), nullValue());
    }

    @Test
    public void removingKeyRemovesAllValues() {
        roperty.set("key_removingKeyRemovesAllValues", "value_removingKeyRemovesAllValues", "description_removingKeyRemovesAllValues", "domainValue1", "domainValue2");
        roperty.reload();
        assertThat(roperty.get("key_removingKeyRemovesAllValues", resolver), Matchers.<Object>is("value_removingKeyRemovesAllValues"));
        roperty.removeKey("key_removingKeyRemovesAllValues");
        assertThat(roperty.get("key_removingKeyRemovesAllValues", resolver), nullValue());
    }

}
