package com.synopsys.integration.detectable.detectable.executable.resolver;

import java.io.File;

import com.synopsys.integration.detectable.detectable.exception.DetectableException;

public interface YarnResolver {
    File resolveYarn() throws DetectableException;
}
