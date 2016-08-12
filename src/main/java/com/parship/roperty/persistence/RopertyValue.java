package com.parship.roperty.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="roperty_value")
public class RopertyValue {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private RopertyKey key;

    private Serializable value;

    @Column(name="change_set")
    private String changeSet;
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
}
