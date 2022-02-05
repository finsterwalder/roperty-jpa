package com.parship.roperty.persistence.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QueryBuilderTest {

    private static final String ATTRIBUTE_NAME = "attributeName";
    public static final String EXPRESSION = "expression";

    @InjectMocks
    private QueryBuilder<Long> queryBuilder;

    @Mock
    private EqualsCriterion equalsCriterion;

    @Mock
    private LikeCriterion likeCriterion;

    @Mock(lenient = true)
    private EntityManager entityManager;

    @Mock(lenient = true)
    private CriteriaBuilder criteriaBuilder;

    @Mock(lenient = true)
    private Metamodel metamodel;

    @Mock(lenient = true)
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
    private Path<String> stringPath;

    @Mock
    private Predicate restriction;

    @Mock
    private TypedQuery<Long> typedQuery;

    @Mock
    private Predicate predicate;

    @BeforeEach
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
        queryBuilder.setResultClass(Long.class);
        when(equalsCriterion.getAttributeName()).thenReturn(ATTRIBUTE_NAME);
        when(equalsCriterion.getComparison()).thenReturn(1L);
        when(entityType.getSingularAttribute(ATTRIBUTE_NAME)).thenReturn(singularAttribute);
        when(criteriaBuilder.equal(path, 1L)).thenReturn(restriction);
        when(criteriaBuilder.and(restriction)).thenReturn(predicate);
        when(root.get(singularAttribute)).thenReturn(path);

        TypedQuery<Long> typedQuery = queryBuilder.equality(equalsCriterion);

        verify(equalsCriterion, times(2)).getAttributeName();
        verify(entityType).getSingularAttribute(ATTRIBUTE_NAME);
        verify(root).get(singularAttribute);
        verify(equalsCriterion, times(2)).getComparison();
        verify(criteriaBuilder).equal(path, 1L);
        verify(criteriaQuery).where(predicate);
        verifyMocks();
        assertThat(typedQuery, is(this.typedQuery));
    }

    @Test
    public void returnsTypedQueryWhenFetchingEntityByAttribute() throws Exception {
        queryBuilder.setResultClass(Long.class);
        when(likeCriterion.getAttributeName()).thenReturn(ATTRIBUTE_NAME);
        when(likeCriterion.getExpression()).thenReturn(EXPRESSION);
        when(entityType.getSingularAttribute(ATTRIBUTE_NAME, String.class)).thenReturn(singularAttribute);
        when(criteriaBuilder.like(stringPath, EXPRESSION)).thenReturn(restriction);
        when(criteriaBuilder.and(restriction)).thenReturn(predicate);
        when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
        when(root.get(singularAttribute)).thenReturn(stringPath);

        TypedQuery<Long> typedQuery = queryBuilder.likeliness(likeCriterion);

        verify(likeCriterion, times(2)).getAttributeName();
        verify(likeCriterion, times(2)).getExpression();
        verify(entityType).getSingularAttribute(ATTRIBUTE_NAME, String.class);
        verify(root).get(singularAttribute);
        verify(criteriaBuilder).like(stringPath, EXPRESSION);
        verify(criteriaBuilder).lower(stringPath);
        verify(criteriaBuilder).and(restriction);
        verify(criteriaQuery).where(predicate);
        verifyMocks();

        assertThat(typedQuery, is(this.typedQuery));
    }

    @Test
    public void failsIfNoCriterionGiven() {
        assertThrows(NullPointerException.class, () -> queryBuilder.equality());
    }

    @Test
    public void failsIfAllAndNoResultClassGiven() {
        assertThrows(NullPointerException.class, () -> queryBuilder.all());
    }

    @Test
    public void failsIfEqualityAndNoResultClassGiven() {
        assertThrows(NullPointerException.class, () -> queryBuilder.equality(new EqualsCriterion()));
    }

    @Test
    public void fetchingAllEntitiesShouldReturnTypedQuery() throws Exception {
        queryBuilder.setResultClass(Long.class);
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
