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

package org.ops4j.pax.useradmin.itest.command;

import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;

import org.apache.felix.shell.Command;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.useradmin.itest.service.CopyFilesEnvironmentCustomizer;
import org.osgi.framework.BundleContext;

/**
 * @author Matthias Kuespert
 * @since  06.08.2009
 */
@RunWith(JUnit4TestRunner.class)
public class CopyTest extends CommandTestBase {

    private static final String FILE_INPUT_RESOURCE = "UserAdminTestData.xml";
    private static final String FILE_INPUT_XML = "./UserAdminTestData.xml";
    private static final String FILE_OUTPUT_XML = "./out.xml";
    private static final String FILE_OUTPUT_CHECK_XML = "./out-check.xml";
    
    @Inject
    private BundleContext m_context;

    protected BundleContext getBundleContext() {
        return m_context;
    };

    // TODO: run the command programmatically via its service interface - that's
    // all we can do here, since the interactive shell must be tested, hmm,
    // interactively ;-)
    //
    // However, if the shell provides scripting we should try that too
    

    @Configuration
    public static Option[] configure() {
        return options(new CopyFilesEnvironmentCustomizer().sourceDir("src/test/resources")
                                                           .sourceFilter(FILE_INPUT_RESOURCE),
                       getBasicFrameworkConfiguration());
    }

    @Test
    public void xml2XmlOk() {
        Command command = getCommand("userAdmin");
        Assert.assertNotNull("Command not found", command);
        File out = new File(FILE_OUTPUT_XML);
        if (out.exists()) {
            out.delete();
        }
        command.execute("userAdmin copyData file://" + FILE_INPUT_XML + " file://" + FILE_OUTPUT_XML, System.out, System.out);
        Assert.assertTrue("No " + FILE_OUTPUT_XML + " generated", out.exists());
        Assert.assertTrue("Empty " + FILE_OUTPUT_XML + " generated", out.length() > 0);
        //
        // copy a second time to get the right order ... TODO: check if we need some sorting ...
        //
        File outCheck = new File(FILE_OUTPUT_CHECK_XML);
        command.execute("userAdmin copyData file://" + FILE_OUTPUT_XML + " file://" + FILE_OUTPUT_CHECK_XML, System.out, System.out);
        Assert.assertTrue("No " + FILE_OUTPUT_CHECK_XML + " generated", outCheck.exists());
        Assert.assertTrue("Empty " + FILE_OUTPUT_CHECK_XML + " generated", outCheck.length() > 0);
        //
        // TODO: compare files
//        Assert.assertEquals("Input and output differ in size", new File(FILE_INPUT_XML).length(), out.length());
        Assert.assertEquals("Output and output-check differ in size", out.length(), outCheck.length());
    }

    @Test
    public void xml2ServiceOk() {
        Command command = getCommand("userAdmin");
        Assert.assertNotNull("Command not found", command);
        command.execute("userAdmin copyData file://" + FILE_INPUT_XML + " userAdmin://org.ops4j.pax.useradmin.pax-useradmin-service", System.out, System.out);
//        Assert.assertTrue("No " + FILE_OUTPUT_XML + " generated", out.exists());
//        Assert.assertTrue("Empty " + FILE_OUTPUT_XML + " generated", out.length() > 0);
    }

    @Test
    public void service2XmlOk() {
        Command command = getCommand("userAdmin");
        Assert.assertNotNull("Command not found", command);
        File out = new File(FILE_OUTPUT_XML);
        if (out.exists()) {
            out.delete();
        }
        command.execute("userAdmin copyData userAdmin://org.ops4j.pax.useradmin.pax-useradmin-service file://" + FILE_OUTPUT_XML, System.out, System.out);
    }

    @Test
    public void xml2Service2xmlOk() {
        Command command = getCommand("userAdmin");
        Assert.assertNotNull("Command not found", command);
        command.execute("userAdmin copyData file://" + FILE_INPUT_XML + " userAdmin://org.ops4j.pax.useradmin.pax-useradmin-service", System.out, System.out);
        //
        File out = new File(FILE_OUTPUT_XML);
        if (out.exists()) {
            out.delete();
        }
        command.execute("userAdmin copyData userAdmin://org.ops4j.pax.useradmin.pax-useradmin-service file://" + FILE_OUTPUT_XML, System.out, System.out);
        Assert.assertTrue("No " + FILE_OUTPUT_XML + " generated", out.exists());
        Assert.assertTrue("Empty " + FILE_OUTPUT_XML + " generated", out.length() > 0);
//        Assert.assertEquals("Input and output differ in size", new File(FILE_INPUT_XML).length(), out.length());
    }
}
