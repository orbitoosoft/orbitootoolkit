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

import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.orbitootoolkit.core.api.ServicePoint;
import org.orbitootoolkit.core.api.ServicePointReference;
import org.orbitootoolkit.core.api.ServicePointReferenceByName;
import org.orbitootoolkit.core.api.ServicePointScan;
import org.orbitootoolkit.core.util.BeanNameGeneratorUtility;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServicePointRegistrar implements ImportBeanDefinitionRegistrar {
    private static final String PROPERTY_BASE_PACKAGES = "basePackages";
    private static final String PROPERTY_NAME_GENERATOR = "nameGenerator";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        log.info("registerBeanDefinitions started:");
        //
        MultiValueMap<String, Object> annotationAttributes = annotationMetadata.getAllAnnotationAttributes(ServicePointScan.class.getName(), false);
        if (annotationAttributes == null) {
            throw new IllegalStateException("ServicePointRegistrar should be imported by @ServicePointScan annotation only.");
        }
        String[] basePackages = ArrayUtils.nullToEmpty((String[]) annotationAttributes.getFirst(PROPERTY_BASE_PACKAGES));
        BeanNameGenerator beanNameGenerator = BeanNameGeneratorUtility.getOrCreateBeanNameGenerator((Class<?>) annotationAttributes.getFirst(PROPERTY_NAME_GENERATOR));
        //
        for (String basePackage : basePackages) {
            log.info(" - scanning package: " + basePackage);
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false) {
                @Override
                protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                    return beanDefinition.getMetadata().isInterface();
                }
            };
            provider.addIncludeFilter(new AnnotationTypeFilter(ServicePoint.class, true, true));
            //
            Set<BeanDefinition> domainContractDefinitions = SetUtils.emptyIfNull(provider.findCandidateComponents(basePackage));
            for (BeanDefinition domainContractDefinition : domainContractDefinitions) {
                String domainContractClassName = domainContractDefinition.getBeanClassName();
                if (StringUtils.isEmpty(domainContractClassName)) {
                    continue;
                }
                //
                Class<?> domainContractClass;
                try {
                    domainContractClass = ClassUtils.forName(domainContractClassName, Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException ex) {
                    throw new IllegalStateException(ex);
                }
                //
                String beanName = beanNameGenerator.generateBeanName(domainContractDefinition, beanDefinitionRegistry);
                //
                ConstructorArgumentValues argumentValues = new ConstructorArgumentValues();
                argumentValues.addGenericArgumentValue(domainContractClass);
                //
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(ServicePointBean.class);
                beanDefinition.setConstructorArgumentValues(argumentValues);
                beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
                beanDefinition.setLazyInit(false);
                beanDefinition.addQualifier(new AutowireCandidateQualifier(ServicePointReference.class));
                beanDefinition.addQualifier(new AutowireCandidateQualifier(ServicePointReferenceByName.class, beanName));
                //
                log.info(" - registering service point [" + beanName + ", " + domainContractClass.getSimpleName() + "]");
                beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
            }
        }
        //
        log.info("registerBeanDefinitions finished:");
    }
}
