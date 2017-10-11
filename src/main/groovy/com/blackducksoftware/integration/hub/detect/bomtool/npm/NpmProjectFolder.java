/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.detect.bomtool.npm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.blackducksoftware.integration.hub.detect.bomtool.NpmBomTool;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class NpmProjectFolder {
    private final String path;
    private final File nodeModulesDirectory;

    private NpmPackageJson packageJson = null;

    public NpmProjectFolder(final String path) {
        this.path = path;
        nodeModulesDirectory = new File(path, NpmBomTool.NODE_MODULES);
    }

    public NpmProjectFolder(final File projectDirectory) {
        this.path = projectDirectory.getPath();
        this.nodeModulesDirectory = new File(projectDirectory, NpmBomTool.NODE_MODULES);
    }

    public NpmProjectFolder getParentNpmProject() {
        File nodeModulesParent = nodeModulesDirectory.getParentFile();

        while (nodeModulesParent != null) {
            if (NpmBomTool.NODE_MODULES.equals(nodeModulesParent.getName())) {
                return new NpmProjectFolder(nodeModulesParent.getParentFile());
            }

            nodeModulesParent = nodeModulesParent.getParentFile();
        }

        return null;
    }

    public NpmProjectFolder getChildNpmProjectFromNodeModules(final String npmProjectName) {
        final File projectFolder = new File(nodeModulesDirectory, npmProjectName);
        if (projectFolder.exists()) {
            return new NpmProjectFolder(projectFolder);
        }

        return null;
    }

    public NpmPackageJson getPackageJson(final Gson gson) {
        if (packageJson == null) {
            try {
                final File packageJsonFile = new File(path, NpmBomTool.PACKAGE_JSON);
                final FileReader fileReader = new FileReader(packageJsonFile);
                final JsonReader jsonReader = new JsonReader(fileReader);
                final NpmPackageJson newPackageJson = gson.fromJson(jsonReader, NpmPackageJson.class);
                packageJson = newPackageJson;
            } catch (final FileNotFoundException e) {
                packageJson = new NpmPackageJson();
            }
        }

        return packageJson;
    }

    public String getPath() {
        return path;
    }

    public File getNodeModulesDirectory() {
        return nodeModulesDirectory;
    }
}
