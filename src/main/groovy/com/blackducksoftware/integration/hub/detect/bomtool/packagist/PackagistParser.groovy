/*
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
package com.blackducksoftware.integration.hub.detect.bomtool.packagist

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.graph.MutableDependencyGraph
import com.blackducksoftware.integration.hub.bdio.graph.MutableMapDependencyGraph
import com.blackducksoftware.integration.hub.bdio.model.Forge
import com.blackducksoftware.integration.hub.bdio.model.dependency.Dependency
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory
import com.blackducksoftware.integration.hub.detect.model.BomToolType
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser

import groovy.transform.TypeChecked

@Component
@TypeChecked
class PackagistParser {

    @Autowired
    ExternalIdFactory externalIdFactory

    public DetectCodeLocation getDependencyGraphFromProject(String sourcePath, String composerJsonText, String composerLockText, boolean includeDev) {
        MutableDependencyGraph graph = new MutableMapDependencyGraph();

        JsonObject composerJsonObject = new JsonParser().parse(composerJsonText) as JsonObject
        String projectName = composerJsonObject.get('name')?.getAsString()
        String projectVersion = composerJsonObject.get('version')?.getAsString()
        List<String> projectPackages = parseRequiredPackages(composerJsonObject, false)

        JsonObject composerLockObject = new JsonParser().parse(composerLockText) as JsonObject
        JsonArray packagesJson = composerLockObject.get('packages')?.getAsJsonArray()

        List<PackagistDependency> packagistPackages = parsePackages(packagesJson, includeDev)
        addToGraph(graph, null, projectPackages, packagistPackages, true)

        ExternalId projectExternalId;
        if (projectName == null || projectVersion == null){
            projectExternalId = externalIdFactory.createPathExternalId(Forge.PACKAGIST, sourcePath);
        }else{
            projectExternalId = externalIdFactory.createNameVersionExternalId(Forge.PACKAGIST, projectName, projectVersion);
        }

        new DetectCodeLocation(BomToolType.PACKAGIST, sourcePath, projectName, projectVersion, projectExternalId, graph)
    }

    private List<PackagistDependency> parsePackages(JsonArray jsonArray, boolean includeDev) {
        List<PackagistDependency>  packages = new ArrayList<PackagistDependency>();
        jsonArray.each {
            def dependency = new PackagistDependency();
            dependency.name = it.getAt('name').toString().replace('"', '')
            dependency.version = it.getAt('version').toString().replace('"', '')
            dependency.requires = parseRequiredPackages(it.getAsJsonObject(), includeDev);
            packages.add(dependency);
        }
        return packages;
    }

    private List<String> parseRequiredPackages(JsonObject jsonObject, boolean includeDev) {
        List<String> requires = new ArrayList<String>();

        requires.addAll(parseRequiredPackagesProperty(jsonObject, 'require'));

        if (includeDev){
            requires.addAll(parseRequiredPackagesProperty(jsonObject, 'require-dev'));
        }

        return requires
    }

    private List<String> parseRequiredPackagesProperty(JsonObject jsonObject, String property){
        List<String> requires = new ArrayList<String>();

        def requiredPackages = jsonObject.get(property)?.getAsJsonObject()

        requiredPackages?.entrySet().each {
            if (!it.key.equalsIgnoreCase('php')) {
                requires.add(it.key);
            }
        }

        return requires
    }

    private Dependency convertToDependency(PackagistDependency dependency) {
        ExternalId id = externalIdFactory.createNameVersionExternalId(Forge.PACKAGIST, dependency.name, dependency.version);
        Dependency newDependency = new Dependency(dependency.name, dependency.version, id);
        return newDependency;
    }

    private void addToGraph(MutableDependencyGraph graph, Dependency parent, List<String> currentRequiredPackages, List<PackagistDependency> allPackages, Boolean root) {
        allPackages.each {
            if (currentRequiredPackages.contains(it.name)){
                Dependency dependency = convertToDependency(it);

                addToGraph(graph, dependency, it.requires, allPackages, false);
                if (root){
                    graph.addChildToRoot(dependency)
                }else{
                    graph.addParentWithChild(parent, dependency)
                }
            }
        }
    }
}
