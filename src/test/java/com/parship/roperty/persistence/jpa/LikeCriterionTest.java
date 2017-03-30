package com.parship.roperty.persistence.jpa;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by daniel on 30.03.17.
 */
public class LikeCriterionTest {

    @Test
    public void containsAllValues() {
        String attributeName = "attributeName";
        String expression = "expression";
        LikeCriterion likeCriterion = new LikeCriterion()
                .withAttributeName(attributeName)
                .withExpression(expression);
        assertThat(likeCriterion.getAttributeName(), is(attributeName));
        assertThat(likeCriterion.getExpression(), is(expression));
    }

}