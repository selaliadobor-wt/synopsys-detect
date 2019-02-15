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
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detect.configuration.DetectConfiguration;
import com.synopsys.integration.detect.configuration.DetectProperty;
import com.synopsys.integration.detect.configuration.PropertyAuthority;
import com.synopsys.integration.detect.util.executable.Executable;
import com.synopsys.integration.detect.util.executable.ExecutableOutput;
import com.synopsys.integration.detect.util.executable.ExecutableRunner;
import com.synopsys.integration.detect.workflow.codelocation.DetectCodeLocation;
import com.synopsys.integration.detect.workflow.codelocation.DetectCodeLocationType;
import com.synopsys.integration.detect.workflow.extraction.Extraction;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CarthageLockExtractor {
    public static final String OUTPUT_FILE = "detect_yarn_proj_dependencies.txt";
    public static final String ERROR_FILE = "detect_yarn_error.txt";

    private final ExternalIdFactory externalIdFactory;
    private final CarthageListParser carthageListParser;

    public CarthageLockExtractor(final ExternalIdFactory externalIdFactory, final CarthageListParser carthageListParser) {
        this.externalIdFactory = externalIdFactory;
        this.carthageListParser = carthageListParser;
    }

    public Extraction extract(final File directory, final File carthageFile) {
        try {
            final List<String> carthageFileText = Files.readAllLines(carthageFile.toPath(), StandardCharsets.UTF_8);


            final DependencyGraph dependencyGraph = carthageListParser.parseCarthageFile(carthageFileText);

            final ExternalId externalId = externalIdFactory.createPathExternalId(Forge.COCOAPODS, directory.getCanonicalPath());
            final DetectCodeLocation detectCodeLocation = new DetectCodeLocation.Builder(DetectCodeLocationType.COCOAPODS, directory.getCanonicalPath(), externalId, dependencyGraph).build();

            return new Extraction.Builder().success(detectCodeLocation).build();
        } catch (final Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }

}
