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
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.easymock.classextension.EasyMock;
import org.junit.Test;
import org.ops4j.pax.useradmin.service.internal.RoleImpl.ImplicationResult;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.UserAdminEvent;
import org.osgi.service.useradmin.UserAdminPermission;

/**
 * Testing the UserImpl and UserCredentials classes.
 * 
 * Note: only successful calls are tested here - failures of the AbstractProperties class are
 *       tested in the RoleImplTest class 
 * 
 * @author Matthias Kuespert
 * @since  11.07.2009
 */
public class UserImplTest {

    private static final String USER_NAME1 = "someUser";
    private static final String USER_NAME2 = "someOtherUser";
    
    private static final String KEY1       = "testCredential1";
    private static final String VALUE1     = "someCredentialValue1";
    private static final String KEY2       = "testCredential2";
    private static final byte[] VALUE2     = "someCredentialValue2".getBytes();
    
    private Map<String, Object> getCredentials() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY1, VALUE1.getBytes());
        properties.put(KEY2, VALUE2);
        return properties;
    }

    @Test
    public void constructNullCredentials() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, null);
        Assert.assertNotNull("Could not create UserImpl instance", user);
        Assert.assertEquals("Mismatching name", USER_NAME1, user.getName());
        Assert.assertEquals("Invalid type", Role.USER, user.getType());
        Assert.assertEquals("Invalid UserAdmin instance", userAdmin, user.getAdmin());
        Assert.assertNotNull(user.getCredentials());
        Assert.assertEquals("Too many initial credentials", 0, user.getCredentials().size());
    }

    @Test
    public void constructEmptyCredentials() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, new HashMap<String, Object>());
        Assert.assertNotNull("Could not create UserImpl instance", user);
        Assert.assertEquals("Mismatching name", USER_NAME1, user.getName());
        Assert.assertEquals("Invalid type", Role.USER, user.getType());
        Assert.assertEquals("Invalid UserAdmin instance", userAdmin, user.getAdmin());
        Assert.assertNotNull(user.getCredentials());
        Assert.assertEquals("Too many initial credentials", 0, user.getCredentials().size());
    }

    @Test
    @SuppressWarnings(value = "unchecked")
    public void constructOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, getCredentials());
        Assert.assertNotNull("Could not create RoleImpl instance", user);
        Assert.assertEquals("Mismatching name", USER_NAME1, user.getName());
        Assert.assertEquals("Invalid type", Role.USER, user.getType());
        Assert.assertEquals("Invalid UserAdmin instance", userAdmin, user.getAdmin());
        Dictionary credentials = user.getCredentials(); 
        Assert.assertNotNull(credentials);
        Assert.assertEquals("Mismatching property count", 2, credentials.size());
        Assert.assertTrue("Mismatching property", Arrays.equals(VALUE1.getBytes(), (byte[])credentials.get(KEY1)));
    }

    @Test
    public void getCredentialOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, getCredentials());
        userAdmin.checkPermission("testCredential1", "getCredential");
        userAdmin.checkPermission("testCredential2", "getCredential");

        EasyMock.replay(userAdmin);
        //
        Assert.assertTrue("Mismatching value", Arrays.equals(VALUE1.getBytes(), (byte[])user.getCredentials().get(KEY1)));
        Assert.assertTrue("Mismatching value", Arrays.equals(VALUE2, (byte[])user.getCredentials().get(KEY2)));
        //
        EasyMock.verify(userAdmin);
    }
    
    @Test
    @SuppressWarnings(value = "unchecked")
    public void addCredentialStorageException() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        //
        StorageException exception = new StorageException("");
        try {
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            userAdmin.checkPermission(KEY1, UserAdminPermission.CHANGE_CREDENTIAL);
            EasyMock.expect(userAdmin.encrypt(VALUE1)).andReturn(VALUE1.getBytes());
            // System.out.println("enc 2 - " + VALUE1 + " -- " + VALUE1.getBytes());
            // doesn't work: sp.setUserCredential(user, KEY1, VALUE1.getBytes());
            sp.setUserCredential(EasyMock.eq(user), EasyMock.eq(KEY1), EasyMock.isA(byte[].class));
            EasyMock.expectLastCall().andThrow(exception);
            userAdmin.logMessage(EasyMock.isA(AbstractProperties.class),
                                 EasyMock.eq(LogService.LOG_ERROR),
                                 EasyMock.matches(exception.getMessage()));
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            userAdmin.checkPermission(KEY2, UserAdminPermission.CHANGE_CREDENTIAL);
            EasyMock.expect(userAdmin.encrypt(VALUE2)).andReturn(VALUE2);
            // doesn't work:  sp.setUserCredential(user, KEY2, VALUE2);
            sp.setUserCredential(EasyMock.eq(user), EasyMock.eq(KEY2), EasyMock.isA(byte[].class));
            EasyMock.expectLastCall().andThrow(exception);
            userAdmin.logMessage(EasyMock.isA(AbstractProperties.class),
                                 EasyMock.eq(LogService.LOG_ERROR),
                                 EasyMock.matches(exception.getMessage()));
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertNull("Setting credential did return some previous value", user.getCredentials().put(KEY1, VALUE1));
        Assert.assertNull("Setting credential did return some previous value", user.getCredentials().put(KEY2, VALUE2));
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    @SuppressWarnings(value = "unchecked")
    public void addCredentialOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        //
        try {
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            userAdmin.checkPermission(KEY1, UserAdminPermission.CHANGE_CREDENTIAL);
            EasyMock.expect(userAdmin.encrypt(VALUE1)).andReturn(VALUE1.getBytes());
            // sp.setUserCredential(user, KEY1, VALUE1);
            sp.setUserCredential(EasyMock.eq(user), EasyMock.eq(KEY1), EasyMock.isA(byte[].class));
            userAdmin.fireEvent(UserAdminEvent.ROLE_CHANGED, user);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            userAdmin.checkPermission(KEY2, UserAdminPermission.CHANGE_CREDENTIAL);
            EasyMock.expect(userAdmin.encrypt(VALUE2)).andReturn(VALUE2);
            // sp.setUserCredential(user, KEY2, VALUE2);
            sp.setUserCredential(EasyMock.eq(user), EasyMock.eq(KEY2), EasyMock.isA(byte[].class));
            userAdmin.fireEvent(UserAdminEvent.ROLE_CHANGED, user);
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertNull("Setting credential did return some previous value", user.getCredentials().put(KEY1, VALUE1));
        Assert.assertNull("Setting credential did return some previous value", user.getCredentials().put(KEY2, VALUE2));
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void removeCredentialStorageException() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, getCredentials());
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        //
        StorageException exception = new StorageException("");
        try {
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            userAdmin.checkPermission(KEY1, UserAdminPermission.CHANGE_CREDENTIAL);
            sp.removeUserCredential(user, KEY1);
            EasyMock.expectLastCall().andThrow(exception);
            userAdmin.logMessage(EasyMock.isA(AbstractProperties.class),
                                 EasyMock.eq(LogService.LOG_ERROR),
                                 EasyMock.matches(exception.getMessage()));
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            userAdmin.checkPermission(KEY2, UserAdminPermission.CHANGE_CREDENTIAL);
            sp.removeUserCredential(user, KEY2);
            EasyMock.expectLastCall().andThrow(exception);
            userAdmin.logMessage(EasyMock.isA(AbstractProperties.class),
                                 EasyMock.eq(LogService.LOG_ERROR),
                                 EasyMock.matches(exception.getMessage()));
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertNull("Removing credential did return some previous value", user.getCredentials().remove(KEY1));
        Assert.assertNull("Removing credential did return some previous value", user.getCredentials().remove(KEY2));
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void removeCredentialOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, getCredentials());
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        //
        try {
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            sp.removeUserCredential(user, KEY1);
            userAdmin.checkPermission(KEY1, UserAdminPermission.CHANGE_CREDENTIAL);
            userAdmin.fireEvent(UserAdminEvent.ROLE_CHANGED, user);
            //
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            sp.removeUserCredential(user, KEY2);
            userAdmin.checkPermission(KEY2, UserAdminPermission.CHANGE_CREDENTIAL);
            userAdmin.fireEvent(UserAdminEvent.ROLE_CHANGED, user);
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
//        Assert.assertNull("Removing credential did return some previous value", user.getCredentials().remove(KEY1));
//        Assert.assertNull("Removing credential did return some previous value", user.getCredentials().remove(KEY2));
        user.getCredentials().remove(KEY1);
        user.getCredentials().remove(KEY2);
        //
        EasyMock.verify(userAdmin, sp);
    }

    // Note: a test for the clear() method is not needed since the Dictionary
    // class does not provide a clear method
    
    @Test (expected = IllegalArgumentException.class)
    public void hasCredentialNullKey() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, getCredentials());
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        //
        userAdmin.checkAdminPermission();
        //
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertFalse(user.hasCredential(null, VALUE1));
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test (expected = IllegalArgumentException.class)
    public void hasCredentialNullValue() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, getCredentials());
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        //
        userAdmin.checkAdminPermission();
        //
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertFalse(user.hasCredential(KEY1, null));
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test (expected = IllegalArgumentException.class)
    public void hasCredentialWrongValueType() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, getCredentials());
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        //
        userAdmin.checkAdminPermission();
        //
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertFalse(user.hasCredential(KEY1, 666));
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void hasCredentialOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(USER_NAME1, userAdmin, null, getCredentials());
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        //
        userAdmin.checkAdminPermission();
        userAdmin.checkPermission(KEY1, UserAdminPermission.GET_CREDENTIAL);
        EasyMock.expect(userAdmin.compareToEncryptedValue(EasyMock.isA(byte[].class),
                                                          EasyMock.isA(byte[].class)))
                .andReturn(true);
        userAdmin.checkAdminPermission();
        userAdmin.checkPermission(KEY2, UserAdminPermission.GET_CREDENTIAL);
        EasyMock.expect(userAdmin.compareToEncryptedValue(EasyMock.isA(byte[].class),
                                                          EasyMock.isA(byte[].class)))
                .andReturn(true);
        //
        EasyMock.replay(userAdmin, sp);
        //
        Assert.assertTrue(user.hasCredential(KEY1, VALUE1.getBytes()));
        Assert.assertTrue(user.hasCredential(KEY2, VALUE2));
        //
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void impliedByOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user1 = new UserImpl(USER_NAME1, userAdmin, null, null);
        UserImpl user2 = new UserImpl(USER_NAME2, userAdmin, null, null);
        //
        EasyMock.replay(userAdmin);
        //
        Collection<String> checkedRoles = new ArrayList<String>();
        Assert.assertEquals("User not implied by itself", ImplicationResult.IMPLIEDBY_YES,
                            user1.isImpliedBy(user1, checkedRoles));
        checkedRoles.clear();
        checkedRoles.add(USER_NAME1);
        Assert.assertEquals("Implication loop not detected",
                            ImplicationResult.IMPLIEDBY_LOOPDETECTED,
                            user1.isImpliedBy(user1, checkedRoles));
        checkedRoles.clear();
        Assert.assertEquals("User implies unexpected user",
                            ImplicationResult.IMPLIEDBY_NO,
                            user1.isImpliedBy(user2, checkedRoles));
        //
        EasyMock.verify(userAdmin);
    }
}
