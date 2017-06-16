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
package org.eclipse.che.plugin.testing.ide.model.info;

import org.eclipse.che.plugin.testing.ide.model.TestState;

/**
 *
 */
public class SuiteInProgressInfo extends TestInProgressInfo {

    private final TestState testState;

    public SuiteInProgressInfo(TestState testState) {
        this.testState = testState;
    }

    @Override
    public boolean isProblem() {
        for (TestState state : testState.getChildren()) {
            if (state.isProblem()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TestStateDescription getDescription() {
        return TestStateDescription.RUNNING;
    }
}