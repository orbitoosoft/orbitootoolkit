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
package org.orbitootoolkit.core.signal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.orbitootoolkit.core.method.MethodDelegate;
import org.orbitootoolkit.core.method.MethodDelegateRepository;
import org.orbitootoolkit.core.service.DomainServiceDesc;
import org.orbitootoolkit.core.service.DomainServiceDescRepository;
import org.orbitootoolkit.core.service.TaggedValueDesc;
import org.orbitootoolkit.core.util.ReflectionUtility;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SignalBean<SignalContract> implements FactoryBean<SignalContract>, BeanNameAware, BeanFactoryAware, InitializingBean, DisposableBean {
    private String signalPointName = null;
    private Class<SignalContract> signalContractClass = null;
    private Class<?> subjectClass = null;
    private List<TaggedValueDesc> subjectTaggedValues = null;

    private String serviceName = null;
    private Class<?> serviceClass = null;
    private String serviceMethodName = null;
    private List<Class<?>> serviceMethodParameterTypes = null;

    //

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String beanName = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private BeanFactory beanFactory = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Object service = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private MethodDelegate serviceMethodDelegate = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private DomainServiceDesc domainServiceDesc = null;

    //

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @SuppressWarnings("java:S1172")
    private static void checkSignalAndServiceMethod(Class<?> signalContractClass, Class<?> serviceClass, String serviceMethodName, List<Class<?>> serviceMethodParameterTypes) {
        if (!signalContractClass.isAnnotationPresent(FunctionalInterface.class)) {
            throw new IllegalStateException("SignalContractClass should be @FunctionalInterface: " + signalContractClass.getSimpleName());
        }
        //
        Method signalMethod = null;
        Method[] methods = ArrayUtils.nullToEmpty(signalContractClass.getMethods(), Method[].class);
        for (Method method : methods) {
            boolean isInstanceField = ((method.getModifiers() & Modifier.STATIC) == 0);
            boolean isDefault = method.isDefault();
            boolean containsSubject = (MethodDelegate.getMethodSubjectIndex(method) != -1);
            if (isInstanceField && !isDefault && containsSubject) {
                signalMethod = method;
            }
        }
        //
        if (signalMethod == null) {
            throw new IllegalStateException("SignalContractClass doesn't contain an abstract  method with @Subject: " + signalContractClass.getSimpleName());
        }
        //
        List<Class<?>> signalMethodParameterTypes = ReflectionUtility.getMethodParameterTypes(signalMethod);
        if (!ListUtils.isEqualList(signalMethodParameterTypes, serviceMethodParameterTypes)) {
            throw new IllegalStateException("SignalMethod and ServiceMethod have different parameters [" + signalContractClass.getSimpleName() + ", " + serviceClass.getSimpleName() + "]");
        }
    }

    private MethodDelegate getServiceMethodDelegate(Class<?> serviceClass, String serviceMethodName, List<Class<?>> serviceMethodParameterTypes) throws NoSuchMethodException {
        Class<?>[] serviceMethodParameterTypesAsArray = serviceMethodParameterTypes.toArray(Class<?>[]::new);
        Method serviceMethod = serviceClass.getDeclaredMethod(serviceMethodName, serviceMethodParameterTypesAsArray);
        return beanFactory.getBean(MethodDelegateRepository.class).get(serviceMethod);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(signalPointName);
        Objects.requireNonNull(signalContractClass);
        Objects.requireNonNull(subjectClass);
        subjectTaggedValues = ListUtils.emptyIfNull(subjectTaggedValues);
        //
        Objects.requireNonNull(serviceName);
        Objects.requireNonNull(serviceClass);
        Objects.requireNonNull(serviceMethodName);
        serviceMethodParameterTypes = ListUtils.emptyIfNull(serviceMethodParameterTypes);
        //
        checkSignalAndServiceMethod(signalContractClass, serviceClass, serviceMethodName, serviceMethodParameterTypes);
        //
        service = beanFactory.getBean(serviceName);
        serviceMethodDelegate = getServiceMethodDelegate(serviceClass, serviceMethodName, serviceMethodParameterTypes);
        //
        domainServiceDesc = new DomainServiceDesc();
        domainServiceDesc.setServicePointName(signalPointName);
        domainServiceDesc.setSubjectClass(subjectClass);
        domainServiceDesc.setSubjectTaggedValues(subjectTaggedValues);
        domainServiceDesc.setServiceName(beanName);
        //
        beanFactory.getBean(DomainServiceDescRepository.class).addDomainServiceDesc(domainServiceDesc);
    }

    @Override
    public void destroy() throws Exception {
        beanFactory.getBean(DomainServiceDescRepository.class).removeDomainServiceDesc(domainServiceDesc);
    }

    //

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Class<?> getObjectType() {
        return signalContractClass;
    }

    @Override
    public SignalContract getObject() throws Exception {
        return signalContractClass.cast(Proxy.newProxyInstance( //
                Thread.currentThread().getContextClassLoader(), new Class[] { signalContractClass }, //
                (proxy, method, args) -> serviceMethodDelegate.invoke(service, args) //
        ));
    }
}
