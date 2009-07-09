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

import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.exam.Inject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * @author Matthias Kuespert
 * @since  09.07.2009
 */
public class UserManagementTest {
    
    @Inject
    private BundleContext m_context = null;
    
    protected UserAdmin getUserAdmin() {
        ServiceReference ref = m_context.getServiceReference(UserAdmin.class.getName());
        Assert.assertNotNull("No UserAdmin service reference found", ref);
        UserAdmin userAdmin = (UserAdmin) m_context.getService(ref);
        Assert.assertNotNull("No UserAdmin service found", userAdmin);
        return userAdmin;
    }
    
//     @Test
    protected void testCreateUser() {
        
        System.out.println("------------- start test -----------------");
        UserAdmin userAdmin = getUserAdmin();

        User user = (User) userAdmin.createRole("jdeveloper", Role.USER);
        Assert.assertNotNull("Could not create user", user);

        
    }

}
