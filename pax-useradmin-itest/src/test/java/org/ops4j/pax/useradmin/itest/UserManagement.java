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
            Assert.assertEquals("Not exactly one role found", 1, roles.length);
            Assert.assertNotNull("Invalid role returned", roles[0]);
            Assert.assertEquals("Role is not a user", Role.USER, roles[0].getType());
            Assert.assertEquals("Mismatching user name", USER_NAME, roles[0].getName());
        } catch (InvalidSyntaxException e) {
            Assert.fail("Invalid filter syntax: " + e.getMessage());
        }
    }

    public void createAndFindGroupOk() {
        UserAdmin userAdmin = getUserAdmin();
        Group group = (Group) userAdmin.createRole(GROUP_NAME, Role.GROUP);
        Assert.assertNotNull("Could not create group", group);
        Assert.assertEquals("Mismatching group name", GROUP_NAME, group.getName());
        //
        // lookup via getRole()
        //
        Role role = userAdmin.getRole(USER_NAME);
        Assert.assertNotNull("No role found with name " + GROUP_NAME, role);
        Assert.assertEquals("Role is not a group", Role.GROUP, role.getType());
        Assert.assertEquals("Mismatching user name", GROUP_NAME, role.getName());
        //
        // lookup via getRoles()
        //
        try {
            Role[] roles = userAdmin.getRoles(null);
            Assert.assertNotNull("No roles returned", roles);
            Assert.assertEquals("Not exactly one role found", 1, roles.length);
            Assert.assertNotNull("Invalid role returned", roles[0]);
            Assert.assertEquals("Role is not a group", Role.GROUP, roles[0].getType());
            Assert.assertEquals("Mismatching user name", GROUP_NAME, roles[0].getName());
        } catch (InvalidSyntaxException e) {
            Assert.fail("Invalid filter syntax: " + e.getMessage());
        }
    }
}
