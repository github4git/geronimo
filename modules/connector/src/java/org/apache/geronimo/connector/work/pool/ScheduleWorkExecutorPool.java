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

package org.apache.geronimo.connector.work.pool;

import javax.resource.spi.work.WorkException;

import org.apache.geronimo.connector.work.WorkerContext;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;

/**
 * WorkExecutorPool handling the submitted Work instances asynchronously.
 * More accurately, its execute method returns immediately after the work
 * submission.
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:33 $
 */
public class ScheduleWorkExecutorPool
        extends AbstractWorkExecutorPool {

    /**
     * Creates a pool with the specified minimum and maximum sizes.
     *
     * @param minSize Minimum size of the work executor pool.
     * @param maxSize Maximum size of the work executor pool.
     */
    public ScheduleWorkExecutorPool(int minSize, int maxSize) {
        super(new LinkedQueue(), minSize, maxSize);
    }

    /**
     * Performs the actual execution of the specified work.
     *
     * @param work Work to be executed.
     */
    public void doExecute(WorkerContext work)
            throws WorkException, InterruptedException {
        super.execute(work);
    }

}
