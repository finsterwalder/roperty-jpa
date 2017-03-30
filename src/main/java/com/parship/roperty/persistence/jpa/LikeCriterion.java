package com.parship.roperty.persistence.jpa;

class LikeCriterion {
    private String attributeName;
    private String expression;

    String getAttributeName() {
        return attributeName;
    }

    LikeCriterion withAttributeName(String attributeName) {
        this.attributeName = attributeName;
        return this;
    }

    String getExpression() {
        return expression;
    }

    LikeCriterion withExpression(String comparison) {
        this.expression = comparison;
        return this;
    }
}
