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

import com.synopsys.integration.detect.detector.*;
import com.synopsys.integration.detect.util.executable.CacheableExecutableFinder;
import com.synopsys.integration.detect.util.executable.CacheableExecutableFinder.CacheableExecutableType;
import com.synopsys.integration.detect.workflow.extraction.Extraction;
import com.synopsys.integration.detect.workflow.file.DetectFileFinder;
import com.synopsys.integration.detect.workflow.search.result.DetectorResult;
import com.synopsys.integration.detect.workflow.search.result.ExecutableNotFoundDetectorResult;
import com.synopsys.integration.detect.workflow.search.result.FileNotFoundDetectorResult;
import com.synopsys.integration.detect.workflow.search.result.PassedDetectorResult;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class CarthageLockDetector extends Detector {
    private static final String CARTHAGE_RESOLVED_FILENAME = "Cartfile.resolved";

    private final DetectFileFinder fileFinder;
    private final CarthageLockExtractor carthageLockExtractor;

    private File carthageFile;

    public CarthageLockDetector(final DetectorEnvironment environment, final DetectFileFinder fileFinder, final CarthageLockExtractor carthageLockExtractor) {
        super(environment, "Carthage File", DetectorType.CARTHAGE);
        this.fileFinder = fileFinder;
        this.carthageLockExtractor = carthageLockExtractor;
    }

    @Override
    public DetectorResult applicable() {
        carthageFile = fileFinder.findFile(environment.getDirectory(), CARTHAGE_RESOLVED_FILENAME);
        if (carthageFile == null) {
            return new FileNotFoundDetectorResult(CARTHAGE_RESOLVED_FILENAME);
        }

        return new PassedDetectorResult();
    }

    @Override
    public DetectorResult extractable() {
        return new PassedDetectorResult();
    }

    @Override
    public Extraction extract(final ExtractionId extractionId) {
        return carthageLockExtractor.extract(environment.getDirectory(), carthageFile);
    }

}
