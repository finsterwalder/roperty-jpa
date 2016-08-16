package com.parship.roperty.persistence.jpa;

import com.parship.roperty.persistence.jpa.EqualsCriterion;
import com.parship.roperty.persistence.jpa.QueryBuilderDelegate;
import com.parship.roperty.persistence.jpa.RopertyKey;
import com.parship.roperty.persistence.jpa.RopertyPersistenceException;
import com.parship.roperty.persistence.jpa.RopertyValue;
import com.parship.roperty.persistence.jpa.RopertyValueDAO;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
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
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        ropertyValueDAO.loadRopertyValues(ropertyKey);
    }

    @Test
    public void returnEmptyListIfNoValuesFoundForRopertyKey() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        Mockito.when(queryBuilderDelegate.equality(Matchers.any(EqualsCriterion.class))).thenReturn(typedQuery);

        List<RopertyValue> ropertyValues = ropertyValueDAO.loadRopertyValues(ropertyKey);

        Mockito.verify(queryBuilderDelegate).createEntityManager();
        Mockito.verify(queryBuilderDelegate).equality(Matchers.any(EqualsCriterion.class));
        Mockito.verify(typedQuery).getResultList();
        Mockito.verify(ropertyKey).getId();
        Mockito.verify(entityManager).close();
        Assert.assertThat(ropertyValues, Matchers.empty());
    }

    @Test
    public void returnValuesFoundForRopertyKey() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        Mockito.when(queryBuilderDelegate.equality(Matchers.any(EqualsCriterion.class))).thenReturn(typedQuery);
        Mockito.when(typedQuery.getResultList()).thenReturn(Arrays.asList(ropertyValue));

        List<RopertyValue> ropertyValues = ropertyValueDAO.loadRopertyValues(ropertyKey);

        Mockito.verify(queryBuilderDelegate).createEntityManager();
        Mockito.verify(queryBuilderDelegate).equality(Matchers.any(EqualsCriterion.class));
        Mockito.verify(typedQuery).getResultList();
        Mockito.verify(ropertyKey).getId();
        Mockito.verify(entityManager).close();
        Assert.assertThat(ropertyValues, Matchers.contains(ropertyValue));
    }

    @Test(expected = NullPointerException.class)
    public void failIfNoEntityManagerOnLoadingSingleRopertyValue() {
        ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfTypedQueryIsNullOnLoadingSingleRopertyValue() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN);
    }

    @Test
    public void returnNullIfSingleRopertyValueNotFound() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        Mockito.when(queryBuilderDelegate.equality(Matchers.any(EqualsCriterion.class), Matchers.any(EqualsCriterion.class))).thenReturn(typedQuery);

        RopertyValue result = ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN);

        Mockito.verify(queryBuilderDelegate).createEntityManager();
        Mockito.verify(queryBuilderDelegate).equality(Matchers.any(EqualsCriterion.class), Matchers.any(EqualsCriterion.class));
        Mockito.verify(typedQuery).getResultList();
        Mockito.verify(entityManager).close();
        Assert.assertThat(result, Matchers.nullValue());
    }

    @Test
    public void returnSingleRopertyValue() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        Mockito.when(queryBuilderDelegate.equality(Matchers.any(EqualsCriterion.class), Matchers.any(EqualsCriterion.class))).thenReturn(typedQuery);
        Mockito.when(typedQuery.getResultList()).thenReturn(Arrays.asList(ropertyValue));

        RopertyValue result = ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN);

        Mockito.verify(queryBuilderDelegate).createEntityManager();
        Mockito.verify(queryBuilderDelegate).equality(Matchers.any(EqualsCriterion.class), Matchers.any(EqualsCriterion.class));
        Mockito.verify(typedQuery).getResultList();
        Mockito.verify(entityManager).close();
        Assert.assertThat(result, Matchers.is(ropertyValue));
    }

    @Test(expected = NullPointerException.class)
    public void failIfCountAndNoEntityManager() {
        ropertyValueDAO.getNumberOfValues(ropertyKey);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfNoTypedQueryOnCount() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        ropertyValueDAO.getNumberOfValues(ropertyKey);
    }

    @Test(expected = RopertyPersistenceException.class)
    public void failIfNoResultOnCount() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        TypedQuery<Long> countQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(queryBuilderDelegate.count(ropertyKey)).thenReturn(countQuery);

        ropertyValueDAO.getNumberOfValues(ropertyKey);
    }

    @Test
    public void countShouldReturnNumberOfValues() {
        Mockito.when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        TypedQuery<Long> countQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(queryBuilderDelegate.count(ropertyKey)).thenReturn(countQuery);
        Mockito.when(countQuery.getSingleResult()).thenReturn(4711L);
        Long result = ropertyValueDAO.getNumberOfValues(ropertyKey);

        Mockito.verify(queryBuilderDelegate).createEntityManager();
        Mockito.verify(queryBuilderDelegate).count(ropertyKey);
        Mockito.verify(countQuery).getSingleResult();
        Mockito.verify(entityManager).close();
        Assert.assertThat(result, Matchers.is(4711L));
    }


}