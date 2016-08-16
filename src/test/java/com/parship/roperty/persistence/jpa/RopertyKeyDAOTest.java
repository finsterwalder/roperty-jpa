package com.parship.roperty.persistence.jpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        RopertyKey result = ropertyKeyDAO.loadRopertyKey(KEY);

        verify(queryBuilderDelegate).createEntityManager();
        verify(entityManager).find(RopertyKey.class, KEY);
        verify(entityManager).close();
        assertThat(result, nullValue());

        verifyNoMoreInteractions(queryBuilderDelegate, entityManager, typedQuery);
    }

    @Test
    public void existingRopertyKeyShouldBeReturned() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        when(entityManager.find(RopertyKey.class, KEY)).thenReturn(ropertyKey);

        RopertyKey result = ropertyKeyDAO.loadRopertyKey(KEY);

        verify(queryBuilderDelegate).createEntityManager();
        verify(entityManager).find(RopertyKey.class, KEY);
        verify(entityManager).close();
        assertThat(result, is(ropertyKey));
    }

    @Test(expected = NullPointerException.class)
    public void failIfMissingEntityManager() {
        ropertyKeyDAO.loadAllRopertyKeys();
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfTypedQueryIsNullOnLoadingAllRopertyKeys() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        ropertyKeyDAO.loadAllRopertyKeys();
    }

    @Test
    public void loadAllReturnsEmptyListIfNoRopertyKeysGiven() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        when(queryBuilderDelegate.all()).thenReturn(typedQuery);

        List<RopertyKey> ropertyKeys = ropertyKeyDAO.loadAllRopertyKeys();

        verify(queryBuilderDelegate).createEntityManager();
        verify(queryBuilderDelegate).all();
        verify(typedQuery).getResultList();
        verify(entityManager).close();
        assertThat(ropertyKeys, empty());
    }

    @Test
    public void loadAllReturnsRopertyKeys() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        when(queryBuilderDelegate.all()).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(ropertyKey));

        List<RopertyKey> ropertyKeys = ropertyKeyDAO.loadAllRopertyKeys();

        verify(queryBuilderDelegate).createEntityManager();
        verify(queryBuilderDelegate).all();
        verify(typedQuery).getResultList();
        verify(entityManager).close();
        assertThat(ropertyKeys, contains(ropertyKey));
        assertThat(ropertyKeys.size(), is(1));
    }

}