/*
 * Copyright 2013 OPS4J
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.useradmin.provider.jpa.internal.dao;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.osgi.service.useradmin.Role;

/**
 * The base class for {@link Role} DAO objects
 */
@Entity(name = "osgi_service_useradmin_Role")
@Table(name = "osgi_service_useradmin_Role", uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
public class DBRole extends DBVersionedObject {

    @Transient
    private int                           type       = Role.ROLE;

    private String                        name;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKey(name = "key")
    @CollectionTable(name = "osgi_service_useradmin_Role_properties_table", joinColumns = @JoinColumn(name = "role_key_id"))
    private final Map<String, DBProperty> properties = new HashMap<String, DBProperty>();

    protected void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, DBProperty> getProperties() {
        return properties;
    }

}
