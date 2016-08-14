package com.parship.roperty.persistence;

import com.parship.roperty.DomainSpecificValue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "roperty_value", indexes = {@Index(columnList = "key"), @Index(columnList = "key, pattern, value", unique=true)})
public class RopertyValue {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "key")
    private RopertyKey key;

    @Column(name = "value", nullable = false)
    private Serializable value;

    @Column(name = "change_set")
    private String changeSet;

    @Column(name = "pattern", nullable = false)
    private String pattern;

    public RopertyKey getKey() {
        return key;
    }

    public void setKey(RopertyKey key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Serializable value) {
        this.value = value;
    }

    public String getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(String changeSet) {
        this.changeSet = changeSet;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean equals(DomainSpecificValue domainSpecificValue) {
        return Objects.equals(domainSpecificValue.getPatternStr(), pattern)
                && Objects.equals(domainSpecificValue.getValue(), value);
    }


}
