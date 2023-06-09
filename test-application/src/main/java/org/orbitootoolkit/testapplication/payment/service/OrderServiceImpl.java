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

import org.orbitootoolkit.core.api.DomainService;
import org.orbitootoolkit.core.api.TaggedValue;
import org.orbitootoolkit.testapplication.payment.api.OrderService;
import org.orbitootoolkit.testapplication.payment.api.PaymentCallback;
import org.orbitootoolkit.testapplication.payment.api.PaymentService;
import org.orbitootoolkit.testapplication.payment.model.ServiceRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    private static final String ORDER_PAYMENT_CALLBACK = "OrderServiceImpl#PaymentCallback";

    @Autowired
    private PaymentService paymentService;

    @Override
    public void orderPayment(String orderId) {
        log.info("orderPayment started: " + orderId);
        paymentService.executePayment(orderId, new BigDecimal("4999.00"), new ServiceRef(ORDER_PAYMENT_CALLBACK));
    }

    @Bean
    @DomainService(servicePointName = "paymentCallback", subjectClass = ServiceRef.class, //
            subjectTaggedValues = @TaggedValue(tag = "value", value = ORDER_PAYMENT_CALLBACK))
    public PaymentCallback getOrderPaymentCallback() {
        return (paymentId, serviceRef) -> log.info("orderPayment finished: " + paymentId);
    }
}
