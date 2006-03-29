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

package org.apache.geronimo.kernel;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.extensions.TestSetup;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.proxy.ProxyFactory;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.jmx.KernelDelegate;
import org.apache.geronimo.kernel.jmx.MBeanServerDelegate;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @version $Rev:386515 $ $Date$
 */
public class GBeanTest extends TestCase {
    private static KernelWraper kernelWraper = new KernelWraper();
    private Kernel realKernel;
    private Kernel kernel;

    /**
     * Test with plain unwrapped kernel
     */
    private static class KernelWraper {
        public Kernel wrap(Kernel kernel) {
            return kernel;
        }
    }

    /**
     * Test with kernel wrapped in KernelGBean
     */
    private static class GBeanKernelLifecycle extends KernelWraper {
        public Kernel wrap(Kernel kernel) {
            return new KernelGBean(kernel);
        }
    }

    /**
     * Test with kernel wrapped in KernelDelegate
     */
    private static class JmxKernelLifecycle extends KernelWraper {
        public Kernel wrap(Kernel kernel) {
            MBeanServerDelegate mbeanServer = new MBeanServerDelegate(kernel);
            return new KernelDelegate(mbeanServer);
        }
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        TestSuite inner = new TestSuite(GBeanTest.class);

        suite.addTest(new TestSetup(inner) {
            protected void setUp() throws Exception {
                super.setUp();
                kernelWraper = new KernelWraper();
            }
        });

        suite.addTest(new TestSetup(inner) {
            protected void setUp() throws Exception {
                super.setUp();
                kernelWraper = new GBeanKernelLifecycle();
            }
        });

        suite.addTest(new TestSetup(inner) {
            protected void setUp() throws Exception {
                super.setUp();
                kernelWraper = new JmxKernelLifecycle();
            }
        });
        return suite;
    }

    public void testListGBeans() throws Exception {
        GBeanData gbean = buildGBeanData("name", "test", MockGBean.getGBeanInfo());

        kernel.loadGBean(gbean, getClass().getClassLoader());
        kernel.startGBean(gbean.getAbstractName());
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean.getAbstractName()));

        Set gbeans = kernel.listGBeans(new AbstractNameQuery(gbean.getAbstractName(), gbean.getGBeanInfo().getInterfaces()));
        assertEquals(1, gbeans.size());
        assertEquals(gbean.getAbstractName(), gbeans.iterator().next());
    }

    public void testLifeCycle() throws Exception {
        GBeanData gbean = buildGBeanData("name", "test", MockGBean.getGBeanInfo());
        gbean.setAttribute("finalInt", new Integer(123));

        // abstract name
        assertFalse(kernel.isLoaded(gbean.getAbstractName()));
        assertFalse(kernel.isRunning(gbean.getAbstractName()));

        kernel.loadGBean(gbean, getClass().getClassLoader());
        assertTrue(kernel.isLoaded(gbean.getAbstractName()));
        assertFalse(kernel.isRunning(gbean.getAbstractName()));

        kernel.startGBean(gbean.getAbstractName());
        assertTrue(kernel.isLoaded(gbean.getAbstractName()));
        assertTrue(kernel.isRunning(gbean.getAbstractName()));

        kernel.stopGBean(gbean.getAbstractName());
        assertTrue(kernel.isLoaded(gbean.getAbstractName()));
        assertFalse(kernel.isRunning(gbean.getAbstractName()));

        kernel.unloadGBean(gbean.getAbstractName());
        assertFalse(kernel.isLoaded(gbean.getAbstractName()));
        assertFalse(kernel.isRunning(gbean.getAbstractName()));


        // short name
        assertFalse(kernel.isLoaded("name"));
        assertFalse(kernel.isRunning("name"));

        kernel.loadGBean(gbean, getClass().getClassLoader());
        assertTrue(kernel.isLoaded("name"));
        assertFalse(kernel.isRunning("name"));

        kernel.startGBean("name");
        assertTrue(kernel.isLoaded("name"));
        assertTrue(kernel.isRunning("name"));

        kernel.stopGBean("name");
        assertTrue(kernel.isLoaded("name"));
        assertFalse(kernel.isRunning("name"));

        kernel.unloadGBean("name");
        assertFalse(kernel.isLoaded("name"));
        assertFalse(kernel.isRunning("name"));


        // type
        assertFalse(kernel.isLoaded(MockGBean.class));
        assertFalse(kernel.isRunning(MockGBean.class));

        kernel.loadGBean(gbean, getClass().getClassLoader());
        assertTrue(kernel.isLoaded(MockGBean.class));
        assertFalse(kernel.isRunning(MockGBean.class));

        kernel.startGBean(MockGBean.class);
        assertTrue(kernel.isLoaded(MockGBean.class));
        assertTrue(kernel.isRunning(MockGBean.class));

        kernel.stopGBean(MockGBean.class);
        assertTrue(kernel.isLoaded(MockGBean.class));
        assertFalse(kernel.isRunning(MockGBean.class));

        kernel.unloadGBean(MockGBean.class);
        assertFalse(kernel.isLoaded(MockGBean.class));
        assertFalse(kernel.isRunning(MockGBean.class));


        // short name and type
        assertFalse(kernel.isLoaded("name", MockGBean.class));
        assertFalse(kernel.isRunning("name", MockGBean.class));

        kernel.loadGBean(gbean, getClass().getClassLoader());
        assertTrue(kernel.isLoaded("name", MockGBean.class));
        assertFalse(kernel.isRunning("name", MockGBean.class));

        kernel.startGBean("name", MockGBean.class);
        assertTrue(kernel.isLoaded("name", MockGBean.class));
        assertTrue(kernel.isRunning("name", MockGBean.class));

        kernel.stopGBean("name", MockGBean.class);
        assertTrue(kernel.isLoaded("name", MockGBean.class));
        assertFalse(kernel.isRunning("name", MockGBean.class));

        kernel.unloadGBean("name", MockGBean.class);
        assertFalse(kernel.isLoaded("name", MockGBean.class));
        assertFalse(kernel.isRunning("name", MockGBean.class));
    }

    public void testGetGBean() throws Exception {
        GBeanData gbean = buildGBeanData("name", "test", MockGBean.getGBeanInfo());
        gbean.setAttribute("finalInt", new Integer(123));

        kernel.loadGBean(gbean, getClass().getClassLoader());
        kernel.startGBean(gbean.getAbstractName());
        assertTrue(kernel.isRunning(gbean.getAbstractName()));

        // abstract name
        MockGBean mockGBean = (MockGBean) kernel.getGBean(gbean.getAbstractName());
        assertEquals(123, mockGBean.getFinalInt());
        assertNull(kernel.getAbstractNameFor(mockGBean));

        // short name
        mockGBean = (MockGBean) kernel.getGBean("name");
        assertEquals(123, mockGBean.getFinalInt());
        assertNull(kernel.getAbstractNameFor(mockGBean));

        // type
        mockGBean = (MockGBean) kernel.getGBean(MockGBean.class);
        assertEquals(123, mockGBean.getFinalInt());
        assertNull(kernel.getAbstractNameFor(mockGBean));

        // short name and type
        mockGBean = (MockGBean) kernel.getGBean("name", MockGBean.class);
        assertEquals(123, mockGBean.getFinalInt());
        assertNull(kernel.getAbstractNameFor(mockGBean));
    }

    public void testInvoke() throws Exception {
        GBeanData gbean = buildGBeanData("name", "test", MockGBean.getGBeanInfo());
        String testValue = "test-value";
        String otherValue = "other-value";
        gbean.setAttribute("value", testValue);

        kernel.loadGBean(gbean, getClass().getClassLoader());
        kernel.startGBean(gbean.getAbstractName());
        assertTrue(kernel.isRunning(gbean.getAbstractName()));
        MockGBean mockGBean = (MockGBean) kernel.getGBean(gbean.getAbstractName());

        //
        // abstract name
        //

        // get and set on gbean
        assertEquals(testValue, mockGBean.getValue());
        mockGBean.setValue(otherValue);
        assertEquals(otherValue, mockGBean.getValue());
        mockGBean.setValue(testValue);

        // get and set with kernel
        assertEquals(testValue, kernel.getAttribute(gbean.getAbstractName(), "value"));
        kernel.setAttribute(gbean.getAbstractName(), "value", otherValue);
        assertEquals(otherValue, kernel.getAttribute(gbean.getAbstractName(), "value"));
        kernel.setAttribute(gbean.getAbstractName(), "value", testValue);
        assertEquals(testValue, kernel.getAttribute(gbean.getAbstractName(), "value"));

        // invoke
        assertEquals(testValue, mockGBean.fetchValue());
        assertEquals(testValue, kernel.invoke(gbean.getAbstractName(), "fetchValue"));
        assertEquals(testValue, mockGBean.doSomething(testValue));
        assertEquals(testValue, kernel.invoke(gbean.getAbstractName(), "doSomething", new Object[] {testValue}, new String[] {String.class.getName()}));
        assertEquals(testValue, mockGBean.getValue());

        //
        // short name
        //

        // get and set on gbean
        assertEquals(testValue, mockGBean.getValue());
        mockGBean.setValue(otherValue);
        assertEquals(otherValue, mockGBean.getValue());
        mockGBean.setValue(testValue);

        // get and set with kernel
        assertEquals(testValue, kernel.getAttribute("name", "value"));
        kernel.setAttribute("name", "value", otherValue);
        assertEquals(otherValue, kernel.getAttribute("name", "value"));
        kernel.setAttribute("name", "value", testValue);
        assertEquals(testValue, kernel.getAttribute("name", "value"));

        // invoke
        assertEquals(testValue, mockGBean.fetchValue());
        assertEquals(testValue, kernel.invoke("name", "fetchValue"));
        assertEquals(testValue, mockGBean.doSomething(testValue));
        assertEquals(testValue, kernel.invoke("name", "doSomething", new Object[] {testValue}, new String[] {String.class.getName()}));
        assertEquals(testValue, mockGBean.getValue());

        //
        // type
        //

        // get and set on gbean
        assertEquals(testValue, mockGBean.getValue());
        mockGBean.setValue(otherValue);
        assertEquals(otherValue, mockGBean.getValue());
        mockGBean.setValue(testValue);

        // get and set with kernel
        assertEquals(testValue, kernel.getAttribute(MockGBean.class, "value"));
        kernel.setAttribute(MockGBean.class, "value", otherValue);
        assertEquals(otherValue, kernel.getAttribute(MockGBean.class, "value"));
        kernel.setAttribute(MockGBean.class, "value", testValue);
        assertEquals(testValue, kernel.getAttribute(MockGBean.class, "value"));

        // invoke
        assertEquals(testValue, mockGBean.fetchValue());
        assertEquals(testValue, kernel.invoke(MockGBean.class, "fetchValue"));
        assertEquals(testValue, mockGBean.doSomething(testValue));
        assertEquals(testValue, kernel.invoke(MockGBean.class, "doSomething", new Object[] {testValue}, new String[] {String.class.getName()}));
        assertEquals(testValue, mockGBean.getValue());

        //
        // short name and type
        //

        // get and set on gbean
        assertEquals(testValue, mockGBean.getValue());
        mockGBean.setValue(otherValue);
        assertEquals(otherValue, mockGBean.getValue());
        mockGBean.setValue(testValue);

        // get and set with kernel
        assertEquals(testValue, kernel.getAttribute("name", MockGBean.class, "value"));
        kernel.setAttribute("name", MockGBean.class, "value", otherValue);
        assertEquals(otherValue, kernel.getAttribute("name", MockGBean.class, "value"));
        kernel.setAttribute("name", MockGBean.class, "value", testValue);
        assertEquals(testValue, kernel.getAttribute("name", MockGBean.class, "value"));

        // invoke
        assertEquals(testValue, mockGBean.fetchValue());
        assertEquals(testValue, kernel.invoke("name", MockGBean.class, "fetchValue"));
        assertEquals(testValue, mockGBean.doSomething(testValue));
        assertEquals(testValue, kernel.invoke("name", MockGBean.class, "doSomething", new Object[] {testValue}, new String[] {String.class.getName()}));
        assertEquals(testValue, mockGBean.getValue());

    }

    public void testLoad() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        GBeanData gbean = buildGBeanData("name", "test", MockGBean.getGBeanInfo());
        gbean.setAttribute("name", "Test");
        gbean.setAttribute("finalInt", new Integer(123));

        kernel.loadGBean(gbean, myCl);
        kernel.startGBean(gbean.getAbstractName());
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean.getAbstractName()));
        assertEquals("Hello", kernel.invoke(gbean.getAbstractName(), "doSomething", new Object[]{"Hello"}, new String[]{String.class.getName()}));

        assertEquals(gbean.getAbstractName().getObjectName().getCanonicalName(), kernel.getAttribute(gbean.getAbstractName(), "objectName"));
        assertEquals(gbean.getAbstractName().getObjectName().getCanonicalName(), kernel.getAttribute(gbean.getAbstractName(), "actualObjectName"));

        assertSame(myCl, kernel.getAttribute(gbean.getAbstractName(), "actualClassLoader"));

        // the MockGBean implemmentation of getConfigurationClassLoader will throw an exception, but since the GBean architecture
        // handles this directly the implementation method will never be called
        kernel.getAttribute(gbean.getAbstractName(), "classLoader");

        assertSame(realKernel, kernel.getAttribute(gbean.getAbstractName(), "kernel"));
        assertSame(realKernel, kernel.getAttribute(gbean.getAbstractName(), "actualKernel"));

        kernel.stopGBean(gbean.getAbstractName());
        kernel.unloadGBean(gbean.getAbstractName());
    }

    public void testEndpoint() throws Exception {
        ClassLoader cl = MockGBean.class.getClassLoader();
        GBeanData gbean1 = buildGBeanData("name", "test", MockGBean.getGBeanInfo());
        gbean1.setAttribute("finalInt", new Integer(123));
        kernel.loadGBean(gbean1, cl);
        kernel.startGBean(gbean1.getAbstractName());

        GBeanData gbean2 = buildGBeanData("name", "test2", MockGBean.getGBeanInfo());
        gbean2.setAttribute("finalInt", new Integer(123));
        gbean2.setReferencePattern("MockEndpoint", gbean1.getAbstractName());
        kernel.loadGBean(gbean2, cl);
        kernel.startGBean(gbean2.getAbstractName());

        assertEquals("endpointCheck", kernel.invoke(gbean2.getAbstractName(), "checkEndpoint", null, null));
    }

    public void testNoProxyEndpoint() throws Exception {
        ClassLoader cl = MockGBean.class.getClassLoader();
        GBeanData gbean1 = buildGBeanData("name", "test", MockGBean.getGBeanInfo());
        gbean1.setAttribute("finalInt", new Integer(123));
        kernel.loadGBean(gbean1, cl);
        kernel.startGBean(gbean1.getAbstractName());

        GBeanData gbean2 = buildGBeanData("name", "test2", MockGBean.getGBeanInfo());
        gbean2.setAttribute("finalInt", new Integer(123));
        gbean2.setReferencePattern("MockEndpoint", gbean1.getAbstractName());
        kernel.loadGBean(gbean2, cl);
        kernel.startGBean(gbean2.getAbstractName());


        MockGBean mockGBean1 = (MockGBean) kernel.getGBean(gbean1.getAbstractName());
        MockGBean mockGBean2 = (MockGBean) kernel.getGBean(gbean2.getAbstractName());
        // this can only be tested of no proxy is on
//        assertSame(mockGBean2.getMockEndpoint(), mockGBean1);
    }

    public void testProxiesInterfaces() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        GBeanData gbean = buildGBeanData("name", "test", MockGBean.getGBeanInfo());
        gbean.setAttribute("name", "Test");
        gbean.setAttribute("finalInt", new Integer(123));
        kernel.loadGBean(gbean, myCl);
        kernel.startGBean(gbean.getAbstractName());
        ProxyManager mgr = kernel.getProxyManager();

        Object test = mgr.createProxy(gbean.getAbstractName(), myCl);
        assertTrue(test instanceof MockEndpoint);
        assertTrue(test instanceof MockParentInterface1);
        assertTrue(test instanceof MockParentInterface2);
        assertTrue(test instanceof MockChildInterface1);
        assertTrue(test instanceof MockChildInterface2);
        assertFalse(test instanceof Comparable);
        ((MockEndpoint)test).doNothing();
        assertEquals("Foo", ((MockEndpoint)test).echo("Foo"));
        ((MockParentInterface1)test).setValue("Foo");
        assertEquals("Foo", ((MockParentInterface1)test).getValue());
        ((MockParentInterface1)test).setMutableInt(6);
        assertEquals(6, ((MockParentInterface1)test).getMutableInt());
        ((MockParentInterface2)test).doNothing();
        assertEquals("Foo", ((MockParentInterface2)test).echo("Foo"));
        ((MockParentInterface2)test).setValue("Foo");
        assertEquals("Foo", ((MockParentInterface2)test).getValue());
        ((MockChildInterface1)test).getFinalInt();
        ((MockChildInterface2)test).doNothing();
        assertEquals("Foo", ((MockChildInterface2)test).doSomething("Foo"));

        test = mgr.createProxy(gbean.getAbstractName(), MockEndpoint.class);
        assertTrue(test instanceof MockEndpoint);
        assertFalse(test instanceof MockParentInterface1);
        assertFalse(test instanceof MockParentInterface2);
        assertFalse(test instanceof MockChildInterface1);
        assertFalse(test instanceof MockChildInterface2);
        assertFalse(test instanceof Comparable);

        ProxyFactory proxyFactory;
        proxyFactory = mgr.createProxyFactory(new Class[]{MockEndpoint.class, MockParentInterface2.class, MockChildInterface2.class}, myCl);
        test = proxyFactory.createProxy(gbean.getAbstractName());
        assertTrue(test instanceof MockEndpoint);
        assertTrue(test instanceof MockParentInterface1);
        assertTrue(test instanceof MockParentInterface2);
        assertTrue(test instanceof MockChildInterface1);
        assertTrue(test instanceof MockChildInterface2);
        assertFalse(test instanceof Comparable);

        proxyFactory = mgr.createProxyFactory(new Class[]{MockEndpoint.class, MockParentInterface1.class, MockChildInterface1.class}, myCl);
        test = proxyFactory.createProxy(gbean.getAbstractName());
        assertTrue(test instanceof MockEndpoint);
        assertTrue(test instanceof MockParentInterface1);
        assertFalse(test instanceof MockParentInterface2);
        assertTrue(test instanceof MockChildInterface1);
        assertFalse(test instanceof MockChildInterface2);
        assertFalse(test instanceof Comparable);

        proxyFactory = mgr.createProxyFactory(new Class[]{MockEndpoint.class, MockParentInterface1.class, MockChildInterface1.class, Comparable.class}, myCl);
        test = proxyFactory.createProxy(gbean.getAbstractName());
        assertTrue(test instanceof MockEndpoint);
        assertTrue(test instanceof MockParentInterface1);
        assertFalse(test instanceof MockParentInterface2);
        assertTrue(test instanceof MockChildInterface1);
        assertFalse(test instanceof MockChildInterface2);

        proxyFactory = mgr.createProxyFactory(new Class[]{MockParentInterface1.class, MockChildInterface1.class, Comparable.class}, myCl);
        test = proxyFactory.createProxy(gbean.getAbstractName());
        assertFalse(test instanceof MockEndpoint);
        assertTrue(test instanceof MockParentInterface1);
        assertFalse(test instanceof MockParentInterface2);
        assertTrue(test instanceof MockChildInterface1);
        assertFalse(test instanceof MockChildInterface2);

        proxyFactory = mgr.createProxyFactory(new Class[]{MockEndpoint.class, Comparable.class}, myCl);
        test = proxyFactory.createProxy(gbean.getAbstractName());
        assertTrue(test instanceof MockEndpoint);
        assertFalse(test instanceof MockParentInterface1);
        assertFalse(test instanceof MockParentInterface2);
        assertFalse(test instanceof MockChildInterface1);
        assertFalse(test instanceof MockChildInterface2);

        proxyFactory = mgr.createProxyFactory(new Class[]{Comparable.class}, myCl);
        test = proxyFactory.createProxy(gbean.getAbstractName());

        try {
            proxyFactory = mgr.createProxyFactory(null, myCl);
            fail();
        } catch (NullPointerException e) {
        }

        try {
            proxyFactory = mgr.createProxyFactory(new Class[0], myCl);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            // two non-assibnable classes
            test = mgr.createProxyFactory(new Class[]{MockGBean.class, String.class}, cl).createProxy(gbean.getAbstractName());
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%p [%t] %m %n")));
        Logger.getRootLogger().setLevel(Level.DEBUG);
        realKernel = KernelFactory.newInstance().createKernel("test");
        realKernel.boot();
        kernel = kernelWraper.wrap(realKernel);
    }

    protected void tearDown() throws Exception {
        realKernel.shutdown();
        realKernel = null;
        kernel = null;
        super.tearDown();
    }

    private GBeanData buildGBeanData(String name, String type, GBeanInfo info) {
        AbstractName abstractName = kernel.getNaming().createRootName(new Artifact("test", "foo", "1", "car"), name, type);
        return new GBeanData(abstractName, info);
    }
}
