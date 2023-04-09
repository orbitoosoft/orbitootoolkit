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

import org.orbitootoolkit.core.api.Signal;
import org.orbitootoolkit.core.api.TaggedValue;
import org.orbitootoolkit.testapplication.payment.api.CallbackHandler;
import org.orbitootoolkit.testapplication.payment.api.LoanService;
import org.orbitootoolkit.testapplication.payment.api.PaymentService;
import org.orbitootoolkit.testapplication.payment.model.Callback;
import org.orbitootoolkit.testapplication.payment.model.CallbackTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoanServiceImpl implements LoanService {
    @Autowired
    private PaymentService paymentService;

    @Override
    public void loanPayment(String loanId) {
        log.info("loanPayment started: " + loanId);
        paymentService.createPayment(loanId, new BigDecimal("4999.00"), new Callback(CallbackTarget.LOAN_SERVICE, loanId));
    }

    @Signal(servicePointName = "callbackServicePoint", servicePointClass = CallbackHandler.class, //
            subjectClass = Callback.class, subjectTaggedValues = @TaggedValue(tag = "target", value = "LOAN_SERVICE"))
    public void acceptLoanPaymentCallback(Callback callback) {
        log.info("loanPayment finished: " + callback);
    }
}
