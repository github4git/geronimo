/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.activemq.management;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geronimo.activemq.ActiveMQBroker;
import org.apache.geronimo.activemq.ActiveMQConnector;
import org.apache.geronimo.activemq.ActiveMQManager;
import org.apache.geronimo.activemq.BrokerServiceGBean;
//import org.apache.geronimo.activemq.TransportConnectorGBeanImpl;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
//import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.EditableConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.management.geronimo.JMSBroker;
import org.apache.geronimo.management.geronimo.JMSConnector;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.activemq.broker.TransportConnector;

/**
 * Implementation of the ActiveMQ management interface.  These are the ActiveMQ
 * management features available at runtime.
 *
 * @version $Rev$ $Date$
 */
@GBean
public class ActiveMQManagerGBean implements ActiveMQManager {
    private static final Logger log = LoggerFactory.getLogger(ActiveMQManagerGBean.class);
    private Kernel kernel;
    private String objectName;

    public ActiveMQManagerGBean(@ParamSpecial(type= SpecialAttributeType.kernel) Kernel kernel,
                                @ParamSpecial(type = SpecialAttributeType.objectName) String objectName) {
        this.kernel = kernel;
        this.objectName = objectName;
    }

    public String getProductName() {
        return "ActiveMQ";
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isEventProvider() {
        return false;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public Object[] getContainers() {
        AbstractNameQuery query = new AbstractNameQuery(ActiveMQBroker.class.getName());
        Set<AbstractName> names = kernel.listGBeans(query);
        ActiveMQBroker[] results = new ActiveMQBroker[names.size()];
        int i=0;
        for (AbstractName name: names) {
            try {
                results[i] = (ActiveMQBroker) kernel.getGBean(name);
            } catch (GBeanNotFoundException e) {
                log.info("broker not found", e);
            }
        }
        return results;
    }

    public String[] getSupportedProtocols() {
        // see files in modules/core/src/conf/META-INF/services/org/activemq/transport/server/
        return new String[]{};// "tcp", "stomp", "vm", "peer", "udp", "multicast", "failover"};
    }

    public NetworkConnector[] getConnectors() {
        List<NetworkConnector> connectors = getConnectorsList();
        return connectors.toArray(new NetworkConnector[] {});
    }

    private List<NetworkConnector> getConnectorsList() {
        List<NetworkConnector> connectors = new ArrayList<NetworkConnector>();
        ActiveMQBroker[] brokers = (ActiveMQBroker[]) getContainers();
        for (ActiveMQBroker broker: brokers) {
            connectors.addAll(getConnectorListForContainer((BrokerServiceGBean) broker));
        }
        return connectors;
    }

    public NetworkConnector[] getConnectors(String protocol) {
        if(protocol == null) {
            return getConnectors();
        }
        List<NetworkConnector> connectors = getConnectorsList();
        filterConnectorsByProtocol(protocol, connectors);
        return connectors.toArray(new NetworkConnector[] {});
    }

    private void filterConnectorsByProtocol(String protocol, List<NetworkConnector> connectors) {
        for (Iterator<NetworkConnector> connectorIterator = connectors.iterator(); connectorIterator.hasNext();) {
            if (protocol.equals(connectorIterator.next().getProtocol())) {
                connectorIterator.remove();
            }
        }
    }

    public NetworkConnector[] getConnectorsForContainer(Object genericBroker) {

        return getConnectorListForContainer((BrokerServiceGBean) genericBroker).toArray(new NetworkConnector[] {});
    }

    private List<NetworkConnector> getConnectorListForContainer(BrokerServiceGBean broker) {
        List<TransportConnector> transportConnectors = broker.getBrokerContainer().getTransportConnectors();
        List<NetworkConnector> connectors = new ArrayList<NetworkConnector>();
        for (TransportConnector transportConnector: transportConnectors) {
            connectors.add(new ActiveMQTransportConnector(transportConnector));
        }
        return connectors;
    }

    public NetworkConnector[] getConnectorsForContainer(Object broker, String protocol) {
        if(protocol == null) {
            return getConnectorsForContainer(broker);
        }
        List<NetworkConnector> connectors = getConnectorListForContainer((BrokerServiceGBean) broker);
        filterConnectorsByProtocol(protocol, connectors);
        return connectors.toArray(new NetworkConnector[] {});
    }

    /**
     * Returns a new JMSConnector.  Note that
     * the connector may well require further customization before being fully
     * functional (e.g. SSL settings for a secure connector).
     */
    public JMSConnector addConnector(JMSBroker broker, String uniqueName, String protocol, String host, int port) {
        throw new RuntimeException("not implemented");
    }

    public void removeConnector(AbstractName connectorName) {
        throw new RuntimeException("not implemented");
   }

}
