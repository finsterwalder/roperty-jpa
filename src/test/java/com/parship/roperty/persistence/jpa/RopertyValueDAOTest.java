package com.parship.roperty.persistence.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RopertyValueDAOTest {

    private static final String PATTERN = "pattern";
    private static final String CHANGE_SET = "change_set";

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

    @Test
    public void failIfNoEntityManagerOnLoadingRopertyValuesForKey() {
        assertThrows(NullPointerException.class, () -> ropertyValueDAO.loadRopertyValues(ropertyKey));
    }

    @Test
    public void failIfTypedQueryIsNullOnLoadingRopertyValuesForKey() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        assertThrows(RopertyPersistenceException.class, () -> ropertyValueDAO.loadRopertyValues(ropertyKey));
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

    @Test
    public void failIfNoEntityManagerOnLoadingSingleRopertyValue() {
        assertThrows(NullPointerException.class, () -> ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN, CHANGE_SET));
    }

    @Test
    public void failIfTypedQueryIsNullOnLoadingSingleRopertyValue() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        assertThrows(RopertyPersistenceException.class, () -> ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN, CHANGE_SET));
    }

    @Test
    public void returnNullIfSingleRopertyValueNotFound() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        when(queryBuilderDelegate.equality(any(EqualsCriterion.class), any(EqualsCriterion.class), any(EqualsCriterion.class))).thenReturn(typedQuery);

        RopertyValue result = ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN, CHANGE_SET);

        verify(queryBuilderDelegate).createEntityManager();
        verify(queryBuilderDelegate).equality(any(EqualsCriterion.class), any(EqualsCriterion.class), any(EqualsCriterion.class));
        verify(typedQuery).getResultList();
        verify(entityManager).close();
        assertThat(result, nullValue());
    }

    @Test
    public void returnSingleRopertyValue() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        when(queryBuilderDelegate.equality(any(EqualsCriterion.class), any(EqualsCriterion.class), any(EqualsCriterion.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(ropertyValue));

        RopertyValue result = ropertyValueDAO.loadRopertyValue(ropertyKey, PATTERN, CHANGE_SET);

        verify(queryBuilderDelegate).createEntityManager();
        verify(queryBuilderDelegate).equality(any(EqualsCriterion.class), any(EqualsCriterion.class), any(EqualsCriterion.class));
        verify(typedQuery).getResultList();
        verify(entityManager).close();
        assertThat(result, is(ropertyValue));
    }

    @Test
    public void failIfCountAndNoEntityManager() {
        assertThrows(NullPointerException.class, () -> ropertyValueDAO.getNumberOfValues(ropertyKey));
    }

    @Test
    public void failIfNoTypedQueryOnCount() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);

        assertThrows(RopertyPersistenceException.class, () -> ropertyValueDAO.getNumberOfValues(ropertyKey));
    }

    @Test
    public void failIfNoResultOnCount() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(queryBuilderDelegate.count(ropertyKey)).thenReturn(countQuery);

        assertThrows(RopertyPersistenceException.class, () -> ropertyValueDAO.getNumberOfValues(ropertyKey));
    }

    @Test
    public void countShouldReturnNumberOfValues() {
        when(queryBuilderDelegate.createEntityManager()).thenReturn(entityManager);
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(queryBuilderDelegate.count(ropertyKey)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(4711L);
        Long result = ropertyValueDAO.getNumberOfValues(ropertyKey);

        verify(queryBuilderDelegate).createEntityManager();
        verify(queryBuilderDelegate).count(ropertyKey);
        verify(countQuery).getSingleResult();
        verify(entityManager).close();
        assertThat(result, is(4711L));
    }


}
