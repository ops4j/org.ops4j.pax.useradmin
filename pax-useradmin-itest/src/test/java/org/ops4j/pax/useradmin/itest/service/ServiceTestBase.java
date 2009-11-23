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

package org.ops4j.pax.useradmin.itest.service;

import org.junit.Assert;
import org.ops4j.pax.useradmin.itest.UserAdminTestBase;
import org.ops4j.pax.useradmin.service.UserAdminConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.useradmin.UserAdmin;

/**
 * @author Matthias Kuespert
 * @since  23.11.2009
 */
public abstract class ServiceTestBase extends UserAdminTestBase {

    /** Abstract method to retrieve the type of StorageProvider the bundle provides.
     * 
     * @return An identifying string value.
     */
    protected abstract String getProviderType();

    /**
     * @return The <code>UserAdmin</code> service instance to test.
     */
    protected UserAdmin getUserAdmin() {
        BundleContext context = getBundleContext();
        try {
            for (ServiceReference reference : context.getServiceReferences(UserAdmin.class.getName(), null)) {
                String type = (String) reference.getProperty(UserAdminConstants.STORAGEPROVIDER_TYPE);
                if (null != type) {
                    if (type.equals(getProviderType())) {
                        return (UserAdmin) context.getService(reference);
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
        }
        Assert.fail("No UserAdmin service found for provider " + getProviderType());
        return null;
    }
}
