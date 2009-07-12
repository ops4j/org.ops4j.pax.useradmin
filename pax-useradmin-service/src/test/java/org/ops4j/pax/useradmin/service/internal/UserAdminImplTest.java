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
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Testing the UserAdminImpl class.
 * 
 * @author Matthias Kuespert
 * @since  12.07.2009
 */
public class UserAdminImplTest {

    private static final String NAME1 = "someRole";
    private static final String NAME2 = "someOtherRole";

    @Test (expected = IllegalArgumentException.class)
    public void createRoleNoName() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, spTracker, logTracker, eventTracker);
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        userAdmin.createRole(null, Role.ROLE);
        EasyMock.verify(context, spTracker, logTracker, eventTracker);
        Assert.fail("No exception when creating role with invalid name");
    }

    @Test (expected = IllegalArgumentException.class)
    public void createRoleInvalidType() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, spTracker, logTracker, eventTracker);
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        userAdmin.createRole(null, Role.ROLE);
        EasyMock.verify(context, spTracker, logTracker, eventTracker);
        Assert.fail("No exception when creating role with invalid type");
    }

    @Test
    public void createExistingRole() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, spTracker, logTracker, eventTracker);
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        Assert.assertNotNull("No UserAdminImpl instance created", userAdmin);
        EasyMock.verify(context, spTracker, logTracker, eventTracker);

        EasyMock.reset(context, spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        UserImpl role1 = new UserImpl(NAME1, userAdmin, null, null);
        LogService log = EasyMock.createMock(LogService.class);

        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(role1);
            EasyMock.expect(logTracker.getService()).andReturn(log);
            log.log(LogService.LOG_WARNING, "[" + UserAdminImpl.class.getName() + "] role already exists: " + NAME1);
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, sp, log, spTracker, logTracker, eventTracker);
        Assert.assertNull("Duplicate role created", userAdmin.createRole(NAME1, Role.USER));
        EasyMock.verify(context, sp, log, spTracker, logTracker, eventTracker);
    }

    @Test
    public void createNewRoleOk() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, spTracker, logTracker, eventTracker);
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        Assert.assertNotNull("No UserAdminImpl instance created", userAdmin);
        EasyMock.verify(context, spTracker, logTracker, eventTracker);

        EasyMock.reset(context, spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        UserImpl role = new UserImpl(NAME1, userAdmin, null, null);
        EventAdmin eventAdmin = EasyMock.createMock(EventAdmin.class);
        ServiceReference ref = EasyMock.createMock(ServiceReference.class);
        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(null);
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.createUser(userAdmin, NAME1)).andReturn(role);
            EasyMock.expect(eventTracker.getService()).andReturn(eventAdmin);
            EasyMock.expect(context.getServiceReference(UserAdmin.class.getName())).andReturn(ref);
            EasyMock.expect(ref.getProperty(Constants.SERVICE_ID)).andReturn("1");
            EasyMock.expect(ref.getProperty(Constants.OBJECTCLASS)).andReturn(UserAdmin.class.getName());
            EasyMock.expect(ref.getProperty(Constants.SERVICE_PID)).andReturn(UserAdminImpl.PID);
            eventAdmin.postEvent(EasyMock.isA(Event.class));
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, sp, ref, eventAdmin, spTracker, logTracker, eventTracker);
        Assert.assertNotNull("User not created", userAdmin.createRole(NAME1, Role.USER));
        EasyMock.verify(context, sp, ref, eventAdmin, spTracker, logTracker, eventTracker);
    }

    @Test
    public void createNewGroupOk() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, spTracker, logTracker, eventTracker);
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        Assert.assertNotNull("No UserAdminImpl instance created", userAdmin);
        EasyMock.verify(context, spTracker, logTracker, eventTracker);
        //
        EasyMock.reset(context, spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        GroupImpl group = new GroupImpl(NAME1, userAdmin, null, null);
        EventAdmin eventAdmin = EasyMock.createMock(EventAdmin.class);
        ServiceReference ref = EasyMock.createMock(ServiceReference.class);
        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(null);
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.createGroup(userAdmin, NAME1)).andReturn(group);
            EasyMock.expect(eventTracker.getService()).andReturn(eventAdmin);
            EasyMock.expect(context.getServiceReference(UserAdmin.class.getName())).andReturn(ref);
            EasyMock.expect(ref.getProperty(Constants.SERVICE_ID)).andReturn("1");
            EasyMock.expect(ref.getProperty(Constants.OBJECTCLASS)).andReturn(UserAdmin.class.getName());
            EasyMock.expect(ref.getProperty(Constants.SERVICE_PID)).andReturn(UserAdminImpl.PID);
            eventAdmin.postEvent(EasyMock.isA(Event.class));
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, sp, ref, eventAdmin, spTracker, logTracker, eventTracker);
        Assert.assertNotNull("Group not created", userAdmin.createRole(NAME1, Role.GROUP));
        EasyMock.verify(context, sp, ref, eventAdmin, spTracker, logTracker, eventTracker);
    }
 
    @Test
    public void getRolesNullFilter() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, spTracker, logTracker, eventTracker);
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        Assert.assertNotNull("No UserAdminImpl instance created", userAdmin);
        EasyMock.verify(context, spTracker, logTracker, eventTracker);
        //
        // variant 1: no roles found
        //
        EasyMock.reset(context, spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.findRoles(userAdmin, null)).andReturn(new ArrayList<Role>());
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, sp, spTracker, logTracker, eventTracker);
        try {
            Assert.assertNull("Emty result did not return null", userAdmin.getRoles(null));
        } catch (InvalidSyntaxException e) {
            Assert.fail("Invalid filter syntax: " + e.getMessage());
        }
        EasyMock.verify(context, sp, spTracker, logTracker, eventTracker);
        //
        // variant 2: one role found
        //
        EasyMock.reset(context, sp, spTracker, logTracker, eventTracker);
        UserImpl role = new UserImpl(NAME1, userAdmin, null, null);
        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(role);
        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.findRoles(userAdmin, null)).andReturn(roles);
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, sp, spTracker, logTracker, eventTracker);
        try {
            Role[] result = userAdmin.getRoles(null);
            Assert.assertNotNull("No roles found", result);
            Assert.assertEquals("Result size != 1", 1, result.length);
        } catch (InvalidSyntaxException e) {
            Assert.fail("Invalid filter syntax: " + e.getMessage());
        }
        EasyMock.verify(context, sp, spTracker, logTracker, eventTracker);
    }

    @Test
    public void userHasRole() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, spTracker, logTracker, eventTracker);
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        Assert.assertNotNull("No UserAdminImpl instance created", userAdmin);
        EasyMock.verify(context, spTracker, logTracker, eventTracker);
        //
        EasyMock.reset(context, spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        GroupImpl group = new GroupImpl(NAME2, userAdmin, null, null);
        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.addMember(group, user)).andReturn(true);
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.getRole(userAdmin, NAME2)).andReturn(group);
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.getRequiredMembers(userAdmin, group)).andReturn(new ArrayList<Role>());
            Collection<Role> members = new ArrayList<Role>();
            members.add(user);
            EasyMock.expect(sp.getMembers(userAdmin, group)).andReturn(members);
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.getMembers(userAdmin, group)).andReturn(members);
            EasyMock.expect(spTracker.getService()).andReturn(sp);
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, sp, spTracker, logTracker, eventTracker);
        Assert.assertTrue("Member not added", group.addMember(user));
        Assert.assertNotNull("Group has no members", group.getMembers());
        // Assert.assertEquals("", 1, group.getMembers().length);
        Authorization auth = userAdmin.getAuthorization(user);
        Assert.assertTrue(auth.hasRole(NAME2));
        EasyMock.verify(context, sp, spTracker, logTracker, eventTracker);
    }
}
