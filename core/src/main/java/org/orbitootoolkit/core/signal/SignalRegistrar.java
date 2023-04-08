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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.orbitootoolkit.core.api.Signal;
import org.orbitootoolkit.core.api.TaggedValue;
import org.orbitootoolkit.core.service.TaggedValueDesc;
import org.orbitootoolkit.core.util.ReflectionUtility;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SignalRegistrar implements BeanDefinitionRegistryPostProcessor {
    private BeanNameGenerator nameGenerator = DefaultBeanNameGenerator.INSTANCE;

    private static List<TaggedValueDesc> extractTaggedValueDescs(Signal signal) {
        List<TaggedValueDesc> taggedValueDescs = new LinkedList<TaggedValueDesc>();
        TaggedValue[] taggedValues = ArrayUtils.nullToEmpty(signal.subjectTaggedValues(), TaggedValue[].class);
        for (TaggedValue taggedValue : taggedValues) {
            taggedValueDescs.add(new TaggedValueDesc(taggedValue.tag(), taggedValue.value()));
        }
        return Collections.unmodifiableList(taggedValueDescs);
    }

    private void registerSignalBean(BeanDefinitionRegistry registry, String serviceName, Class<?> serviceClass, Method serviceMethod, Signal signal) {
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        propertyValues.addPropertyValue("signalPointName", signal.signalPointName());
        propertyValues.addPropertyValue("signalContractClass", signal.signalContractClass());
        propertyValues.addPropertyValue("subjectClass", signal.subjectClass());
        propertyValues.addPropertyValue("subjectTaggedValues", extractTaggedValueDescs(signal));
        //
        propertyValues.addPropertyValue("serviceName", serviceName);
        propertyValues.addPropertyValue("serviceClass", serviceClass);
        propertyValues.addPropertyValue("serviceMethodName", serviceMethod.getName());
        propertyValues.addPropertyValue("serviceMethodParameterTypes", ReflectionUtility.getMethodParameterTypes(serviceMethod));
        //
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(SignalBean.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setLazyInit(false);
        beanDefinition.setPropertyValues(propertyValues);
        //
        String beanName = nameGenerator.generateBeanName(beanDefinition, registry);
        //
        log.info(" - registering signalBean [" + beanName + "] to method [" + ReflectionUtility.getSimpleName(serviceMethod) + "]");
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private void registerSignalBeans(BeanDefinitionRegistry registry, String serviceName, BeanDefinition serviceDefinition) {
        String serviceClassName = serviceDefinition.getBeanClassName();
        if (StringUtils.isEmpty(serviceClassName)) {
            return;
        }
        //
        Class<?> serviceClass;
        try {
            serviceClass = ClassUtils.forName(serviceClassName, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        //
        Method[] serviceMethods = ArrayUtils.nullToEmpty(MethodUtils.getMethodsWithAnnotation(serviceClass, Signal.class), Method[].class);
        for (Method serviceMethod : serviceMethods) {
            Signal[] signals = ArrayUtils.nullToEmpty(serviceMethod.getAnnotationsByType(Signal.class), Signal[].class);
            for (Signal signal : signals) {
                registerSignalBean(registry, serviceName, serviceClass, serviceMethod, signal);
            }
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("postProcessBeanDefinitionRegistry started:");
        for (String beanDefinitionName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            registerSignalBeans(registry, beanDefinitionName, beanDefinition);
        }
        log.info("postProcessBeanDefinitionRegistry finished:");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // DO NOTHING
    }
}
