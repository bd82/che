/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.openshift.client;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.openshift.api.model.DoneableRoute;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteFluent.SpecNested;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

@Singleton
public class OpenShiftRouteCreator {
    private static final Logger LOG = LoggerFactory.getLogger(OpenShiftRouteCreator.class);
    private static final String TLS_TERMINATION_EDGE = "edge";
    private static final String REDIRECT_INSECURE_EDGE_TERMINATION_POLICY = "Redirect";

    public void createRoute (final String namespace,
                             final String workspaceName,
                             final String openShiftNamespaceExternalAddress,
                             final String serverRef,
                             final String serviceName,
                             final boolean enableTls) {

        if (openShiftNamespaceExternalAddress == null) {
            throw new IllegalArgumentException("Property che.docker.ip.external must be set when using openshift.");
        }

        try (OpenShiftClient openShiftClient = new DefaultOpenShiftClient()) {
            String routeName = generateRouteName(workspaceName, serverRef);
            String serviceHost = generateRouteHost(routeName, openShiftNamespaceExternalAddress);
               SpecNested<DoneableRoute> routeSpec = openShiftClient
                    .routes()
                    .inNamespace(namespace)
                    .createNew()
                    .withNewMetadata()
                      .withName(routeName)
                      .addToLabels(OpenShiftConnector.OPENSHIFT_DEPLOYMENT_LABEL, serviceName)
                    .endMetadata()
                    .withNewSpec()
                      .withHost(serviceHost)
                      .withNewTo()
                        .withKind("Service")
                        .withName(serviceName)
                      .endTo()
                      .withNewPort()
                        .withNewTargetPort()
                          .withStrVal(serverRef)
                        .endTargetPort()
                      .endPort();

            if (enableTls) {
                routeSpec.withNewTls()
                             .withTermination(TLS_TERMINATION_EDGE)
                             .withInsecureEdgeTerminationPolicy(REDIRECT_INSECURE_EDGE_TERMINATION_POLICY)
                         .endTls();
            }

            Route route = routeSpec.endSpec().done();

            LOG.info("OpenShift route {} created", route.getMetadata().getName());
        }
    }

    private String generateRouteName(final String workspaceName, final String serverRef) {
        return serverRef + "-" + workspaceName;
    }

    private String generateRouteHost(final String routeName, final String openShiftNamespaceExternalAddress) {
        return routeName + "-"  + openShiftNamespaceExternalAddress;
    }
}