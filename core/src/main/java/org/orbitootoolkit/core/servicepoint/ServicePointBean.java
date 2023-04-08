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
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import org.apache.commons.lang3.ArrayUtils;
import org.orbitootoolkit.core.method.MethodDelegate;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class ServicePointBean<DomainContract> implements FactoryBean<DomainContract>, BeanNameAware, BeanFactoryAware, InitializingBean {
    private Class<DomainContract> domainContractClass = null;
    private String beanName = null;
    private BeanFactory beanFactory = null;
    private ServicePointDispatcher servicePointDispatcher = null;

    //

    public ServicePointBean(Class<DomainContract> domainContractClass) {
        this.domainContractClass = domainContractClass;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    private static void checkDomainContract(Class<?> domainContractClass) {
        Method[] methods = ArrayUtils.nullToEmpty(domainContractClass.getMethods(), Method[].class);
        for (Method method : methods) {
            boolean isInstanceField = ((method.getModifiers() & Modifier.STATIC) == 0);
            boolean isDefault = method.isDefault();
            boolean containsSubject = (MethodDelegate.getMethodSubjectIndex(method) != -1);
            if (isInstanceField && !isDefault && !containsSubject) {
                throw new IllegalStateException("All abstract methods should specify @Subject: " + domainContractClass.getSimpleName());
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        checkDomainContract(domainContractClass);
        //
        servicePointDispatcher = beanFactory.getBean(ServicePointDispatcher.class);
    }

    //

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Class<?> getObjectType() {
        return domainContractClass;
    }

    @Override
    public DomainContract getObject() throws Exception {
        return domainContractClass.cast(Proxy.newProxyInstance( //
                Thread.currentThread().getContextClassLoader(), new Class[] { domainContractClass }, //
                (proxy, method, args) -> servicePointDispatcher.invoke(beanName, method, args) //
        ));
    }
}
