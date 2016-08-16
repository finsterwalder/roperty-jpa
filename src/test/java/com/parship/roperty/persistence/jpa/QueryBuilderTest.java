package com.parship.roperty.persistence.jpa;

import com.parship.roperty.persistence.jpa.EqualsCriterion;
import com.parship.roperty.persistence.jpa.QueryBuilder;
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

    @Mock
    private Predicate predicate;

    @Before
    public void prepareMocks() {
        Mockito.when(entityManager.getMetamodel()).thenReturn(metamodel);
        Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        Mockito.when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQuery);
        Mockito.when(metamodel.entity(Long.class)).thenReturn(entityType);
        Mockito.when(criteriaQuery.from(entityType)).thenReturn(root);
        Mockito.when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        queryBuilder.withEntityManager(entityManager);
    }

    @Test
    public void fetchingEntityByAttributeEqualityShouldReturnTypedQuery() throws Exception {
        queryBuilder.withResultClass(Long.class);
        Mockito.when(equalsCriterion.getAttributeName()).thenReturn("attributeName");
        Mockito.when(equalsCriterion.getComparison()).thenReturn(1L);
        Mockito.when(entityType.getSingularAttribute("attributeName")).thenReturn(singularAttribute);
        Mockito.when(criteriaBuilder.equal(path, 1L)).thenReturn(restriction);
        Mockito.when(criteriaBuilder.and(restriction)).thenReturn(predicate);
        Mockito.when(root.get(singularAttribute)).thenReturn(path);

        TypedQuery<Long> typedQuery = queryBuilder.equality(equalsCriterion);

        Mockito.verify(equalsCriterion, Mockito.times(2)).getAttributeName();
        Mockito.verify(entityType).getSingularAttribute("attributeName");
        Mockito.verify(root).get(singularAttribute);
        Mockito.verify(equalsCriterion, Mockito.times(2)).getComparison();
        Mockito.verify(criteriaBuilder).equal(path, 1L);
        Mockito.verify(criteriaQuery).where(predicate);
        verifyMocks();
        Assert.assertThat(typedQuery, Matchers.is(this.typedQuery));
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
        Mockito.verify(criteriaQuery).select(root);
        Assert.assertThat(typedQuery, Matchers.is(this.typedQuery));
    }

    private void verifyMocks() {
        Mockito.verify(entityManager).getMetamodel();
        Mockito.verify(entityManager).getCriteriaBuilder();
        Mockito.verify(criteriaBuilder).createQuery(Long.class);
        Mockito.verify(metamodel).entity(Long.class);
        Mockito.verify(criteriaQuery).from(entityType);
        Mockito.verify(entityManager).createQuery(criteriaQuery);
    }

}