/**
 * synopsys-detect
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.detect.detector.carthage;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CarthageListParser extends BaseCarthageParser {
    private final Logger logger = LoggerFactory.getLogger(CarthageListParser.class);
    private final ExternalIdFactory externalIdFactory;

    public static final Pattern GITHUB_LINE_PATTERN = Pattern.compile("^github \\\"(.*)\\\" .+ (.*)\\s*");
    public static final Pattern GIT_TAG_COMMIT_PATTERN = Pattern.compile("^git\\S* \\\"(.*)\\\" \\\"(.*)\\\"\\s*");


    public CarthageListParser(final ExternalIdFactory externalIdFactory) {
        this.externalIdFactory = externalIdFactory;
    }

    public DependencyGraph parseCarthageFile(final List<String> carthageFileContents) {
        final MutableDependencyGraph graph = new MutableMapDependencyGraph();
        for (String line : carthageFileContents) {

            if (line.isEmpty()) {
                continue;
            }

            Dependency dependency = parseDependencyFromLine(line);
            if(dependency != null){
                graph.addChildToRoot(parseDependencyFromLine(line));
            }
        }

        return graph;
    }


    public Dependency parseDependencyFromLine(String line) {
        String product = "", version = "";
        boolean found = false;
        final Matcher githubMatcher = GITHUB_LINE_PATTERN.matcher(line);
        final Matcher gitMatcher = GIT_TAG_COMMIT_PATTERN.matcher(line);

        if (githubMatcher.find()) {
            String[] pathComponents = githubMatcher.group(1).split("/");


            if (pathComponents.length > 0) {
                product = pathComponents[pathComponents.length - 1];
                version = githubMatcher.group(2);
            }

            found = true;
        } else if (gitMatcher.find()) {
            String[] pathComponents = gitMatcher.group(1).split("/");

            if (pathComponents.length > 0) {
                product = pathComponents[pathComponents.length - 1];
                version = gitMatcher.group(2);
            }

            found = true;
        }

        ExternalId externalId = externalIdFactory.createNameVersionExternalId(Forge.COCOAPODS,product,version);

        //If we've found it with the version then add as much information about it as we can
        if (found) {
            return new Dependency(product,version,externalId);
        }else{
            return null;
        }
    }

}
