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
package org.ops4j.pax.useradmin.itest;

import org.junit.Assert;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * User management related tests.
 * 
 * @author Matthias Kuespert
 * @since  09.07.2009
 */
public abstract class UserManagement extends UserAdminTestBase {
    
    private static final String USER_NAME = "jdeveloper";
    private static final String GROUP_NAME = "developers";

    protected void createAndFindUserOk() {
        UserAdmin userAdmin = getUserAdmin();
        User user = (User) userAdmin.createRole(USER_NAME, Role.USER);
        Assert.assertNotNull("Could not create user", user);
        Assert.assertEquals("Mismatching user name", USER_NAME, user.getName());
        //
        // lookup via getRole()
        //
        Role role = userAdmin.getRole(USER_NAME);
        Assert.assertNotNull("No role found with name " + USER_NAME, role);
        Assert.assertEquals("Role is not a user", Role.USER, role.getType());
        Assert.assertEquals("Mismatching user name", USER_NAME, role.getName());
        //
        // lookup via getRoles()
        //
        try {
            Role[] roles = userAdmin.getRoles(null);
            Assert.assertNotNull("No roles returned", roles);
            Assert.assertEquals("Not exactly two roles found", 2, roles.length);
            for  (Role r : roles) {
                if (USER_NAME.equals(r.getName())) {
                    Assert.assertEquals("Role is not a user", Role.USER, r.getType());
                    return;
                }
            }
        } catch (InvalidSyntaxException e) {
            Assert.fail("Invalid filter syntax: " + e.getMessage());
        }
        Assert.fail("Cannot find user " + USER_NAME);
    }

    protected void createAndFindGroupOk() {
        UserAdmin userAdmin = getUserAdmin();
        Group group = (Group) userAdmin.createRole(GROUP_NAME, Role.GROUP);
        Assert.assertNotNull("Could not create group", group);
        Assert.assertEquals("Mismatching group name", GROUP_NAME, group.getName());
        //
        // lookup via getRole()
        //
        Role role = userAdmin.getRole(GROUP_NAME);
        Assert.assertNotNull("No role found with name " + GROUP_NAME, role);
        Assert.assertEquals("Role is not a group", Role.GROUP, role.getType());
        Assert.assertEquals("Mismatching user name", GROUP_NAME, role.getName());
        //
        // lookup via getRoles()
        //
        try {
            Role[] roles = userAdmin.getRoles(null);
            Assert.assertNotNull("No roles returned", roles);
            Assert.assertEquals("Not exactly two roles found", 2, roles.length);
            for  (Role r : roles) {
                if (GROUP_NAME.equals(r.getName())) {
                    Assert.assertEquals("Role is not a user", Role.GROUP, r.getType());
                    return;
                }
            }
        } catch (InvalidSyntaxException e) {
            Assert.fail("Invalid filter syntax: " + e.getMessage());
        }
        Assert.fail("Cannot find group " + GROUP_NAME);
    }

    protected void createAndRemoveUserOk() {
        UserAdmin userAdmin = getUserAdmin();
        User user = (User) userAdmin.createRole(USER_NAME, Role.USER);
        Assert.assertNotNull("Could not create user", user);
        Assert.assertEquals("Mismatching user name", USER_NAME, user.getName());
        //
        // remove user
        //
        Assert.assertTrue("Could not delete user", userAdmin.removeRole(user.getName()));
        Assert.assertNull("Unexpected user found", userAdmin.getRole(USER_NAME));
    }
    
    protected void createAndRemoveUserWithGroupsOk() {
        UserAdmin userAdmin = getUserAdmin();
        User user = (User) userAdmin.createRole(USER_NAME, Role.USER);
        Assert.assertNotNull("Could not create user", user);
        Assert.assertEquals("Mismatching user name", USER_NAME, user.getName());
        Group group = (Group) userAdmin.createRole(GROUP_NAME, Role.GROUP);
        Assert.assertNotNull("Could not create group", group);
        Assert.assertEquals("Mismatching group name", GROUP_NAME, group.getName());
        //
        group.addMember(user);
        Role[] members = group.getMembers();
        Assert.assertNotNull("No members found", members);
        Assert.assertEquals("Mismatching member count", 1, members.length);
        //
        // remove user
        //
        Assert.assertTrue("Could not delete user", userAdmin.removeRole(user.getName()));
        members = group.getMembers();
        Assert.assertNull("Unexpected members found", members);
    }

    @SuppressWarnings(value = "unchecked")
    protected void setAndGetAttributesOk() {
        UserAdmin userAdmin = getUserAdmin();
        User user = (User) userAdmin.createRole(USER_NAME, Role.USER);
        Assert.assertNotNull("Could not create user", user);
        Assert.assertEquals("Mismatching user name", USER_NAME, user.getName());
        //
        Assert.assertNull("Value 1 not set", user.getProperties().put("", "emptyKeyValue"));
        Assert.assertNull("Value 2 not set", user.getProperties().put("stringKey", "stringKeyValue"));
        Assert.assertNull("Value 3 not set", user.getProperties().put("byteKey", "byteKeyValue".getBytes()));
        //
        String stringValue;
        byte[] byteValue;
        stringValue = (String) user.getProperties().get("");
        Assert.assertNotNull("Retrieving value for empty key returned null", stringValue);
        Assert.assertEquals("emptyKeyValue", stringValue);
        stringValue = (String) user.getProperties().get("stringKey");
        Assert.assertNotNull("Retrieving string value for key returned null", stringValue);
        Assert.assertEquals("stringKeyValue", stringValue);
        byteValue = (byte[]) user.getProperties().get("byteKey");
        Assert.assertNotNull("Retrieving byte value for key returned null", byteValue);
        Assert.assertArrayEquals("byteKeyValue".getBytes(), byteValue);
        //
        Assert.assertNotNull(user.getProperties().put("", "emptyKeyChangedValue"));
        Assert.assertEquals("emptyKeyChangedValue", (String) user.getProperties().get(""));
    }

    @SuppressWarnings(value = "unchecked")
    protected void setAndGetCredentialsOk() {
        UserAdmin userAdmin = getUserAdmin();
        User user = (User) userAdmin.createRole(USER_NAME, Role.USER);
        Assert.assertNotNull("Could not create user", user);
        Assert.assertEquals("Mismatching user name", USER_NAME, user.getName());
        //
        Assert.assertNull("Value 1 not set", user.getCredentials().put("", "emptyKeyValue"));
        Assert.assertNull("Value 2 not set", user.getCredentials().put("stringKey", "stringKeyValue"));
        Assert.assertNull("Value 3 not set", user.getCredentials().put("byteKey", "byteKeyValue".getBytes()));
        //
        String stringValue;
        byte[] byteValue;
        stringValue = (String) user.getCredentials().get("");
        Assert.assertNotNull("Retrieving value for empty key returned null", stringValue);
        Assert.assertEquals("emptyKeyValue", stringValue);
        stringValue = (String) user.getCredentials().get("stringKey");
        Assert.assertNotNull("Retrieving string value for key returned null", stringValue);
        Assert.assertEquals("stringKeyValue", stringValue);
        byteValue = (byte[]) user.getCredentials().get("byteKey");
        Assert.assertNotNull("Retrieving byte value for key returned null", byteValue);
        Assert.assertArrayEquals("byteKeyValue".getBytes(), byteValue);
        //
        Assert.assertNotNull(user.getCredentials().put("", "emptyKeyChangedValue"));
        Assert.assertEquals("emptyKeyChangedValue", (String) user.getCredentials().get(""));
    }
}
