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

import junit.framework.Assert;

import org.osgi.framework.BundleContext;
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
    
    
    public void hasRole() {
        
        UserAdmin userAdmin = getUserAdmin();
        User user1 = (User) userAdmin.createRole(USER_NAME1, Role.USER);
        User user2 = (User) userAdmin.createRole(USER_NAME2, Role.USER);
        Group group1 = (Group) userAdmin.createRole(GROUP_NAME1, Role.GROUP);
        Group group2 = (Group) userAdmin.createRole(GROUP_NAME2, Role.GROUP);
        Group group3 = (Group) userAdmin.createRole(GROUP_NAME3, Role.GROUP);
        
        Assert.assertTrue(group1.addMember(user2));
        Assert.assertTrue(group2.addMember(user1));
        Assert.assertTrue(group2.addMember(user2));
        Assert.assertTrue(group3.addMember(user1));
        
        Assert.assertNotNull("Group has no members", group3.getMembers());
        Assert.assertEquals("Mismatching member count", 1, group3.getMembers().length);

        Authorization auth = userAdmin.getAuthorization(user1);
        Assert.assertNotNull("No Authorization found", auth);
        Assert.assertTrue("Role not authorized", auth.hasRole(GROUP_NAME3));
    }
}
