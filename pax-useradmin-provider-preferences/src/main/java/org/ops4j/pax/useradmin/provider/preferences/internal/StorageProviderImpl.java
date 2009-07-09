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
package org.ops4j.pax.useradmin.provider.preferences.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.ops4j.pax.useradmin.service.spi.UserAdminFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Matthias Kuespert
 * @since 08.07.2009
 */
public class StorageProviderImpl implements StorageProvider {

    public static String PREFERENCE_USER = "Pax UserAdmin";
    
    private static String NODE_TYPE = "type";

    
    private static String MEMBERS_NODE = "members";
    private static String REQUIRED_MEMBER_STRING = "required";
    private static String BASIC_MEMBER_STRING = "basic";
    
    private static String PROPERTIES_NODE = "properties";
    private static String CREDENTIALS_NODE = "credentials";
    private static String TYPES_NODE = "types";
    
    /**
     * The ServiceTracker which monitors the service used to store data.
     */
    private ServiceTracker m_preferencesService = null;

    private Preferences m_rootNode = null;
    
    private BundleContext m_context = null;
    
    private PreferencesService getPreferencesService() throws StorageException {
        PreferencesService service = (PreferencesService) m_preferencesService.getService();
        if (null == service) {
            throw new StorageException("No PreferencesService available");
        }
        return service;
    }
    
    private Map<String, String> loadAttributes(Preferences node) throws BackingStoreException {
        Preferences propertyTypes = node.node(TYPES_NODE);
        Map<String, String> properties = new HashMap<String, String>();
        for (String key : node.keys()) {
            if (propertyTypes.getBoolean(key, false)) {
                properties.put(key, node.get(key, ""));
            } else {
                properties.put(key, node.getByteArray(key, "".getBytes()).toString());
            }
        }
        return properties;
    }
    
    private void storeAttribute(Preferences node, String key, String value) throws BackingStoreException {
        Preferences propertyTypes = node.node(TYPES_NODE);
        propertyTypes.putBoolean(key, true);
        node.put(key, value);
    }
    
    private void storeAttribute(Preferences node, String key, byte[] value) throws BackingStoreException {
        Preferences propertyTypes = node.node(TYPES_NODE);
        propertyTypes.putBoolean(key, false);
        node.putByteArray(key, value);
    }
    
    private Role loadRole(UserAdminFactory factory, String name, Filter filter) throws StorageException {
        Preferences node = m_rootNode.node(name);
        //
        Map<String, String> properties = null;
        try {
            if (node.nodeExists(PROPERTIES_NODE)) {
                properties = loadAttributes(node.node(PROPERTIES_NODE));
            }
        } catch (BackingStoreException e) {
            throw new StorageException("Error reading properties of node '"+ node.absolutePath() + "': " + e.getMessage());
        }
        //
        if (null != filter) {
            if (null == properties && !"*".equals(filter.toString())) {
                return null;
            }
            Dictionary<String, String> dict = new Hashtable<String, String>(properties);
            if (!filter.match(dict)) {
                return null;
            }
        }
        //
        Map<String, String> credentials = null;
        try {
            if (node.nodeExists(CREDENTIALS_NODE)) {
                properties = loadAttributes(node.node(CREDENTIALS_NODE));
            }
        } catch (BackingStoreException e) {
            throw new StorageException("Error reading credentials of node '"+ node.absolutePath() + "': " + e.getMessage());
        }
        //
        int type = new Integer(properties.get(NODE_TYPE));
        Role role = null;
        switch (type) {
            case User.USER:
                role = factory.createUser(name, properties, credentials);
                break;
            case User.GROUP:
                role = factory.createGroup(name, properties, credentials);
                break;
            default:
                throw new StorageException("Invalid role type: " + properties.get(NODE_TYPE));
        }
        return role;
    }
    
    private Collection<Role> loadRoles(UserAdminFactory factory, Filter filter) throws StorageException {
        String[] roleNames = null;
        try {
            roleNames = m_rootNode.childrenNames();
        } catch (BackingStoreException e) {
            throw new StorageException("Error reading children of node '"+ m_rootNode.absolutePath() + "': " + e.getMessage());
        }
        //
        Collection<Role> roles = new ArrayList<Role>();
        for (String name : roleNames) {
            Role role = loadRole(factory, name, filter);
            roles.add(role);
        }
        return roles;
    }

    public Collection<Role> loadMembers(UserAdminFactory factory, Group group, String memberType) throws StorageException, BackingStoreException {
        Preferences node = m_rootNode.node(group.getName());
        if (node.nodeExists(MEMBERS_NODE)) {
            Preferences membersNode = node.node(MEMBERS_NODE);
            Collection<Role> members = new ArrayList<Role>();
            for (String name : membersNode.keys()) {
                if (memberType.equals(membersNode.get(name, ""))) {
                    Role role = loadRole(factory, name, null);
                    members.add(role);
                }
            }
            return members;
        }
        return null;
    }

    public StorageProviderImpl(BundleContext context) throws StorageException {
        m_context = context;
        m_preferencesService = new ServiceTracker(context, PreferencesService.class.getName(), null);
        PreferencesService service = getPreferencesService();
        m_rootNode = service.getUserPreferences(PREFERENCE_USER);
    }

    public User createUser(UserAdminFactory factory, String name) throws StorageException {
        Preferences node = m_rootNode.node(name);
        node.putInt(NODE_TYPE, Role.USER);
        User user = factory.createUser(name, null, null);
        return user;
    }

    public Group createGroup(UserAdminFactory factory, String name) throws StorageException {
        Preferences node = m_rootNode.node(name);
        node.putInt(NODE_TYPE, Role.USER);
        Group group = factory.createGroup(name, null, null);
        return group;
    }

    public void deleteRole(Role role) throws StorageException {
        try {
            if (m_rootNode.nodeExists(role.getName())) {
                m_rootNode.node(role.getName()).removeNode();
            }
        } catch (BackingStoreException e) {
            throw new StorageException("Error removing node '"+ role.getName() + "': " + e.getMessage());
        }
    }

    public Collection<Role> getMembers(UserAdminFactory factory, Group group) throws StorageException {
        try {
            return loadMembers(factory, group, BASIC_MEMBER_STRING);
        } catch (BackingStoreException e) {
            throw new StorageException("Error retrieving basic members of group '"+ group.getName() + "': " + e.getMessage());
        }
    }

    public Collection<Role> getRequiredMembers(UserAdminFactory factory, Group group) throws StorageException {
        try {
            return loadMembers(factory, group, REQUIRED_MEMBER_STRING);
        } catch (BackingStoreException e) {
            throw new StorageException("Error retrieving required members of group '"+ group.getName() + "': " + e.getMessage());
        }
    }

    public void addMember(Group group, Role role) throws StorageException {
        // TODO Auto-generated method stub

    }

    public void addRequiredMember(Group group, Role role) throws StorageException {
        // TODO Auto-generated method stub

    }

    public void removeMember(Group group, Role user) throws StorageException {
        // TODO Auto-generated method stub

    }

    public Collection<String> getImpliedRoles(String userName) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setRoleAttribute(Role role, String key, String value) throws StorageException {
        try {
            Preferences node = m_rootNode.node(role.getName() + "/" + PROPERTIES_NODE);
            storeAttribute(node, key, value);
        } catch (BackingStoreException e) {
            throw new StorageException("Error storing attribute for role '"+ role.getName() + "': " + e.getMessage());
        }
    }

    public void setRoleAttribute(Role role, String key, byte[] value) throws StorageException {
        try {
            Preferences node = m_rootNode.node(role.getName() + "/" + PROPERTIES_NODE);
            storeAttribute(node, key, value);
        } catch (BackingStoreException e) {
            throw new StorageException("Error storing attribute for role '"+ role.getName() + "': " + e.getMessage());
        }
    }

    public void removeRoleAttribute(Role role, String key) throws StorageException {
        try {
            Preferences node = m_rootNode.node(role.getName() + "/" + CREDENTIALS_NODE);
            node.remove(key);
        } catch (IllegalStateException e) {
            throw new StorageException("Error removing attribute from role '"+ role.getName() + "': " + e.getMessage());
        }
    }

    public void clearRoleAttributes(Role role) throws StorageException {
        try {
            Preferences node = m_rootNode.node(role.getName());
            if (node.nodeExists(PROPERTIES_NODE)) {
                node.node(PROPERTIES_NODE).removeNode();
            }
        } catch (BackingStoreException e) {
            throw new StorageException("Error clearing attributes of role '"+ role.getName() + "': " + e.getMessage());
        }
    }

    public void setUserCredential(User user, String key, String value) throws StorageException {
        try {
            Preferences node = m_rootNode.node(user.getName() + "/" + CREDENTIALS_NODE);
            storeAttribute(node, key, value);
        } catch (BackingStoreException e) {
            throw new StorageException("Error storing credential for user '"+ user.getName() + "': " + e.getMessage());
        }
    }

    public void setUserCredential(User user, String key, byte[] value) throws StorageException {
        try {
            Preferences node = m_rootNode.node(user.getName() + "/" + CREDENTIALS_NODE);
            storeAttribute(node, key, value);
        } catch (BackingStoreException e) {
            throw new StorageException("Error storing credential for user '"+ user.getName() + "': " + e.getMessage());
        }
    }

    public void removeUserCredential(User user, String key) throws StorageException {
        try {
            Preferences node = m_rootNode.node(user.getName() + "/" + CREDENTIALS_NODE);
            node.remove(key);
        } catch (IllegalStateException e) {
            throw new StorageException("Error removing credential from user '"+ user.getName() + "': " + e.getMessage());
        }
    }

    public void clearUserCredentials(User user) throws StorageException {
        try {
            Preferences node = m_rootNode.node(user.getName());
            if (node.nodeExists(CREDENTIALS_NODE)) {
                node.node(CREDENTIALS_NODE).removeNode();
            }
        } catch (BackingStoreException e) {
            throw new StorageException("Error clearing credentials of user '"+ user.getName() + "': " + e.getMessage());
        }
    }

    public Role getRole(UserAdminFactory factory, String name) throws StorageException {
        return loadRole(factory, name, null);
    }

    public User getUser(UserAdminFactory factory, String key, String value) throws StorageException {
        try {
            Filter filter = m_context.createFilter("(" + key + "=" + value + ")");
            Collection<Role> roles = loadRoles(factory, filter);
            Collection<User> users = new ArrayList<User>();
            for (Role role : roles) {
                if (Role.USER == role.getType()) {
                    users.add((User) role);
                }
            }
            //
            if (users.size() == 1) {
                return users.iterator().next();
            }
        } catch (InvalidSyntaxException e) {
            throw new StorageException("Invalid filter '" + e.getFilter() + "': " + e.getMessage());
        }
        return null;
    }

    public Collection<Role> findRoles(UserAdminFactory factory, String filterString) throws StorageException {
        try {
            Filter filter = m_context.createFilter(filterString);
            Collection<Role> roles = loadRoles(factory, filter);
            return roles;
        } catch (InvalidSyntaxException e) {
            throw new StorageException("Invalid filter '" + e.getFilter() + "': " + e.getMessage());
        }
    }
}
