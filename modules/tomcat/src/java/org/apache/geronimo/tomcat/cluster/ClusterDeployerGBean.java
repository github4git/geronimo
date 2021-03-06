/**
*
* Copyright 2003-2005 The Apache Software Foundation
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
package org.apache.geronimo.tomcat.cluster;

import java.util.Map;

import org.apache.catalina.cluster.ClusterDeployer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;

public class ClusterDeployerGBean  extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

    private static final Log log = LogFactory
            .getLog(ClusterDeployerGBean.class);

    public static final String J2EE_TYPE = "ClusterDeployer";

    protected final ClusterDeployer deployer;

    public ClusterDeployerGBean() {
       deployer = null; 
    }
    
    protected ClusterDeployerGBean(String className) throws Exception{
       super();     
       deployer = (ClusterDeployer)Class.forName(className).newInstance();
    }
    
    public ClusterDeployerGBean(String className, Map initParams) throws Exception {

        super(); // TODO: make it an attribute

        // Validate
        if (className == null) {
            throw new IllegalArgumentException(
                    "Must have a 'className' attribute.");
        }

        // Create the CatalinaCluster object
        deployer = (ClusterDeployer) Class.forName(className).newInstance();

        // Set the parameters
        setParameters(deployer, initParams);

    }

    public Object getInternalObject() {
        return deployer;
    }

    public void doFail() {
        log.warn("Failed: "+ deployer.getClass().getName());
    }

    public void doStart() throws Exception {
        log.debug("Started "+ deployer.getClass().getName() +" gbean.");
    }

    public void doStop() throws Exception {
        log.debug("Stopped " + deployer.getClass().getName() + " gbean.");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("ClusterDeployer", ClusterDeployerGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addOperation("getInternalObject");
        infoFactory.setConstructor(new String[] { "className", "initParams" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
