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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.ejb;

import java.rmi.RemoteException;

/**
 *
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:39 $
 */
public interface EntityBean extends EnterpriseBean {
    void ejbActivate() throws EJBException, RemoteException;

    void ejbLoad() throws EJBException, RemoteException;

    void ejbPassivate() throws EJBException, RemoteException;

    void ejbRemove() throws RemoveException, EJBException, RemoteException;

    void ejbStore() throws EJBException, RemoteException;

    void setEntityContext(EntityContext ctx) throws EJBException, RemoteException;

    void unsetEntityContext() throws EJBException, RemoteException;
}
