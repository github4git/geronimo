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

package org.apache.geronimo.j2ee.management.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.geronimo.j2ee.management.J2EEManagedObject;

/**
 * 
 * 
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:52 $
 */
public class Util {
    static String[] getObjectNames(Collection refs) {
        ArrayList names = new ArrayList(refs.size());
        for (Iterator i = refs.iterator(); i.hasNext();) {
            J2EEManagedObject managedObject = (J2EEManagedObject) i.next();
            try {
                names.add(managedObject.getobjectName());
            } catch (IllegalStateException e) {
                // ignore - means the proxy went offline whilst we were iterating
            }
        }
        return (String[]) names.toArray(new String[names.size()]);
    }
}
