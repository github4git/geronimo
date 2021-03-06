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

package org.apache.geronimo.jetty.connector;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.jetty.JettyContainer;
import org.apache.geronimo.management.geronimo.WebManager;
import org.mortbay.http.SocketListener;

/**
 * @version $Rev$ $Date$
 */
public class HTTPConnector extends JettyConnector {
    public HTTPConnector(JettyContainer container) {
        super(container, new SocketListener());
    }

    public String getProtocol() {
        return WebManager.PROTOCOL_HTTP;
    }

    public int getDefaultPort() {
        return 80;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Jetty Connector HTTP", HTTPConnector.class, JettyConnector.GBEAN_INFO);
        infoFactory.setConstructor(new String[]{"JettyContainer"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
