/**
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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.easymock.classextension.EasyMock;
import org.junit.Test;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Role;

/**
 * @author Matthias Kuespert
 * @since 28.07.2009
 */
public class AuthorizationImplTest {

    private static final String USER_NAME1  = "someRole1";
    private static final String USER_NAME2  = "someRole2";
    private static final String GROUP_NAME1 = "someGroup1";
    private static final String GROUP_NAME2 = "someGroup2";
    private static final String GROUP_NAME3 = "someGroup3";

    @Test
    public void getNameOk() {
        PaxUserAdmin userAdmin = EasyMock.createMock(PaxUserAdmin.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, null);
        UserImpl userAnyone = new UserImpl(Role.USER_ANYONE, userAdmin, null, null);

        Authorization authorization = new AuthorizationImpl(userAdmin, user);
        Assert.assertEquals("Name mismatch", USER_NAME1, authorization.getName());
        //
        authorization = new AuthorizationImpl(userAdmin, null);
        Assert.assertEquals("Name mismatch", null, authorization.getName());
        //
        authorization = new AuthorizationImpl(userAdmin, userAnyone);
        Assert.assertEquals("Name mismatch", Role.USER_ANYONE, authorization.getName());
    }

    @Test
    public void getRolesInvalidSyntaxException() {
        PaxUserAdmin userAdmin = EasyMock.createMock(PaxUserAdmin.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, null);

        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        try {
            EasyMock.expect(userAdmin.getRoles(null)).andThrow(new InvalidSyntaxException("", null));
        } catch (InvalidSyntaxException e) {
            // expected
        }
        EasyMock.replay(userAdmin, sp);
        //
        Authorization authorization = new AuthorizationImpl(userAdmin, user);
        Assert.assertNotNull("Authorization object not created", authorization);
        try {
            Assert.assertNull("Roles found after exception was thrown", authorization.getRoles());
        } catch (IllegalStateException e) {
            // expected
        }
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void getRolesOk() {
        PaxUserAdmin userAdmin = EasyMock.createMock(PaxUserAdmin.class);
        UserImpl userAnyone = new UserImpl(Role.USER_ANYONE, userAdmin, null, null);
        UserImpl user1 = new UserImpl(USER_NAME1, userAdmin, null, null);
        UserImpl user2 = new UserImpl(USER_NAME2, userAdmin, null, null);

        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        GroupImpl group1 = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        GroupImpl group2 = new GroupImpl(GROUP_NAME2, userAdmin, null, null);
        GroupImpl group3 = new GroupImpl(GROUP_NAME3, userAdmin, null, null);
        try {
            // 1st addMember()
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group1, user1)).andReturn(true);
            EasyMock.expect(userAdmin.getRoles(null)).andReturn(new Role[] { user1, group1, group2 });
            //
            // 2nd addMember()
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group3, userAnyone)).andReturn(true);
            //
            // 1st getRoles()
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group1)).andReturn(new ArrayList<Role>());
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            Collection<Role> group1Members = new ArrayList<Role>();
            group1Members.add(user1);
            EasyMock.expect(sp.getMembers(userAdmin, group1)).andReturn(group1Members);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group2)).andReturn(new ArrayList<Role>());
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            Collection<Role> group2Members = new ArrayList<Role>();
            EasyMock.expect(sp.getMembers(userAdmin, group2)).andReturn(group2Members);
            //
            // 2nd getRoles()
            EasyMock.expect(userAdmin.getRoles(null)).andReturn(null);
            //
            // 3rd getRoles()
            EasyMock.expect(userAdmin.getRoles(null)).andReturn(new Role[] { userAnyone });
            //
        } catch (StorageException e) {
            Assert.fail("Unexpected StorageException: " + e.getMessage());
        } catch (InvalidSyntaxException e) {
            Assert.fail("Unexpected InvalidSyntaxException: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertTrue("User 1 not added to group 1", group1.addMember(user1));
        Assert.assertTrue("User Anyone not added to group 3", group3.addMember(userAnyone));
        //
        Authorization authorization = new AuthorizationImpl(userAdmin, user1);
        String[] roles = authorization.getRoles();
        Assert.assertNotNull("No authorized roles found", roles);
        Assert.assertEquals("Not exactly 2 authorized roles found", 2, roles.length);
        //
        authorization = new AuthorizationImpl(userAdmin, user2);
        roles = authorization.getRoles();
        Assert.assertNull(roles);
        //
        authorization = new AuthorizationImpl(userAdmin, group3);
        roles = authorization.getRoles();
        Assert.assertNull(roles);
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void hasRoleOk() {
        // TODO: implement test
    }
}
