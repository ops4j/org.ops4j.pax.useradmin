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
import org.ops4j.pax.useradmin.service.internal.RoleImpl.ImplicationResult;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Role;

/**
 * Testing the GroupImpl class.
 * 
 * @author Matthias Kuespert
 * @since  12.07.2009
 */
public class GroupImplTest {

    private static final String GROUP_NAME1 = "someGroup1";
    private static final String GROUP_NAME2 = "someGroup2";
    private static final String USER_NAME1 = "user1";
    private static final String USER_NAME2 = "user2";
    private static final String USER_NAME3 = "user3";
    
    @Test
    public void constructOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        Assert.assertNotNull("Could not create GroupImpl instance", group);
        Assert.assertEquals("Mismatching name", GROUP_NAME1, group.getName());
        Assert.assertEquals("Invalid type", Role.GROUP, group.getType());
        Assert.assertEquals("Invalid UserAdmin instance", userAdmin, group.getAdmin());
    }
    
    @Test
    public void addAndRemoveMember() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        RoleImpl role1 = new UserImpl(USER_NAME1, userAdmin, null, null);
        RoleImpl role2 = new UserImpl(USER_NAME2, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        StorageException exception = new StorageException("");
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
            //
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.removeMember(group, role2)).andThrow(exception);
            userAdmin.logMessage(EasyMock.eq(group),
                                 EasyMock.isA(String.class),
                                 EasyMock.eq(LogService.LOG_ERROR));
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertTrue("Member not added", group.addMember(role1));
        Assert.assertTrue("Member not added", group.addMember(role2));
        Assert.assertTrue("Member not removed", group.removeMember(role1));
        Assert.assertFalse("Member removed", group.removeMember(role2));
        //
        EasyMock.verify(userAdmin, sp);
    }

    // basic members
    
    @Test
    public void addBasicMemberNull() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        EasyMock.replay(userAdmin);
        //
        Assert.assertFalse("Null member added", group.addMember(null));
        //
        EasyMock.verify(userAdmin);
    }

    @Test
    public void addBasicMemberStorageException() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        RoleImpl role = new UserImpl(USER_NAME1, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        StorageException exception = new StorageException("");
        try {
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group, role)).andThrow(exception);
            userAdmin.logMessage(EasyMock.eq(group),
                                 EasyMock.isA(String.class),
                                 EasyMock.eq(LogService.LOG_ERROR));
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertFalse("Member not added", group.addMember(role));
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void addBasicMemberOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
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
        //
        Assert.assertTrue("Member not added", group.addMember(role));
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void getNoBasicMembers() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        try {
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.getMembers(userAdmin, group)).andReturn(new ArrayList<Role>());
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertNull("No null returned when no basic members", group.getMembers());
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void addAndGetSomeBasicMembers() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        RoleImpl role1 = new UserImpl(USER_NAME1, userAdmin, null, null);
        RoleImpl role2 = new UserImpl(USER_NAME2, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        StorageException exception = new StorageException("");
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
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.getMembers(userAdmin, group)).andThrow(exception);
            userAdmin.logMessage(EasyMock.eq(group),
                                 EasyMock.isA(String.class),
                                 EasyMock.eq(LogService.LOG_ERROR));
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertTrue("Member not added", group.addMember(role1));
        Assert.assertTrue("Member not added", group.addMember(role2));
        Assert.assertNotNull("Null returned for existing basic members", group.getMembers());
        Assert.assertNull("Non null returned exception during retrieval of basic members", group.getMembers());
        //
        EasyMock.verify(userAdmin, sp);
    }

    // required members

    @Test
    public void addRequiredMemberNull() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        //
        EasyMock.replay(userAdmin);
        //
        Assert.assertFalse("Null member added", group.addRequiredMember(null));
        //
        EasyMock.verify(userAdmin);
    }

    @Test
    public void addRequiredMemberStorageException() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        RoleImpl role = new UserImpl(USER_NAME1, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        StorageException exception = new StorageException("");
        try {
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addRequiredMember(group, role)).andThrow(exception);
            userAdmin.logMessage(EasyMock.eq(group),
                                 EasyMock.isA(String.class),
                                 EasyMock.eq(LogService.LOG_ERROR));
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertFalse("Member not added", group.addRequiredMember(role));
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void addRequiredMemberOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
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
        //
        Assert.assertTrue("Member not added", group.addRequiredMember(role));
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void getNoRequiredMembers() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        try {
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group)).andReturn(new ArrayList<Role>());
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertNull("No null returned when no basic members", group.getRequiredMembers());
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void addAndGetSomeRequiredMembers() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        RoleImpl role1 = new UserImpl(USER_NAME1, userAdmin, null, null);
        RoleImpl role2 = new UserImpl(USER_NAME2, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        StorageException exception = new StorageException("");
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
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group)).andThrow(exception);
            userAdmin.logMessage(EasyMock.eq(group),
                                 EasyMock.isA(String.class),
                                 EasyMock.eq(LogService.LOG_ERROR));
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertTrue("Member not added", group.addRequiredMember(role1));
        Assert.assertTrue("Member not added", group.addRequiredMember(role2));
        Assert.assertNotNull("Null returned for existing basic members", group.getRequiredMembers());
        Assert.assertNull("Non null returned exception during retrieval of required members", group.getRequiredMembers());
        //
        EasyMock.verify(userAdmin, sp);
    }

    /*
     * Setup:
     * 
     * user1 - basic member of group1 and group2 
     * user2 - basic member of group1
     * user3 - not in any group
     * 
     * group1:
     *     - basic: user1, user2
     * group2:
     *     - required: group1
     *     - basic: user1
     */
    
    @Test
    public void impliedByOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        GroupImpl group1 = new GroupImpl(GROUP_NAME1, userAdmin, null, null);
        GroupImpl group2 = new GroupImpl(GROUP_NAME2, userAdmin, null, null);
        RoleImpl user1 = new UserImpl(USER_NAME1, userAdmin, null, null);
        RoleImpl user2 = new UserImpl(USER_NAME2, userAdmin, null, null);
        RoleImpl user3 = new UserImpl(USER_NAME3, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        //
        try {
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group1, user1)).andReturn(true);
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group1, user2)).andReturn(true);
            //
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addMember(group2, user1)).andReturn(true);
            userAdmin.checkAdminPermission();
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            EasyMock.expect(sp.addRequiredMember(group2, group1)).andReturn(true);
            //
            // User1 implies group1
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            Collection<Role> requiredRoles = new ArrayList<Role>();
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group1)).andReturn(requiredRoles);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            Collection<Role> basicRoles = new ArrayList<Role>();
            basicRoles.add(user1);
            basicRoles.add(user2);
            EasyMock.expect(sp.getMembers(userAdmin, group1)).andReturn(basicRoles);
            //
            // User2 implies group1
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            requiredRoles = new ArrayList<Role>();
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group1)).andReturn(requiredRoles);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            basicRoles = new ArrayList<Role>();
            basicRoles.add(user1);
            basicRoles.add(user2);
            EasyMock.expect(sp.getMembers(userAdmin, group1)).andReturn(basicRoles);
            //
            // User3 implies group1
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            requiredRoles = new ArrayList<Role>();
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group1)).andReturn(requiredRoles);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            basicRoles = new ArrayList<Role>();
            basicRoles.add(user1);
            basicRoles.add(user2);
            EasyMock.expect(sp.getMembers(userAdmin, group1)).andReturn(basicRoles);
            //
            // User 1 implies group2
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            requiredRoles = new ArrayList<Role>();
            requiredRoles.add(group1);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group2)).andReturn(requiredRoles);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            requiredRoles = new ArrayList<Role>();
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group1)).andReturn(requiredRoles);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            basicRoles = new ArrayList<Role>();
            basicRoles.add(user1);
            basicRoles.add(user2);
            EasyMock.expect(sp.getMembers(userAdmin, group1)).andReturn(basicRoles);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            basicRoles = new ArrayList<Role>();
            basicRoles.add(user1);
            EasyMock.expect(sp.getMembers(userAdmin, group2)).andReturn(basicRoles);
            //
            // User 2 implies group2
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            requiredRoles = new ArrayList<Role>();
            requiredRoles.add(group1);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group2)).andReturn(requiredRoles);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            requiredRoles = new ArrayList<Role>();
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group1)).andReturn(requiredRoles);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            basicRoles = new ArrayList<Role>();
            basicRoles.add(user1);
            basicRoles.add(user2);
            EasyMock.expect(sp.getMembers(userAdmin, group1)).andReturn(basicRoles);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            basicRoles = new ArrayList<Role>();
            basicRoles.add(user1);
            EasyMock.expect(sp.getMembers(userAdmin, group2)).andReturn(basicRoles);
            //
            // User 2 loop
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            requiredRoles = new ArrayList<Role>();
            requiredRoles.add(group1);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group2)).andReturn(requiredRoles);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            requiredRoles = new ArrayList<Role>();
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group1)).andReturn(requiredRoles);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            basicRoles = new ArrayList<Role>();
            basicRoles.add(user1);
            basicRoles.add(user2);
            EasyMock.expect(sp.getMembers(userAdmin, group1)).andReturn(basicRoles);
//            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
//            requiredRoles = new ArrayList<Role>();
//            requiredRoles.add(user2);
//            EasyMock.expect(sp.getRequiredMembers(userAdmin, group1)).andReturn(requiredRoles);
//            //
//            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
//            basicRoles = new ArrayList<Role>();
//            basicRoles.add(user1);
//            EasyMock.expect(sp.getMembers(userAdmin, group1)).andReturn(basicRoles);
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertTrue("Member not added", group1.addMember(user1));
        Assert.assertTrue("Member not added", group1.addMember(user2));
        Assert.assertTrue("Member not added", group2.addMember(user1));
        Assert.assertTrue("Member not added", group2.addRequiredMember(group1));
        //
        Collection<String> checkedRoles = new ArrayList<String>();
        Assert.assertEquals("Group not implied by itself", ImplicationResult.IMPLIEDBY_YES, group1.isImpliedBy(group1, checkedRoles));
        checkedRoles.clear();
        Assert.assertEquals("User 1 does not imply group 1", ImplicationResult.IMPLIEDBY_YES, group1.isImpliedBy(user1, checkedRoles));
        checkedRoles.clear();
        Assert.assertEquals("User 2 does not imply group 1", ImplicationResult.IMPLIEDBY_YES, group1.isImpliedBy(user2, checkedRoles));
        checkedRoles.clear();
        Assert.assertEquals("User 3 does imply group 1", ImplicationResult.IMPLIEDBY_NO, group1.isImpliedBy(user3, checkedRoles));
        checkedRoles.clear();
        Assert.assertEquals("User 1 does not imply group 2", ImplicationResult.IMPLIEDBY_YES, group2.isImpliedBy(user1, checkedRoles));
        checkedRoles.clear();
        Assert.assertEquals("User 2 does imply group 2", ImplicationResult.IMPLIEDBY_NO, group2.isImpliedBy(user2, checkedRoles));
        checkedRoles.clear();
        checkedRoles.add(GROUP_NAME1);
        Assert.assertEquals("Loop not detected", ImplicationResult.IMPLIEDBY_LOOPDETECTED, group1.isImpliedBy(group1, checkedRoles));
        checkedRoles.clear();
        checkedRoles.add(USER_NAME1);
        Assert.assertEquals("Loop not detected", ImplicationResult.IMPLIEDBY_NO, group2.isImpliedBy(user1, checkedRoles));
        //
        EasyMock.verify(userAdmin);
    }
}
