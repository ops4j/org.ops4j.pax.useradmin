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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;

/**
 * @author Matthias Kuespert
 * @since  06.07.2009
 */
public class LdapWrapper {

    /**
     * The Spring LdapTemplate which is used for access.
     */
    private LdapTemplate m_template = null;

    /**
     * Only used for unit tests to provide a mocked template.
     * 
     * @param template
     */
    public void setTemplate(LdapTemplate template) {
        m_template = template;
    }
    
    private LdapTemplate getTemplate() throws StorageException {
        if (null == m_template) {
            throw new StorageException("no Ldap template available - check your configuration");
        }
        return m_template;
    }

    public List<Map<String, String>> searchRoles(DistinguishedName base, String filter) throws StorageException {
        return (List<Map<String, String>>) getTemplate().search(base, filter, new NodeAttributesMapper());
    }

    protected class NodeAttributesMapper implements AttributesMapper {
        public Object mapFromAttributes(Attributes attributes) throws NamingException {
            Map<String, String> properties = new HashMap<String, String>();
            NamingEnumeration<String> keys = attributes.getIDs();
            while (keys.hasMoreElements()) {
                String key = keys.next();
                String value = "";
                NamingEnumeration<?> values = attributes.get(key).getAll();
                while (values.hasMore()) {
                    String data = (String) values.next();
                    if (!"".equals(value) && !value.endsWith(StorageProvider.SEPARATOR_ATT_VALUES)) {
                        value += StorageProvider.SEPARATOR_ATT_VALUES + " ";
                    }
                    if (null != data && data.length() > 0) {
                        value += data;
                    }
                    properties.put(key, value);
                }
            }
            return properties;
        }
    }
}
