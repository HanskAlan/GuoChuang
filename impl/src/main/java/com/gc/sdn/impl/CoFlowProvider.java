/*
 * Copyright © 2017 yy and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.gc.sdn.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yy
 */
public class CoFlowProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CoFlowProvider.class);

    private final DataBroker dataBroker;

    private final NotificationPublishService notificationPublishService;

    private final NotificationService notificationService;

    private ListenerRegistration<NotificationListener> registration = null;

    public CoFlowProvider(final DataBroker dataBroker,
                          final NotificationPublishService notificationPublishService,
                          final NotificationService notificationService) {

        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        this.notificationService = notificationService;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("CoflowProvider Session Initiated");
        if (notificationService != null) {
            LOG.info("NotificationService is: " + notificationService.toString());
            CoFlowPacketProcessingListener packetProcessingListener = new CoFlowPacketProcessingListener();
            registration = notificationService.registerNotificationListener(packetProcessingListener);
        }
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("CoflowProvider Closed");
        if (registration != null) {
            registration.close();
        }
    }
}