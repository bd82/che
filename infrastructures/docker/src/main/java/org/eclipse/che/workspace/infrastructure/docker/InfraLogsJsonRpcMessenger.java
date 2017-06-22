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
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.notification.RemoteSubscriptionManager;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerLogEvent;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Send infrastructure log events using JSON RPC to the clients.
 *
 * @author Anton Korneta
 */
@Singleton
public class InfraLogsJsonRpcMessenger {
    private final RemoteSubscriptionManager remoteSubscriptionManager;

    @Inject
    public InfraLogsJsonRpcMessenger(RemoteSubscriptionManager remoteSubscriptionManager) {
        this.remoteSubscriptionManager = remoteSubscriptionManager;
    }

    @PostConstruct
    private void postConstruct() {
        remoteSubscriptionManager.register("installer/log", InstallerLogEvent.class, this::predicate);
        remoteSubscriptionManager.register("machine/log", InstallerLogEvent.class, this::predicate);
    }

    private boolean predicate(InstallerLogEvent event, Map<String, String> scope) {
        final RuntimeIdentityDto r = event.getRuntimeId();
        return r != null && r.getWorkspaceId().equals(scope.get("workspaceId"));
    }

}
