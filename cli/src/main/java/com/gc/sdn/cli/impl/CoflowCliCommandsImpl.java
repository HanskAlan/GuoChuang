/*
 * Copyright © 2017 yy and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.gc.sdn.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gc.sdn.cli.api.CoflowCliCommands;

public class CoflowCliCommandsImpl implements CoflowCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(CoflowCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public CoflowCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("CoflowCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}