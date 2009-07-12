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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.easymock.classextension.EasyMock;
import org.junit.Test;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.UserAdminEvent;
import org.osgi.service.useradmin.UserAdminPermission;

/**
 * Testing the UserImpl and UserCredentials classes.
 * 
 * Note: only successful calls are tested here - failures are tested in the RoleImplTest class 
 * 
 * @author Matthias Kuespert
 * @since  11.07.2009
 */
public class UserImplTest {

    private static final String NAME = "someUser";
    private static final String KEY1 = "testCredential1";
    private static final String VALUE1 = "someCredentialValue1";
    
    private Map<String, String> getCredentials() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(KEY1, VALUE1);
        return properties;
    }

    @Test
    public void constructNullCredentials() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(NAME, userAdmin, null, null);
        Assert.assertNotNull("Could not create UserImpl instance", user);
        Assert.assertEquals("Mismatching name", NAME, user.getName());
        Assert.assertEquals("Invalid type", Role.USER, user.getType());
        Assert.assertEquals("Invalid UserAdmin instance", userAdmin, user.getAdmin());
        Assert.assertNotNull(user.getCredentials());
        Assert.assertEquals("Too many initial credentials", 0, user.getCredentials().size());
    }

    @Test
    public void constructEmptyCredentials() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(NAME, userAdmin, null, new HashMap<String, String>());
        Assert.assertNotNull("Could not create UserImpl instance", user);
        Assert.assertEquals("Mismatching name", NAME, user.getName());
        Assert.assertEquals("Invalid type", Role.USER, user.getType());
        Assert.assertEquals("Invalid UserAdmin instance", userAdmin, user.getAdmin());
        Assert.assertNotNull(user.getCredentials());
        Assert.assertEquals("Too many initial credentials", 0, user.getCredentials().size());
    }

    @Test
    @SuppressWarnings(value = "unchecked")
    public void constructOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(NAME, userAdmin, null, getCredentials());
        Assert.assertNotNull("Could not create RoleImpl instance", user);
        Assert.assertEquals("Mismatching name", NAME, user.getName());
        Assert.assertEquals("Invalid type", Role.USER, user.getType());
        Assert.assertEquals("Invalid UserAdmin instance", userAdmin, user.getAdmin());
        Dictionary credentials = user.getCredentials(); 
        Assert.assertEquals("Mismatching property count", 1, credentials.size());
        Assert.assertEquals("Mismatching property", VALUE1, credentials.get(KEY1));
    }

    @Test
    public void getCredentialOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(NAME, userAdmin, null, getCredentials());
        userAdmin.checkPermission("testCredential1", "getCredential");

        EasyMock.replay(userAdmin);
        //
        Assert.assertEquals("Mismatching value", VALUE1, user.getCredentials().get(KEY1));
        EasyMock.verify(userAdmin);
    }
    
    @Test
    @SuppressWarnings(value = "unchecked")
    public void addCredentialOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(NAME, userAdmin, null, null);
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        //
        try {
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            sp.setUserCredential(user, KEY1, VALUE1);
            userAdmin.checkPermission(KEY1, UserAdminPermission.CHANGE_CREDENTIAL);
            userAdmin.fireEvent(UserAdminEvent.ROLE_CHANGED, user);
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        user.getCredentials().put(KEY1, VALUE1);
        EasyMock.verify(userAdmin, sp);
    }

    @Test
    public void removeCredentialOk() {
        UserAdminImpl userAdmin = EasyMock.createMock(UserAdminImpl.class);
        UserImpl user = new UserImpl(NAME, userAdmin, null, getCredentials());
        StorageProvider sp = EasyMock.createMock(StorageProvider.class);
        //
        try {
            EasyMock.expect(userAdmin.getStorageProvider()).andReturn(sp);
            sp.removeUserCredential(user, KEY1);
            userAdmin.checkPermission(KEY1, UserAdminPermission.CHANGE_CREDENTIAL);
            userAdmin.fireEvent(UserAdminEvent.ROLE_CHANGED, user);
        } catch (StorageException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        EasyMock.replay(userAdmin, sp);
        //
        user.getCredentials().remove(KEY1);
        EasyMock.verify(userAdmin, sp);
    }

    // Note: a test for the clear() method is not needed since the Dictionary
    // class does not provide a clear method
}
