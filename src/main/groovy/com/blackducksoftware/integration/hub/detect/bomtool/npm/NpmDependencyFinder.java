/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.detect.bomtool.npm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.graph.MutableDependencyGraph;
import com.blackducksoftware.integration.hub.bdio.graph.MutableMapDependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.Forge;
import com.blackducksoftware.integration.hub.bdio.model.dependency.Dependency;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.blackducksoftware.integration.hub.detect.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.model.BomToolType;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation;
import com.google.gson.Gson;

@Component
public class NpmDependencyFinder {

    @Autowired
    Gson gson;

    @Autowired
    ExternalIdFactory externalIdFactory;

    @Autowired
    DetectConfiguration detectConfiguration;

    public DetectCodeLocation createDependencyGraph(final String sourcePath) {
        final NpmProjectFolder npmProjectFolder = new NpmProjectFolder(sourcePath);
        final NpmPackageJson npmPackageJson = npmProjectFolder.getPackageJson(gson);

        final Stack<NpmProjectFolder> projectFolderStack = new Stack<>();
        projectFolderStack.push(npmProjectFolder);

        final String projectName = npmPackageJson.name;
        final String projectVersion = npmPackageJson.version;

        final Set<String> startingDependencies = generateStartingDependenciesList(npmPackageJson, true);
        final MutableDependencyGraph graph = new MutableMapDependencyGraph();

        final Dependency root = generateDependency(npmPackageJson);

        traverseNodeModulesStructure(startingDependencies, projectFolderStack, root, true, graph);
        return new DetectCodeLocation(BomToolType.NPM, sourcePath, projectName, projectVersion, root.externalId, graph);
    }

    private void traverseNodeModulesStructure(final Set<String> dependenciesCheckList, final Stack<NpmProjectFolder> projectFolderStack, final Dependency parent, final boolean firstIteration, final MutableDependencyGraph graph) {
        while (!projectFolderStack.isEmpty()) {
            final NpmProjectFolder currentProjectFolder = projectFolderStack.pop();
            final Iterator<String> dependenciesCheckListIterator = dependenciesCheckList.iterator();
            while (dependenciesCheckListIterator.hasNext()) {
                final String dependencyName = dependenciesCheckListIterator.next();
                final NpmProjectFolder childProjectFolder = currentProjectFolder.getChildNpmProjectFromNodeModules(dependencyName);
                if (childProjectFolder != null) {
                    final NpmPackageJson packageJson = childProjectFolder.getPackageJson(gson);
                    if (packageJson.name != null && packageJson.version != null) {
                        addDependencyToGraph(childProjectFolder, parent, firstIteration, graph, projectFolderStack);
                    }

                    dependenciesCheckListIterator.remove();
                }
            }
        }
    }

    private void addDependencyToGraph(final NpmProjectFolder projectFolder, final Dependency parent, final boolean firstIteration, final MutableDependencyGraph graph, final Stack<NpmProjectFolder> projectFolderStack) {
        final Dependency child = generateDependency(projectFolder.getPackageJson(gson));
        final boolean dependencyAlreadyExists = graph.hasDependency(child);

        if (firstIteration) {
            graph.addChildToRoot(child);
        } else {
            graph.addChildWithParents(child, parent);
        }

        if (!dependencyAlreadyExists) {
            final Stack<NpmProjectFolder> projectFolderStackCopy = (Stack<NpmProjectFolder>) projectFolderStack.clone();
            projectFolderStackCopy.add(projectFolder.getParentNpmProject());
            if (projectFolder.getNodeModulesDirectory().exists()) {
                projectFolderStackCopy.add(projectFolder);
            }
            final Set<String> startingDependencies = generateStartingDependenciesList(projectFolder.getPackageJson(gson), false);
            traverseNodeModulesStructure(startingDependencies, projectFolderStackCopy, child, false, graph);
        }
    }

    private Dependency generateDependency(final NpmPackageJson packageJson) {
        final String projectName = packageJson.name;
        final String projectVersion = packageJson.version;
        final ExternalId externalId = externalIdFactory.createNameVersionExternalId(Forge.NPM, projectName, projectVersion);
        final Dependency dependency = new Dependency(projectName, projectVersion, externalId);
        return dependency;
    }

    private Set<String> generateStartingDependenciesList(final NpmPackageJson packageJson, final boolean initialProjectDependencies) {
        final Set<String> startingDependencies = new HashSet<>();
        if (packageJson.dependencies != null) {
            startingDependencies.addAll(packageJson.dependencies.keySet());
        }
        if (initialProjectDependencies && detectConfiguration.getNpmIncludeDevDependencies() && (packageJson.devDependencies != null)) {
            startingDependencies.addAll(packageJson.devDependencies.keySet());
        }

        return startingDependencies;
    }
}
