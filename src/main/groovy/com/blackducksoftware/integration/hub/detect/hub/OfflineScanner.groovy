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
package com.blackducksoftware.integration.hub.detect.hub

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.HubSupportHelper
import com.blackducksoftware.integration.hub.cli.SimpleScanService
import com.blackducksoftware.integration.hub.global.HubCredentials
import com.blackducksoftware.integration.hub.global.HubServerConfig
import com.blackducksoftware.integration.hub.scan.HubScanConfig
import com.blackducksoftware.integration.log.Slf4jIntLogger
import com.blackducksoftware.integration.util.CIEnvironmentVariables
import com.google.gson.Gson

@Component
class OfflineScanner {
    private static final Logger logger = LoggerFactory.getLogger(OfflineScanner.class)

    @Autowired
    Gson gson

    void offlineScan(HubScanConfig hubScanConfig) {
        def intLogger = new Slf4jIntLogger(logger)

        def offlineCredentials = createOfflineCredentials()

        //        def hubServerConfig = new HubServerConfig(new URL('http://www.blackducksoftware.com'), 0, offlineCredentials, null, true)
        def hubServerConfig = new HubServerConfig(null, 0, null, null, false)

        def hubSupportHelper = new HubSupportHelper()
        hubSupportHelper.setHub3_7Support()
        hubSupportHelper.setHasBeenChecked(true)

        def ciEnvironmentVariables = new CIEnvironmentVariables()
        ciEnvironmentVariables.putAll(System.getenv())

        def simpleScanService = new SimpleScanService(intLogger, gson, hubServerConfig, hubSupportHelper, ciEnvironmentVariables, hubScanConfig, null, null)
        simpleScanService.setupAndExecuteScan()
    }

    public HubCredentials createOfflineCredentials() {
        def credentials = new HubCredentials('', 'notblank')
        credentials
    }
}
