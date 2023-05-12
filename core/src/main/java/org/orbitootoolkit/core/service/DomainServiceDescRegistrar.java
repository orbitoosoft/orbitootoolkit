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
package org.orbitootoolkit.core.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.orbitootoolkit.core.api.DomainService;
import org.orbitootoolkit.core.api.TaggedValue;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DomainServiceDescRegistrar implements BeanDefinitionRegistryPostProcessor {
    private BeanNameGenerator nameGenerator = DefaultBeanNameGenerator.INSTANCE;

    private static List<TaggedValueDesc> extractTaggedValueDescs(DomainService domainService) {
        List<TaggedValueDesc> taggedValueDescs = new LinkedList<TaggedValueDesc>();
        TaggedValue[] taggedValues = ArrayUtils.nullToEmpty(domainService.subjectTaggedValues(), TaggedValue[].class);
        for (TaggedValue taggedValue : taggedValues) {
            taggedValueDescs.add(new TaggedValueDesc(taggedValue.tag(), taggedValue.value()));
        }
        return Collections.unmodifiableList(taggedValueDescs);
    }

    private void registerDomainServiceDesc(BeanDefinitionRegistry registry, String serviceName, DomainService domainService) {
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        propertyValues.addPropertyValue("serviceName", serviceName);
        propertyValues.addPropertyValue("servicePointName", domainService.servicePointName());
        propertyValues.addPropertyValue("subjectClass", domainService.subjectClass());
        propertyValues.addPropertyValue("subjectTaggedValues", extractTaggedValueDescs(domainService));
        //
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DomainServiceDesc.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setLazyInit(false);
        beanDefinition.setPropertyValues(propertyValues);
        //
        String beanName = nameGenerator.generateBeanName(beanDefinition, registry);
        //
        log.info(" - registering bean [" + serviceName + ", " + domainService.toString() + "]");
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    @SuppressWarnings("java:S6204")
    private List<DomainService> getDomainServices(AnnotatedTypeMetadata metadata) {
        MergedAnnotations mergedAnnotations = (metadata != null) ? metadata.getAnnotations() : null;
        if ((mergedAnnotations != null) && mergedAnnotations.isDirectlyPresent(DomainService.class)) {
            return mergedAnnotations.stream(DomainService.class) //
                    .filter(MergedAnnotation::isDirectlyPresent) //
                    .map(MergedAnnotation::synthesize) //
                    .collect(Collectors.toUnmodifiableList());
        } else {
            return Collections.emptyList();
        }
    }

    private List<DomainService> getDomainServices(BeanDefinition beanDefinition) {
        if (beanDefinition instanceof AnnotatedBeanDefinition) {
            AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
            //
            List<DomainService> factoryMethodDomainServices = getDomainServices(annotatedBeanDefinition.getFactoryMethodMetadata());
            if (CollectionUtils.isNotEmpty(factoryMethodDomainServices)) {
                return factoryMethodDomainServices;
            }
            //
            return getDomainServices(annotatedBeanDefinition.getMetadata());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("postProcessBeanDefinitionRegistry started:");
        for (String beanDefinitionName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            List<DomainService> domainServices = getDomainServices(beanDefinition);
            for (DomainService domainService : domainServices) {
                registerDomainServiceDesc(registry, beanDefinitionName, domainService);
            }
        }
        log.info("postProcessBeanDefinitionRegistry finished:");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // DO NOTHING
    }
}
