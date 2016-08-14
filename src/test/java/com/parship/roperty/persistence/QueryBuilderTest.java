package com.parship.roperty.persistence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryBuilderTest {

    @InjectMocks
    private QueryBuilder<Long> queryBuilder;

    @Mock
    private EqualsCriterion equalsCriterion;

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Metamodel metamodel;

    @Mock
    private CriteriaQuery<Long> criteriaQuery;

    @Mock
    private EntityType<Long> entityType;

    @Mock
    private Root<Long> root;

    @Mock
    private SingularAttribute singularAttribute;

    @Mock
    private Path<?> path;

    @Mock
    private Predicate restriction;

    @Mock
    private TypedQuery<Long> typedQuery;

    @Before
    public void prepareMocks() {
        when(entityManager.getMetamodel()).thenReturn(metamodel);
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQuery);
        when(metamodel.entity(Long.class)).thenReturn(entityType);
        when(criteriaQuery.from(entityType)).thenReturn(root);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        queryBuilder.withEntityManager(entityManager);
    }

    @Test
    public void fetchingEntityByAttributeEqualityShouldReturnTypedQuery() throws Exception {
        queryBuilder.withResultClass(Long.class);
        when(equalsCriterion.getAttributeName()).thenReturn("attributeName");
        when(equalsCriterion.getComparison()).thenReturn(1L);
        when(entityType.getSingularAttribute("attributeName")).thenReturn(singularAttribute);
        when(criteriaBuilder.equal(path, 1L)).thenReturn(restriction);
        when(root.get(singularAttribute)).thenReturn(path);

        TypedQuery<Long> typedQuery = queryBuilder.equality(equalsCriterion);

        verify(equalsCriterion, times(2)).getAttributeName();
        verify(entityType).getSingularAttribute("attributeName");
        verify(root).get(singularAttribute);
        verify(equalsCriterion, times(2)).getComparison();
        verify(criteriaBuilder).equal(path, 1L);
        verify(criteriaQuery).where(restriction);
        verifyMocks();
        assertThat(typedQuery, is(this.typedQuery));
    }

    @Test(expected = NullPointerException.class)
    public void failsIfNoCriterionGiven() {
        queryBuilder.equality();
    }

    @Test(expected = NullPointerException.class)
    public void failsIfAllAndNoResultClassGiven() {
        queryBuilder.all();
    }

    @Test(expected = NullPointerException.class)
    public void failsIfEqualityAndNoResultClassGiven() {
        queryBuilder.equality(new EqualsCriterion());
    }

    @Test
    public void fetchingAllEntitiesShouldReturnTypedQuery() throws Exception {
        queryBuilder.withResultClass(Long.class);
        TypedQuery<Long> typedQuery = queryBuilder.all();

        verifyMocks();
        verify(criteriaQuery).select(root);
        assertThat(typedQuery, is(this.typedQuery));
    }

    private void verifyMocks() {
        verify(entityManager).getMetamodel();
        verify(entityManager).getCriteriaBuilder();
        verify(criteriaBuilder).createQuery(Long.class);
        verify(metamodel).entity(Long.class);
        verify(criteriaQuery).from(entityType);
        verify(entityManager).createQuery(criteriaQuery);
    }

}