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
package org.apache.geronimo.security.jaas;

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public interface LoginModuleSettings {
    public String getLoginDomainName();

    public void setLoginDomainName(String loginDomainName);

    public Properties getOptions();

    public void setOptions(Properties options);

    public String getLoginModuleClass();

    public void setLoginModuleClass(String loginModuleClass);

    public boolean isServerSide();

    public void setServerSide(boolean serverSide);

    public boolean isWrapPrincipals();

    public void setWrapPrincipals(boolean wrapPrincipals);
}