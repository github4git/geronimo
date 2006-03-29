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

package org.apache.geronimo.kernel.config;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;

import javax.management.ObjectName;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The standard non-editable ConfigurationManager implementation.  That is,
 * you can save a lost configurations and stuff, but not change the set of
 * GBeans included in a configuration.
 *
 * @version $Rev:386276 $ $Date$
 * @see EditableConfigurationManager
 */
public class KernelConfigurationManager extends SimpleConfigurationManager implements GBeanLifecycle {

    protected final Kernel kernel;
    protected final ManageableAttributeStore attributeStore;
    protected final PersistentConfigurationList configurationList;
    private final ArtifactManager artifactManager;
    protected final ClassLoader classLoader;
    private final ShutdownHook shutdownHook;

    public KernelConfigurationManager(Kernel kernel,
            Collection stores,
            ManageableAttributeStore attributeStore,
            PersistentConfigurationList configurationList,
            ArtifactManager artifactManager,
            ArtifactResolver artifactResolver,
            Collection repositories,
            ClassLoader classLoader) {

        super(stores,
                createArtifactResolver(artifactResolver, artifactManager, repositories),
                kernel == null? null: kernel.getNaming(),
                repositories);

        this.kernel = kernel;
        this.attributeStore = attributeStore;
        this.configurationList = configurationList;
        this.artifactManager = artifactManager;
        this.classLoader = classLoader;

        shutdownHook = new ShutdownHook(kernel);
    }

    private static ArtifactResolver createArtifactResolver(ArtifactResolver artifactResolver, ArtifactManager artifactManager, Collection repositories) {
        if (artifactResolver != null) {
            return artifactResolver;
        }
        return new DefaultArtifactResolver(artifactManager, repositories);
    }

    public synchronized Configuration loadConfiguration(Artifact configurationId) throws NoSuchConfigException, IOException, InvalidConfigException {
        // todo hack for bootstrap deploy
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus == null && kernel.isLoaded(Configuration.getConfigurationAbstractName(configurationId))) {
            try {
                Configuration configuration = (Configuration) kernel.getGBean(Configuration.getConfigurationAbstractName(configurationId));
                configurationStatus = createConfigurationStatus(configuration);
                configurationStatus.load();
                //even worse hack
                configurationStatus.start();
                configurations.put(configurationId, configurationStatus);
                return configurationStatus.getConfiguration();
            } catch (GBeanNotFoundException e) {
                // configuration was unloaded, just continue as normal
            }
        }

        return super.loadConfiguration(configurationId);
    }

    protected Configuration load(GBeanData configurationData, Map loadedConfigurations) throws InvalidConfigException {
        Artifact configurationId = getConfigurationId(configurationData);
        AbstractName configurationName = Configuration.getConfigurationAbstractName(configurationId);
        // load the configuration
        try {
            kernel.loadGBean(configurationData, classLoader);
        } catch (GBeanAlreadyExistsException e) {
            throw new InvalidConfigException("Unable to load configuration gbean " + configurationId, e);
        }

        // start the configuration and assure it started
        Configuration configuration;
        try {
            kernel.startGBean(configurationName);
            if (State.RUNNING_INDEX != kernel.getGBeanState(configurationName)) {
                throw new InvalidConfigurationException("Configuration gbean failed to start " + configurationId);
            }

            // get the configuration
            configuration = (Configuration) kernel.getGBean(configurationName);

            // declare the dependencies as loaded
            if (artifactManager != null) {
                artifactManager.loadArtifacts(configurationId, configuration.getDependencies());
            }

            log.debug("Loaded Configuration " + configurationName);
        } catch (Exception e) {
            unload(configurationId);
            if (e instanceof InvalidConfigException) {
                throw (InvalidConfigException) e;
            }
            throw new InvalidConfigException("Error starting configuration gbean " + configurationId, e);
        }
        return configuration;
    }

    public void start(Configuration configuration) throws InvalidConfigException {
        // load the attribute overrides from the attribute store
        Collection gbeans = configuration.getGBeans().values();
        if (attributeStore != null) {
            gbeans = attributeStore.applyOverrides(getConfigurationId(configuration), gbeans, configuration.getConfigurationClassLoader());
        }

        ConfigurationUtil.startConfigurationGBeans(gbeans, configuration, kernel);

        if (configurationList != null) {
            configurationList.addConfiguration(getConfigurationId(configuration).toString());
        }
    }

    protected void stop(Configuration configuration) throws InvalidConfigException {
        try {
            Collection gbeans = configuration.getGBeans().values();

            // stop the gbeans
            for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
                GBeanData gbeanData = (GBeanData) iterator.next();
                AbstractName gbeanName = gbeanData.getAbstractName();
                kernel.stopGBean(gbeanName);
            }

            for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
                GBeanData gbeanData = (GBeanData) iterator.next();
                AbstractName gbeanName = gbeanData.getAbstractName();
                kernel.unloadGBean(gbeanName);
            }
        } catch (Exception e) {
            throw new InvalidConfigException("Could not stop gbeans in configuration", e);
        }
        if (configurationList != null) {
            configurationList.removeConfiguration(getConfigurationId(configuration).toString());
        }
    }

    protected void unload(Configuration configuration) {
        Artifact configurationId = getConfigurationId(configuration);
        unload(configurationId);
    }

    private void unload(Artifact configurationId) {
        AbstractName configurationName;
        try {
            configurationName = Configuration.getConfigurationAbstractName(configurationId);
        } catch (InvalidConfigException e) {
            throw new AssertionError(e);
        }

        // unload this configuration
        try {
            kernel.stopGBean(configurationName);
        } catch (GBeanNotFoundException ignored) {
            // Good
        } catch (Exception stopException) {
            log.warn("Unable to stop failed configuration: " + configurationId, stopException);
        }

        try {
            kernel.unloadGBean(configurationName);
        } catch (GBeanNotFoundException ignored) {
            // Good
        } catch (Exception unloadException) {
            log.warn("Unable to unload failed configuration: " + configurationId, unloadException);
        }
    }

    public void doStart() {
        kernel.registerShutdownHook(shutdownHook);
    }

    public void doStop() {
        kernel.unregisterShutdownHook(shutdownHook);
    }

    public void doFail() {
        log.error("Cofiguration manager failed");
    }

    private static class ShutdownHook implements Runnable {
        private static final ObjectName CONFIG_QUERY = JMXUtil.getObjectName("geronimo.config:*");
        private final Kernel kernel;

        public ShutdownHook(Kernel kernel) {
            this.kernel = kernel;
        }

        public void run() {
            while (true) {
                Set configs = kernel.listGBeans(new AbstractNameQuery(Configuration.class.getName()));
                if (configs.isEmpty()) {
                    return;
                }
                for (Iterator i = configs.iterator(); i.hasNext();) {
                    AbstractName configName = (AbstractName) i.next();
                    if (kernel.isLoaded(configName)) {
                        try {
                            kernel.stopGBean(configName);
                        } catch (GBeanNotFoundException e) {
                            // ignore
                        } catch (InternalKernelException e) {
                            log.warn("Could not stop configuration: " + configName, e);
                        }
                        try {
                            kernel.unloadGBean(configName);
                        } catch (GBeanNotFoundException e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(KernelConfigurationManager.class, "ConfigurationManager");
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addReference("Stores", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addReference("AttributeStore", ManageableAttributeStore.class, ManageableAttributeStore.ATTRIBUTE_STORE);
        infoFactory.addReference("PersistentConfigurationList", PersistentConfigurationList.class, PersistentConfigurationList.PERSISTENT_CONFIGURATION_LIST);
        infoFactory.addReference("ArtifactManager", ArtifactManager.class, "ArtifactManager");
        infoFactory.addReference("ArtifactResolver", ArtifactResolver.class, "ArtifactResolver");
        infoFactory.addReference("Repositories", Repository.class, "Repository");
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addInterface(ConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"kernel", "Stores", "AttributeStore", "PersistentConfigurationList", "ArtifactManager", "ArtifactResolver", "Repositories", "classLoader"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
