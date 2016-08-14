package com.parship.roperty.persistence;

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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RopertyValueDAOTest {

    public static final String KEY = "key";
    public static final String PATTERN = "pattern";

    @InjectMocks
    private RopertyValueDAO ropertyValueDAO;

    @Mock
    private QueryBuilderDelegate<RopertyValue> queryBuilderDelegate;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<RopertyValue> typedQuery;

    @Mock
    private RopertyKey ropertyKey;

    @Mock
    private RopertyValue ropertyValue;

    @Mock
    private Object value;

    @Test(expected = NullPointerException.class)
    public void failIfNoEntityManagerOnLoadingRopertyValuesForKey() {
        ropertyValueDAO.loadRopertyValues(ropertyKey);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfTypedQueryIsNullOnLoadingRopertyValuesForKey() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        ropertyValueDAO.loadRopertyValues(ropertyKey);
    }

    @Test
    public void returnEmptyListIfNoValuesFoundForRopertyKey() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        when(queryBuilderDelegate.equality(any(EqualsCriterion.class))).thenReturn(typedQuery);

        List<RopertyValue> ropertyValues = ropertyValueDAO.loadRopertyValues(ropertyKey);

        verify(queryBuilderDelegate).createEntityManager();
        verify(queryBuilderDelegate).equality(any(EqualsCriterion.class));
        verify(typedQuery).getResultList();
        verify(ropertyKey).getId();
        verify(entityManager).close();
        assertThat(ropertyValues, empty());
    }

    @Test
    public void returnValuesFoundForRopertyKey() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        when(queryBuilderDelegate.equality(any(EqualsCriterion.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(ropertyValue));

        List<RopertyValue> ropertyValues = ropertyValueDAO.loadRopertyValues(ropertyKey);

        verify(queryBuilderDelegate).createEntityManager();
        verify(queryBuilderDelegate).equality(any(EqualsCriterion.class));
        verify(typedQuery).getResultList();
        verify(ropertyKey).getId();
        verify(entityManager).close();
        assertThat(ropertyValues, contains(ropertyValue));
    }

    @Test(expected = NullPointerException.class)
    public void failIfNoEntityManagerOnLoadingSingleRopertyValue() {
        ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfTypedQueryIsNullOnLoadingSingleRopertyValue() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN);
    }

    @Test
    public void returnNullIfSingleRopertyValueNotFound() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        when(queryBuilderDelegate.equality(any(EqualsCriterion.class), any(EqualsCriterion.class))).thenReturn(typedQuery);

        RopertyValue result = ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN);

        verify(queryBuilderDelegate).createEntityManager();
        verify(queryBuilderDelegate).equality(any(EqualsCriterion.class), any(EqualsCriterion.class));
        verify(typedQuery).getResultList();
        verify(entityManager).close();
        assertThat(result, nullValue());
    }

    @Test
    public void returnSingleRopertyValue() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        when(queryBuilderDelegate.equality(any(EqualsCriterion.class), any(EqualsCriterion.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(ropertyValue));

        RopertyValue result = ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN);

        verify(queryBuilderDelegate).createEntityManager();
        verify(queryBuilderDelegate).equality(any(EqualsCriterion.class), any(EqualsCriterion.class));
        verify(typedQuery).getResultList();
        verify(entityManager).close();
        assertThat(result, is(ropertyValue));

        Mockito.verifyNoMoreInteractions(queryBuilderDelegate, entityManager, typedQuery, ropertyKey, ropertyValue, value);

    }


}