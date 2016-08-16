package com.parship.roperty.persistence.jpa;

public class EqualsCriterion<Y> {
    private String attributeName;
    private Y comparison;

    public String getAttributeName() {
        return attributeName;
    }

    public EqualsCriterion<Y> withAttributeName(String attributeName) {
        this.attributeName = attributeName;
        return this;
    }

    public Y getComparison() {
        return comparison;
    }

    public EqualsCriterion<Y> withComparison(Y comparison) {
        this.comparison = comparison;
        return this;
    }
}
