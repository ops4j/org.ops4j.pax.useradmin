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

package org.ops4j.pax.useradmin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.ops4j.pax.useradmin.itest.ldap.FrameworkConfiguration;

/**
 * @author Matthias Kuespert
 * @since  07.08.2009
 */
public class TestUtil {
    
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
    
    public static void copyResourceToFile(String resourcePath, File directory) {
        InputStream is = FrameworkConfiguration.class.getResourceAsStream(resourcePath);
        Assert.assertNotNull("Could not load file: " + resourcePath, is);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(directory.getPath()
                                                                      + resourcePath));
            while (true) {
                String line = reader.readLine();
                if (null == line)
                    break;
                writer.append(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(  "Error copying resource " + resourcePath + " to "
                        + directory.getPath() + " - " + e.getMessage());
        }
    }
    
}
