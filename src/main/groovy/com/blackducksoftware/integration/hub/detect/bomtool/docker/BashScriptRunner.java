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
package com.blackducksoftware.integration.hub.detect.bomtool.docker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.detect.util.executable.Executable;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableManager;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableOutput;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunnerException;

@Component
public class BashScriptRunner {

    @Autowired
    ExecutableRunner executableRunner;

    @Autowired
    ExecutableManager executableManager;

    ExecutableOutput runScript(final File scriptFile, final String bashExecutable, final File workingDirectory, final List<String> scriptArgs) throws ExecutableRunnerException, IOException {
        return runScript(scriptFile, bashExecutable, workingDirectory, new HashMap<String, String>(), scriptArgs);
    }

    ExecutableOutput runScript(final File scriptFile, final String bashExecutable, final File workingDirectory, final Map<String, String> environmentVariables, final List<String> scriptArgs) throws IOException, ExecutableRunnerException {
        if (SystemUtils.IS_OS_WINDOWS) {
            final List<String> bashArgs = new ArrayList<>();
            bashArgs.add("-c");
            String script = "\"";
            script += scriptFile.getCanonicalPath().replace("\\", "/");
            for (final String arg : scriptArgs) {
                script += " " + arg.replace("\\", "/");
            }
            script += "\"";
            bashArgs.add(script);
            final Executable executable = new Executable(workingDirectory, bashExecutable, bashArgs);
            return executableRunner.execute(executable);
        } else {
            final List<String> bashArgs = new ArrayList<>();
            bashArgs.add("-c");
            bashArgs.add("\"" + scriptFile.getCanonicalPath() + "\"");
            final Executable executable = new Executable(workingDirectory, bashExecutable, bashArgs);
            return executableRunner.execute(executable);
        }
    }
}
