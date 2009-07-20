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
package org.ops4j.pax.useradmin.provider.ldap;

/**
 * @author Matthias Kuespert
 * @since  18.07.2009
 */
public class ConfigurationConstants {

    public static final String SERVICE_PID                 = "org.ops4j.pax.useradmin.provider.ldap";
    
    public static final String ATTR_OBJECTCLASS            = "objectClass";
    public static final String PROTOCOL_LDAP               = "ldap";

    public static final String PROP_LDAP_SERVER_URL        = "org.ops4j.pax.user.ldap.server.url";
    public static final String PROP_LDAP_SERVER_PORT       = "org.ops4j.pax.user.ldap.server.port";
    public static final String PROP_LDAP_ROOT_DN           = "org.ops4j.pax.user.ldap.root.dn";
    public static final String PROP_LDAP_ROOT_USERS        = "org.ops4j.pax.user.ldap.root.users";
    public static final String PROP_LDAP_ROOT_GROUPS       = "org.ops4j.pax.user.ldap.root.groups";
    public static final String PROP_LDAP_ACCESS_USER       = "org.ops4j.pax.user.ldap.access.user";
    public static final String PROP_LDAP_ACCESS_PWD        = "org.ops4j.pax.user.ldap.access.pwd";
    
    public static final String DEFAULT_LDAP_SERVER_URL     = "localhost";
    public static final String DEFAULT_LDAP_SERVER_PORT    = "8088";
    public static final String DEFAULT_LDAP_ROOT_DN        = "dc=ops4j,dc=org";
    public static final String DEFAULT_LDAP_ROOT_USERS     = "ou=people";
    public static final String DEFAULT_LDAP_ROOT_GROUPS    = "ou=groups";
    
    public static final String PROP_USER_OBJECTCLASS       = "org.ops4j.pax.user.ldap.user.objectclass";
    public static final String PROP_USER_IDATTR            = "org.ops4j.pax.user.ldap.user.idattr";
    public static final String PROP_GROUP_OBJECTCLASS      = "org.ops4j.pax.user.ldap.group.objectclass";
    public static final String PROP_GROUP_IDATTR           = "org.ops4j.pax.user.ldap.group.idattr";
    
    public static final String DEFAULT_USER_OBJECTCLASS    = "organizationalPerson, inetOrgPerson, person, top";
    public static final String DEFAULT_USER_IDATTR         = "uid";
    public static final String DEFAULT_GROUP_OBJECTCLASS   = "organizationalRole, top ";
    public static final String DEFAULT_GROUP_IDATTR        = "cn";

}
