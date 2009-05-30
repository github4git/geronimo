/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.jetty7.security;

import java.security.Principal;
import java.security.AccessControlContext;
import java.util.Arrays;

import javax.security.auth.Subject;

import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.RunAsToken;
import org.eclipse.jetty.server.UserIdentity;
import org.apache.geronimo.jetty7.handler.GeronimoUserIdentity;
import org.apache.geronimo.jetty7.handler.GeronimoRunAsToken;
import org.apache.geronimo.jetty7.handler.GeronimoUserIdentityWrapper;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.jacc.RunAsSource;

/**
 * @version $Rev$ $Date$
 */
public class JettyIdentityService implements IdentityService {

    private final AccessControlContext defaultAcc;
    private final RunAsSource runAsSource;

    public JettyIdentityService(AccessControlContext defaultAcc, RunAsSource runAsSource) {
        this.defaultAcc = defaultAcc;
        this.runAsSource = runAsSource;
    }

    //Umm, what was this supposed to do?
    public void associate(UserIdentity user) {
//        if (user instanceof GeronimoUserIdentityWrapper) {
//            return ((GeronimoUserIdentityWrapper) user).newWrapper(context);
//        } else {
//            return new GeronimoUserIdentityWrapper(user, context);
//        }
    }

    public void disassociate(GeronimoUserIdentityWrapper source) {
    }

    public Object setRunAs(UserIdentity userIdentity, RunAsToken token) {
        GeronimoRunAsToken geronimoRunAsToken = (GeronimoRunAsToken) token;
        Subject runAsSubject = geronimoRunAsToken.getRunAsSubject();
        return ContextManager.pushNextCaller(runAsSubject);
    }

    public void unsetRunAs(Object previousToken) {
        ContextManager.popCallers((Callers) previousToken);
    }

    public UserIdentity newUserIdentity(Subject subject, Principal userPrincipal, String[] roles) {
        if (subject != null) {
            AccessControlContext acc = ContextManager.registerSubjectShort(subject, userPrincipal, roles == null? null: Arrays.asList(roles));
            ContextManager.setCallers(subject, subject);
            return new GeronimoUserIdentity(subject, userPrincipal, acc);
        }
        return new GeronimoUserIdentity(null, null, defaultAcc);
    }

    public RunAsToken newRunAsToken(String runAsName) {
        Subject runAsSubject = runAsSource.getSubjectForRole(runAsName);
        return new GeronimoRunAsToken(runAsSubject);
    }

    public UserIdentity getSystemUserIdentity() {
        return new GeronimoUserIdentity(null, null, defaultAcc);
    }
}