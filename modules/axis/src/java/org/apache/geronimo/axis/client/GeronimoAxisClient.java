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
package org.apache.geronimo.axis.client;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerChain;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.MessageContext;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Call;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoAxisClient extends AxisClient {

    private final Map portNameToSEIFactoryMap;

    public GeronimoAxisClient(EngineConfiguration engineConfiguration, Map portNameToSEIFactoryMap) {
        super(engineConfiguration);
        this.portNameToSEIFactoryMap = portNameToSEIFactoryMap;
    }

    protected HandlerChain getJAXRPChandlerChain(MessageContext context) {

        QName portQName = (QName) context.getProperty(Call.WSDL_PORT_NAME);
        if(portQName == null) {
            return null;
        }
        String portName = portQName.getLocalPart();

        SEIFactory seiFactory = (SEIFactory) portNameToSEIFactoryMap.get(portName);
        if (seiFactory == null) {
            return null;
        }
        HandlerChain handlerChain = seiFactory.createHandlerChain();
        return handlerChain;

    }
}
