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

package org.apache.geronimo.kernel.log;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.LogFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoLogging {

    //this needs to go before the instance constants or you get an NPE in the constructor.
    private static final Map levels = new HashMap();

    public static final GeronimoLogging TRACE = new GeronimoLogging("TRACE");
    public static final GeronimoLogging DEBUG = new GeronimoLogging("DEBUG");
    public static final GeronimoLogging INFO = new GeronimoLogging("INFO");
    public static final GeronimoLogging WARN = new GeronimoLogging("WARN");
    public static final GeronimoLogging ERROR = new GeronimoLogging("ERROR");
    public static final GeronimoLogging FATAL = new GeronimoLogging("FATAL");

    private static boolean initialized = false;
    private static GeronimoLogging consoleLogLevel = ERROR;
    private static GeronimoLogging defaultLevel;

    /**
     * Initializes the logging system used by Geronimo.  This MUST be called in
     * in the main class used to start the geronimo server.  This method forces
     * commons logging to use GeronimoLogFactory, starts the initial commons-logging
     * logging system, and forces mx4j to use commons logging.
     */
    public static void initialize(GeronimoLogging level) {
        if (!initialized) {
            defaultLevel = level;
            consoleLogLevel = level;

            // force the log factory to initialize
            LogFactory.getLog(GeronimoLogging.class);

            // force mx4j to use commons logging
            // Use reflection so mx4j is not required (this is important in JDK 1.5)
            // mx4j.log.Log.redirectTo(new mx4j.log.CommonsLogger());
            try {
                Class clazz = Class.forName("mx4j.log.Log");
                Class paramClazz = Class.forName("mx4j.log.Logger");
                Method method = clazz.getDeclaredMethod("redirectTo", new Class[] {paramClazz});
                paramClazz = Class.forName("mx4j.log.CommonsLogger");
                method.invoke(null, new Object[] {paramClazz.newInstance()});
            } catch (ClassNotFoundException e) {
                // MX4J is not present.
            } catch (Exception e) {
                throw (AssertionError) new AssertionError("Cannot force MX4J to use commons logging.").initCause(e);
            }

            initialized = true;
        }
    }

    public static void setDefaultLogLevel(GeronimoLogging level) {
        defaultLevel = level;
    }

    public static GeronimoLogging getDefaultLevel() {
        return defaultLevel;
    }

    public static GeronimoLogging getConsoleLogLevel() {
        return consoleLogLevel;
    }

    public static void setConsoleLogLevel(GeronimoLogging consoleLogLevel) {
        GeronimoLogging.consoleLogLevel = consoleLogLevel;
    }

    public static GeronimoLogging getGeronimoLogging(String level) {
        return (GeronimoLogging) levels.get(level);
    }

    private final String level;

    private GeronimoLogging(String level) {
        this.level = level;
        levels.put(level, this);
    }

    public String toString() {
        return level;
    }

    /**
     * Check if the Geronimo bootstrap logging initialization is enabled.
     *
     * <p>Checks the system property <tt>geronimo.bootstrap.logging.enabled</tt>
     * if not set, or set to "true" then bootstrap logging initialization is enabled.
     *
     * @return  True of bootstrap logging initialization is enabled.
     */
    public static boolean isBootstrapLoggingInitializationEnabled() {
        String value = System.getProperty("geronimo.bootstrap.logging.enabled");
        if (value == null) {
            return true;
        }
        else {
            return Boolean.valueOf(value).booleanValue();
        }
    }
}
