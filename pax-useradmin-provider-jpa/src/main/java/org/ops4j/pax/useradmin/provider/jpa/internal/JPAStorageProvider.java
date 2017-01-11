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
package org.ops4j.pax.useradmin.provider.jpa.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.ops4j.pax.useradmin.provider.jpa.ConfigurationConstants;
import org.ops4j.pax.useradmin.provider.jpa.internal.dao.DBCredential;
import org.ops4j.pax.useradmin.provider.jpa.internal.dao.DBGroup;
import org.ops4j.pax.useradmin.provider.jpa.internal.dao.DBProperty;
import org.ops4j.pax.useradmin.provider.jpa.internal.dao.DBRole;
import org.ops4j.pax.useradmin.provider.jpa.internal.dao.DBUser;
import org.ops4j.pax.useradmin.provider.jpa.internal.dao.DBVersionedObject;
import org.ops4j.pax.useradmin.service.PaxUserAdminConstants;
import org.ops4j.pax.useradmin.service.spi.CredentialProvider;
import org.ops4j.pax.useradmin.service.spi.Decryptor;
import org.ops4j.pax.useradmin.service.spi.EncryptedValue;
import org.ops4j.pax.useradmin.service.spi.Encryptor;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.ops4j.pax.useradmin.service.spi.UserAdminFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the actuall class that implements the SPI {@link StorageProvider} by
 * using an underlying {@link EntityManagerFactory} service. This implementation
 * is very conversative and pesemistic so it might not be very performant in
 * long network delay and heavy use scenarios. A caching JPA Provider like
 * EclipseLink would help here in increasing performance.
 */
public class JPAStorageProvider
        implements StorageProvider, CredentialProvider {

    private static final Logger                  LOG = LoggerFactory.getLogger(JPAStorageProvider.class);

    private final EntityManagerFactory           entityManagerFactory;
    private ServiceRegistration<StorageProvider> serviceRegistration;
    private final Long                           trackedServiceID;
    private Map<String, DBRole>                  roleNames;
    private EntityManager                        entityManager;

    /**
     * @param entityManagerFactory
     *            the {@link EntityManagerFactory} to use as a backing store
     * @param trackedServiceID
     *            the ID of the tracked service from the
     *            {@link EntityManagerFactory}
     */
    JPAStorageProvider(EntityManagerFactory entityManagerFactory, Long trackedServiceID) {
        this.entityManagerFactory = entityManagerFactory;
        this.trackedServiceID = trackedServiceID;
    }

    /**
     * Create a new user with the given name. The user initially has no
     * properties or credentials assigned.
     * 
     * @see UserAdmin#createRole(String, int)
     * @param factory
     *            The <code>UserAdminFactory</code> used to create the
     *            implementation object.
     * @param name
     *            The user name.
     * @return An object implementing the <code>User</code> interface or null if
     *         a role with the given name already exists.
     * @throws StorageException
     *             if the user could not be created
     */
    @Override
    public synchronized User createUser(final UserAdminFactory factory, final String name) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        if (map.containsKey(name)) {
            return null;
        }
        return accessTransaction(new TransactionAccess<User>() {

            @Override
            public User doWork(EntityManager manager, EntityTransaction transaction) {
                DBUser user = new DBUser();
                user.setName(name);
                manager.persist(user);
                transaction.commit();
                map.put(name, user);
                return factory.createUser(name, null, null);
            }

            @Override
            public String getProblemString() {
                return "the user '" + name + "' can't be created";
            }
        });
    }

    /**
     * Create a new group with the given name. The group initially has no
     * properties or credentials assigned.
     * 
     * @see UserAdmin#createRole(String, int)
     * @param factory
     *            The <code>UserAdminFactory</code> used to create the
     *            implementation object.
     * @param name
     *            The group name.
     * @return An object implementing the <code>Group</code> interface or null
     *         if a role with the given name already exists.
     * @throws StorageException
     *             if the user could not be created
     */
    @Override
    public synchronized Group createGroup(final UserAdminFactory factory, final String name) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        if (map.containsKey(name)) {
            return null;
        }
        return accessTransaction(new TransactionAccess<Group>() {

            @Override
            public Group doWork(EntityManager manager, EntityTransaction transaction) {
                DBGroup group = new DBGroup();
                group.setName(name);
                manager.persist(group);
                transaction.commit();
                map.put(name, group);
                return factory.createGroup(name, null, null);
            }

            @Override
            public String getProblemString() {
                return "the group '" + name + "' can't be created";
            }
        });
    }

    /**
     * Deletes the role with the given name. The role is also removed from all
     * groups it is a member of.
     * 
     * @see UserAdmin#removeRole(String)
     * @param role
     *            The <code>Role</code> to delete.
     * @throws StorageException
     *             if the role could not be deleted.
     */
    @Override
    public synchronized boolean deleteRole(final Role role) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        final DBRole removedRole = map.remove(role.getName());
        if (removedRole != null) {
            return accessTransaction(new TransactionAccess<Boolean>() {

                @Override
                public Boolean doWork(EntityManager manager, EntityTransaction transaction) {
                    DBRole find = refreshItem(manager, removedRole);
                    // remove from groups
                    for (DBRole otherRole : map.values()) {
                        if (otherRole instanceof DBGroup) {
                            DBGroup dbGroup = refreshItem(manager, (DBGroup) otherRole);
                            boolean basicRemoved = dbGroup.getBasicMember().remove(find);
                            boolean requredRemoved = dbGroup.getRequiredMember().remove(find);
                            if (basicRemoved || requredRemoved) {
                                //Update the item in the cache...
                                map.put(dbGroup.getName(), dbGroup);
                            }
                        }
                    }
                    //remove role itself
                    manager.remove(find);
                    transaction.commit();
                    return true;
                }

                @Override
                public String getProblemString() {
                    return "the role '" + role.getName() + "' can't be deleted";
                }
            });
        } else {
            return false;
        }
    }

    /**
     * Retrieve basic members of the given group. Eventually creates new Role
     * objects via the given factory.
     * 
     * @see Group#getMembers()
     * @param factory
     *            The <code>UserAdminFactory</code> used to create member roles.
     * @param group
     *            The <code>Group</code> whose members are retrieved.
     * @return A collection of <code>Role</code> objects that are basic members
     *         of the given group.
     */
    @Override
    public Collection<Role> getMembers(UserAdminFactory factory, Group group) throws StorageException {
        return loadMembers(factory, group, MemberType.BASIC);
    }

    /**
     * Retrieve required members of the given group. Eventually creates new Role
     * objects via the given factory.
     * 
     * @see Group#getRequiredMembers()
     * @param factory
     *            The <code>UserAdminFactory</code> used to create member roles.
     * @param group
     *            The <code>Group</code> whose members are retrieved.
     * @return A collection of <code>Role</code> objects that are required
     *         members of the given group.
     */
    @Override
    public Collection<Role> getRequiredMembers(UserAdminFactory factory, Group group) throws StorageException {
        return loadMembers(factory, group, MemberType.REQUIRED);
    }

    /**
     * Adds a role as a basic member to a group.
     * 
     * @see Group#addMember(Role)
     * @param group
     *            The <code>Group</code> to add the <code>Role</code> as basic
     *            member.
     * @param role
     *            The <code>Role</code> to add.
     * @return True if the given role was added - false otherwise.
     */
    @Override
    public synchronized boolean addMember(final Group group, final Role role) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        DBRole dbGroupRole = map.get(group.getName());
        if (dbGroupRole instanceof DBGroup) {
            final DBGroup dbGroup = (DBGroup) dbGroupRole;
            final DBRole dbRole = map.get(role.getName());
            if (dbRole != null) {
                if (dbGroup.getBasicMember().contains(dbRole)) {
                    //Already present..
                    return false;
                }
                return accessTransaction(new TransactionAccess<Boolean>() {

                    @Override
                    public Boolean doWork(EntityManager manager, EntityTransaction transaction) {
                        DBGroup findGroup = refreshItem(manager, dbGroup);
                        DBRole findRole = refreshItem(manager, dbRole);
                        findGroup.getBasicMember().add(findRole);
                        transaction.commit();
                        //Update cache...
                        map.put(findGroup.getName(), findGroup);
                        map.put(findRole.getName(), findRole);
                        return true;
                    }

                    @Override
                    public String getProblemString() {
                        return "The role " + role.getName() + " can't be added to the group " + group.getName();
                    }
                });
            } else {
                throw new StorageException("The role " + role.getName() + " does not exits");
            }
        } else {
            throw new StorageException("The group " + group.getName() + " does not exits");
        }
    }

    /**
     * Adds a role as a required member to a group.
     * 
     * @see Group#addRequiredMember(Role)
     * @param group
     *            The <code>Group</code> to add the <code>Role</code> as
     *            required member.
     * @param role
     *            The <code>Role</code> to add.
     * @return True if the given role was added - false otherwise.
     */
    @Override
    public synchronized boolean addRequiredMember(final Group group, final Role role) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        DBRole dbGroupRole = map.get(group.getName());
        if (dbGroupRole instanceof DBGroup) {
            final DBGroup dbGroup = (DBGroup) dbGroupRole;
            final DBRole dbRole = map.get(role.getName());
            if (dbRole != null) {
                if (dbGroup.getRequiredMember().contains(dbRole)) {
                    //Already present..
                    return false;
                }
                return accessTransaction(new TransactionAccess<Boolean>() {

                    @Override
                    public Boolean doWork(EntityManager manager, EntityTransaction transaction) {
                        DBGroup findGroup = refreshItem(manager, dbGroup);
                        DBRole findRole = refreshItem(manager, dbRole);
                        findGroup.getRequiredMember().add(findRole);
                        transaction.commit();
                        //Update cache...
                        map.put(findGroup.getName(), findGroup);
                        map.put(findRole.getName(), findRole);
                        return true;
                    }

                    @Override
                    public String getProblemString() {
                        return "The role " + role.getName() + " can't be added to the group " + group.getName();
                    }
                });
            } else {
                throw new StorageException("The role " + role.getName() + " does not exits");
            }

        } else {
            throw new StorageException("The group " + group.getName() + " does not exits");
        }
    }

    /**
     * Removes a member from the given group.
     * 
     * @see Group#removeMember(Role)
     */
    @Override
    public synchronized boolean removeMember(final Group group, final Role role) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        DBRole dbGroupRole = map.get(group.getName());
        if (dbGroupRole instanceof DBGroup) {
            final DBGroup dbGroup = (DBGroup) dbGroupRole;
            final DBRole dbRole = map.get(role.getName());
            if (dbRole != null) {
                if (!dbGroup.getBasicMember().contains(dbRole) && !dbGroup.getRequiredMember().contains(dbRole)) {
                    //not present..
                    return false;
                }
                return accessTransaction(new TransactionAccess<Boolean>() {

                    @Override
                    public Boolean doWork(EntityManager manager, EntityTransaction transaction) {
                        DBGroup findGroup = refreshItem(manager, dbGroup);
                        DBRole findRole = refreshItem(manager, dbRole);
                        findGroup.getBasicMember().remove(dbRole);
                        findGroup.getRequiredMember().remove(dbRole);
                        transaction.commit();
                        //Update cache...
                        map.put(findGroup.getName(), findGroup);
                        map.put(findRole.getName(), findRole);
                        return true;
                    }

                    @Override
                    public String getProblemString() {
                        return "The role " + role.getName() + " can't be added to the group " + group.getName();
                    }
                });
            } else {
                throw new StorageException("The role " + role.getName() + " does not exits");
            }
        } else {
            throw new StorageException("The group " + group.getName() + " does not exits");
        }
    }

    /**
     * Sets a <code>String</code> attribute to a role.
     * 
     * @param role
     *            The <code>Role</code> to set the attribute to.
     * @param key
     *            The key of the attribute.
     * @param value
     *            The value of the attribute.
     */
    @Override
    public synchronized void setRoleAttribute(final Role role, final String key, final Object value) throws StorageException {
        if (value == null) {
            removeRoleAttribute(role, key);
            return;
        }
        if (value instanceof String || value instanceof byte[]) {
            final Map<String, DBRole> map = getRoleNamesMap();
            final DBRole dbRole = map.get(role.getName());
            accessTransaction(new TransactionAccess<Void>() {

                @Override
                public Void doWork(EntityManager manager, EntityTransaction transaction) {
                    DBProperty dbvalue = new DBProperty();
                    dbvalue.setKey(key);
                    if (value instanceof String) {
                        dbvalue.setData((String) value);
                    } else /*if (value instanceof byte[])*/ {
                        dbvalue.setData((byte[]) value);
                    }
                    DBRole refreshItem = refreshItem(manager, dbRole);
                    refreshItem.getProperties().put(key, dbvalue);
                    transaction.commit();
                    map.put(refreshItem.getName(), refreshItem);
                    return null;
                }

                @Override
                public String getProblemString() {
                    return "the attribute '" + key + "' of role " + role.getName() + " can't be set";
                }
            });
        } else {
            throw new StorageException("Invalid class type for value: " + value.getClass().getName() + " only String and byte[] is allowed!");
        }
    }

    /**
     * Removes an attribute from a role.
     * 
     * @param role
     *            The <code>Role</code> to remove the attribute from.
     * @param key
     *            The key of the attribute.
     */
    @Override
    public synchronized void removeRoleAttribute(final Role role, final String key) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        final DBRole dbRole = map.get(role.getName());
        accessTransaction(new TransactionAccess<Void>() {

            @Override
            public Void doWork(EntityManager manager, EntityTransaction transaction) {
                DBRole refreshItem = refreshItem(manager, dbRole);
                refreshItem.getProperties().remove(key);
                transaction.commit();
                map.put(refreshItem.getName(), refreshItem);
                return null;
            }

            @Override
            public String getProblemString() {
                return "the attribute of role " + role.getName() + " can't be removed";
            }
        });

    }

    /**
     * Removes all attributes from the given role.
     * 
     * @param role
     *            The <code>Role</code> to remove the attribute(s) from.
     */
    @Override
    public synchronized void clearRoleAttributes(final Role role) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        final DBRole dbRole = map.get(role.getName());
        accessTransaction(new TransactionAccess<Void>() {

            @Override
            public Void doWork(EntityManager manager, EntityTransaction transaction) {
                DBRole refreshItem = refreshItem(manager, dbRole);
                refreshItem.getProperties().clear();
                transaction.commit();
                map.put(refreshItem.getName(), refreshItem);
                return null;
            }

            @Override
            public String getProblemString() {
                return "the properties of role " + role.getName() + " can't be cleared";
            }
        });

    }

    /**
     * Sets a <code>String</code> credential to a user.
     * 
     * @param user
     *            The <code>User</code> to set the credential to.
     * @param key
     *            The key of the credential.
     * @param value
     *            The value of the credential.
     */
    @Override
    public synchronized void setUserCredential(Encryptor encryptor, final User user, final String key, final Object value) throws StorageException {
        if (value == null) {
            removeUserCredential(user, key);
            return;
        }
        final Map<String, DBRole> map = getRoleNamesMap();
        final DBRole dbRole = map.get(user.getName());
        if (dbRole instanceof DBUser) {
            final DBUser dbUser = (DBUser) dbRole;
            final DBCredential dbvalue = new DBCredential();
            EncryptedValue encrypt;
            if (value instanceof String) {
                encrypt = encryptor.encrypt(key, (String) value);
            } else if (value instanceof byte[]) {
                encrypt = encryptor.encrypt(key, (byte[]) value);
            } else {
                throw new StorageException("Invalid class type for value: " + value.getClass().getName() + " only String and byte[] is allowed!");
            }
            dbvalue.setKey(key);
            dbvalue.setParameter(encrypt.getAlgorithmParameter());
            dbvalue.setSalt(encrypt.getSalt());
            dbvalue.setVerificationBytes(encrypt.getVerificationBytes());
            dbvalue.setData(encrypt.getEncryptedBytes());
            accessTransaction(new TransactionAccess<Void>() {

                @Override
                public Void doWork(EntityManager manager, EntityTransaction transaction) {
                    DBUser refreshItem = refreshItem(manager, dbUser);
                    refreshItem.getCredentials().put(key, dbvalue);
                    transaction.commit();
                    map.put(refreshItem.getName(), refreshItem);
                    return null;
                }

                @Override
                public String getProblemString() {
                    return "the credential of user " + user.getName() + " can't be set";
                }
            });
        } else {
            throw new StorageException("invalid role specified as user: " + user.getName());
        }
    }

    @Override
    public synchronized void removeUserCredential(final User user, final String key) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        final DBRole dbRole = map.get(user.getName());
        if (dbRole instanceof DBUser) {
            final DBUser dbUser = (DBUser) dbRole;
            accessTransaction(new TransactionAccess<Void>() {

                @Override
                public Void doWork(EntityManager manager, EntityTransaction transaction) {
                    DBUser refreshItem = refreshItem(manager, dbUser);
                    refreshItem.getCredentials().remove(key);
                    transaction.commit();
                    map.put(refreshItem.getName(), refreshItem);
                    return null;
                }

                @Override
                public String getProblemString() {
                    return "the credential of user " + user.getName() + " can't be removed";
                }
            });
        } else {
            throw new StorageException("invalid role specified as user: " + user.getName());
        }
    }

    /**
     * Removes all credentials for a user.
     * 
     * @param user
     *            The <code>User</code> to remove the credentials for.
     */
    @Override
    public synchronized void clearUserCredentials(final User user) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        final DBRole dbRole = map.get(user.getName());
        if (dbRole instanceof DBUser) {
            final DBUser dbUser = (DBUser) dbRole;
            accessTransaction(new TransactionAccess<Void>() {

                @Override
                public Void doWork(EntityManager manager, EntityTransaction transaction) {
                    DBUser refreshItem = refreshItem(manager, dbUser);
                    refreshItem.getCredentials().clear();
                    transaction.commit();
                    map.put(refreshItem.getName(), refreshItem);
                    return null;
                }

                @Override
                public String getProblemString() {
                    return "the properties of role " + user.getName() + " can't be cleared";
                }
            });
        } else {
            throw new StorageException("invalid role specified as user: " + user.getName());
        }

    }

    /**
     * Returns the role with the given name.
     * 
     * @see UserAdmin#getRole(String)
     * @param factory
     *            The <code>UserAdminFactory</code> used to eventually create
     *            the implementation object.
     * @param name
     *            The role to find.
     * @return A <code>Role</code> implementation.
     */
    @Override
    public Role getRole(UserAdminFactory factory, String name) throws StorageException {
        return loadRole(factory, name, null);
    }

    /**
     * Retrieves the user with the given attributes.
     * 
     * @see UserAdmin#getUser(String, String)
     * @param factory
     *            The <code>UserAdminFactory</code> used to eventually create
     *            the implementation object.
     * @param key
     *            The attribute key to search for.
     * @param value
     *            The attribute value to search for.
     * @return The <code>User</code> object matching the query.
     */
    @Override
    public User getUser(UserAdminFactory factory, String key, String value) throws StorageException {
        try {
            Filter filter = createFilter("(" + key + "=" + value + ")");
            Collection<Role> roles = loadRoles(factory, filter);
            Collection<User> users = new ArrayList<User>();
            for (Role role : roles) {
                if (Role.USER == role.getType()) {
                    users.add((User) role);
                }
            }
            if (users.size() == 1) {
                return users.iterator().next();
            }
        } catch (InvalidSyntaxException e) {
            throwStorageException("Invalid filter '" + e.getFilter() + "'", e);
        }
        return null;
    }

    /**
     * Returns the roles that match the given filter.
     * 
     * @see UserAdmin#getRoles(String)
     * @param factory
     *            The <code>UserAdminFactory</code> used to eventually create
     *            the implementation object.
     * @param filterString The search filter for the roles to be retreived.
     */
    @Override
    public Collection<Role> findRoles(UserAdminFactory factory, String filterString) throws StorageException {
        try {
            Filter filter = null;
            if (filterString != null) {
                filter = createFilter(filterString);
            }
            return loadRoles(factory, filter);
        } catch (InvalidSyntaxException e) {
            throwStorageException("Invalid filter '" + e.getFilter() + "'", e);
        }
        return Collections.emptyList();
    }

    private Role loadRole(UserAdminFactory factory, String name, Filter filter) throws StorageException {
        DBRole dbRole = getRoleFromMap(name);
        if (dbRole == null) {
            return null;
        }
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        for (Entry<String, DBProperty> entry : dbRole.getProperties().entrySet()) {
            DBProperty value = entry.getValue();
            properties.put(entry.getKey(), value.getType() == DBProperty.TYPE_STRING ? value.getDataAsString() : value.getData());
        }
        if (filter != null) {
            if (properties.isEmpty()) {
                return null;
            }
            if (!filter.match(properties)) {
                return null;
            }
        }
        Set<String> keySet = null;
        if (dbRole instanceof DBUser) {
            Map<String, DBCredential> credentials = ((DBUser) dbRole).getCredentials();
            if (credentials != null) {
                keySet = credentials.keySet();
            }
        }
        Role role;
        switch (dbRole.getType()) {
            case User.USER:
                role = factory.createUser(name, properties, keySet);
                break;
            case User.GROUP:
                role = factory.createGroup(name, properties, keySet);
                break;
            default:
                throw new StorageException("Invalid role type for role '" + name + "': " + dbRole.getType() + " only USER and GROUP are allowed!");
        }
        return role;
    }

    /**
     * Fetch a given role by name in a syncronized fashion...
     */
    private synchronized DBRole getRoleFromMap(String name) throws StorageException {
        Map<String, DBRole> roleNamesMap = getRoleNamesMap();
        return roleNamesMap.get(name);
    }

    private synchronized Collection<Role> loadRoles(UserAdminFactory factory, Filter filter) throws StorageException {
        Collection<Role> roles = new ArrayList<Role>();
        for (String name : getRoleNamesMap().keySet()) {
            Role role = loadRole(factory, name, filter);
            if (role != null) {
                roles.add(role);
            }
        }
        return roles;
    }

    private synchronized Collection<Role> loadMembers(UserAdminFactory factory, Group group, MemberType memberType) throws StorageException {
        DBRole dbRole = getRoleNamesMap().get(group.getName());
        if (dbRole instanceof DBGroup) {
            DBGroup dbGroup = (DBGroup) dbRole;
            Set<DBRole> member;
            switch (memberType) {
                case BASIC:
                    member = dbGroup.getBasicMember();
                    break;
                case REQUIRED:
                    member = dbGroup.getRequiredMember();
                    break;
                default:
                    throw new StorageException("the MEMBER type " + memberType + " is not supported");
            }
            Collection<Role> members = new ArrayList<Role>();
            for (DBRole dbrole : member) {
                Role role = loadRole(factory, dbrole.getName(), null);
                if (role != null) {
                    members.add(role);
                }
            }
            return members;
        } else {
            throw new StorageException("The group " + group.getName() + " is invalid");
        }
    }

    private synchronized Map<String, DBRole> getRoleNamesMap() throws StorageException {
        if (roleNames == null) {
            //Initial load from the DB...
            roleNames = accessTransaction(new TransactionAccess<Map<String, DBRole>>() {

                @Override
                public Map<String, DBRole> doWork(EntityManager manager, EntityTransaction transaction) {
                    Map<String, DBRole> loadedRoles = new HashMap<String, DBRole>();
                    addRoles(listItems(DBGroup.class, manager), loadedRoles);
                    addRoles(listItems(DBUser.class, manager), loadedRoles);
                    addRoles(listItems(DBRole.class, manager), loadedRoles);
                    if (!loadedRoles.containsKey(Role.USER_ANYONE)) {
                        //create default role...
                        DBUser dbRole = new DBUser();
                        dbRole.setName(Role.USER_ANYONE);
                        manager.persist(dbRole);
                        loadedRoles.put(dbRole.getName(), dbRole);
                    }
                    return loadedRoles;
                }

                <T> List<T> listItems(Class<T> type, EntityManager manager) {
                    final CriteriaBuilder builder = manager.getCriteriaBuilder();
                    final CriteriaQuery<T> query = builder.createQuery(type);
                    query.from(type);
                    TypedQuery<T> typedQuery = manager.createQuery(query);
                    return typedQuery.getResultList();
                }

                <T extends DBRole> void addRoles(Collection<T> list, Map<String, DBRole> loadedRoles) {
                    for (T dbGroup : list) {
                        loadedRoles.put(dbGroup.getName(), dbGroup);
                    }
                }

                @Override
                public String getProblemString() {
                    return "reading roles from the database failed!";
                }
            });
        }
        return roleNames;
    }

    /**
     * Handles the (transactional) access to the database
     */
    private synchronized <T> T accessTransaction(TransactionAccess<T> callable) throws StorageException {
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
        }
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        T result = null;
        RuntimeException exception = null;
        try {
            result = callable.doWork(entityManager, transaction);
            if (transaction.isActive()) {
                transaction.commit();
            }
        } catch (RuntimeException e) {
            exception = e;
        } finally {
            try {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
            } catch (RuntimeException e) {
                //ignore...
            }
        }
        if (exception != null) {
            try {
                //recreate EM on exception...
                entityManager.close();
            } catch (RuntimeException e) {
                //ignore...
            }
            entityManager = null;
            LOG.error("[{}] database operation failed, purge internal cache to syncronize with database!", trackedServiceID, exception);
            if (roleNames != null) {
                roleNames.clear();
                roleNames = null;
            }
            throwStorageException(callable.getProblemString(), exception);
        }
        return result;
    }

    private <T extends DBVersionedObject> T refreshItem(EntityManager manager, T object) {
        @SuppressWarnings("unchecked")
        T find = (T) manager.find(object.getClass(), object.getID());
        if (find == null) {
            throw new IllegalStateException("The database item of type " + object.getClass().getName() + " with id " + object.getID()
                    + " can't be found in the database");
        }
        return find;
    }

    /**
     * register this service under the given {@link BundleContext}
     */
    synchronized void register(BundleContext context) {
        if (serviceRegistration != null) {
            throw new IllegalStateException("This object is already registered under another bundle context!");
        }
        //Propagate the properties of the EMF service...
        Dictionary<String, Object> properties = new Hashtable<String, Object>(entityManagerFactory.getProperties());
        //Set stoarage provider type
        properties.put(PaxUserAdminConstants.STORAGEPROVIDER_TYPE, ConfigurationConstants.STORAGEPROVIDER_TYPE);
        //set the service id of the underlying service
        properties.put(ConfigurationConstants.TRACKED_SERVICE_ID, trackedServiceID);
        serviceRegistration = context.registerService(StorageProvider.class, this, properties);
    }

    private Filter createFilter(String filterString) throws InvalidSyntaxException {
        ClassLoader loader = getClass().getClassLoader();
        if (loader instanceof BundleReference) {
            return ((BundleReference) loader).getBundle().getBundleContext().createFilter(filterString);
        } else {
            //This should never happen...
            throw new AssertionError("Provider is not loaded by OSGi!");
        }
    }

    /**
     * unregister the service again
     */
    synchronized void unregister() {
        if (serviceRegistration == null) {
            throw new IllegalStateException("This object is not registered!");
        }
        serviceRegistration.unregister();
        serviceRegistration = null;
        if (roleNames != null) {
            roleNames.clear();
            roleNames = null;
        }
    }

    private static void throwStorageException(String message, Throwable throwable) throws StorageException {
        throw new StorageException(message, throwable);
    }

    @Override
    public synchronized Object getUserCredential(Decryptor decryptor, User user, String key) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        DBRole role = map.get(user.getName());
        if (role instanceof DBUser) {
            DBUser dbuser = (DBUser) role;
            DBCredential dbCredential = dbuser.getCredentials().get(key);
            if (dbCredential != null) {
                return decryptor.decrypt(dbCredential.getEncryptedBytes(), dbCredential.getVerificationBytes(), dbCredential.getSalt(), dbCredential.getAlgorithmParameter());
            }
        }
        return null;
    }

    @Override
    public synchronized boolean hasUserCredential(Decryptor decryptor, User user, String key, Object value) throws StorageException {
        final Map<String, DBRole> map = getRoleNamesMap();
        DBRole role = map.get(user.getName());
        if (role instanceof DBUser) {
            DBUser dbuser = (DBUser) role;
            DBCredential dbCredential = dbuser.getCredentials().get(key);
            if (dbCredential != null) {
                if (value instanceof String) {
                    return decryptor.verify(key, (String) value, dbCredential.getVerificationBytes(), dbCredential.getSalt(), dbCredential.getAlgorithmParameter());
                }
                if (value instanceof byte[]) {
                    return decryptor.verify(key, (byte[]) value, dbCredential.getVerificationBytes(), dbCredential.getSalt(), dbCredential.getAlgorithmParameter());
                }
            }
        }
        return false;
    }

    @Override
    public CredentialProvider getCredentialProvider() {
        return this;
    }

    @Override
    public void configurationUpdated(Map<String, ?> properties) {
        LOG.info("Configuration updated: {}", properties);
    }
}
