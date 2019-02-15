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
package com.synopsys.integration.detect.detector;

import com.synopsys.integration.detect.detector.carthage.CarthageLockDetector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.synopsys.integration.detect.detector.bitbake.BitbakeDetector;
import com.synopsys.integration.detect.detector.clang.ClangDetector;
import com.synopsys.integration.detect.detector.cocoapods.PodlockDetector;
import com.synopsys.integration.detect.detector.conda.CondaCliDetector;
import com.synopsys.integration.detect.detector.cpan.CpanCliDetector;
import com.synopsys.integration.detect.detector.cran.PackratLockDetector;
import com.synopsys.integration.detect.detector.go.GoCliDetector;
import com.synopsys.integration.detect.detector.go.GoLockDetector;
import com.synopsys.integration.detect.detector.go.GoVendorDetector;
import com.synopsys.integration.detect.detector.go.GoVndrDetector;
import com.synopsys.integration.detect.detector.gradle.GradleInspectorDetector;
import com.synopsys.integration.detect.detector.hex.RebarDetector;
import com.synopsys.integration.detect.detector.maven.MavenPomDetector;
import com.synopsys.integration.detect.detector.maven.MavenPomWrapperDetector;
import com.synopsys.integration.detect.detector.npm.NpmCliDetector;
import com.synopsys.integration.detect.detector.npm.NpmPackageLockDetector;
import com.synopsys.integration.detect.detector.npm.NpmShrinkwrapDetector;
import com.synopsys.integration.detect.detector.nuget.NugetProjectDetector;
import com.synopsys.integration.detect.detector.nuget.NugetSolutionDetector;
import com.synopsys.integration.detect.detector.packagist.ComposerLockDetector;
import com.synopsys.integration.detect.detector.pear.PearCliDetector;
import com.synopsys.integration.detect.detector.pip.PipInspectorDetector;
import com.synopsys.integration.detect.detector.pip.PipenvDetector;
import com.synopsys.integration.detect.detector.rubygems.GemlockDetector;
import com.synopsys.integration.detect.detector.sbt.SbtResolutionCacheDetector;
import com.synopsys.integration.detect.detector.yarn.YarnLockDetector;
import com.synopsys.integration.detect.tool.bazel.BazelDetector;
import com.synopsys.integration.detect.tool.docker.DockerDetector;

public class DetectorFactory implements BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public BazelDetector createBazelDetector(final DetectorEnvironment environment) {
        return beanFactory.getBean(BazelDetector.class, environment);
    }

    public DockerDetector createDockerDetector(final DetectorEnvironment environment) {
        return beanFactory.getBean(DockerDetector.class, environment);
    }

    public BitbakeDetector createBitbakeBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(BitbakeDetector.class, environment);
    }

    public ClangDetector createClangBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(ClangDetector.class, environment);
    }

    public ComposerLockDetector createComposerLockBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(ComposerLockDetector.class, environment);
    }

    public CondaCliDetector createCondaBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(CondaCliDetector.class, environment);
    }

    public CpanCliDetector createCpanCliBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(CpanCliDetector.class, environment);
    }

    public GemlockDetector createGemlockBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(GemlockDetector.class, environment);
    }

    public GoCliDetector createGoCliBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(GoCliDetector.class, environment);
    }

    public GoLockDetector createGoLockBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(GoLockDetector.class, environment);
    }

    public GoVndrDetector createGoVndrBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(GoVndrDetector.class, environment);
    }

    public GoVendorDetector createGoVendorBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(GoVendorDetector.class, environment);
    }

    public GradleInspectorDetector createGradleInspectorBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(GradleInspectorDetector.class, environment);
    }

    public MavenPomDetector createMavenPomBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(MavenPomDetector.class, environment);
    }

    public MavenPomWrapperDetector createMavenPomWrapperBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(MavenPomWrapperDetector.class, environment);
    }

    public NpmCliDetector createNpmCliBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(NpmCliDetector.class, environment);
    }

    public NpmPackageLockDetector createNpmPackageLockBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(NpmPackageLockDetector.class, environment);
    }

    public NugetProjectDetector createNugetProjectBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(NugetProjectDetector.class, environment);
    }

    public NpmShrinkwrapDetector createNpmShrinkwrapBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(NpmShrinkwrapDetector.class, environment);
    }

    public NugetSolutionDetector createNugetSolutionBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(NugetSolutionDetector.class, environment);
    }

    public PackratLockDetector createPackratLockBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(PackratLockDetector.class, environment);
    }

    public PearCliDetector createPearCliBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(PearCliDetector.class, environment);
    }

    public PipenvDetector createPipenvBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(PipenvDetector.class, environment);
    }

    public PipInspectorDetector createPipInspectorBomTool(final DetectorEnvironment environment) {
        //final String requirementsFile = detectConfiguration.getProperty(DetectProperty.DETECT_PIP_REQUIREMENTS_PATH, PropertyAuthority.None);
        return beanFactory.getBean(PipInspectorDetector.class, environment);
    }

    public PodlockDetector createPodLockBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(PodlockDetector.class, environment);
    }

    public RebarDetector createRebarBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(RebarDetector.class, environment);
    }

    public SbtResolutionCacheDetector createSbtResolutionCacheBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(SbtResolutionCacheDetector.class, environment);
    }

    public YarnLockDetector createYarnLockBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(YarnLockDetector.class, environment);
    }

    public CarthageLockDetector createCarthageBomTool(final DetectorEnvironment environment) {
        return beanFactory.getBean(CarthageLockDetector.class, environment);
    }
}
