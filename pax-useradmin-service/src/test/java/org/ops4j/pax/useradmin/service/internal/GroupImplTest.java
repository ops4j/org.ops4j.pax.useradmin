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
import org.osgi.service.useradmin.Role;

/**
 * Testing the GroupImpl class.
 * 
 * @author Matthias Kuespert
 * @since  12.07.2009
 */
public class GroupImplTest {

    private static final String GROUP_NAME = "someGroup";
    private static final String USER_NAME1 = "user1";
    private static final String USER_NAME2 = "user2";
    
    @Test
    public void constructOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME, userAdmin, null, null);
        Assert.assertNotNull("Could not create GroupImpl instance", group);
        Assert.assertEquals("Mismatching name", GROUP_NAME, group.getName());
        Assert.assertEquals("Invalid type", Role.GROUP, group.getType());
        Assert.assertEquals("Invalid UserAdmin instance", userAdmin, group.getAdmin());
    }
    
    @Test
    public void addAndRemoveMember() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME, userAdmin, null, null);
        RoleImpl role1 = new UserImpl(USER_NAME1, userAdmin, null, null);
        RoleImpl role2 = new UserImpl(USER_NAME2, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        try {
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group, role1)).andReturn(true);
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group, role2)).andReturn(true);
            //
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.removeMember(group, role1)).andReturn(true);
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        Assert.assertEquals("Member not added", true, group.addMember(role1));
        Assert.assertEquals("Member not added", true, group.addMember(role2));
        Assert.assertNotNull("Member not removed", group.removeMember(role1));
        EasyMock.verify(userAdmin, sp);
    }

    // basic members
    
    @Test
    public void addBasicMemberNull() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME, userAdmin, null, null);
        EasyMock.replay(userAdmin);
        Assert.assertEquals("Null member added", false, group.addMember(null));
        EasyMock.verify(userAdmin);
    }

    @Test
    public void addBasicMemberOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME, userAdmin, null, null);
        RoleImpl role = new UserImpl(USER_NAME1, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        try {
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group, role)).andReturn(true);
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        Assert.assertEquals("Member not added", true, group.addMember(role));
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void getNoBasicMembers() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        try {
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.getMembers(userAdmin, group)).andReturn(new ArrayList<Role>());
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        Assert.assertEquals("No null returned when no basic members", null, group.getMembers());
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void addAndGetSomeBasicMembers() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME, userAdmin, null, null);
        RoleImpl role1 = new UserImpl(USER_NAME1, userAdmin, null, null);
        RoleImpl role2 = new UserImpl(USER_NAME2, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        try {
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group, role1)).andReturn(true);
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group, role2)).andReturn(true);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            Collection<Role> roles = new ArrayList<Role>();
            roles.add(role1);
            roles.add(role2);
            EasyMock.expect(sp.getMembers(userAdmin, group)).andReturn(roles);
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        Assert.assertEquals("Member not added", true, group.addMember(role1));
        Assert.assertEquals("Member not added", true, group.addMember(role2));
        Assert.assertNotNull("Null returned for existing basic members", group.getMembers());
        EasyMock.verify(userAdmin, sp);
    }

    // required members

    @Test
    public void addRequiredMemberNull() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME, userAdmin, null, null);
        EasyMock.replay(userAdmin);
        Assert.assertEquals("Null member added", false, group.addRequiredMember(null));
        EasyMock.verify(userAdmin);
    }

    @Test
    public void addRequiredMemberOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME, userAdmin, null, null);
        RoleImpl role = new UserImpl(USER_NAME1, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        try {
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addRequiredMember(group, role)).andReturn(true);
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        Assert.assertEquals("Member not added", true, group.addRequiredMember(role));
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void getNoRequiredMembers() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        try {
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group)).andReturn(new ArrayList<Role>());
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        Assert.assertEquals("No null returned when no basic members", null, group.getRequiredMembers());
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void addAndGetSomeRequiredMembers() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME, userAdmin, null, null);
        RoleImpl role1 = new UserImpl(USER_NAME1, userAdmin, null, null);
        RoleImpl role2 = new UserImpl(USER_NAME2, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        try {
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addRequiredMember(group, role1)).andReturn(true);
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addRequiredMember(group, role2)).andReturn(true);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            Collection<Role> roles = new ArrayList<Role>();
            roles.add(role1);
            roles.add(role2);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group)).andReturn(roles);
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        Assert.assertEquals("Member not added", true, group.addRequiredMember(role1));
        Assert.assertEquals("Member not added", true, group.addRequiredMember(role2));
        Assert.assertNotNull("Null returned for existing basic members", group.getRequiredMembers());
        EasyMock.verify(userAdmin, sp);
    }
}
