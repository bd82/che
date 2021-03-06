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
package org.eclipse.che.api.core.jsonrpc.commons.reception;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcErrorTransmitter;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Function configurator to define a function to be applied when we
 * handle incoming JSON RPC request with no params object while the
 * result of a function is a list of objects.
 *
 * @param <R>
 *         type of result object
 */
public class FunctionConfiguratorNoneToMany<R> {
    private final static Logger LOGGER = getLogger(FunctionConfiguratorNoneToMany.class);

    private final RequestHandlerManager handlerManager;

    private final String   method;
    private final Class<R> rClass;

    FunctionConfiguratorNoneToMany(RequestHandlerManager handlerManager, String method, Class<R> rClass) {
        this.handlerManager = handlerManager;

        this.method = method;
        this.rClass = rClass;
    }

    /**
     * Define a function to be applied
     *
     * @param function
     *         function
     */
    public void withFunction(Function<String, List<R>> function) {
        checkNotNull(function, "Request function must not be null");

        LOGGER.debug("Configuring incoming request binary: " +
                     "function for method: " + method + ", " +
                     "result object class: " + rClass);

        handlerManager.registerNoneToMany(method, rClass, function);
    }
}
