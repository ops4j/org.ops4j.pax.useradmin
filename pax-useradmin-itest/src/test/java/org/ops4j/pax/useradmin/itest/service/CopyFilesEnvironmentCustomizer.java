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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.ops4j.pax.exam.options.ExecutionCustomizer;

/**
 * @author Matthias Kuespert
 * @since 10.09.2009
 */
public class CopyFilesEnvironmentCustomizer extends ExecutionCustomizer {

    private String m_sourceFilter = "";
    
    private String m_sourcePath = "";

    private String m_targetPath = "";
    
    public CopyFilesEnvironmentCustomizer() {
    }
    
    public CopyFilesEnvironmentCustomizer sourceFilter(String filter) {
        m_sourceFilter = filter;
        return this;
    }

    public CopyFilesEnvironmentCustomizer sourceDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException("sourceDir does not exist: " + path);
        }
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("sourceDir is not a directory: " + path);
        }
        m_sourcePath = path;
        return this;
    }

    public CopyFilesEnvironmentCustomizer targetDir(String path) {
        m_targetPath = path;
        return this;
    }

    
    public static void copyFile(File srcFile, File tgtDir) {
        System.err.println(" ------ cp " + srcFile.getAbsolutePath() + " to " + tgtDir.getAbsolutePath());
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(srcFile));
        } catch (FileNotFoundException e) {
            throw new IllegalAccessError("Could not access file: " + srcFile.getName());
        }
        if (!tgtDir.exists()) {
            tgtDir.mkdirs();
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(  tgtDir.getPath()
                                                                      + "/" + srcFile.getName()));
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
            throw new IllegalAccessError(  "Error copying resource " + srcFile.getAbsolutePath() + " to "
                                         + tgtDir.getPath() + " - " + e.getMessage());
        }
    }
    
    @Override
    public void customizeEnvironment(File workingDir) {
//         System.err.println("----------------------- working dir is: " + workingDir.getAbsolutePath());
//         System.err.println("----------------------- current dir is: " + new File(".").getAbsolutePath());
        String[] srcFiles = new File("./" + m_sourcePath).list(new FilenameFilter() {
                                                                    public boolean accept(File dir, String name) {
                                                                            return name.matches(m_sourceFilter);
                                                                    }
                                                               });
        for (String name : srcFiles) {
            File srcFile = new File(m_sourcePath + "/" + name);
            copyFile(srcFile, new File(workingDir + "/" + m_targetPath));
        }
    }
}
