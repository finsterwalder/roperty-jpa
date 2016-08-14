CREATE TABLE roperty_key (
    id character varying(255) NOT NULL,
    description character varying(255)
);

ALTER TABLE ONLY roperty_key
    ADD CONSTRAINT roperty_key_pkey PRIMARY KEY (id);

CREATE TABLE roperty_value (
    id bigint NOT NULL,
    key character varying(255) NOT NULL,
    pattern character varying(255) NOT NULL,
    value bytea,
    change_set character varying(255)
);

ALTER TABLE ONLY roperty_value
    ADD CONSTRAINT roperty_value_pkey PRIMARY KEY (id);

ALTER TABLE ONLY roperty_value
    ADD CONSTRAINT roperty_value_key_pattern_value UNIQUE (key, pattern);

CREATE INDEX roperty_value_fkey_index ON roperty_value USING btree (key);

ALTER TABLE ONLY roperty_value
    ADD CONSTRAINT roperty_value_fkey FOREIGN KEY (key) REFERENCES roperty_key(id);
