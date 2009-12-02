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
import org.osgi.service.useradmin.UserAdmin;

/**
 * The StorageProvider interface defines the methods needed by a UserAdmin
 * implementation to maintain persistent UserAdmin data.
 *
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public interface StorageProvider {

    // private enum GroupType { BASIC, REQUIRED };

    // role management

    /**
     * Create a new user with the given name. The user initially has no
     * properties or credentials assigned.
     * 
     * @see UserAdmin#createRole(String, int)
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
     * @see UserAdmin#createRole(String, int)
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
     * @see UserAdmin#removeRole(String)
     * 
     * @param role The <code>Role</code> to delete.
     * @throws StorageException if the role could not be deleted.
     */
    boolean deleteRole(Role role) throws StorageException;

    // group management

    /**
     * Retrieve basic members of the given group. Eventually creates new Role
     * objects via the given factory.
     * 
     * @see Group#getMembers()
     * 
     * @param factory The <code>UserAdminFactory</code> used to create member
     *                roles.
     * @param group The <code>Group</code> whose members are retrieved.
     * @return A collection of <code>Role</code> objects that are basic members
     *         of the given group.
     * @throws StorageException
     */
    Collection<Role> getMembers(UserAdminFactory factory, Group group) throws StorageException;

    /**
     * Retrieve required members of the given group. Eventually creates new Role
     * objects via the given factory.
     * 
     * @see Group#getRequiredMembers()
     * 
     * @param factory The <code>UserAdminFactory</code> used to create member
     *                roles.
     * @param group The <code>Group</code> whose members are retrieved.
     * @return A collection of <code>Role</code> objects that are required members
     *         of the given group.
     * @throws StorageException
     */
    Collection<Role> getRequiredMembers(UserAdminFactory factory, Group group) throws StorageException;

    /**
     * Adds a role as a basic member to a group.
     * 
     * @see Group#addMember(Role)
     * 
     * @param group The <code>Group</code> to add the <code>Role</code> as basic member.
     * @param role The <code>Role</code> to add.
     * @return True if the given role was added - false otherwise.
     * @throws StorageException
     */
    boolean addMember(Group group, Role role) throws StorageException;

    /**
     * Adds a role as a required member to a group.
     * 
     * @see Group#addRequiredMember(Role)
     * 
     * @param group The <code>Group</code> to add the <code>Role</code> as required member.
     * @param role The <code>Role</code> to add.
     * @return True if the given role was added - false otherwise.
     * @throws StorageException
     */
    boolean addRequiredMember(Group group, Role role) throws StorageException;

    /**
     * Removes a member from the given group.
     * 
     * @see Group#removeMember(Role)
     * 
     * @param group
     * @param role
     * @return
     * @throws StorageException
     */
    boolean removeMember(Group group, Role role) throws StorageException;

    // property management

    /**
     * Sets a <code>String</code> attribute to a role.
     * 
     * @param role The <code>Role</code> to set the attribute to.
     * @param key The key of the attribute.
     * @param value The value of the attribute.
     * @throws StorageException
     */
    void setRoleAttribute(Role role, String key, Object value) throws StorageException;

    /**
     * Removes an attribute from a role.
     * 
     * @param role The <code>Role</code> to remove the attribute from.
     * @param key The key of the attribute.
     * @throws StorageException
     */
    void removeRoleAttribute(Role role, String key) throws StorageException;

    /**
     * Removes all attributes from the given role.
     * 
     * @param role The <code>Role</code> to remove the attribute(s) from.
     * @throws StorageException
     */
    void clearRoleAttributes(Role role) throws StorageException;

    // credential management

    /**
     * Sets a <code>String</code> credential to a user.
     * 
     * @param user The <code>User</code> to set the credential to.
     * @param key The key of the credential.
     * @param value The value of the credential.
     * @throws StorageException
     */
    void setUserCredential(User user, String key, Object value) throws StorageException;

    /**
     * Removes a credential from a role.
     * 
     * @param user The <code>User</code> to remove the credential from.
     * @param key The key of the credential.
     * @throws StorageException
     */
    void removeUserCredential(User user, String key) throws StorageException;

    /**
     * Removes all credentials for a user.
     * 
     * @param user The <code>User</code> to remove the credentials for.
     * @throws StorageException
     */
    void clearUserCredentials(User user) throws StorageException;

    // role getters & finders

    /**
     * Returns the role with the given name.
     * 
     * @see UserAdmin#getRole(String)
     * 
     * @param factory The <code>UserAdminFactory</code> used to eventually create the
     *                implementation object.
     * @param name The role to find.
     * @return A <code>Role</code> implementation.
     * @throws StorageException
     */
    Role getRole(UserAdminFactory factory, String name) throws StorageException;

    /**
     * Retrieves the user with the given attributes.
     * 
     * @see UserAdmin#getUser(String, String)
     * 
     * @param factory The <code>UserAdminFactory</code> used to eventually create the
     *                implementation object.
     * @param key The attribute key to search for.
     * @param value The attribute value to search for.
     * @return The <code>User</code> object matching the query.
     * @throws StorageException
     */
    User getUser(UserAdminFactory factory, String key, String value) throws StorageException;

    /**
     * Returns the roles that match the given filter.
     * 
     * @see UserAdmin#getRoles(String)
     * 
     * @param factory The <code>UserAdminFactory</code> used to eventually create the
     *                implementation object.
     * @param filter 
     * @return
     * @throws StorageException
     */
    Collection<Role> findRoles(UserAdminFactory factory, String filter) throws StorageException;
}
