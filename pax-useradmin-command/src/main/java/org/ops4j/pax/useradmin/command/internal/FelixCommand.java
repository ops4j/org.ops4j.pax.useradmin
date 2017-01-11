/*
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

package org.ops4j.pax.useradmin.command.internal;

import java.io.PrintStream;

import org.apache.felix.shell.Command;
import org.ops4j.pax.useradmin.command.CommandException;
import org.osgi.framework.BundleContext;

public class FelixCommand implements Command {

    private BundleContext m_context = null;
    
    public FelixCommand(BundleContext context) {
        m_context = context;
    }

    /**
     * @see Command#execute(String, PrintStream, PrintStream)
     */
    public void execute(String commandLine, PrintStream out, PrintStream err) {
        String[] arguments = commandLine.split(" ");
        if (arguments.length <= 2) {
            System.out.println("Not enough arguments in commandline: " + commandLine);
            return;
        }
        if ("copyData".equals(arguments[1])) {
            if (arguments.length < 4) {
                System.out.println("userAdmin copyData needs two arguments.");
                return;
            }
            try {
                new UserAdminCommandImpl(m_context).copyData(arguments[2], arguments[3]);
            } catch (CommandException e) {
                err.println("CommandException caught: " + e.getMessage());
                e.printStackTrace(err);
                return;
            }
        } else {
            err.println("Unknown function for userAdmin command: " + arguments[1] + " in command-line " + commandLine);
            return;
        }
    }

    /**
     * @see Command#getName()
     */
    public String getName() {
        return "userAdmin";
    }

    /**
     * @see Command#getShortDescription()
     */
    public String getShortDescription() {
        return "UserAdmin related commands";
    }

    /**
     * @see Command#getUsage()
     */
    public String getUsage() {
        return "userAdmin copyData <source-uri> <target-uri>";
    }
}
