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

package org.apache.geronimo.connector.outbound;

import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.mock.MockConnection;
import org.apache.geronimo.connector.mock.MockConnectionFactory;
import org.apache.geronimo.connector.mock.MockManagedConnection;
import org.apache.geronimo.connector.mock.MockManagedConnectionFactory;
import org.apache.geronimo.connector.mock.MockXAResource;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentContext;
import org.apache.geronimo.connector.outbound.connectiontracking.DefaultComponentInterceptor;
import org.apache.geronimo.connector.outbound.connectiontracking.DefaultInterceptor;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.bridge.RealmBridge;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.ContainerTransactionContext;
import org.apache.geronimo.transaction.UnspecifiedTransactionContext;
import org.apache.geronimo.transaction.UserTransactionImpl;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.7 $ $Date: 2004/04/06 00:21:21 $
 *
 * */
public class ConnectionManagerTest extends TestCase implements DefaultInterceptor, RealmBridge {

    protected boolean useConnectionRequestInfo = false;
    protected boolean useSubject = true;
    protected boolean useTransactionCaching = true;
    protected boolean useLocalTransactions = false;
    protected boolean useTransactions = true;
    protected int maxSize = 5;
    protected int blockingTimeout = 100;
    protected String name = "testCF";
    //dependencies
    protected RealmBridge realmBridge = this;
    protected ConnectionTrackingCoordinator connectionTrackingCoordinator;
    protected Kernel kernel;

    protected TransactionManager transactionManager;
    protected ConnectionManagerDeployment connectionManagerDeployment;
    protected MockConnectionFactory connectionFactory;
    protected MockManagedConnectionFactory mockManagedConnectionFactory;
    protected DefaultComponentContext defaultComponentContext;
    protected DefaultComponentInterceptor defaultComponentInterceptor;
    protected Set unshareableResources = new HashSet();
    protected MockManagedConnection mockManagedConnection;
    protected Subject subject;

    protected UserTransactionImpl userTransaction;

    protected void setUp() throws Exception {
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        kernel = new Kernel("test.kernel", "testdomain");
        kernel.boot();
        transactionManager = new TransactionManagerImpl();
        mockManagedConnectionFactory = new MockManagedConnectionFactory();
        subject = new Subject();
        connectionManagerDeployment = new ConnectionManagerDeployment(useConnectionRequestInfo,
                useSubject,
                useTransactionCaching,
                useLocalTransactions,
                useTransactions,
                maxSize,
                blockingTimeout,
                //name,
                realmBridge,
                connectionTrackingCoordinator);
        connectionManagerDeployment.doStart();
        connectionFactory = (MockConnectionFactory) connectionManagerDeployment.createConnectionFactory(mockManagedConnectionFactory);
        defaultComponentContext = new DefaultComponentContext();
        defaultComponentInterceptor = new DefaultComponentInterceptor(this, connectionTrackingCoordinator, unshareableResources);
    }

    protected void tearDown() throws Exception {
        connectionTrackingCoordinator = null;
        kernel.shutdown();
        kernel = null;
        transactionManager = null;
        mockManagedConnectionFactory = null;
        connectionManagerDeployment = null;
        connectionFactory = null;
        defaultComponentContext = null;
    }


    public void testSingleTransactionCall() throws Throwable {
        ContainerTransactionContext transactionContext = new ContainerTransactionContext(transactionManager);
        TransactionContext.setContext(transactionContext);
        transactionContext.begin();
        defaultComponentInterceptor.invoke(defaultComponentContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertEquals("XAResource should know one xid", 1, mockXAResource.getKnownXids().size());
        assertNull("Should not be committed", mockXAResource.getCommitted());
        transactionManager.commit();
        assertNotNull("Should be committed", mockXAResource.getCommitted());
    }

    public void testNoTransactionCall() throws Throwable {
        TransactionContext.setContext(new UnspecifiedTransactionContext());
        defaultComponentInterceptor.invoke(defaultComponentContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertEquals("XAResource should know 0 xid", 0, mockXAResource.getKnownXids().size());
        assertNull("Should not be committed", mockXAResource.getCommitted());
    }

    public void testOneTransactionTwoCalls() throws Throwable {
        ContainerTransactionContext transactionContext = new ContainerTransactionContext(transactionManager);
        TransactionContext.setContext(transactionContext);
        transactionContext.begin();
        defaultComponentInterceptor.invoke(defaultComponentContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertEquals("XAResource should know one xid", 1, mockXAResource.getKnownXids().size());
        assertNull("Should not be committed", mockXAResource.getCommitted());
        defaultComponentInterceptor.invoke(defaultComponentContext);
        assertEquals("Expected same XAResource", mockXAResource, mockManagedConnection.getXAResource());
        assertEquals("XAResource should know one xid", 1, mockXAResource.getKnownXids().size());
        assertNull("Should not be committed", mockXAResource.getCommitted());
        transactionManager.commit();
        assertNotNull("Should be committed", mockXAResource.getCommitted());
    }

    public void testUserTransactionEnlistingExistingConnections() throws Throwable {
        TransactionContext.setContext(new UnspecifiedTransactionContext());
        userTransaction = new UserTransactionImpl();
        userTransaction.setUp(transactionManager, connectionTrackingCoordinator);
        userTransaction.setOnline(true);
        defaultComponentInterceptor.invoke(defaultComponentContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertEquals("XAResource should know 1 xid", 1, mockXAResource.getKnownXids().size());
        assertNotNull("Should be committed", mockXAResource.getCommitted());
    }

    public Object invoke(InstanceContext newInstanceContext) throws Throwable {
        MockConnection mockConnection = (MockConnection) connectionFactory.getConnection();
        mockManagedConnection = mockConnection.getManagedConnection();
        if (userTransaction != null) {
            userTransaction.begin();
            MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
            assertEquals("XAResource should know one xid", 1, mockXAResource.getKnownXids().size());
            assertNull("Should not be committed", mockXAResource.getCommitted());
            userTransaction.commit();
            assertNotNull("Should be committed", mockXAResource.getCommitted());
        }
        mockConnection.close();
        return null;
    }

    public Subject mapSubject(Subject sourceSubject) {
        return subject;
    }
}
