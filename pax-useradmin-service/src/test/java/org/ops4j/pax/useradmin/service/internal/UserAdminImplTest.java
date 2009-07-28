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
import java.util.Dictionary;

import junit.framework.Assert;

import org.easymock.classextension.EasyMock;
import org.junit.Test;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.service.useradmin.UserAdminEvent;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Testing the UserAdminImpl class.
 * 
 * @author Matthias Kuespert
 * @since  12.07.2009
 */
public class UserAdminImplTest {

    private static final String NAME1 = "someRole";

    @Test (expected = IllegalArgumentException.class)
    public void createNoStorageProviderTracker() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        //
        EasyMock.replay(logTracker, eventTracker);
        //
        Assert.assertNotNull(new UserAdminImpl(context, null, logTracker, eventTracker));
        //
        EasyMock.verify(logTracker, eventTracker);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void createNoLogServiceTracker() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        //
        EasyMock.replay(spTracker, eventTracker);
        //
        Assert.assertNotNull(new UserAdminImpl(context, spTracker, null, eventTracker));
        //
        EasyMock.verify(spTracker, eventTracker);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void createNoEventAdminTracker() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        //
        EasyMock.replay(spTracker, logTracker);
        //
        Assert.assertNotNull(new UserAdminImpl(context, spTracker, logTracker, null));
        //
        EasyMock.verify(spTracker, logTracker);
    }
    
    private UserAdminImpl createUserAdmin(BundleContext context, ServiceTracker spTracker, ServiceTracker logTracker, ServiceTracker eventTracker) {
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
        return userAdmin;
    }

    private UserAdminImpl createUserAdmin(ServiceTracker spTracker, ServiceTracker logTracker, ServiceTracker eventTracker) {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        return createUserAdmin(context, spTracker, logTracker, eventTracker);
    }

    @Test
    public void createOk() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        //
        Assert.assertNotNull(createUserAdmin(spTracker, logTracker, eventTracker));
    }
    
    @Test
    @SuppressWarnings(value = "unchecked")
    public void updatedOk() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        Dictionary<String, String> properties = EasyMock.createMock(Dictionary.class);
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
            //
            EasyMock.expect(properties.get(UserAdminImpl.PROP_SECURITY)).andReturn(null);
            EasyMock.expect(properties.get(UserAdminImpl.PROP_SECURITY)).andReturn("true");
            EasyMock.expect(properties.get(UserAdminImpl.PROP_SECURITY)).andReturn("TrUe");
            EasyMock.expect(properties.get(UserAdminImpl.PROP_SECURITY)).andReturn("yes");
            EasyMock.expect(properties.get(UserAdminImpl.PROP_SECURITY)).andReturn("yEs");
            EasyMock.expect(properties.get(UserAdminImpl.PROP_SECURITY)).andReturn("false");
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(properties, spTracker, logTracker, eventTracker);
        //
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        try {
            userAdmin.updated(null);
            userAdmin.updated(properties);
            Assert.assertFalse(userAdmin.doCheckSecurity());
            userAdmin.updated(properties);
            Assert.assertTrue(userAdmin.doCheckSecurity());
            userAdmin.updated(properties);
            Assert.assertTrue(userAdmin.doCheckSecurity());
            userAdmin.updated(properties);
            Assert.assertTrue(userAdmin.doCheckSecurity());
            userAdmin.updated(properties);
            Assert.assertTrue(userAdmin.doCheckSecurity());
            userAdmin.updated(properties);
            Assert.assertFalse(userAdmin.doCheckSecurity());
        } catch (ConfigurationException e) {
            Assert.fail("Unexpected ConfigurationException: " + e.getMessage());
        }
        //
        EasyMock.verify(properties, spTracker, logTracker, eventTracker);
    }
    
    @Test
    public void fireEventNoService() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        Role role = EasyMock.createMock(Role.class);
        LogService logService = EasyMock.createMock(LogService.class);
        //
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
            EasyMock.expect(eventTracker.getService()).andReturn(null);
            EasyMock.expect(role.getName()).andReturn("some Name");
            EasyMock.expect(logTracker.getService()).andReturn(logService);
            logService.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
            //
            EasyMock.expect(eventTracker.getService()).andReturn(null);
            EasyMock.expect(logTracker.getService()).andReturn(logService);
            logService.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(spTracker, logTracker, eventTracker, role, logService);
        //
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        Assert.assertNotNull(userAdmin);
        userAdmin.fireEvent(UserAdminEvent.ROLE_CHANGED, role);
        userAdmin.fireEvent(UserAdminEvent.ROLE_CHANGED, null);
        //
        EasyMock.verify(spTracker, logTracker, eventTracker, role, logService);
    }
    
    @Test
    public void createUserOk() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        //
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, spTracker, logTracker, eventTracker);
        //
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        Assert.assertNotNull("No UserAdmin created", userAdmin);
        Assert.assertNotNull("No user created", userAdmin.createUser("some name", null, null));
        //
        EasyMock.verify(context, spTracker, logTracker, eventTracker);
    }
    
    @Test
    public void createGroupOk() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        //
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, spTracker, logTracker, eventTracker);
        //
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        Assert.assertNotNull("No UserAdmin created", userAdmin);
        Assert.assertNotNull("No user created", userAdmin.createGroup("some name", null, null));
        //
        EasyMock.verify(context, spTracker, logTracker, eventTracker);
    }
    
    @Test
    public void getStorageProviderNoService() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        //
        try {
            spTracker.open();
            logTracker.open();
            eventTracker.open();
            EasyMock.expect(spTracker.getService()).andReturn(null);
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(context, spTracker, logTracker, eventTracker);
        //
        UserAdminImpl userAdmin = new UserAdminImpl(context, spTracker, logTracker, eventTracker);
        Assert.assertNotNull(userAdmin);
        try {
            userAdmin.getStorageProvider();
        } catch (StorageException e) {
            Assert.assertEquals("Exception message mismatch", UserAdminMessages.MSG_MISSING_STORAGE_SERVICE, e.getMessage());
        }
        //
        EasyMock.verify(context, spTracker, logTracker, eventTracker);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void createUserRoleNoName() {
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
    public void createUserRoleInvalidType() {
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
        userAdmin.createRole(NAME1, Role.ROLE);
        EasyMock.verify(context, spTracker, logTracker, eventTracker);
        Assert.fail("No exception when creating role with invalid type");
    }

    @Test
    public void createExistingUserRole() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
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
        EasyMock.replay(sp, log, spTracker, logTracker, eventTracker);
        //
        Assert.assertNull("Duplicate role created", userAdmin.createRole(NAME1, Role.USER));
        //
        EasyMock.verify(sp, log, spTracker, logTracker, eventTracker);
    }

    @Test
    public void createUserRoleStorageException() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        ServiceReference ref = EasyMock.createMock(ServiceReference.class);
        StorageException exception = new StorageException("");
        LogService log = EasyMock.createMock(LogService.class);
        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(null);
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.createUser(userAdmin, NAME1)).andThrow(exception);
            EasyMock.expect(logTracker.getService()).andReturn(log);
            log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(sp, ref, spTracker, logTracker, eventTracker);
        Assert.assertNull("User created when exception was thrown", userAdmin.createRole(NAME1, Role.USER));
        EasyMock.verify(sp, ref, spTracker, logTracker, eventTracker);
    }

    @Test
    public void createUserRoleOk() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(context, spTracker, logTracker, eventTracker);
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
    public void createGroupRoleOk() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(context, spTracker, logTracker, eventTracker);
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
 
    @Test (expected = IllegalArgumentException.class)
    public void removeRoleNullName() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        EasyMock.replay(spTracker, logTracker, eventTracker);
        //
        userAdmin.removeRole(null);
        Assert.fail("No exception on null name");
        //
        EasyMock.verify(spTracker, logTracker, eventTracker);
    }
    
    @Test
    public void removeDefaultRole() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        LogService log = EasyMock.createMock(LogService.class);
        //
        EasyMock.expect(logTracker.getService()).andReturn(log);
        log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        //
        EasyMock.expect(logTracker.getService()).andReturn(log);
        log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        EasyMock.replay(log, spTracker, logTracker, eventTracker);
        //
        Assert.assertFalse(userAdmin.removeRole(""));
        Assert.assertFalse(userAdmin.removeRole(Role.USER_ANYONE));
        //
        EasyMock.verify(log, spTracker, logTracker, eventTracker);
    }

    @Test
    public void removeRoleNotFound() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(context, spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        LogService log = EasyMock.createMock(LogService.class);
        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(null);
            EasyMock.expect(logTracker.getService()).andReturn(log);
            log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        } catch (StorageException e) {
            Assert.fail("Unexpected StorageException: " + e.getMessage());
        }
        //
        EasyMock.replay(context, sp, log, spTracker, logTracker, eventTracker);
        //
        Assert.assertFalse(userAdmin.removeRole(NAME1));
        //
        EasyMock.verify(context, sp, log, spTracker, logTracker, eventTracker);
    }

    @Test
    public void removeRoleNotDeleted() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(context, spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        LogService log = EasyMock.createMock(LogService.class);

        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(user);
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.deleteRole(user)).andReturn(false);
            EasyMock.expect(logTracker.getService()).andReturn(log);
            log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        } catch (StorageException e) {
            Assert.fail("Unexpected StorageException: " + e.getMessage());
        }
        //
        EasyMock.replay(context, sp, log, spTracker, logTracker, eventTracker);
        //
        Assert.assertFalse(userAdmin.removeRole(NAME1));
        //
        EasyMock.verify(context, sp, log, spTracker, logTracker, eventTracker);
    }

    @Test
    public void removeRoleStorageException() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(context, spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        StorageException exception = new StorageException("");
        LogService log = EasyMock.createMock(LogService.class);

        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(user);
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.deleteRole(user)).andThrow(exception);
            EasyMock.expect(logTracker.getService()).andReturn(log);
            log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        } catch (StorageException e) {
            Assert.fail("Unexpected StorageException: " + e.getMessage());
        }
        //
        EasyMock.replay(context, sp, log, spTracker, logTracker, eventTracker);
        //
        Assert.assertFalse(userAdmin.removeRole(NAME1));
        //
        EasyMock.verify(context, sp, log, spTracker, logTracker, eventTracker);
    }

    @Test
    public void removeRoleOk() {
        BundleContext context = EasyMock.createMock(BundleContext.class);
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(context, spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        EventAdmin eventAdmin = EasyMock.createMock(EventAdmin.class);
        ServiceReference ref = EasyMock.createMock(ServiceReference.class);

        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(user);
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.deleteRole(user)).andReturn(true);
            EasyMock.expect(eventTracker.getService()).andReturn(eventAdmin);
            EasyMock.expect(context.getServiceReference(UserAdmin.class.getName())).andReturn(ref);
            EasyMock.expect(ref.getProperty(Constants.SERVICE_ID)).andReturn("1");
            EasyMock.expect(ref.getProperty(Constants.OBJECTCLASS)).andReturn(UserAdmin.class.getName());
            EasyMock.expect(ref.getProperty(Constants.SERVICE_PID)).andReturn(UserAdminImpl.PID);
            eventAdmin.postEvent(EasyMock.isA(Event.class));
        } catch (StorageException e) {
            Assert.fail("Unexpected StorageException: " + e.getMessage());
        }
        //
        EasyMock.replay(context, sp, eventAdmin, ref, spTracker, logTracker, eventTracker);
        //
        Assert.assertTrue(userAdmin.removeRole(NAME1));
        //
        EasyMock.verify(context, sp, eventAdmin, ref, spTracker, logTracker, eventTracker);
    }

    @Test
    public void getRolesNullFilter() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        // variant 1: no roles found
        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.findRoles(userAdmin, null)).andReturn(new ArrayList<Role>());
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(sp, spTracker, logTracker, eventTracker);
        try {
            Assert.assertNull("Emty result did not return null", userAdmin.getRoles(null));
        } catch (InvalidSyntaxException e) {
            Assert.fail("Invalid filter syntax: " + e.getMessage());
        }
        EasyMock.verify(sp, spTracker, logTracker, eventTracker);
        //
        // variant 2: one role found
        //
        EasyMock.reset(sp, spTracker, logTracker, eventTracker);
        UserImpl role = new UserImpl(NAME1, userAdmin, null, null);
        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(role);
        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.findRoles(userAdmin, null)).andReturn(roles);
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(sp, spTracker, logTracker, eventTracker);
        try {
            Role[] result = userAdmin.getRoles(null);
            Assert.assertNotNull("No roles found", result);
            Assert.assertEquals("Result size != 1", 1, result.length);
        } catch (InvalidSyntaxException e) {
            Assert.fail("Invalid filter syntax: " + e.getMessage());
        }
        EasyMock.verify(sp, spTracker, logTracker, eventTracker);
    }

    @Test
    public void getRolesStorageException() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        //
        // variant 1: no roles found
        //
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        StorageException exception = new StorageException("");
        LogService log = EasyMock.createMock(LogService.class);
        try {
            EasyMock.expect(spTracker.getService()).andReturn(sp);
            EasyMock.expect(sp.findRoles(userAdmin, null)).andThrow(exception);
            EasyMock.expect(logTracker.getService()).andReturn(log);
            log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(sp, log, spTracker, logTracker, eventTracker);
        //
        try {
            Assert.assertNull("Roles returned when exception occured", userAdmin.getRoles(null));
        } catch (InvalidSyntaxException e) {
            Assert.fail("Unexpected InvalidSyntax exception: " + e.getMessage());
        }
        //
        EasyMock.verify(sp, log, spTracker, logTracker, eventTracker);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void getRoleNullName() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        EasyMock.replay(spTracker, logTracker, eventTracker);
        userAdmin.getRole(null);
        EasyMock.verify(spTracker, logTracker, eventTracker);
    }
    
    @Test
    public void getRoleEmptyName() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        UserImpl user = new UserImpl(Role.USER_ANYONE, userAdmin, null, null);
        //
        EasyMock.expect(spTracker.getService()).andReturn(sp);
        try {
            EasyMock.expect(sp.getRole(userAdmin, Role.USER_ANYONE)).andReturn(user);
        } catch (StorageException e) {
            Assert.fail("Unexpected StorageException caught: " + e.getMessage());
        }
        //
        EasyMock.replay(sp, spTracker, logTracker, eventTracker);
        //
        User result = null;
        try {
            result = (User) userAdmin.getRole("");
        } catch (Exception e) {
            Assert.fail("Unexpected StorageException caught: " + e.getMessage());
        }
        Assert.assertNotNull("No user.anyone found", result);
        Assert.assertEquals("User name mismatch", Role.USER_ANYONE, result.getName());
        //
        EasyMock.verify(sp, spTracker, logTracker, eventTracker);
    }

    @Test
    public void getRoleStorageException() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        StorageException exception = new StorageException("");
        LogService log = EasyMock.createMock(LogService.class);
        //
        EasyMock.expect(spTracker.getService()).andReturn(sp);
        try {
            EasyMock.expect(sp.getRole(userAdmin, Role.USER_ANYONE)).andThrow(exception);
            EasyMock.expect(logTracker.getService()).andReturn(log);
            log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        } catch (StorageException e) {
            Assert.fail("Unexpected StorageException caught: " + e.getMessage());
        }
        //
        EasyMock.replay(sp, log, spTracker, logTracker, eventTracker);
        //
        User result = null;
        try {
            result = (User) userAdmin.getRole("");
        } catch (Exception e) {
            Assert.fail("Unexpected StorageException caught: " + e.getMessage());
        }
        Assert.assertNull("User found when exception was thrown", result);
        //
        EasyMock.verify(sp, log, spTracker, logTracker, eventTracker);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void getUserNullKey() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        EasyMock.replay(spTracker, logTracker, eventTracker);
        userAdmin.getUser(null, "");
        EasyMock.verify(spTracker, logTracker, eventTracker);
    }

    @Test (expected = IllegalArgumentException.class)
    public void getUserNullValue() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        EasyMock.replay(spTracker, logTracker, eventTracker);
        userAdmin.getUser("", null);
        EasyMock.verify(spTracker, logTracker, eventTracker);
    }

    @Test
    public void getUserStorageException() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        StorageException exception = new StorageException("");
        LogService log = EasyMock.createMock(LogService.class);
        //
        EasyMock.expect(spTracker.getService()).andReturn(sp);
        try {
            EasyMock.expect(sp.getUser(userAdmin, "", "")).andThrow(exception);
            EasyMock.expect(logTracker.getService()).andReturn(log);
            log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        } catch (StorageException e) {
            Assert.fail("Unexpected StorageException caught: " + e.getMessage());
        }
        //
        EasyMock.replay(sp, log, spTracker, logTracker, eventTracker);
        //
        User result = null;
        try {
            result = (User) userAdmin.getUser("", "");
        } catch (Exception e) {
            Assert.fail("Unexpected StorageException caught: " + e.getMessage());
        }
        Assert.assertNull("User found when exception was thrown", result);
        //
        EasyMock.verify(sp, log, spTracker, logTracker, eventTracker);
    }
    
    @Test
    public void getUserOk() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        UserImpl user = new UserImpl(Role.USER_ANYONE, userAdmin, null, null);
        //
        EasyMock.expect(spTracker.getService()).andReturn(sp);
        try {
            EasyMock.expect(sp.getUser(userAdmin, "", "")).andReturn(user);
        } catch (StorageException e) {
            Assert.fail("Unexpected StorageException caught: " + e.getMessage());
        }
        //
        EasyMock.replay(sp, spTracker, logTracker, eventTracker);
        //
        User result = null;
        try {
            result = (User) userAdmin.getUser("", "");
        } catch (Exception e) {
            Assert.fail("Unexpected StorageException caught: " + e.getMessage());
        }
        Assert.assertNotNull("No user found", result);
        //
        EasyMock.verify(sp, spTracker, logTracker, eventTracker);
    }

    @Test (expected = IllegalArgumentException.class)
    public void getAuthorizationNullUser() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        //
        EasyMock.replay(spTracker, logTracker, eventTracker);
        //
        Assert.assertNull("Authorization created for null user", userAdmin.getAuthorization(null));
        //
        EasyMock.verify(spTracker, logTracker, eventTracker);
    }
    
    @Test
    public void getAuthorizationOk() {
        ServiceTracker spTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker logTracker = EasyMock.createMock(ServiceTracker.class);
        ServiceTracker eventTracker = EasyMock.createMock(ServiceTracker.class);
        UserAdminImpl userAdmin = createUserAdmin(spTracker, logTracker, eventTracker);
        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        //
        EasyMock.replay(spTracker, logTracker, eventTracker);
        //
        Assert.assertNotNull("No authorization created for user", userAdmin.getAuthorization(user));
        //
        EasyMock.verify(spTracker, logTracker, eventTracker);
    }
}
