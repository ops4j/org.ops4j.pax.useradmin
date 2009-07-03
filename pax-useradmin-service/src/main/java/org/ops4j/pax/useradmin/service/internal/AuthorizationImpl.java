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

import org.osgi.service.useradmin.Authorization;

/**
 * Implementation of the Authorization interface.
 * 
 * @author Matthias Kuespert
 * @since  02.07.2009
 */
public class AuthorizationImpl implements Authorization {

	private String m_name = null;
	private String[] m_roles = null;
	
	protected AuthorizationImpl(String name, String[] roles) {
	    m_name = name;
	    m_roles = roles;
	}
	
	public String getName() {
		return m_name;
	}

	public String[] getRoles() {
		return m_roles;
	}

	public boolean hasRole(String name) {
		if (null != m_roles) {
			for (String role : m_roles) {
				if (role.equals(name)) {
					return true;
				}
			}
		}
		return false;
	}
}
