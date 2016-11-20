/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.util;

import org.apache.geronimo.interop.properties.BooleanProperty;
import org.apache.geronimo.interop.properties.SystemProperties;


public abstract class MemoryBarrier {
    public static BooleanProperty useVolatileMemoryBarrierProperty =
            new BooleanProperty(SystemProperties.class, "org.apache.geronimo.interop.useVolatileMemoryBarrier")
            .defaultValue(true);

    public static final boolean USE_VOLATILE = useVolatileMemoryBarrierProperty.getBoolean();
}