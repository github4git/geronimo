/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.system.serverinfo;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ServerInfoTest extends TestCase {

    public final void testResolvePath() {
        ServerInfo si = null;

        String pathArg;
        {
            si = new BasicServerInfo();
            pathArg = "/";
            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
            pathArg = "/x";
            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
            pathArg = "/x/y";
            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
            pathArg = "C:/Documents and Settings/Administrator/Application Data/geronimo";
            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));

            pathArg = ".";
            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
            pathArg = "x";
            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
            pathArg = "x/y";
            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
            pathArg = "Documents and Settings/Administrator/Application Data/geronimo";
            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
        }

        try {
            String basedir = "/";
            si = new BasicServerInfo(basedir);
            pathArg = "Documents and Settings/Administrator/Application Data/geronimo";
            assertEquals(new File(basedir, pathArg).getAbsolutePath(), si.resolvePath(pathArg));
        } catch (Exception e) {
            fail("ServerInfo ctor threw exception " + e);
        }

        //try {
        //    String basedir = File.listRoots()[0].getAbsolutePath();
        //    si = new ServerInfo(basedir);
        //    pathArg = "Documents and Settings/Administrator/Application Data/geronimo";
        //    assertEquals(new File(basedir, pathArg).getAbsolutePath(), si.resolvePath(pathArg));
        //} catch (Exception e) {
        //    fail("ServerInfo ctor threw exception " + e);
        //}
    }

    public final void testServerInfo() throws Exception {
		try {
			File file;
			try {
				file = File.createTempFile("geronimo", null);
				// a workaround - ServerInfo sets system-wide property
				System.setProperty("org.apache.geronimo.base.dir", file.getName());
				new BasicServerInfo(file.getName());
				fail("ServerInfo should throw exception when given non-directory path");
			} catch (IOException ioe) {
				fail(ioe.getMessage());
			} catch (Exception expected) {
			}

			String basedir = ".";
			// a workaround - ServerInfo sets system-wide property
			System.setProperty("org.apache.geronimo.base.dir", basedir);
			ServerInfo si = new BasicServerInfo(basedir);
			assertNotNull(System.getProperty("org.apache.geronimo.base.dir"));
			assertEquals("base directory is incorrect", basedir, si.getBaseDirectory());
		} finally {
			System.getProperties().remove("org.apache.geronimo.base.dir");
		}
    }
}
