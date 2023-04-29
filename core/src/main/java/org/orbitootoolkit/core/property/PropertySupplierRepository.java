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
package org.orbitootoolkit.core.property;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.orbitootoolkit.core.api.Tag;
import org.orbitootoolkit.core.util.ReflectionUtility;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class PropertySupplierRepository {
    private Map<Class<?>, List<PropertySupplier>> propertySupplierMap = new ConcurrentHashMap<Class<?>, List<PropertySupplier>>();

    private List<PropertySupplier> createPropertySuppliers(Class<?> subjectClass) {
        Objects.requireNonNull(subjectClass);
        log.debug("createPropertySuppliers started: " + subjectClass.getSimpleName());
        List<PropertySupplier> propertySuppliers = new LinkedList<PropertySupplier>();
        //
        Field[] taggedFields = FieldUtils.getFieldsWithAnnotation(subjectClass, Tag.class);
        for (Field taggedField : taggedFields) {
            if (taggedField.getDeclaringClass().equals(subjectClass)) {
                if (PropertySupplier.isFieldSupported(taggedField)) {
                    propertySuppliers.add(new PropertySupplier(taggedField));
                } else {
                    log.warn("Cannot create propertySupplier from: " + ReflectionUtility.getSimpleName(taggedField));
                }
            }
        }
        //
        Method[] taggedMethods = MethodUtils.getMethodsWithAnnotation(subjectClass, Tag.class, false, true);
        for (Method taggedMethod : taggedMethods) {
            if (PropertySupplier.isMethodSupported(taggedMethod)) {
                propertySuppliers.add(new PropertySupplier(taggedMethod));
            } else {
                log.warn("Cannot create propertySupplier from: " + ReflectionUtility.getSimpleName(taggedMethod));
            }
        }
        //
        Collections.sort(propertySuppliers, Comparator.comparing(PropertySupplier::getPriority));
        propertySuppliers.addAll(getPropertySuppliers(subjectClass.getSuperclass()));
        //
        log.debug("createPropertySuppliers finished: " + subjectClass.getSimpleName());
        return Collections.unmodifiableList(propertySuppliers);
    }

    private List<PropertySupplier> getPropertySuppliers(Class<?> subjectClass) {
        Objects.requireNonNull(subjectClass);
        if (Object.class.equals(subjectClass)) {
            return Collections.emptyList();
        } else if (propertySupplierMap.containsKey(subjectClass)) {
            return propertySupplierMap.get(subjectClass);
        } else {
            List<PropertySupplier> tagSuppliers = createPropertySuppliers(subjectClass);
            propertySupplierMap.put(subjectClass, tagSuppliers);
            return tagSuppliers;
        }
    }

    //

    public Set<Property> getProperties(Object subject) {
        Objects.requireNonNull(subject);
        log.debug("getProperties started: " + subject.getClass().getSimpleName());
        List<Property> propertiesAsList = new LinkedList<Property>();
        //
        List<PropertySupplier> propertySuppliers = getPropertySuppliers(subject.getClass());
        for (PropertySupplier propertySupplier : propertySuppliers) {
            try {
                propertySupplier.extractPropertiesTo(propertiesAsList, subject);
            } catch (PropertySupplierException ex) {
                throw new IllegalStateException("Cannot obtain properties from: " + subject.getClass().getSimpleName(), ex);
            }
        }
        //
        Map<Property, Property> propertiesAsMap = new HashMap<Property, Property>();
        for (Property newProperty : propertiesAsList) {
            if (propertiesAsMap.containsKey(newProperty)) {
                Property oldProperty = propertiesAsMap.get(newProperty);
                if (newProperty.canReplace(oldProperty)) {
                    propertiesAsMap.put(oldProperty, newProperty);
                }
            } else {
                propertiesAsMap.put(newProperty, newProperty);
            }
        }
        //
        Set<Property> propertiesAsSet = new HashSet<>(propertiesAsMap.values());
        log.debug("getProperties finished: " + propertiesAsSet.size());
        return propertiesAsSet;
    }
}
