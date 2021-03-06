/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.management.geronimo;

/**
 * Geronimo extension to the standard JSR-77 WebModule type.
 *
 * @version $Rev$ $Date$
 */
public interface WebModule extends org.apache.geronimo.management.WebModule {
    /**
     * Gets the web context for this web app.
     */
    public String getContextPath();

    /**
     * Gets the ObjectName of the WebContainer that's hosting this
     * WebModule.
     */
    public String getContainerName();
}
