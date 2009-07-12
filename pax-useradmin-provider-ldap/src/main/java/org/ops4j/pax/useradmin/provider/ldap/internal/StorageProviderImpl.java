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
package org.ops4j.pax.useradmin.provider.ldap.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.UserAdminFactory;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

// import com.novell.ldap.LDAPException;

public class StorageProviderImpl implements StorageProvider, ManagedService {

    public static String ATTR_OBJECTCLASS          = "objectClass";
    
    public static String PROP_LDAP_SERVER_URL      = "org.ops4j.pax.user.ldap.server.url";
    public static String PROP_LDAP_SERVER_PORT     = "org.ops4j.pax.user.ldap.server.port";
    public static String PROP_LDAP_ROOT_DN         = "org.ops4j.pax.user.ldap.rootdn";
    public static String PROP_LDAP_ACCESS_USER     = "org.ops4j.pax.user.ldap.access.user";
    public static String PROP_LDAP_ACCESS_PWD      = "org.ops4j.pax.user.ldap.access.pwd";
    
    public static String DEFAULT_LDAP_SERVER_URL   = "ldap://localhost";
    public static String DEFAULT_LDAP_SERVER_PORT  = "8088";
    public static String DEFAULT_LDAP_ROOT_DN      = "dc=ops4j,dc=org";

    public static String PROP_OBJECTCLASS_USER     = "org.ops4j.pax.user.ldap.objectclass.user";
    public static String PROP_OBJECTCLASS_GROUP    = "org.ops4j.pax.user.ldap.objectclass.group";
    public static String PROP_IDATTR_USER          = "org.ops4j.pax.user.ldap.idattr.user";
    public static String PROP_IDATTR_GROUP         = "org.ops4j.pax.user.ldap.idattr.group";

    public static String DEFAULT_OBJECTCLASS_USER  = "organizationalPerson";
    public static String DEFAULT_OBJECTCLASS_GROUP = "organizationalRole";
    public static String DEFAULT_IDATTR_USER       = "uid";
    public static String DEFAULT_IDATTR_GROUP      = "cn";

    private String       m_rootDN                  = DEFAULT_LDAP_ROOT_DN;
    private String       m_objectclassUser         = DEFAULT_OBJECTCLASS_USER;
    private String       m_objectclassGroup        = DEFAULT_OBJECTCLASS_GROUP;
    private String       m_idattrUser              = DEFAULT_IDATTR_USER;
    private String       m_idattrGroup             = DEFAULT_IDATTR_GROUP;
    
    /**
     * The Spring LdapTemplate which is used for access.
     */
    private LdapWrapper m_wrapper = null;

    /**
     * Constructor.
     * 
     * @param wrapper Unit tests can provide a mock via this parameter.
     */
    public StorageProviderImpl(LdapWrapper wrapper) {
        m_wrapper = wrapper;
        if (null == m_wrapper) {
            m_wrapper = new LdapWrapper();
        }
    }

    @SuppressWarnings(value = "unchecked")
    public void updated(Dictionary properties) throws ConfigurationException {
        //
        m_rootDN = DEFAULT_LDAP_ROOT_DN;
        m_objectclassUser = DEFAULT_OBJECTCLASS_USER;
        m_objectclassGroup = DEFAULT_OBJECTCLASS_GROUP;
        m_idattrUser = DEFAULT_IDATTR_USER;
        m_idattrGroup = DEFAULT_IDATTR_GROUP;
        //
        if (null != properties) {
            m_wrapper.init(properties);
            //
            m_rootDN = getMandatoryProperty(properties, PROP_LDAP_ROOT_DN);
            m_objectclassUser = (String) properties.get(PROP_OBJECTCLASS_USER);
            m_objectclassGroup = (String) properties.get(PROP_OBJECTCLASS_GROUP);
            m_idattrUser = (String) properties.get(PROP_IDATTR_USER);
            if (null == m_idattrUser) {
                m_idattrUser = DEFAULT_IDATTR_USER;
            }
            m_idattrGroup = (String) properties.get(PROP_IDATTR_USER);
            if (null == m_idattrGroup) {
                m_idattrGroup = DEFAULT_IDATTR_GROUP;
            }
        }
    }

    protected static String getMandatoryProperty(Dictionary<String, String> properties, String name) throws ConfigurationException {
        String value = (String) properties.get(name);
        if (null == value) {
            throw new ConfigurationException(name,
                                             "no value given for property - please check the configuration");
        }
        return value;
    }
    
    protected void stop() {
//        try {
            if (null != m_wrapper) {
                m_wrapper.stop();
            }
//        } catch (LDAPException e) {
//            // TODO log error
//            e.printStackTrace();
//        }
    }

    private LdapWrapper getWrapper() throws StorageException {
        if (null == m_wrapper) {
            throw new StorageException("no Ldap wrapper available - check your configuration");
        }
        return m_wrapper;
    }

    public User createUser(UserAdminFactory factory, String name) throws StorageException {
//        LdapTemplate template = getWrapper();
        // template.bind(dn, obj, attributes)
        // TODO Auto-generated method stub
        return null;
    }

    public Group createGroup(UserAdminFactory factory, String name) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void deleteRole(Role role) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    
    public Collection<Role> getMembers(UserAdminFactory factory, Group group) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Collection<Role> getRequiredMembers(UserAdminFactory factory, Group group) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public boolean addMember(Group group, Role role) throws StorageException {
        // TODO Auto-generated method stub
        return false;
    }
    
    public boolean addRequiredMember(Group group, Role role) throws StorageException {
        // TODO Auto-generated method stub
        return false;
    }
    
    public boolean removeMember(Group group, Role role) throws StorageException {
        // TODO Auto-generated method stub
        return false;
    }
    
    public Collection<String> getImpliedRoles(String userName) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void setRoleAttribute(Role role, String key, String value) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    public void setRoleAttribute(Role role, String key, byte[] value) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    
    public void removeRoleAttribute(Role role, String key) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    
    public void clearRoleAttributes(Role role) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    
    public void setUserCredential(User user, String key, String value) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    public void setUserCredential(User user, String key, byte[] value) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    
    public void removeUserCredential(User user, String key) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    
    public void clearUserCredentials(User user) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    
    public Role getRole(UserAdminFactory factory, String name) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public User getUser(UserAdminFactory factory, String key, String value) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Collection<Role> findRoles(UserAdminFactory factory, String filter) throws StorageException {
        LdapWrapper wrapper = getWrapper();
        Collection<Role> roles = new ArrayList<Role>();
        List<Map<String, String>> nodeProperties = wrapper.searchRoles(m_rootDN, filter);
        if (null == nodeProperties) {
            // TODO: should we throw an exception?
            return roles;
        }
        for (Map<String, String> roleData : nodeProperties) {
            if (null != roleData) {
                String objectClassData = roleData.get(ATTR_OBJECTCLASS);
                if (null == objectClassData) {
                    throw new StorageException("Objectclass attribute '" + ATTR_OBJECTCLASS
                                    + "' not found in server result.");
                }
                if (objectClassData.contains(m_objectclassGroup)) {
                    String name = roleData.get(m_idattrGroup);
                    if (null == name || "".equals(name)) {
                        throw new StorageException("No name specified in role attributes: "
                                        + roleData);
                    }
                    // TODO: read credentials
                    System.out.println("create group '" + name + "' with attributes: " + roleData);
                    Group group = factory.createGroup(name, roleData, null);
                    roles.add(group);
                } else if (objectClassData.contains(m_objectclassUser)) {
                    String name = roleData.get(m_idattrUser);
                    if (null == name || "".equals(name)) {
                        throw new StorageException("No name specified in role attributes: "
                                        + roleData);
                    }
                    // TODO: read credentials
                    System.out.println("create user '" + name + "' with attributes: " + roleData);
                    User user = factory.createUser(name, roleData, null);
                    roles.add(user);
                } else {
                    System.out.println("create failed with data: " + roleData);
                }
            }
        }
	    return roles;
	}

}
