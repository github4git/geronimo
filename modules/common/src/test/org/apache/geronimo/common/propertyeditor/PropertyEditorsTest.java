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

package org.apache.geronimo.common.propertyeditor;

import java.beans.PropertyEditorManager;
import java.util.List;

import junit.framework.TestCase;

/**
 * Unit test for {@link PropertyEditors} class.
 *
 * @version $Rev$ $Date$
 */
public class PropertyEditorsTest
    extends TestCase
{
    private String[] editorSearchPath;

    public void testDefaultEditorSearchPath()
    {
        String element = "org.apache.geronimo.common.propertyeditor";
        List path = PropertyEditors.getEditorSearchPath();
        assertNotNull(path);
        assertEquals(element, path.get(path.size() - 1));
    }
    
    public void testAppendEditorSearchPath()
    {
        String element = "my.path";
        PropertyEditors.appendEditorSearchPath(element);
        List path = PropertyEditors.getEditorSearchPath();
        assertNotNull(path);
        assertEquals(element, path.get(path.size() - 1));
    }

    protected void setUp() throws Exception {
        editorSearchPath = PropertyEditorManager.getEditorSearchPath();
    }

    protected void tearDown() throws Exception {
        PropertyEditorManager.setEditorSearchPath(editorSearchPath);
    }
}
