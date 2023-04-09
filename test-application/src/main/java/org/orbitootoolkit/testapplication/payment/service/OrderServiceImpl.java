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

import org.orbitootoolkit.core.api.SignalMapping;
import org.orbitootoolkit.core.api.TaggedValue;
import org.orbitootoolkit.testapplication.payment.api.CallbackHandler;
import org.orbitootoolkit.testapplication.payment.api.OrderService;
import org.orbitootoolkit.testapplication.payment.api.PaymentService;
import org.orbitootoolkit.testapplication.payment.model.Callback;
import org.orbitootoolkit.testapplication.payment.model.CallbackTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private PaymentService paymentService;

    @Override
    public void orderPayment(String orderId) {
        log.info("orderPayment started: " + orderId);
        paymentService.createPayment(orderId, new BigDecimal("10000.00"), new Callback(CallbackTarget.ORDER_SERVICE, orderId));
    }

    @SignalMapping(servicePointName = "callbackServicePoint", servicePointClass = CallbackHandler.class, //
            subjectClass = Callback.class, subjectTaggedValues = @TaggedValue(tag = "target", value = "ORDER_SERVICE"))
    public void acceptOrderPaymentCallback(Callback callback) {
        log.info("orderPayment finished: " + callback);
    }
}
