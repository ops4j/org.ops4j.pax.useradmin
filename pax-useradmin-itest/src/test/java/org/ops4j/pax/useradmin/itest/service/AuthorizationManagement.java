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

package org.ops4j.pax.useradmin.itest.service;

import junit.framework.Assert;

import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * @author Matthias Kuespert
 * @since  12.07.2009
 */
public abstract class AuthorizationManagement extends UserAdminTestBase {

    private static final String USER_NAME1 = "tboss";
    private static final String USER_NAME2 = "jdeveloper";
    
    private static final String GROUP_NAME1 = "developers";
    private static final String GROUP_NAME2 = "people";
    private static final String GROUP_NAME3 = "admins";
    private static final String GROUP_NAME4 = "additional-admins";
    
    private User user1 = null;
    private User user2 = null;

    private Group group1 = null;
    private Group group2 = null;
    private Group group3 = null;
    private Group group4 = null;
    
    protected void setup(boolean groupsAreNeverEmpty) {
        int defGroupSize = groupsAreNeverEmpty ? 1 : 0;
        //
        UserAdmin userAdmin = getUserAdmin();
        user1 = (User) userAdmin.createRole(USER_NAME1, Role.USER);
        user2 = (User) userAdmin.createRole(USER_NAME2, Role.USER);
        group1 = (Group) userAdmin.createRole(GROUP_NAME1, Role.GROUP);
        group2 = (Group) userAdmin.createRole(GROUP_NAME2, Role.GROUP);
        group3 = (Group) userAdmin.createRole(GROUP_NAME3, Role.GROUP);
        group4 = (Group) userAdmin.createRole(GROUP_NAME4, Role.GROUP);
        //
        Assert.assertNotNull("User1 is null", user1);
        Assert.assertNotNull("User2 is null", user2);
        Assert.assertNotNull("Group1 is null", group1);
        Assert.assertNotNull("Group2 is null", group2);
        Assert.assertNotNull("Group3 is null", group3);
        Assert.assertNotNull("Group4 is null", group4);
        //
        System.out.println("-------- Ading member to Group: " + group1.getName());
        Assert.assertTrue(group1.addMember(user2));
        System.out.println("-------- Group: " + group1.getName());
        Assert.assertNotNull("Group has no members", group1.getMembers());
        for (Role role : group1.getMembers()) {
            System.out.println("     - " + role.getName());
        }
        Assert.assertEquals("Mismatching member count", 1 + defGroupSize, group1.getMembers().length);
        //
        Assert.assertTrue(group2.addMember(user1));
        Assert.assertTrue(group2.addMember(user2));
        System.out.println("-------- Group: " + group2.getName());
        for (Role role : group2.getMembers()) {
            System.out.println("     - " + role.getName());
        }
        Assert.assertNotNull("Group has no members", group2.getMembers());
        Assert.assertEquals("Mismatching member count", 2 + defGroupSize, group2.getMembers().length);
        //
        Assert.assertTrue(group3.addMember(user1));
        Assert.assertTrue(group3.addMember(group4));
        Assert.assertNotNull("Group has no members", group3.getMembers());
        Assert.assertEquals("Mismatching member count", 2 + defGroupSize, group3.getMembers().length);
    }
    
    protected void hasRole() {
        UserAdmin userAdmin = getUserAdmin();
        Authorization auth = userAdmin.getAuthorization(user1);
        Assert.assertNotNull("No Authorization found", auth);
        Assert.assertTrue("Role not authorized", auth.hasRole(GROUP_NAME3));
    }
}
