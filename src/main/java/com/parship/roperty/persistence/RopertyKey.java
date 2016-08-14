package com.parship.roperty.persistence;

import org.apache.commons.lang3.Validate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="roperty_key")
public class RopertyKey {

    @Id
    private String id;

    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        Validate.notBlank(id, "id must not be null or blank");
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "RopertyKey{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
