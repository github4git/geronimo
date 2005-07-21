/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.console.derbylogmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.console.util.KernelHelper;
import org.apache.geronimo.console.util.ObjectNameConstants;

public class DerbyLogHelper extends KernelHelper {
    private static ObjectName objName;

    private static ArrayList logs = new ArrayList();

    private static boolean cached = false;

    private static int lineCount = 0;

    private static final String DATE_PATTERN_REGEX = "2004";

    private static final String DERBY_SYSTEM_HOME = "derby.system.home";

    private static final String DERBY_SYS_HOME_PROP = "derbySystemHome";

    private static final String LOG_FILENAME = "derby.log";

    public static Collection getLogs() throws IOException {
        if (!cached) {
            refresh();
        }
        return logs;
    }

    public static void refresh() throws IOException {
        cached = false;
        logs.clear();
        BufferedReader in = new BufferedReader(getFileReader());
        lineCount = 0;
        Stack holder = new Stack();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            holder.push(line);
            lineCount++;
        }
        logs.addAll(holder);
        cached = true;
    }

    public static int getLineCount() {
        return lineCount;
    }

    private static InputStreamReader getFileReader() throws IOException {
        String pathToFile = getSystemHome() + File.separator + LOG_FILENAME;
        return new FileReader(new File(pathToFile));
    }

    public static String getSystemHome() {
        return System.getProperty(DERBY_SYSTEM_HOME);
    }

    static {
        try {
            objName = new ObjectName(ObjectNameConstants.DERBY_OBJECT_NAME);
        } catch (MalformedObjectNameException e) {

        }
    }

}
