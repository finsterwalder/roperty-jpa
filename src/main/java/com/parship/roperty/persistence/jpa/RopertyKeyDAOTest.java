package com.parship.roperty.persistence.jpa;

import com.parship.roperty.persistence.jpa.QueryBuilderDelegate;
import com.parship.roperty.persistence.jpa.RopertyKey;
import com.parship.roperty.persistence.jpa.RopertyKeyDAO;
import com.parship.roperty.persistence.jpa.RopertyPersistenceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RopertyKeyDAOTest {

    public static final String KEY = "key";

    @InjectMocks
    private RopertyKeyDAO ropertyKeyDAO;

    @Mock
    private QueryBuilderDelegate<RopertyKey> queryBuilderDelegate;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<RopertyKey> typedQuery;

    @Mock
    private RopertyKey ropertyKey;

    @Test
    public void nonExistingRopertyKeyShouldReturnNull() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        RopertyKey result = ropertyKeyDAO.loadRopertyKey(KEY);

        Mockito.verify(queryBuilderDelegate).createEntityManager();
        Mockito.verify(entityManager).find(RopertyKey.class, KEY);
        Mockito.verify(entityManager).close();
        Assert.assertThat(result, Matchers.nullValue());

        Mockito.verifyNoMoreInteractions(queryBuilderDelegate, entityManager, typedQuery);
    }

    @Test
    public void existingRopertyKeyShouldBeReturned() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        Mockito.when(entityManager.find(RopertyKey.class, KEY)).thenReturn(ropertyKey);

        RopertyKey result = ropertyKeyDAO.loadRopertyKey(KEY);

        Mockito.verify(queryBuilderDelegate).createEntityManager();
        Mockito.verify(entityManager).find(RopertyKey.class, KEY);
        Mockito.verify(entityManager).close();
        Assert.assertThat(result, Matchers.is(ropertyKey));
    }

    @Test(expected = NullPointerException.class)
    public void failIfMissingEntityManager() {
        ropertyKeyDAO.loadAllRopertyKeys();
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfTypedQueryIsNullOnLoadingAllRopertyKeys() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        ropertyKeyDAO.loadAllRopertyKeys();
    }

    @Test
    public void loadAllReturnsEmptyListIfNoRopertyKeysGiven() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        Mockito.when(queryBuilderDelegate.all()).thenReturn(typedQuery);

        List<RopertyKey> ropertyKeys = ropertyKeyDAO.loadAllRopertyKeys();

        Mockito.verify(queryBuilderDelegate).createEntityManager();
        Mockito.verify(queryBuilderDelegate).all();
        Mockito.verify(typedQuery).getResultList();
        Mockito.verify(entityManager).close();
        Assert.assertThat(ropertyKeys, Matchers.empty());
    }

    @Test
    public void loadAllReturnsRopertyKeys() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        Mockito.when(queryBuilderDelegate.all()).thenReturn(typedQuery);
        Mockito.when(typedQuery.getResultList()).thenReturn(Arrays.asList(ropertyKey));

        List<RopertyKey> ropertyKeys = ropertyKeyDAO.loadAllRopertyKeys();

        Mockito.verify(queryBuilderDelegate).createEntityManager();
        Mockito.verify(queryBuilderDelegate).all();
        Mockito.verify(typedQuery).getResultList();
        Mockito.verify(entityManager).close();
        Assert.assertThat(ropertyKeys, Matchers.contains(ropertyKey));
        Assert.assertThat(ropertyKeys.size(), Matchers.is(1));
    }

}