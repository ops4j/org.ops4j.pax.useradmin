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

package org.ops4j.pax.useradmin.service;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;

/**
 * @author Matthias Kuespert
 * @since  28.11.2009
 */
public class UserAdminToolsTest {

    /**
     * The properties used for testing.
     */
    private Dictionary<String, String> m_properties = null;
    
    private static final String MANDATORY_NOT_SPECIFIED = "mandatory.not.specified";
    private static final String MANDATORY_SPECIFIED = "mandatory.specified";
    private static final String OPTIONAL_NOT_SPECIFIED = "mandatory.not.specified";
    private static final String OPTIONAL_SPECIFIED = "mandatory.specified";
    
    private static final String VALUE = "some value";
    private static final String OPTIONAL_VALUE = "some optional value";

    @Before
    public void initData() {
        m_properties = new Hashtable<String, String>();
        m_properties.put(MANDATORY_SPECIFIED, VALUE);
        m_properties.put(OPTIONAL_SPECIFIED, VALUE);
    }
    
    @Test
    public void createUserAdminTools() {
        UserAdminTools uat = new UserAdminTools();
        Assert.assertNotNull("No UserAdminTools object created", uat);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void getMandatoryPropertyNullProperties() throws ConfigurationException {
        UserAdminTools.getMandatoryProperty(null, MANDATORY_NOT_SPECIFIED);
    }

    @Test (expected = ConfigurationException.class)
    public void getMandatoryPropertyNotSpecified() throws ConfigurationException {
        UserAdminTools.getMandatoryProperty(m_properties, MANDATORY_NOT_SPECIFIED);
    }

    @Test
    public void getMandatoryPropertySpecifiedOk() throws ConfigurationException {
        UserAdminTools.getMandatoryProperty(m_properties, MANDATORY_SPECIFIED);
    }

    @Test (expected = IllegalArgumentException.class)
    public void getOptionalPropertyNullProperties() throws ConfigurationException {
        UserAdminTools.getOptionalProperty(null, OPTIONAL_NOT_SPECIFIED, OPTIONAL_VALUE);
    }

    @Test
    public void getOptionalPropertyNotSpecifiedOk() throws ConfigurationException {
        String value = UserAdminTools.getOptionalProperty(m_properties, OPTIONAL_SPECIFIED, OPTIONAL_VALUE);
        Assert.assertEquals("Value mismatch", VALUE, value);
    }

    @Test
    public void getOptionalPropertySpecifiedOk() throws ConfigurationException {
        String value = UserAdminTools.getOptionalProperty(m_properties, OPTIONAL_NOT_SPECIFIED, OPTIONAL_VALUE);
        Assert.assertEquals("Value mismatch", OPTIONAL_VALUE, value);
    }
}
