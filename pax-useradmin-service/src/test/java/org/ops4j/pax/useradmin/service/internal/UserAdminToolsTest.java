/*
 * Copyright 2009 OPS4J
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

package org.ops4j.pax.useradmin.service.internal;

import java.util.Hashtable;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.useradmin.service.spi.UserAdminTools;
import org.osgi.service.cm.ConfigurationException;

public class UserAdminToolsTest {

    /**
     * The properties used for testing.
     */
    private Hashtable<String, Object> m_properties            = null;

    private static final String       MANDATORY_NOT_SPECIFIED = "mandatory.not.specified";
    private static final String       MANDATORY_SPECIFIED     = "mandatory.specified";
    private static final String       OPTIONAL_NOT_SPECIFIED  = "mandatory.not.specified";
    private static final String       OPTIONAL_SPECIFIED      = "mandatory.specified";

    private static final String       VALUE                   = "some value";
    private static final String       OPTIONAL_VALUE          = "some optional value";

    @Before
    public void initData() {
        m_properties = new Hashtable<String, Object>();
        m_properties.put(MANDATORY_SPECIFIED, VALUE);
        m_properties.put(OPTIONAL_SPECIFIED, VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMandatoryPropertyNullProperties() throws ConfigurationException {
        UserAdminTools.getMandatoryProperty((Map<String, Object>) null, MANDATORY_NOT_SPECIFIED);
    }

    @Test(expected = ConfigurationException.class)
    public void getMandatoryPropertyNotSpecified() throws ConfigurationException {
        UserAdminTools.getMandatoryProperty((Map<String, Object>) m_properties, MANDATORY_NOT_SPECIFIED);
    }

    @Test
    public void getMandatoryPropertySpecifiedOk() throws ConfigurationException {
        UserAdminTools.getMandatoryProperty((Map<String, Object>) m_properties, MANDATORY_SPECIFIED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOptionalPropertyNullProperties() throws ConfigurationException {
        UserAdminTools.getOptionalProperty((Map<String, Object>) null, OPTIONAL_NOT_SPECIFIED, OPTIONAL_VALUE);
    }

    @Test
    public void getOptionalPropertyNotSpecifiedOk() throws ConfigurationException {
        String value = UserAdminTools.getOptionalProperty((Map<String, Object>) m_properties, OPTIONAL_SPECIFIED, OPTIONAL_VALUE);
        Assert.assertEquals("Value mismatch", VALUE, value);
    }

    @Test
    public void getOptionalPropertySpecifiedOk() throws ConfigurationException {
        String value = UserAdminTools.getOptionalProperty((Map<String, Object>) m_properties, OPTIONAL_NOT_SPECIFIED, OPTIONAL_VALUE);
        Assert.assertEquals("Value mismatch", OPTIONAL_VALUE, value);
    }
}
