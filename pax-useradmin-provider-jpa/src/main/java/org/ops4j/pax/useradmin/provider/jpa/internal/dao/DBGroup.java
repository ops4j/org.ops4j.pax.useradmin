/**
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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;

/**
 * A simple DAO that represents a {@link Group} in the database
 */
@Entity(name = "osgi_service_useradmin_Group")
@Table(name = "osgi_service_useradmin_Group")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class DBGroup extends DBUser {

    @JoinTable(name = "osgi_service_useradmin_Group_requiredMembers")
    private final Set<DBRole> requiredMember = new HashSet<DBRole>();

    @JoinTable(name = "osgi_service_useradmin_Group_basicMember")
    private final Set<DBRole> basicMember    = new HashSet<DBRole>();

    /**
     * 
     */
    public DBGroup() {
        setType(Role.GROUP);
    }

    /**
     * @return the current value of requiredMember
     */
    public Set<DBRole> getRequiredMember() {
        return requiredMember;
    }

    /**
     * @return the current value of basicMember
     */
    public Set<DBRole> getBasicMember() {
        return basicMember;
    }
}
