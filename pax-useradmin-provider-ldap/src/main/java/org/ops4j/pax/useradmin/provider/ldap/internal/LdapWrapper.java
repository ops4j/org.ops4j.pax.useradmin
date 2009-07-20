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

import java.io.UnsupportedEncodingException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.useradmin.provider.ldap.ConfigurationConstants;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.osgi.service.cm.ConfigurationException;

 import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;

/**
 * Wraps the core Ldap access.
 * 
 * @author Matthias Kuespert
 * @since 06.07.2009
 */
public class LdapWrapper {

    private LDAPConnection m_connection = null;

    protected void stop() throws LDAPException {
        if (null != m_connection) {
            m_connection.disconnect();
        }
    }

    /**
     * @return The LDAPConnection used to access the Ldap server.
     * @throws StorageException If no template is set, i.e. init() has not been called.
     */
    protected LDAPConnection getConnection() throws StorageException {
        if (null == m_connection) {
            throw new StorageException("no Ldap connection available - check your configuration");
        }
        return m_connection;
    }
    
    /**
     * Initialize the wrapper with the given properties.
     * 
     * @param properties The new properties.
     * @throws ConfigurationException If an error occurs during initialization.
     */
    public void init(Dictionary<String, String> properties) throws ConfigurationException {

        if (null != properties) {
            try {
                stop();
                //
                if (null == m_connection) {
                    m_connection = new LDAPConnection();
                }
                //
//                String host = StorageProviderImpl.getMandatoryProperty(properties,
//                                                                       ConfigurationConstants.PROP_LDAP_SERVER_URL);
//                String port = StorageProviderImpl.getMandatoryProperty(properties,
//                                                                       ConfigurationConstants.PROP_LDAP_SERVER_PORT);
//                m_connection.connect(host, new Integer(port));
                
                // TODO: authentication
//                m_connection.bind(loginDN, password.getBytes("UTF8") );
                
                m_connection.bind(LDAPConnection.LDAP_V3, "", "".getBytes("UTF8"));

            } catch (LDAPException e) {
                throw new ConfigurationException(null,   "LDAPException during intialization: "
                                                       + e.getMessage());
            } catch (UnsupportedEncodingException e) {
                throw new ConfigurationException(null,   "Usupported encoding during intialization: "
                                                       + e.getMessage());
            }
        } else {
            System.out.println("----------------------------------> no properties ---------------------------------");
            // throw new ConfigurationException(null, "No properties given for intialization");
        }
    }

    public List<Map<String, String>> searchRoles(String base, String filter) throws StorageException {
        LDAPConnection connection = getConnection();
        
        
        try {
            connection.search(base, LDAPConnection.SCOPE_SUB, filter, new String[] {""}, false);
        } catch (LDAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
