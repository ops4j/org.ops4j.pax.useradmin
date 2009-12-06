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

package org.ops4j.pax.useradmin.webservice;

/**
 * @author Matthias Kuespert
 * @since  31.08.2009
 */
public interface UserInformationService {

    /**
     * Authenticates a user for a given role using the specified password.
     * 
     * @param name The user name.
     * @param password The passowrd as MD5 encrypted char array.
     * @param role The role wich autentication is asked for.
     * @return True if the given user is authenticated for the given role - false otherwise.
     */
    boolean authenticate(String name, byte[] password, String role);

    /**
     * Authenticates a user using the specified password and returns all roles this user is authorized to.
     * 
     * @param name The user name.
     * @param password The passowrd as MD5 encrypted char array.
     * @return An array of role names that the user is authorized to.
     */
    String[] authenticate(String name, byte[] password);
    
    /**
     * Returns all user names.
     * 
     * @return An array of user names.
     */
    String getUsers();
}
