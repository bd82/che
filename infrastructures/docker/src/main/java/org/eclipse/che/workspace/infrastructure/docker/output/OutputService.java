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
package org.eclipse.che.workspace.infrastructure.docker.output;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerLogEvent;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Service for handling bootstrapper & installer logs.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class OutputService {
    private final RequestHandlerConfigurator requestHandler;
    private final EventService               eventService;

    @Inject
    public OutputService(RequestHandlerConfigurator requestHandler, EventService eventService) {
        this.requestHandler = requestHandler;
        this.eventService = eventService;
    }

    @PostConstruct
    public void configureMethods() {
        requestHandler.newConfiguration()
                      .methodName("installer/log")
                      .paramsAsDto(InstallerLogEvent.class)
                      .noResult()
                      .withConsumer(this::handleInstallerLog);
    }

    private void handleInstallerLog(InstallerLogEvent installerStatusEvent) {
        //TODO: spi actions here
        eventService.publish(installerStatusEvent);
    }
}
