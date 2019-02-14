package com.synopsys.integration.detect.util.executable;

public class NpmExecutableResolverOptions {
    private final String npmPath;
    private final String npmNodePath;

    public NpmExecutableResolverOptions(final String npmPath, final String npmNodePath) {
        this.npmPath = npmPath;
        this.npmNodePath = npmNodePath;
    }

    public String getNpmPath() {
        return npmPath;
    }

    public String getNpmNodePath() {
        return npmNodePath;
    }
}
