/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.testing.server;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.testing.server.framework.TestFrameworkRegistry;
import org.eclipse.che.api.testing.server.framework.TestMessagesOutputTransmitter;
import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.api.testing.shared.Constants;
import org.eclipse.che.api.testing.shared.TestDetectionContext;
import org.eclipse.che.api.testing.shared.TestDetectionResult;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestPosition;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Singleton
public class TestingRPCService {

    private final RequestTransmitter requestTransmitter;
    private final TestFrameworkRegistry frameworkRegistry;
    private String endpoint;
    private TestMessagesOutputTransmitter outputTransmitter;

    @Inject
    public TestingRPCService(RequestTransmitter requestTransmitter, TestFrameworkRegistry frameworkRegistry) {
        this.requestTransmitter = requestTransmitter;
        this.frameworkRegistry = frameworkRegistry;
    }

    @Inject
    private void configureRunTestHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                .methodName(Constants.RUN_TESTS_METHOD)
                .paramsAsDto(TestExecutionContext.class)
                .resultAsBoolean()
                .withBiFunction(this::runTests);
    }

    @Inject
    private void configureTestDetectionHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                .methodName(Constants.TESTING_RPC_TEST_DETECTION_NAME)
                .paramsAsDto(TestDetectionContext.class)
                .resultAsDto(TestDetectionResult.class)
                .withBiFunction(this::handleTestDetection);
    }

    private boolean runTests(String endpoint, TestExecutionContext context) {
        this.endpoint = endpoint;
        TestRunner testRunner = frameworkRegistry.getTestRunner(context.getFrameworkName());

        if (testRunner != null) {
            if (outputTransmitter != null) {
                outputTransmitter.stop();
            }
            ProcessHandler processHandler = testRunner.execute(context);
            outputTransmitter = new TestMessagesOutputTransmitter(processHandler, requestTransmitter, endpoint);
            return true;
        } else {
            //TODO add logging and send info message about failure
            return false;
        }
    }


    private TestDetectionResult handleTestDetection(String endpointId, TestDetectionContext request) {
        TestDetectionResult result = DtoFactory.newDto(TestDetectionResult.class);

        List<TestPosition> testPositions = new ArrayList<>();
        for (TestRunner testRunner : frameworkRegistry.getAllTestRunners()) {
            testPositions.addAll(testRunner.detectTests(request));
        }

        result.setTestPosition(testPositions);
        result.setTestFile(!testPositions.isEmpty());
        return result;
    }
}