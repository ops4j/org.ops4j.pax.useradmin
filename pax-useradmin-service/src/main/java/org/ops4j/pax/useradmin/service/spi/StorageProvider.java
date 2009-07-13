/*
 * Copyright 2009 Matthias Kuespert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.useradmin.service.spi;

import java.util.Collection;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * The interface used to maintain persistent UserAdmin data.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public interface StorageProvider {

    // role management

    /**
     * Create a new user with the given name. The user initially has no
     * properties or credentials assigned.
     * 
     * @param factory The <code>UserAdminFactory</code> used to create the
     *            implementation object.
     * @param name The user name.
     * @return An object implementing the <code>User</code> interface or null if
     *         a role with the given name already exists.
     * @throws StorageException if the user could not be created
     */
    User createUser(UserAdminFactory factory, String name) throws StorageException;

    /**
     * Create a new group with the given name. The group initially has no
     * properties or credentials assigned.
     * 
     * @param factory The <code>UserAdminFactory</code> used to create the
     *            implementation object.
     * @param name The group name.
     * @return An object implementing the <code>Group</code> interface or null
     *         if a role with the given name already exists.
     * @throws StorageException if the user could not be created
     */
    Group createGroup(UserAdminFactory factory, String name) throws StorageException;

    /**
     * Deletes the role with the given name. The role is also removed from all
     * groups it is a member of.
     * 
     * @param role The <code>Role</code> to delete.
     * @throws StorageException if the role could not be deleted.
     */
    boolean deleteRole(Role role) throws StorageException;

    // group management

    Collection<Role> getMembers(UserAdminFactory factory, Group group) throws StorageException;

    Collection<Role> getRequiredMembers(UserAdminFactory factory, Group group) throws StorageException;

    boolean addMember(Group group, Role role) throws StorageException;

    boolean addRequiredMember(Group group, Role role) throws StorageException;

    boolean removeMember(Group group, Role role) throws StorageException;

    // property management

    void setRoleAttribute(Role role, String key, String value) throws StorageException;

    void setRoleAttribute(Role role, String key, byte[] value) throws StorageException;

    void removeRoleAttribute(Role role, String key) throws StorageException;

    void clearRoleAttributes(Role role) throws StorageException;

    // credential management

    void setUserCredential(User user, String key, String value) throws StorageException;

    void setUserCredential(User user, String key, byte[] value) throws StorageException;

    void removeUserCredential(User user, String key) throws StorageException;

    void clearUserCredentials(User user) throws StorageException;

    // getters & finders

    Role getRole(UserAdminFactory factory, String name) throws StorageException;

    User getUser(UserAdminFactory factory, String key, String value) throws StorageException;

    Collection<Role> findRoles(UserAdminFactory factory, String filter) throws StorageException;
}
