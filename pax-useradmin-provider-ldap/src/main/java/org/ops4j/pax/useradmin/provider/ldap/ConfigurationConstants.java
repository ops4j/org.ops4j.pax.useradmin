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

package org.ops4j.pax.useradmin.provider.ldap;

/**
 * Definition of constants and default values used for configuration.
 */
@SuppressWarnings("unused")
public interface ConfigurationConstants {

    /**
     * The provider type of this <code>StorageProvider</code>.
     */
    String STORAGEPROVIDER_TYPE            = "Ldap";

    String ATTR_OBJECTCLASS                = "objectClass";
    String PROTOCOL_LDAP                   = "ldap";

    // property names

    String PROP_LDAP_SERVER_URL            = "org.ops4j.pax.useradmin.ldap.server.url";
    String PROP_LDAP_SERVER_PORT           = "org.ops4j.pax.useradmin.ldap.server.port";
    String PROP_LDAP_ROOT_DN               = "org.ops4j.pax.useradmin.ldap.root.dn";
    String PROP_LDAP_ROOT_USERS            = "org.ops4j.pax.useradmin.ldap.root.users";
    String PROP_LDAP_ROOT_GROUPS           = "org.ops4j.pax.useradmin.ldap.root.groups";
    String PROP_LDAP_ACCESS_USER           = "org.ops4j.pax.useradmin.ldap.access.user";
    String PROP_LDAP_ACCESS_PWD            = "org.ops4j.pax.useradmin.ldap.access.pwd";

    String PROP_USER_OBJECTCLASS           = "org.ops4j.pax.useradmin.ldap.user.objectclass";
    String PROP_USER_ATTR_ID               = "org.ops4j.pax.useradmin.ldap.user.attr.id";
    String PROP_USER_ATTR_MANDATORY        = "org.ops4j.pax.useradmin.ldap.user.attr.mandatory";
    String PROP_USER_ATTR_CREDENTIAL       = "org.ops4j.pax.useradmin.ldap.user.attr.credential";

    String PROP_GROUP_OBJECTCLASS          = "org.ops4j.pax.useradmin.ldap.group.objectclass";
    String PROP_GROUP_ATTR_ID              = "org.ops4j.pax.useradmin.ldap.group.attr.id";
    String PROP_GROUP_ATTR_MANDATORY       = "org.ops4j.pax.useradmin.ldap.group.attr.mandatory";
    String PROP_GROUP_ATTR_CREDENTIAL      = "org.ops4j.pax.useradmin.ldap.group.attr.credential";

    String PROP_GROUP_ENTRY_OBJECTCLASS    = "org.ops4j.pax.useradmin.ldap.group.entry.objectclass";
    String PROP_GROUP_ENTRY_ATTR_ID        = "org.ops4j.pax.useradmin.ldap.group.entry.attr.id";
    String PROP_GROUP_ENTRY_ATTR_MEMBER    = "org.ops4j.pax.useradmin.ldap.group.entry.attr.member";

    // default values

    String DEFAULT_LDAP_SERVER_URL         = "localhost";
    String DEFAULT_LDAP_SERVER_PORT        = "8099";
    String DEFAULT_LDAP_ROOT_DN            = "dc=ops4j,dc=org";
    String DEFAULT_LDAP_ROOT_USERS         = "ou=people";
    String DEFAULT_LDAP_ROOT_GROUPS        = "ou=groups";

    String DEFAULT_USER_OBJECTCLASS        = "organizationalPerson, inetOrgPerson, person, top";
    String DEFAULT_USER_ATTR_ID            = "uid";
    String DEFAULT_USER_ATTR_MANDATORY     = "cn, sn";
    String DEFAULT_USER_ATTR_CREDENTIAL    = "userpassword";

    String DEFAULT_GROUP_OBJECTCLASS       = "organizationalUnit, simpleSecurityObject";            // top not allowed!
    String DEFAULT_GROUP_ATTR_ID           = "ou";
    String DEFAULT_GROUP_ATTR_MANDATORY    = "userpassword";
    String DEFAULT_GROUP_ATTR_CREDENTIAL   = "userpassword";

    String DEFAULT_GROUP_ENTRY_OBJECTCLASS = "groupOfNames";                                        // top not allowed!
    String DEFAULT_GROUP_ENTRY_ATTR_ID     = "cn";
    String DEFAULT_GROUP_ENTRY_ATTR_MEMBER = "member";                                              // note: assumed mandatory by this implementation
}
