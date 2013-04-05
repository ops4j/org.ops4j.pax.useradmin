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
import java.util.Dictionary;
import java.util.concurrent.Executors;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.useradmin.service.PaxUserAdminConstants;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.service.useradmin.UserAdminEvent;
import org.osgi.service.useradmin.UserAdminListener;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Testing the UserAdminImpl class.
 */
@SuppressWarnings("unchecked")
public class UserAdminImplTest {

    private static final String NAME1 = "someRole";

    @Test(expected = IllegalArgumentException.class)
    public void createNoStorageProviderTracker() {
        new PaxUserAdmin(null, EasyMock.createMock(ServiceTracker.class), EasyMock.createMock(ServiceTracker.class), EasyMock.createMock(ServiceTracker.class), Executors.newCachedThreadPool());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNoLogServiceTracker() {
        new PaxUserAdmin(EasyMock.createMock(StorageProvider.class), null, EasyMock.createMock(ServiceTracker.class), EasyMock.createMock(ServiceTracker.class), Executors.newCachedThreadPool());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNoEventAdminTracker() {
        new PaxUserAdmin(EasyMock.createMock(StorageProvider.class), EasyMock.createMock(ServiceTracker.class), null, EasyMock.createMock(ServiceTracker.class), Executors.newCachedThreadPool());
    }

    @Test
    public void logOk() {
        Role role = EasyMock.createMock(Role.class);
        LogService logService = EasyMock.createMock(LogService.class);
        PaxUserAdmin userAdmin = createUserAdmin(logService);
        logService.log(EasyMock.eq(0), EasyMock.isA(String.class));
        logService.log(EasyMock.eq(0), EasyMock.isA(String.class));
        logService.log(EasyMock.eq(0), EasyMock.isA(String.class));
        logService.log(EasyMock.eq(LogService.LOG_DEBUG), EasyMock.matches(".*test - .*"));
        logService.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.matches(".*test - .*"));
        logService.log(EasyMock.eq(LogService.LOG_INFO), EasyMock.matches(".*test - .*"));
        logService.log(EasyMock.eq(LogService.LOG_WARNING), EasyMock.matches(".*test - .*"));
        EasyMock.replay(role, logService);
        userAdmin.logMessage(null, 0, null);
        userAdmin.logMessage(this, 0, null);
        userAdmin.logMessage(this, 0, "test - NOTHING");
        userAdmin.logMessage(this, LogService.LOG_DEBUG, "test - DEBUG");
        userAdmin.logMessage(this, LogService.LOG_ERROR, "test - ERROR");
        userAdmin.logMessage(this, LogService.LOG_INFO, "test - INFO");
        userAdmin.logMessage(this, LogService.LOG_WARNING, "test - WARNING");
        EasyMock.verify(role, logService);
    }

    @Test
    public void logNoServiceOk() {
        PaxUserAdmin userAdmin = createUserAdmin((LogService) null);
        userAdmin.logMessage(this, LogService.LOG_DEBUG, "test - DEBUG");
    }

    @Test
    public void fireEventNoServiceNoListenersOk() {
        LogService logService = EasyMock.createMock(LogService.class);
        Role role = EasyMock.createMock(Role.class);
        PaxUserAdmin userAdmin = createUserAdmin(EasyMock.createMock(StorageProvider.class), logService, (EventAdmin) null);
        EasyMock.expect(role.getName()).andReturn("some Name");
        logService.log(EasyMock.eq(LogService.LOG_DEBUG), EasyMock.isA(String.class));
        EasyMock.replay(role, logService);
        userAdmin.fireEvent(UserAdminEvent.ROLE_CHANGED, role);
        EasyMock.verify(role, logService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fireEventNullRole() {
        PaxUserAdmin userAdmin = createUserAdmin();
        userAdmin.fireEvent(UserAdminEvent.ROLE_CHANGED, null);
    }

    @Test
    public void fireEventServicePresentOk() {
        Role role = EasyMock.createMock(Role.class);
        LogService logService = EasyMock.createMock(LogService.class);
        EventAdmin eventAdmin = EasyMock.createMock(EventAdmin.class);
        PaxUserAdmin userAdmin = createUserAdmin(EasyMock.createMock(StorageProvider.class), logService, eventAdmin);
        EasyMock.expect(role.getName()).andReturn("some Name");
        EasyMock.expect(role.getType()).andReturn(Role.USER);
        eventAdmin.postEvent(EasyMock.isA(Event.class));
        EasyMock.replay(eventAdmin, role, logService);
        userAdmin.fireEvent(UserAdminEvent.ROLE_CHANGED, role);
        EasyMock.verify(eventAdmin, role, logService);
    }

    // TODO: this can be done now!

    //    @Test
    //    public void fireEventListenerPresentOk() {
    //
    //    }

    @Test
    public void createUserOk() {
        PaxUserAdmin userAdmin = createUserAdmin();
        User user = userAdmin.createUser("some name", null, null);
        Assert.assertNotNull("No user created", user);
        Assert.assertEquals("Type mismatch", user.getType(), Role.USER);
    }

    @Test
    public void createGroupOk() {
        PaxUserAdmin userAdmin = createUserAdmin();
        Group group = userAdmin.createGroup("some name", null, null);
        Assert.assertNotNull("No user created", group);
        Assert.assertEquals("Type mismatch", group.getType(), Role.GROUP);
    }

    @Test
    public void getStorageProviderNoService() {
        PaxUserAdmin userAdmin = createUserAdmin();
        try {
            userAdmin.getStorageProvider();
        } catch (StorageException e) {
            Assert.assertEquals("Exception message mismatch", UserAdminMessages.MSG_MISSING_STORAGE_SERVICE, e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void createUserRoleNoName() {
        PaxUserAdmin userAdmin = createUserAdmin();
        userAdmin.createRole(null, Role.ROLE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createUserRoleInvalidType() {
        PaxUserAdmin userAdmin = createUserAdmin();
        userAdmin.createRole(NAME1, Role.ROLE);
    }

    @Test
    public void createExistingUserRole() throws StorageException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        LogService log = EasyMock.createMock(LogService.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp, log);
        UserImpl role1 = new UserImpl(NAME1, userAdmin, null, null);
        EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(role1);
        log.log(LogService.LOG_INFO, "[" + PaxUserAdmin.class.getName() + "] createRole() - role already exists: " + NAME1);
        EasyMock.replay(sp, log);
        Assert.assertNull("Duplicate role created", userAdmin.createRole(NAME1, Role.USER));
        EasyMock.verify(sp, log);
    }

    @Test
    public void createUserRoleStorageException() throws StorageException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        LogService log = EasyMock.createMock(LogService.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp, log);
        StorageException exception = new StorageException("");
        EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(null);
        EasyMock.expect(sp.createUser(userAdmin, NAME1)).andThrow(exception);
        log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        EasyMock.replay(sp, log);
        Assert.assertNull("User created when exception was thrown", userAdmin.createRole(NAME1, Role.USER));
        EasyMock.verify(sp, log);
    }

    @Test
    public void createUserRoleOk() throws StorageException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        EventAdmin eventAdmin = EasyMock.createMock(EventAdmin.class);
        LogService log = EasyMock.createMock(LogService.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp, log, eventAdmin);
        UserImpl role = new UserImpl(NAME1, userAdmin, null, null);
        EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(null);
        EasyMock.expect(sp.createUser(userAdmin, NAME1)).andReturn(role);
        eventAdmin.postEvent(EasyMock.isA(Event.class));
        log.log(EasyMock.eq(LogService.LOG_INFO), EasyMock.isA(String.class));
        EasyMock.replay(sp, log, eventAdmin);
        Assert.assertNotNull("User not created", userAdmin.createRole(NAME1, Role.USER));
        EasyMock.verify(sp, log, eventAdmin);
    }

    @Test
    public void createGroupRoleOk() throws StorageException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        EventAdmin eventAdmin = EasyMock.createMock(EventAdmin.class);
        LogService log = EasyMock.createMock(LogService.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp, log, eventAdmin);
        GroupImpl group = new GroupImpl(NAME1, userAdmin, null, null);
        EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(null);
        EasyMock.expect(sp.createGroup(userAdmin, NAME1)).andReturn(group);
        eventAdmin.postEvent(EasyMock.isA(Event.class));
        log.log(EasyMock.eq(LogService.LOG_INFO), EasyMock.isA(String.class));
        EasyMock.replay(sp, eventAdmin, log);
        Assert.assertNotNull("Group not created", userAdmin.createRole(NAME1, Role.GROUP));
        EasyMock.verify(sp, eventAdmin, log);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeRoleNullName() {
        PaxUserAdmin userAdmin = createUserAdmin();
        userAdmin.removeRole(null);
    }

    @Test
    public void removeDefaultRole() {
        LogService log = EasyMock.createMock(LogService.class);
        PaxUserAdmin userAdmin = createUserAdmin(log);
        log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        EasyMock.replay(log);
        Assert.assertFalse(userAdmin.removeRole(""));
        Assert.assertFalse(userAdmin.removeRole(Role.USER_ANYONE));
        EasyMock.verify(log);
    }

    @Test
    public void removeRoleNotFound() throws StorageException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        LogService log = EasyMock.createMock(LogService.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp, log);
        EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(null);
        log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        //
        EasyMock.replay(sp, log);
        //
        Assert.assertFalse(userAdmin.removeRole(NAME1));
        //
        EasyMock.verify(sp);
    }

    @Test
    public void removeRoleNotDeleted() throws StorageException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        LogService log = EasyMock.createMock(LogService.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp, log);
        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(user);
        EasyMock.expect(sp.deleteRole(user)).andReturn(false);
        log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        EasyMock.replay(sp, log);
        //
        Assert.assertFalse(userAdmin.removeRole(NAME1));
        //
        EasyMock.verify(sp, log);
    }

    @Test
    public void removeRoleStorageException() throws StorageException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        LogService log = EasyMock.createMock(LogService.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp, log);
        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        StorageException exception = new StorageException("");
        EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(user);
        EasyMock.expect(sp.deleteRole(user)).andThrow(exception);
        log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        //
        EasyMock.replay(sp, log);
        //
        Assert.assertFalse(userAdmin.removeRole(NAME1));
        //
        EasyMock.verify(sp, log);
    }

    @Test
    public void removeRoleOk() throws StorageException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        EventAdmin eventAdmin = EasyMock.createMock(EventAdmin.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp, eventAdmin);

        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        EasyMock.expect(sp.getRole(userAdmin, NAME1)).andReturn(user);
        EasyMock.expect(sp.deleteRole(user)).andReturn(true);
        eventAdmin.postEvent(EasyMock.isA(Event.class));
        //
        EasyMock.replay(sp, eventAdmin);
        //
        Assert.assertTrue(userAdmin.removeRole(NAME1));
        //
        EasyMock.verify(sp, eventAdmin);
    }

    @Test
    public void getRolesNullFilter() throws StorageException, InvalidSyntaxException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp);
        // variant 1: no roles found
        EasyMock.expect(sp.findRoles(userAdmin, null)).andReturn(new ArrayList<Role>());
        EasyMock.replay(sp);
        Assert.assertNull("Empty result did not return null", userAdmin.getRoles(null));
        EasyMock.verify(sp);
        //
        // variant 2: one role found
        //
        EasyMock.reset(sp);
        UserImpl role = new UserImpl(NAME1, userAdmin, null, null);
        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(role);
        EasyMock.expect(sp.findRoles(userAdmin, null)).andReturn(roles);
        EasyMock.replay(sp);
        Role[] result = userAdmin.getRoles(null);
        Assert.assertNotNull("No roles found", result);
        Assert.assertEquals("Result size != 1", 1, result.length);
        EasyMock.verify(sp);
    }

    @Test
    public void getRolesStorageException() throws StorageException, InvalidSyntaxException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        StorageException exception = new StorageException("");
        LogService log = EasyMock.createMock(LogService.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp, log);
        EasyMock.expect(sp.findRoles(userAdmin, null)).andThrow(exception);
        log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        EasyMock.replay(sp, log);
        Assert.assertNull("Roles returned when exception occured", userAdmin.getRoles(null));
        EasyMock.verify(sp, log);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRoleNullName() {
        PaxUserAdmin userAdmin = createUserAdmin();
        userAdmin.getRole(null);
    }

    @Test
    public void getRoleEmptyName() throws StorageException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp);
        UserImpl user = new UserImpl(Role.USER_ANYONE, userAdmin, null, null);
        EasyMock.expect(sp.getRole(userAdmin, Role.USER_ANYONE)).andReturn(user);
        EasyMock.replay(sp);
        User result = (User) userAdmin.getRole("");
        Assert.assertNotNull("No user.anyone found", result);
        Assert.assertEquals("User name mismatch", Role.USER_ANYONE, result.getName());
        EasyMock.verify(sp);
    }

    @Test
    public void getRoleStorageException() throws StorageException {
        LogService log = EasyMock.createMock(LogService.class);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp, log);
        StorageException exception = new StorageException("");
        EasyMock.expect(sp.getRole(userAdmin, Role.USER_ANYONE)).andThrow(exception);
        log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        EasyMock.replay(sp, log);
        User result = (User) userAdmin.getRole("");
        Assert.assertNull("User found when exception was thrown", result);
        EasyMock.verify(sp, log);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getUserNullKey() {
        PaxUserAdmin userAdmin = createUserAdmin();
        userAdmin.getUser(null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getUserNullValue() {
        PaxUserAdmin userAdmin = createUserAdmin();
        userAdmin.getUser("", null);
    }

    @Test
    public void getUserStorageException() throws StorageException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        LogService log = EasyMock.createMock(LogService.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp, log);
        StorageException exception = new StorageException("");
        EasyMock.expect(sp.getUser(userAdmin, "", "")).andThrow(exception);
        log.log(EasyMock.eq(LogService.LOG_ERROR), EasyMock.isA(String.class));
        EasyMock.replay(sp, log);
        User result = userAdmin.getUser("", "");
        Assert.assertNull("User found when exception was thrown", result);
        EasyMock.verify(sp, log);
    }

    @Test
    public void getUserOk() throws StorageException {
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        PaxUserAdmin userAdmin = createUserAdmin(sp);
        UserImpl user = new UserImpl(Role.USER_ANYONE, userAdmin, null, null);
        EasyMock.expect(sp.getUser(userAdmin, "", "")).andReturn(user);
        EasyMock.replay(sp);
        User result = null;
        result = userAdmin.getUser("", "");
        Assert.assertNotNull("No user found", result);
        EasyMock.verify(sp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAuthorizationNullUser() {
        PaxUserAdmin userAdmin = createUserAdmin();
        Assert.assertNull("No Authorization created for <null> user", userAdmin.getAuthorization(null));
    }

    @Test
    public void getAuthorizationOk() {
        PaxUserAdmin userAdmin = createUserAdmin();
        UserImpl user = new UserImpl(NAME1, userAdmin, null, null);
        //TODO: shoudl it really be possible to create any user??
        Assert.assertNotNull("No authorization created for dummy user", userAdmin.getAuthorization(user));
    }

    private static PaxUserAdmin createUserAdmin() {
        return createUserAdmin(EasyMock.createMock(StorageProvider.class), null, null);
    }

    private static PaxUserAdmin createUserAdmin(StorageProvider storageProvider) {
        return createUserAdmin(storageProvider, null, null);
    }

    private static PaxUserAdmin createUserAdmin(LogService logService) {
        return createUserAdmin(EasyMock.createMock(StorageProvider.class), logService, null);
    }

    private static PaxUserAdmin createUserAdmin(StorageProvider storageProvider, LogService logService) {
        return createUserAdmin(storageProvider, logService, null);
    }

    private static PaxUserAdmin createUserAdmin(StorageProvider storageProvider, EventAdmin eventAdmin) {
        return createUserAdmin(storageProvider, null, eventAdmin);
    }

    /**
     * This method allows to create a {@link PaxUserAdmin} service for
     * testpurpose
     * 
     * @return
     */
    @SuppressWarnings({ "rawtypes" })
    private static PaxUserAdmin createUserAdmin(StorageProvider storageProvider, LogService logService, EventAdmin eventAdmin) {
        ServiceTracker<UserAdminListener, UserAdminListener> listenerTracker = EasyMock.createNiceMock(ServiceTracker.class);
        EasyMock.expect(listenerTracker.getService()).andStubReturn(null);
        EasyMock.expect(listenerTracker.getService(EasyMock.<ServiceReference<UserAdminListener>> anyObject())).andStubReturn(null);
        EasyMock.expect(listenerTracker.getServices()).andStubReturn(null);
        EasyMock.expect(listenerTracker.getServices(EasyMock.<UserAdminListener[]> anyObject())).andStubReturn(new UserAdminListener[0]);
        ServiceTracker<LogService, LogService> logServiceTracker = createTracker(logService);
        //Create a reference to the Eventadmin
        ServiceTracker<EventAdmin, EventAdmin> eventAdminTracker = createTracker(eventAdmin);
        PaxUserAdmin userAdmin = new PaxUserAdmin(storageProvider, logServiceTracker, eventAdminTracker, listenerTracker, Executors.newCachedThreadPool());
        //register it!
        BundleContext bundleContext = EasyMock.createNiceMock(BundleContext.class);
        ServiceRegistration serviceRegistration = EasyMock.createNiceMock(ServiceRegistration.class);
        ServiceReference serviceReference = EasyMock.createNiceMock(ServiceReference.class);
        EasyMock.expect(serviceRegistration.getReference()).andReturn(serviceReference);
        EasyMock.expect(serviceReference.getProperty(Constants.SERVICE_ID)).andReturn("1");
        EasyMock.expect(serviceReference.getProperty(Constants.OBJECTCLASS)).andReturn(UserAdmin.class.getName());
        EasyMock.expect(serviceReference.getProperty(Constants.SERVICE_PID)).andReturn(PaxUserAdminConstants.SERVICE_PID);
        EasyMock.expect(bundleContext.registerService(EasyMock.eq(UserAdmin.class), EasyMock.eq(userAdmin), EasyMock.<Dictionary<String, Object>> anyObject())).andReturn(serviceRegistration);
        EasyMock.replay(listenerTracker, logServiceTracker, eventAdminTracker, bundleContext, serviceRegistration, serviceReference);
        userAdmin.register(bundleContext, "junit", 0l);
        EasyMock.verify(bundleContext);
        EasyMock.reset(bundleContext);
        return userAdmin;
    }

    /**
     * @param logService
     * @return
     */
    private static <T> ServiceTracker<T, T> createTracker(T service) {
        ServiceTracker<T, T> tracker = EasyMock.createNiceMock(ServiceTracker.class);
        EasyMock.expect(tracker.getService()).andStubReturn(service);
        EasyMock.expect(tracker.getServices()).andStubReturn(new Object[] { service });
        return tracker;
    }
}
