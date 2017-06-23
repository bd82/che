package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.notification.RemoteEventService;
import org.eclipse.che.api.workspace.shared.dto.event.MachineLogEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Anton Korneta
 */
@Singleton
public class MachineLogger {
    private final RemoteEventService remoteEventService;

    @Inject
    public MachineLogger(RemoteEventService remoteEventService) {
        this.remoteEventService = remoteEventService;
    }

public void publish(MachineLogEvent logEvent) {
        remoteEventService.publish("machine/log",
                                   logEvent,
                                   (event, scope) -> event.getRuntimeId()
                                                          .getWorkspaceId()
                                                          .equals(scope.get("workspaceId")));
    }
}
