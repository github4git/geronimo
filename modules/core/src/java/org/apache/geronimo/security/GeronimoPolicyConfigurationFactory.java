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
 *        Apache Software Foundation (http:www.apache.org/)."
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
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.security;

import java.util.HashMap;
import java.util.Map;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;


/**
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/02 04:31:44 $
 */
public class GeronimoPolicyConfigurationFactory extends PolicyConfigurationFactory {
    private Map configurations = new HashMap();

    public final static int GENERICFACTORY = 0;
    public final static int EJBFACTORY = 1;
    public final static int WEBFACTORY = 2;
    private static int factoryType = EJBFACTORY;

    public static void setFactoryType(int type) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new GeronimoSecurityPermission("setFactoryType"));

        factoryType = type;
    }

    public PolicyConfiguration getPolicyConfiguration(String contextID, boolean remove) throws PolicyContextException {
        PolicyConfiguration configuration = (PolicyConfiguration) configurations.get(contextID);

        if (configuration == null || remove) {
            switch (factoryType) {
                case EJBFACTORY:
                    configuration = new PolicyConfigurationEJB(contextID);
                    break;
                case WEBFACTORY:
                    configuration = new PolicyConfigurationWeb(contextID);
                    break;
                case GENERICFACTORY:
                    configuration = new PolicyConfigurationGeneric(contextID);
                    break;
                default:
                    configuration = new PolicyConfigurationGeneric(contextID);
                    break;
            }
            configurations.put(contextID, configuration);
        }

        return configuration;
    }

    public boolean inService(String contextID) throws PolicyContextException {
        javax.security.jacc.PolicyConfiguration configuration = getPolicyConfiguration(contextID, false);

        return configuration.inService();
    }

}
