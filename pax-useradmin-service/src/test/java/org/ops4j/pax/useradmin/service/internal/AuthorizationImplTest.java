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
 * @since  28.07.2009
 */
public class AuthorizationImplTest {

    private static final String NAME1 = "someRole";
    private static final String GROUP_NAME1 = "someGroup1";
    private static final String GROUP_NAME2 = "someGroup2";

    @Test
    public void getNameOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        
        Authorization authorization = new AuthorizationImpl(userAdmin, user);
        Assert.assertEquals("Name mismatch", NAME1, authorization.getName());
        //
        authorization = new AuthorizationImpl(userAdmin, null);
        Assert.assertEquals("Name mismatch", null, authorization.getName());
//        Assert.assertEquals("Name mismatch", Role.USER_ANYONE, authorization.getName());
    }
    
    @Test
    public void getRolesInvalidSyntaxException() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        
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
        Assert.assertNull("Roles found after exception was thrown", authorization.getRoles());
        //
        EasyMock.verify(userAdmin, sp);
    }
    
    @Test
    public void getRolesOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        
        Authorization authorization = new AuthorizationImpl(userAdmin, user);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        GroupImpl group1 = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        GroupImpl group2 = new GroupImpl(GROUP_NAME2, userAdmin, null, null);
        try {
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group1, user)).andReturn(true);
            EasyMock.expect(userAdmin.getRoles(null)).andReturn(new Role[] { user, group1, group2 });
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group1)).andReturn(new ArrayList<Role>());
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            Collection<Role> group1Members = new ArrayList<Role>();
            group1Members.add(user);
            EasyMock.expect(sp.getMembers(userAdmin, group1)).andReturn(group1Members);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group2)).andReturn(new ArrayList<Role>());
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            Collection<Role> group2Members = new ArrayList<Role>();
            EasyMock.expect(sp.getMembers(userAdmin, group2)).andReturn(group2Members);
        } catch (StorageException e) {
            Assert.fail("Unexpected StorageException: " + e.getMessage());
        } catch (InvalidSyntaxException e) {
            Assert.fail("Unexpected InvalidSyntaxException: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        group1.addMember(user);
        //
        String[] roles = authorization.getRoles();
        Assert.assertNotNull("No authorized roles found", roles);
        Assert.assertEquals("Not exactly 2 authorized roles found", 2, roles.length);
        //
        EasyMock.verify(userAdmin, sp);
    }
}
