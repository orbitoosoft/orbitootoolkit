/*-
 * ========================LICENSE_START=================================
 * orbitoo-toolkit-test-application
 * %%
 * Copyright (C) 2023 orbitoo-soft
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * =========================LICENSE_END==================================
 */
package org.orbitootoolkit.testapplication.payment.service;

import java.math.BigDecimal;
import java.time.Duration;

import org.apache.commons.lang3.ThreadUtils;
import org.orbitootoolkit.core.api.ServicePointReference;
import org.orbitootoolkit.testapplication.payment.api.PaymentService;
import org.orbitootoolkit.testapplication.payment.api.PaymentServiceCallback;
import org.orbitootoolkit.testapplication.payment.model.ServiceRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    @ServicePointReference
    private PaymentServiceCallback paymentServiceCallback;

    private static void sleep(Duration duration) {
        try {
            ThreadUtils.sleep(duration);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Async
    @Override
    public void executePayment(String paymentId, BigDecimal amount, ServiceRef callbackServiceRef) {
        // simulate the payment
        log.info("payment started [" + paymentId + ", " + amount + "]");
        sleep(Duration.ofSeconds(3));
        log.info("payment finished [" + paymentId + ", " + amount + "]");
        // send the callback
        sleep(Duration.ofSeconds(1));
        paymentServiceCallback.paymentExecuted(paymentId, callbackServiceRef);
    }
}
