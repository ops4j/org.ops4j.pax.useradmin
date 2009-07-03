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
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.useradmin.UserAdmin;

/**
 * Activator which starts the UserAdmin service.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class Activator implements BundleActivator {

    /**
     * Stored ServiceReference of the UserAdmin service.
     */
    private ServiceReference m_userAdminReference = null;

    /**
     * @see BundleActivator#start(BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(Constants.SERVICE_PID, UserAdminImpl.PID);
        ServiceRegistration reg = context.registerService(UserAdmin.class.getName(),
                                                          new UserAdminImpl(context), properties);
        m_userAdminReference = reg.getReference();
    }

    /**
     * @see BundleActivator#stop(BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        if (null != m_userAdminReference) {
            context.ungetService(m_userAdminReference);
            m_userAdminReference = null;
        }
    }
}
