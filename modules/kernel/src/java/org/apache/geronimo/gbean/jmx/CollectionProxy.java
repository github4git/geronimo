/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.gbean.jmx;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.WaitingException;

/**
 *
 *
 * @version $Revision: 1.11 $ $Date: 2004/01/26 06:50:46 $
 */
public class CollectionProxy implements Proxy {
    private static final Log log = LogFactory.getLog(CollectionProxy.class);

    /**
     * The GBeanMBean to which this proxy belongs.
     */
    private GBeanMBean gmbean;

    /**
     * Name of the reference
     */
    public String name;

    /**
     * A map from object names to the proxy
     */
    private Map proxies = new HashMap();

    /**
     * A map from object names to the proxy interceptor
     */
    private Map interceptors = new HashMap();

    /**
     * Proxy collection implementation held by the component
     */
    private ClientCollection proxy = new ClientCollection();

    /**
     * Facotry for proxy instances.
     */
    private ProxyFactory factory;

    /**
     * Is this proxy currently stopped?
     */
    private boolean stopped;

    public CollectionProxy(GBeanMBean gmbean, String name, Class type) {
        this.gmbean = gmbean;
        this.name = name;
        factory = new ProxyFactory(type);
    }

    public synchronized void destroy() {
        for (Iterator iterator = interceptors.values().iterator(); iterator.hasNext();) {
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) iterator.next();
            interceptor.disconnect();
        }
        proxy.listeners = null;
        gmbean = null;
        name = null;
        proxies = null;
        interceptors = null;
        proxy = null;
        factory = null;
        stopped = true;
    }

    public synchronized Object getProxy() {
        return proxy;
    }

    public synchronized Set getTargets() {
        return Collections.unmodifiableSet(proxies.keySet());
    }

    public synchronized void addTarget(ObjectName target) {
        // if this is a new target...
        if (!proxies.containsKey(target)) {
            try {
                ProxyMethodInterceptor interceptor = new ProxyMethodInterceptor(factory.getType());
                interceptor.connect(gmbean.getServer(), target, proxy.isStopped());
                interceptors.put(target, interceptor);
                proxies.put(target, factory.create(interceptor));
                if (!stopped) {
                    proxy.fireMemberAdddedEvent(target);
                }
            } catch (InvocationTargetException e) {
                log.info("Could not create optional proxy to mbean: objectName=" + target);

            }
        }
    }

    public synchronized void removeTarget(ObjectName target) {
        Object targetProxy = proxies.remove(target);
        if (targetProxy != null) {
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) interceptors.remove(target);
            if (interceptor != null) {
                interceptor.disconnect();
            }
            if (!stopped) {
                proxy.fireMemberRemovedEvent(targetProxy);
            }
        }
    }

    public synchronized void start() throws WaitingException {
        stopped = false;
        for (Iterator iterator = interceptors.values().iterator(); iterator.hasNext();) {
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) iterator.next();
            interceptor.start();
        }
    }

    public synchronized void stop() {
        stopped = true;
        for (Iterator iterator = interceptors.values().iterator(); iterator.hasNext();) {
            ProxyMethodInterceptor interceptor = (ProxyMethodInterceptor) iterator.next();
            interceptor.stop();
        }
    }

    private class ClientCollection implements ReferenceCollection {
        private Set listeners = new HashSet();

        public boolean isStopped() {
            synchronized (CollectionProxy.this) {
                return stopped;
            }
        }

        public void addReferenceCollectionListener(ReferenceCollectionListener listener) {
            synchronized (CollectionProxy.this) {
                listeners.add(listener);
            }
        }

        public void removeReferenceCollectionListener(ReferenceCollectionListener listener) {
            synchronized (CollectionProxy.this) {
                listeners.remove(listener);
            }
        }

        private void fireMemberAdddedEvent(Object member) {
            ArrayList listenerCopy;
            synchronized (CollectionProxy.this) {
                listenerCopy = new ArrayList(listeners);
            }
            for (Iterator iterator = listenerCopy.iterator(); iterator.hasNext();) {
                ReferenceCollectionListener listener = (ReferenceCollectionListener) iterator.next();
                try {
                    listener.memberAdded(new ReferenceCollectionEvent(name, member));
                } catch (Throwable t) {
                    log.error("Listener threw exception", t);
                }
            }
        }

        private void fireMemberRemovedEvent(Object member) {
            ArrayList listenerCopy;
            synchronized (CollectionProxy.this) {
                listenerCopy = new ArrayList(listeners);
            }
            for (Iterator iterator = listenerCopy.iterator(); iterator.hasNext();) {
                ReferenceCollectionListener listener = (ReferenceCollectionListener) iterator.next();
                try {
                    listener.memberRemoved(new ReferenceCollectionEvent(name, member));
                } catch (Throwable t) {
                    log.error("Listener threw exception", t);
                }
            }
        }

        public int size() {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    return 0;
                }
                return proxies.size();
            }
        }

        public boolean isEmpty() {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    return true;
                }
                return proxies.isEmpty();
            }
        }

        public boolean contains(Object o) {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    return false;
                }
                return proxies.containsValue(o);
            }
        }

        public Iterator iterator() {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    return new Iterator() {
                        public boolean hasNext() {
                            return false;
                        }

                        public Object next() {
                            throw new NoSuchElementException();
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                return new Iterator() {
                    // copy the proxies, so the client can iterate without concurrent modification
                    // this is necssary since the client has nothing to synchronize on
                    private final Iterator iterator = new ArrayList(proxies.values()).iterator();

                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    public Object next() {
                        return iterator.next();
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }

        public Object[] toArray() {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    return new Object[0];
                }
                return proxies.values().toArray();
            }
        }

        public Object[] toArray(Object a[]) {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    if (a.length > 0) {
                        a[0] = null;
                    }
                    return a;
                }
                return proxies.values().toArray(a);
            }
        }

        public boolean containsAll(Collection c) {
            synchronized (CollectionProxy.this) {
                if (stopped) {
                    return c.isEmpty();
                }
                return proxies.values().containsAll(c);
            }
        }

        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }
    }
}
