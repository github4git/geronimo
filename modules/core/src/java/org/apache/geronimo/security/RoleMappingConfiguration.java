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
package org.apache.geronimo.security;

import java.util.Collection;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;


/**
 * <p>The methods of this interface are used by containers to create role mappings in a <code>Policy</code> provider.
 * An object that implements the <code>RoleMappingConfiguration</code> interface provides the role mapping configuration
 * interface for a corresponding policy context within the corresponding Policy provider.</p>
 *
 * <p>Geronimo will obtain an instance of this class by calling
 * <code>PolicyConfigurationFactory.getPolicyConfiguration</code>.  If the object that is returned <i>also</i>
 * implements <code>RoleMappingConfiguration</code>, Geronimo will call the methods of that interface to provide role
 * mappings to the <code>Policy</code> provider</p>
 * @version $Revision: 1.2 $ $Date: 2004/01/02 04:31:44 $
 * @see        javax.security.jacc.PolicyConfiguration
 * @see        javax.security.jacc.PolicyConfigurationFactory#getPolicyConfiguration
 */
public interface RoleMappingConfiguration extends PolicyConfiguration {

    /**
     * Add a mapping from a module's security roles to physical principals.  Mapping principals to the same role twice
     * will cause a <code>PolicyContextException</code> to be thrown.
     * @param role The role that is to be mapped to a set of principals.
     * @param principals The set of principals that are to be mapped to to role.
     * @throws PolicyContextException if the mapping principals to the same role twice occurs.
     */
    public void addRoleMapping(String role, Collection principals) throws PolicyContextException;
}
