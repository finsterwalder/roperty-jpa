package com.parship.roperty.persistence.jpa;

class EqualsCriterion<Y> {
    private String attributeName;
    private Y comparison;

    String getAttributeName() {
        return attributeName;
    }

    EqualsCriterion<Y> withAttributeName(String attributeName) {
        this.attributeName = attributeName;
        return this;
    }

    Y getComparison() {
        return comparison;
    }

    EqualsCriterion<Y> withComparison(Y comparison) {
        this.comparison = comparison;
        return this;
    }
}
