package com.parship.roperty.persistence;


import com.parship.roperty.DefaultDomainSpecificValueFactory;
import com.parship.roperty.DefaultKeyValuesFactory;
import com.parship.roperty.DomainSpecificValueFactory;
import com.parship.roperty.KeyValues;
import com.parship.roperty.KeyValuesFactory;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class H2IntegrationTest {

    @Test
    public void keyAndValueShouldBePersistedInRelationalDatabase() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("com.parship.roperty.persistence");
        RelationalPersistence relationalPersistence = new RelationalPersistence();
        relationalPersistence.setEntityManagerFactory(entityManagerFactory);
        DomainSpecificValueFactory domainSpecificValueFactory = new DefaultDomainSpecificValueFactory();
        KeyValuesFactory keyValuesFactory = new DefaultKeyValuesFactory();
        KeyValues inputKeyValues = new KeyValues(domainSpecificValueFactory);
        inputKeyValues.setDescription("description");
        inputKeyValues.putWithChangeSet("changeSet", "value1", "domain1", "domain11", "domain111");
        inputKeyValues.putWithChangeSet("changeSet", "value2", "domain1", "domain11", "domain112");
        inputKeyValues.putWithChangeSet("changeSet", "value3", "domain1", "domain12", "domain121");
        inputKeyValues.putWithChangeSet("changeSet", "value4", "domain2", "domain21", "domain211");

        relationalPersistence.store("key", inputKeyValues, "changeSet");
        KeyValues outputKeyValues = relationalPersistence.load("key", keyValuesFactory, domainSpecificValueFactory);

        assertThat(inputKeyValues.getDescription(), is(outputKeyValues.getDescription()));
        assertThat(outputKeyValues.getDomainSpecificValues(), equalTo(inputKeyValues.getDomainSpecificValues()));
    }

}
