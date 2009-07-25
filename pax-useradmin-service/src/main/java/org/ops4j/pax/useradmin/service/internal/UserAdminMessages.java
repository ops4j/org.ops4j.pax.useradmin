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

/**
 * Message texts which are used more than once.
 * 
 * @author Matthias Kuespert
 * @since  02.07.2009
 */
public class UserAdminMessages {

	public static String MSG_INVALID_NAME = "The 'name' parameter is not allowed to be null";
	public static String MSG_INVALID_ROLE_TYPE = "Only GROUP or USER is allowed as role type";
	public static String MSG_MISSING_STORAGE_SERVICE = "No StorageProvider service available ... check your setup";
	
	public static String MSG_INVALID_KEY = "The 'key' parameter must not be null";
    public static String MSG_EMPTY_KEY = "The 'key' parameter must not be empty";
	public static String MSG_INVALID_KEY_TYPE = "Only String is allowed as key for role properties";
    public static String MSG_INVALID_VALUE = "The 'value' parameter must not be null";
	public static String MSG_INVALID_VALUE_TYPE = "Only String or byte[] is allowed as value for role properties";
    public static String MSG_INVALID_ROLE = "The 'role' parameter must not be null";
    public static String MSG_INVALID_FILTER = "The 'filter' parameter must not be null";
    public static String MSG_INVALID_USER = "The 'user' parameter must not be null";
	public static String MSG_INVALID_USERADMIN = "The 'userAdmin' parameter must not be null";
	public static String MSG_INVALID_STORAGE = "The 'storageProvider' parameter must not be null";

}
