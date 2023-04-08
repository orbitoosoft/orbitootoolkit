/*-
 * ========================LICENSE_START=================================
 * orbitoo-toolkit-core
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
package org.orbitootoolkit.core.servicepoint;

import java.lang.reflect.Method;

import org.orbitootoolkit.core.method.MethodDelegate;
import org.orbitootoolkit.core.method.MethodDelegateRepository;
import org.orbitootoolkit.core.service.DomainServiceDesc;
import org.orbitootoolkit.core.service.DomainServiceDescRepository;
import org.orbitootoolkit.core.util.ReflectionUtility;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class ServicePointDispatcher {
    @Autowired
    private BeanFactory beanFactory = null;

    @Autowired
    private MethodDelegateRepository methodDelegateRepository = null;

    @Autowired
    private DomainServiceDescRepository domainServiceDescRepository = null;

    public Object invoke(String servicePointName, Method method, Object[] args) throws Throwable {
        log.debug("invoke started [" + servicePointName + ", " + ReflectionUtility.getSimpleName(method) + "]");
        MethodDelegate methodDelegate = methodDelegateRepository.get(method);
        Object subject = methodDelegate.getSubject(args);
        if (subject == null) {
            throw new NotFoundException("Cannot find subject for: " + servicePointName);
        }
        DomainServiceDesc domainServiceDesc = domainServiceDescRepository.findDomainServiceDesc(servicePointName, subject);
        if (domainServiceDesc == null) {
            throw new NotFoundException("Cannot find service for: " + servicePointName);
        }
        Object service = beanFactory.getBean(domainServiceDesc.getServiceName());
        //
        try {
            Object result = methodDelegate.invoke(service, args);
            log.debug("invoke finished:");
            return result;
        } catch (Throwable throwable) {
            log.debug("invoked finished with exception: " + throwable.getClass().getSimpleName());
            throw throwable;
        }
    }
}
