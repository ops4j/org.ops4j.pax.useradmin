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

package org.ops4j.pax.useradmin;

import java.io.File;

/**
 * Static utility methods.
 * 
 * TODO: find a better name and place for this code.
 * 
 * @author Matthias Kuespert
 * @since  07.08.2009
 */
public class Utilities {
    
    public static void delete(File root, boolean deleteRoot) {
        if (root.isDirectory()) {
            for (File f : root.listFiles()) {
                delete(f, true);
            }
        }
        if (deleteRoot) {
            root.delete();
        }
    }
}
