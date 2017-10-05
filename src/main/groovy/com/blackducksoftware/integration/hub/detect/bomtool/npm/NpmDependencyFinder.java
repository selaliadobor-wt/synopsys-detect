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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.graph.MutableDependencyGraph;
import com.blackducksoftware.integration.hub.bdio.graph.MutableMapDependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.Forge;
import com.blackducksoftware.integration.hub.bdio.model.dependency.Dependency;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.blackducksoftware.integration.hub.detect.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.bomtool.NpmBomTool;
import com.blackducksoftware.integration.hub.detect.model.BomToolType;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

@Component
public class NpmDependencyFinder {

    @Autowired
    Gson gson;

    @Autowired
    ExternalIdFactory externalIdFactory;

    @Autowired
    DetectConfiguration detectConfiguration;

    public DetectCodeLocation createDependencyGraph(final String sourcePath) {
        final File packageJsonFile = new File(sourcePath, NpmBomTool.PACKAGE_JSON);
        final File nodeModulesFolder = new File(sourcePath, NpmBomTool.NODE_MODULES);

        final List<File> nodeModulesFiles = generateNodeModulesStack(nodeModulesFolder);

        final NpmPackageJson npmPackageJson = generatePackageJson(packageJsonFile);
        final String projectName = npmPackageJson.name;
        final String projectVersion = npmPackageJson.version;

        final Set<String> startingDependencies = generateStartingDependenciesList(npmPackageJson);
        final MutableDependencyGraph graph = new MutableMapDependencyGraph();

        final ExternalId externalId = externalIdFactory.createNameVersionExternalId(Forge.NPM, projectName, projectVersion);
        final Dependency root = new Dependency(projectName, projectVersion, externalId);

        traverseNodeModulesStructure2(startingDependencies, nodeModulesFiles, root, root, graph);
        return new DetectCodeLocation(BomToolType.NPM, sourcePath, projectName, projectVersion, externalId, graph);
    }

    private List<File> traverseNodeModulesStructure(final Set<String> dependenciesCheckList, List<File> nodeModulesItems, final Dependency parent, final Dependency root, final MutableDependencyGraph graph) {
        for (int i = 0; i < nodeModulesItems.size(); i++) {
            final File nodeModulesFile = nodeModulesItems.get(i);
            if (dependenciesCheckList.contains(nodeModulesFile.getName())) {
                final NpmPackageJson packageJson = generatePackageJson(new File(nodeModulesFile, NpmBomTool.PACKAGE_JSON));
                final ExternalId externalId = externalIdFactory.createNameVersionExternalId(Forge.NPM, packageJson.name, packageJson.version);
                final Dependency child = new Dependency(packageJson.name, packageJson.version, externalId);

                final boolean nodeAlreadyExists = graph.getDependency(externalId) == null;

                if (parent.equals(root)) {
                    graph.addChildToRoot(child);
                } else {
                    graph.addChildWithParent(child, parent);
                }

                if (nodeAlreadyExists) {
                    final File newNodeModules = new File(nodeModulesFile, NpmBomTool.NODE_MODULES);
                    List<File> newNodeModulesList = nodeModulesItems;
                    if (newNodeModules.exists()) {
                        newNodeModulesList = new ArrayList<>(generateNodeModulesStack(newNodeModules));
                        newNodeModulesList.addAll(nodeModulesItems);
                    }
                    final Set<String> startingDependencies = generateStartingDependenciesList(packageJson);
                    nodeModulesItems = traverseNodeModulesStructure(startingDependencies, newNodeModulesList, child, root, graph);
                }
            }
        }

        return nodeModulesItems;
    }

    private void traverseNodeModulesStructure2(final Set<String> dependenciesCheckList, final List<File> nodeModulesItems, final Dependency parent, final Dependency root, final MutableDependencyGraph graph) {
        final Iterator<File> nodeModulesIterator = nodeModulesItems.iterator();

        while (nodeModulesIterator.hasNext()) {
            final File nodeModulesFile = nodeModulesIterator.next();

            if (dependenciesCheckList.contains(nodeModulesFile.getName())) {
                final NpmPackageJson packageJson = generatePackageJson(new File(nodeModulesFile, NpmBomTool.PACKAGE_JSON));
                final ExternalId externalId = externalIdFactory.createNameVersionExternalId(Forge.NPM, packageJson.name, packageJson.version);
                final Dependency child = new Dependency(packageJson.name, packageJson.version, externalId);

                final boolean nodeExists = graph.getDependency(externalId) != null;

                if (parent.equals(root)) {
                    graph.addChildToRoot(child);
                } else {
                    graph.addChildWithParent(child, parent);
                }

                if (!nodeExists) {
                    final File newNodeModules = new File(nodeModulesFile, NpmBomTool.NODE_MODULES);
                    List<File> newNodeModulesList = nodeModulesItems;
                    if (newNodeModules.exists()) {
                        newNodeModulesList = new ArrayList<>(generateNodeModulesStack(newNodeModules));
                        newNodeModulesList.addAll(nodeModulesItems);
                    }
                    final Set<String> startingDependencies = generateStartingDependenciesList(packageJson);
                    traverseNodeModulesStructure2(startingDependencies, newNodeModulesList, child, root, graph);
                } else {
                    nodeModulesIterator.remove();
                }
            }
        }
    }

    private List<File> generateNodeModulesStack(final File nodeModulesFolder) {
        final List<File> nodeModulesFiles = new ArrayList<>();
        nodeModulesFiles.addAll(Arrays.asList(nodeModulesFolder.listFiles()));
        return nodeModulesFiles;
    }

    private NpmPackageJson generatePackageJson(final File packageJsonFile) {
        try {
            final FileReader fileReader = new FileReader(packageJsonFile);
            final JsonReader jsonReader = new JsonReader(fileReader);
            final NpmPackageJson packageJson = gson.fromJson(jsonReader, NpmPackageJson.class);
            return packageJson;
        } catch (final FileNotFoundException e) {
            return new NpmPackageJson();
        }
    }

    private Set<String> generateStartingDependenciesList(final NpmPackageJson packageJson) {
        final Set<String> startingDependencies = new HashSet<>();
        if (packageJson.dependencies != null) {
            startingDependencies.addAll(packageJson.dependencies.keySet());
        }
        if (detectConfiguration.getNpmIncludeDevDependencies() && (packageJson.devDependencies != null)) {
            startingDependencies.addAll(packageJson.devDependencies.keySet());
        }

        return startingDependencies;
    }
}
