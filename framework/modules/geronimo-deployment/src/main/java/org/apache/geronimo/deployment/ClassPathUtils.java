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

package org.apache.geronimo.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class ClassPathUtils {
    
    private static final Logger log = LoggerFactory.getLogger(ClassPathUtils.class);
    
    // values for lenience vs. strict manifest classpath interpretation
    private final static int manifestClassLoaderMode;
    private final static String manifestClassLoaderMessage;
    private final static int MFCP_LENIENT = 1;
    private final static int MFCP_STRICT = 2;

    static {
        // Extract the LenientMFCP value if specified.  If not, default to strict..
        String mode = System.getProperty("Xorg.apache.geronimo.deployment.LenientMFCP");
        int mfcpMode = MFCP_STRICT;    // Default to strict
        String mfcpModeMessage = "Strict Manifest Classpath";
        if (mode != null) {
            if (mode.equals("true")) {
                mfcpMode = MFCP_LENIENT;
                mfcpModeMessage = "Lenient Manifest Classpath";
            }
        }

        manifestClassLoaderMode = mfcpMode;
        manifestClassLoaderMessage = mfcpModeMessage;
        LoggerFactory.getLogger(DeploymentContext.class).info(
                "The " + manifestClassLoaderMessage + " processing mode is in effect.\n" +
                        "This option can be altered by specifying -DXorg.apache.geronimo.deployment.LenientMFCP=true|false\n" +
                        "Specify =\"true\" for more lenient processing such as ignoring missing jars and references that are not spec compliant.");
    }
    
    interface JarFileFactory {
        JarFile newJarFile(URI relativeURI) throws IOException;

        String getManifestClassPath(JarFile jarFile) throws IOException;

        boolean isDirectory(URI relativeURI) throws IOException;

        File[] listFiles(URI relativeURI) throws IOException;
    }
    
    /**
     * Import the classpath from a jar file's manifest.  The imported classpath
     * is crafted relative to <code>moduleBaseUri</code>.
     *
     * @param moduleFile    the jar file from which the manifest is obtained.
     * @param moduleBaseUri the base for the imported classpath
     * @param factory       the factory for constructing JarFiles and the way to extract the manifest classpath from a JarFile. Introduced to make
     *                      testing plausible, but may be useful for in-IDE deployment.
     * @param problems      List to save all the problems we encounter.
     * @throws DeploymentException if there is a problem with the classpath in
     *                             the manifest
     */
    public static void addManifestClassPath(JarFile moduleFile, URI moduleBaseUri, LinkedHashSet<String> classPath, JarFileFactory factory, List<DeploymentException> problems) throws DeploymentException {
        String manifestClassPath;
        try {
            manifestClassPath = factory.getManifestClassPath(moduleFile);
        } catch (IOException e) {
            problems.add(new DeploymentException("Could not read manifest: " + moduleBaseUri, e));
            return;
        }

        if (manifestClassPath == null) {
            return;
        }

        for (StringTokenizer tokenizer = new StringTokenizer(manifestClassPath, " "); tokenizer.hasMoreTokens();) {
            String path = tokenizer.nextToken();

            URI pathUri;
            try {
                pathUri = new URI(path);
            } catch (URISyntaxException e) {
                problems.add(new DeploymentException("Invalid manifest classpath entry: module= " + moduleBaseUri + ", path= " + path));
                continue;
            }

            if (pathUri.isAbsolute()) {
                problems.add(new DeploymentException("Manifest class path entries must be relative (J2EE 1.4 Section 8.2): path= " + path + ", module= " + moduleBaseUri));
                continue;
            }

            URI targetUri = moduleBaseUri.resolve(pathUri);

            try {
                if (factory.isDirectory(targetUri)) {
                    if (!targetUri.getPath().endsWith("/")) {
                        targetUri = URI.create(targetUri.getPath() + "/");
                    }
                    for (File file : factory.listFiles(targetUri)) {
                        if (file.isDirectory()) {
                            log.debug("Sub directory [" + file.getAbsolutePath() + "] in the manifest entry directory is ignored");
                            continue;
                        }
                        if (!file.getName().endsWith(".jar")) {
                            log.debug("Only jar files are added to classpath, file [" + file.getAbsolutePath() + "] is ignored");
                            continue;
                        }
                        classPath.add(targetUri.resolve(file.getName()).getPath());
                    }
                } else {
                    if (!pathUri.getPath().endsWith(".jar")) {
                        if (manifestClassLoaderMode == MFCP_STRICT) {
                            problems.add(new DeploymentException(
                                    "Manifest class path entries must end with the .jar extension (J2EE 1.4 Section 8.2): path= "
                                            + path + ", module= " + moduleBaseUri));
                        } else {
                            log.info("The " + manifestClassLoaderMessage + " processing mode is in effect.\n"
                                    + "Therefore, a manifest classpath entry which does not end with .jar, "
                                    + pathUri + " is being permitted and ignored.");
                        }
                        continue;
                    }
                    classPath.add(targetUri.getPath());
                }
            } catch (IOException e) {
                if (manifestClassLoaderMode == MFCP_STRICT) {
                    problems.add(new DeploymentException(
                            "An IOException resulting from manifest classpath : targetUri= " + targetUri, e));
                } else {
                    log.info("The " + manifestClassLoaderMessage + " processing mode is in effect.\n"
                            + "Therefore, an IOException resulting from manifest classpath " + targetUri
                            + " is being ignored.");
                }
            }
        }
    }
    
    /**
     * Recursively construct the complete set of paths in the ear for the manifest classpath of the supplied modulefile.
     * Used only in PersistenceUnitBuilder to figure out if a persistence.xml relates to the starting module.  Having a classloader for
     * each ejb module would eliminate the need for this and be more elegant.
     *
     * @param moduleFile    the module that we start looking at classpaths at, in the car.
     * @param moduleBaseUri where the moduleFile is inside the car file.  For an (unpacked) war this ends with / which means we also need:
     * @param resolutionUri the uri to resolve all entries against. For a module such as an ejb jar that is part of the
     *                      root ear car it is ".".  For a sub-configuration such as a war it is the "reverse" of the path to the war file in the car.
     *                      For instance, if the war file is foo/bar/myweb.war, the resolutionUri is "../../..".
     * @param classpath     the classpath list we are constructing.
     * @param exclusions    the paths to not investigate.  These are typically the other modules in the ear/car file: they will have their contents processed for themselves.
     * @param factory       the factory for constructing JarFiles and the way to extract the manifest classpath from a JarFile. Introduced to make
     *                      testing plausible, but may be useful for in-IDE deployment.
     * @param problems      List to save all the problems we encounter.
     * @throws org.apache.geronimo.common.DeploymentException
     *          if something goes wrong.
     */
    public static void getCompleteManifestClassPath(JarFile moduleFile, URI moduleBaseUri, URI resolutionUri, ClassPathList classpath, ModuleList exclusions, JarFileFactory factory, List<DeploymentException> problems) throws DeploymentException {
        String manifestClassPath;
        try {
            manifestClassPath = factory.getManifestClassPath(moduleFile);
        } catch (IOException e) {
            problems.add(new DeploymentException(printInfo("Could not read manifest: " + moduleBaseUri, moduleBaseUri, classpath, exclusions), e));
            return;
        }

        if (manifestClassPath == null) {
            return;
        }

        for (StringTokenizer tokenizer = new StringTokenizer(manifestClassPath, " "); tokenizer.hasMoreTokens();) {
            String path = tokenizer.nextToken();

            URI pathUri;
            try {
                pathUri = new URI(path);
            } catch (URISyntaxException e) {
                problems.add(new DeploymentException(printInfo("Invalid manifest classpath entry, path= " + path, moduleBaseUri, classpath, exclusions)));
                continue;
            }

            if (pathUri.isAbsolute()) {
                problems.add(new DeploymentException(printInfo("Manifest class path entries must be relative (J2EE 1.4 Section 8.2): path= " + path, moduleBaseUri, classpath, exclusions)));
                continue;
            }

            URI targetUri = moduleBaseUri.resolve(pathUri);

            try {
                if (factory.isDirectory(targetUri)) {
                    if (!targetUri.getPath().endsWith("/")) {
                        targetUri = URI.create(targetUri.getPath() + "/");
                    }
                    for (File file : factory.listFiles(targetUri)) {
                        if (file.isDirectory()) {
                            log.debug("Sub directory [" + file.getAbsolutePath() + "] in the manifest entry directory is ignored");
                            continue;
                        }
                        if (!file.getName().endsWith(".jar")) {
                            log.debug("Only jar files are added to classpath, file [" + file.getAbsolutePath() + "] is ignored");
                            continue;
                        }
                        addToClassPath(moduleBaseUri, resolutionUri, targetUri.resolve(file.getName()), classpath, exclusions, factory, problems);
                    }
                } else {
                    if (!pathUri.getPath().endsWith(".jar")) {
                        if (manifestClassLoaderMode == MFCP_STRICT) {
                            problems.add(new DeploymentException(printInfo(
                                    "Manifest class path entries must end with the .jar extension (J2EE 1.4 Section 8.2): path= "
                                            + path, moduleBaseUri, classpath, exclusions)));
                        } else {
                            log.info("The " + manifestClassLoaderMessage + " processing mode is in effect.\n"
                                    + "Therefore, a manifest classpath entry which does not end with .jar, "
                                    + pathUri + " is being permitted and ignored.");
                        }
                        continue;
                    }
                    addToClassPath(moduleBaseUri, resolutionUri, targetUri, classpath, exclusions, factory, problems);
                }
            } catch (IOException e) {
                if (manifestClassLoaderMode == MFCP_STRICT) {
                    problems.add(new DeploymentException(
                            "An IOException resulting from manifest classpath : targetUri= " + targetUri, e));
                } else {
                    log.info("The " + manifestClassLoaderMessage + " processing mode is in effect.\n"
                            + "Therefore, an IOException resulting from manifest classpath " + targetUri
                            + " is being ignored.");
                }
            }
        }
    }

    private static void addToClassPath(URI moduleBaseUri, URI resolutionUri, URI targetUri, ClassPathList classpath, ModuleList exclusions, JarFileFactory factory, List<DeploymentException> problems) throws DeploymentException {
        String targetEntry = targetUri.toString();
        if (exclusions.contains(targetEntry)) {
            return;
        }
        URI resolvedUri = resolutionUri.resolve(targetUri);
        String classpathEntry = resolvedUri.toString();
        //don't get caught in circular references
        if (classpath.contains(classpathEntry)) {
            return;
        }
        classpath.add(classpathEntry);
        JarFile classPathJarFile;
        try {
            classPathJarFile = factory.newJarFile(targetUri);
        } catch (IOException e) {
            if (manifestClassLoaderMode == MFCP_STRICT) {
                problems.add(new DeploymentException(
                        printInfo(
                                "Manifest class path entries must be a valid jar file, or if it is a directory, all the files with jar suffix in it must be a valid jar file (JAVAEE 5 Section 8.2):  resolved to targetURI= "
                                        + targetUri, moduleBaseUri, classpath, exclusions), e));
            } else {
                log.info("The " + manifestClassLoaderMessage + " processing mode is in effect.\n"
                        + "Therefore, an IOException resulting from manifest classpath " + targetUri
                        + " is being ignored.");
            }
            return;
        }

        getCompleteManifestClassPath(classPathJarFile, targetUri, resolutionUri, classpath, exclusions, factory, problems);
    }

    private static String printInfo(String message, URI moduleBaseUri, ClassPathList classpath, ModuleList exclusions) {
        StringBuffer buf = new StringBuffer(message).append("\n");
        buf.append("    looking at: ").append(moduleBaseUri);
        buf.append("    current classpath: ").append(classpath);
        buf.append("    ignoring modules: ").append(exclusions);
        return buf.toString();
    }

}