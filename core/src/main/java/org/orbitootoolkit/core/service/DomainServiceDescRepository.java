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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.orbitootoolkit.core.property.Property;
import org.orbitootoolkit.core.property.PropertySupplierRepository;
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
public class DomainServiceDescRepository {
    private Map<DomainServiceKey, DomainServiceDesc> domainServiceMap = new ConcurrentHashMap<DomainServiceKey, DomainServiceDesc>();

    @Autowired
    private PropertySupplierRepository propertySupplierRepository = null;

    public void addDomainServiceDesc(DomainServiceDesc domainServiceDesc) {
        domainServiceMap.put(DomainServiceKey.from(domainServiceDesc), domainServiceDesc);
        log.info("added domainServiceDesc: " + domainServiceDesc);
    }

    public void removeDomainServiceDesc(DomainServiceDesc domainServiceDesc) {
        domainServiceMap.remove(DomainServiceKey.from(domainServiceDesc));
        log.info("removed domainServiceDesc: " + domainServiceDesc);
    }

    private static SortedSet<DomainServiceKeyBuilder> createKeyBuilders(String servicePointName, Object subject, Set<Property> subjectProperties) {
        TreeSet<DomainServiceKeyBuilder> keyBuilders = new TreeSet<DomainServiceKeyBuilder>(DomainServiceKeyBuilder.COMPARATOR.reversed());
        //
        Class<?> subjectClass = subject.getClass();
        while (subjectClass != null) {
            keyBuilders.add(new DomainServiceKeyBuilder(servicePointName, subjectClass));
            subjectClass = subjectClass.getSuperclass();
        }
        for (Property subjectProperty : subjectProperties) {
            keyBuilders.add(new DomainServiceKeyBuilder(servicePointName, subjectProperty.getDeclaringClass(), subjectProperty.getPriority()));
        }
        //
        return keyBuilders;
    }

    public DomainServiceDesc findDomainServiceDesc(String servicePointName, Object subject) {
        Objects.requireNonNull(servicePointName);
        Objects.requireNonNull(subject);
        log.debug("findDomainServiceDesc started: " + servicePointName);
        //
        Set<Property> subjectProperties = propertySupplierRepository.getProperties(subject);
        Set<DomainServiceKeyBuilder> keyBuilders = createKeyBuilders(servicePointName, subject, subjectProperties);
        //
        for (DomainServiceKeyBuilder keyBuilder : keyBuilders) {
            DomainServiceKey key = keyBuilder.filterAndBuild(subjectProperties);
            DomainServiceDesc domainServiceDesc = domainServiceMap.get(key);
            if (domainServiceDesc != null) {
                log.debug("findDomainServiceDesc finished: " + domainServiceDesc);
                return domainServiceDesc;
            }
        }
        //
        log.debug("findDomainServiceDesc finished: null");
        return null;
    }
}
