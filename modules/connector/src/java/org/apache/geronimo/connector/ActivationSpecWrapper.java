/**
 *
 * Copyright 2004-2005 The Apache Software Foundation
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

package org.apache.geronimo.connector;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.ResourceManager;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;

/**
 * Wrapper for ActivationSpec instances.
 * The framework assumes all RequiredConfigProperties are of type String, although it
 * is unclear if this is required by the spec.
 *
 * @version $Rev$ $Date$
 */
public class ActivationSpecWrapper implements ResourceManager {

    protected final ActivationSpec activationSpec;

    private final ResourceAdapterWrapper resourceAdapterWrapper;
    private final String containerId;

    /**
     * Default constructor required when a class is used as a GBean Endpoint.
     */
    public ActivationSpecWrapper() {
        activationSpec = null;
        containerId = null;
        resourceAdapterWrapper = null;
    }

    /**
     * Normal managed constructor.
     *
     * @param activationSpecClass Class of admin object to be wrapped.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public ActivationSpecWrapper(final String activationSpecClass,
                                 final String containerId,
                                 final ResourceAdapterWrapper resourceAdapterWrapper,
                                 final ClassLoader cl) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        Class clazz = cl.loadClass(activationSpecClass);
        this.activationSpec = (ActivationSpec) clazz.newInstance();
        this.containerId = containerId;
        this.resourceAdapterWrapper = resourceAdapterWrapper;
    }

    /**
     */
    public ActivationSpecWrapper(ActivationSpec activationSpec, ResourceAdapterWrapper resourceAdapterWrapper)  {
        this.activationSpec = activationSpec;
        this.resourceAdapterWrapper = resourceAdapterWrapper;
        this.containerId = null;
    }

    /**
     * Returns class of wrapped ActivationSpec.
     *
     * @return class of wrapped ActivationSpec
     */
//    public String getActivationSpecClass() {
//        return activationSpecClass;
//    }

    public String getContainerId() {
        return containerId;
    }

    public ResourceAdapterWrapper getResourceAdapterWrapper() {
        return resourceAdapterWrapper;
    }


    //GBeanLifecycle implementation
    public void activate(final MessageEndpointFactory messageEndpointFactory) throws ResourceException {
        ResourceAdapter resourceAdapter = activationSpec.getResourceAdapter();
        if (resourceAdapter == null) {
            resourceAdapterWrapper.registerResourceAdapterAssociation(activationSpec);
        }
        resourceAdapterWrapper.endpointActivation(messageEndpointFactory, activationSpec);
    }

    public void deactivate(final MessageEndpointFactory messageEndpointFactory) {
        ResourceAdapter resourceAdapter = activationSpec.getResourceAdapter();
        if (resourceAdapter != null) {
            resourceAdapterWrapper.endpointDeactivation(messageEndpointFactory, activationSpec);
        } else {
            //this should never happen, activation spec should have been registered with r.a.
            throw new IllegalStateException("ActivationSpec was never registered with ResourceAdapter");
        }
    }

    //Operations.
    public NamedXAResource getRecoveryXAResources() throws SystemException {
        if (resourceAdapterWrapper == null) {
            throw new IllegalStateException("Attempting to use activation spec when it is not activated");
        }
        try {
            XAResource[] xaResources = resourceAdapterWrapper.getXAResources(new ActivationSpec[]{activationSpec});
            if (xaResources == null || xaResources.length == 0) {
                return null;
            }
            return new WrapperNamedXAResource(xaResources[0], containerId);
        } catch (ResourceException e) {
            throw (SystemException) new SystemException("Could not get XAResource for recovery for mdb: " + containerId).initCause(e);
        }
    }

    public void returnResource(NamedXAResource xaResource) {
        //do nothing, no way to return anything.
    }

}
