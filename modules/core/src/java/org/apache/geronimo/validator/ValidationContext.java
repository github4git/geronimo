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
package org.apache.geronimo.validator;

import java.io.PrintWriter;

import javax.enterprise.deploy.shared.ModuleType;

import org.apache.xmlbeans.XmlObject;

/**
 * Holds all the context information for the current validation process.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/12 08:19:27 $
 */
public class ValidationContext {
    public final PrintWriter out;
    public final String moduleName;
    public final ClassLoader loader;
    public final ModuleType type;
    public final XmlObject[] standardDD;
    public final Object[] serverDD;
    private Object currentStandardDD;
    private Object currentNode;

    public ValidationContext(ClassLoader loader, String moduleName, PrintWriter out, Object[] serverDD, XmlObject[] standardDD, ModuleType type) {
        this.loader = loader;
        this.moduleName = moduleName;
        this.out = out;
        this.serverDD = serverDD;
        this.standardDD = standardDD;
        this.type = type;
    }

    /**
     * At the moment, this is the standard DD we're validating.
     */
    public Object getCurrentStandardDD() {
        return currentStandardDD;
    }

    /**
     * At the moment, this is the standard DD we're validating.
     */
    void setCurrentStandardDD(Object currentStandardDD) {
        this.currentStandardDD = currentStandardDD;
    }

    /**
     * At the moment, this is the node on the standard DD that we're
     * validating.  It corresponds to the XPath that a particular
     * test is interested in.
     */
    public Object getCurrentNode() {
        return currentNode;
    }

    /**
     * At the moment, this is the node on the standard DD that we're
     * validating.  It corresponds to the XPath that a particular
     * test is interested in.
     */
    void setCurrentNode(Object currentNode) {
        this.currentNode = currentNode;
    }
}
