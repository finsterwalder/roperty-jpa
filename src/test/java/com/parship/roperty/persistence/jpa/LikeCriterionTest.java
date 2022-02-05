package com.parship.roperty.persistence.jpa;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

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
