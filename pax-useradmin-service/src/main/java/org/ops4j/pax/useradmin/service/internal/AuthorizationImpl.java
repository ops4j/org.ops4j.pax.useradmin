package org.ops4j.pax.useradmin.service.internal;

import org.osgi.service.useradmin.Authorization;

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
