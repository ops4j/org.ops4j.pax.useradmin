/**
 * Copyright 2012 OPS4J
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
package org.ops4j.pax.useradmin.service.spi;

import java.util.Collection;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * This interface adds the method {@link #isImpliedBy(SPIRole, Collection)} to
 * the default OSGi {@link Role} interface, which is required if SPI Provider
 * like to provide their own implementation of {@link User}s and {@link Group}s
 * instead of using the factory
 * 
 * @author Christoph Läubrich
 * @since 15.Jan.2012
 */
public interface SPIRole extends Role {

    /**
     * States used as return value for isImpliedBy() calls.
     */
    public enum ImplicationResult {
        /**
         * given role is implied by this one
         */
        IMPLIEDBY_YES,
        /**
         * given role is not implied by this one
         */
        IMPLIEDBY_NO,
        /**
         * detected a loop - e.g. a group containing itself.
         */
        IMPLIEDBY_LOOPDETECTED;
    };

    /**
     * Checks if this role is implied by the given one.
     * 
     * @param role
     *            The role to check.
     * @param checkedRoles
     *            Used for loop detection.
     * @return An <code>ImplicationResult</code>.
     */
    public ImplicationResult isImpliedBy(SPIRole role, Collection<String> checkedRoles);
}
