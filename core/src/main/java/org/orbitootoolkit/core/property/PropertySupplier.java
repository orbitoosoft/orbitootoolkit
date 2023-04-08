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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.orbitootoolkit.core.api.Tag;
import org.orbitootoolkit.core.util.ReflectionUtility;

public class PropertySupplier {
    private static final String MESSAGE_UNEXPECTED_EXCEPTION = "Unexpected exception: ";
    private Class<?> declaringClass = null;
    private String propertyName = null;
    private MethodHandle methodHandle = null;

    //

    public static boolean isFieldSupported(Field field) {
        Tag tag = field.getAnnotation(Tag.class);
        //
        boolean isInstanceField = ((field.getModifiers() & Modifier.STATIC) == 0);
        boolean hasTag = (tag != null) ? StringUtils.isNotEmpty(tag.name()) : false;
        //
        return isInstanceField && hasTag;
    }

    public static boolean isMethodSupported(Method method) {
        Tag tag = method.getAnnotation(Tag.class);
        //
        boolean isInstanceMethod = ((method.getModifiers() & Modifier.STATIC) == 0);
        boolean hasNoParameters = ArrayUtils.isEmpty(method.getParameterTypes());
        boolean hasTag = (tag != null) ? StringUtils.isNotEmpty(tag.name()) : false;
        //
        return isInstanceMethod && hasNoParameters && hasTag;
    }

    //

    public PropertySupplier(Field field) {
        if (!isFieldSupported(field)) {
            throw new IllegalArgumentException("Cannot create propertySupplier from: " + ReflectionUtility.getSimpleName(field));
        }
        //
        this.declaringClass = field.getDeclaringClass();
        this.propertyName = field.getAnnotation(Tag.class).name();
        //
        if (!field.trySetAccessible()) {
            throw new IllegalStateException("Cannot access: " + ReflectionUtility.getSimpleName(field));
        }
        //
        try {
            this.methodHandle = MethodHandles.lookup().unreflectGetter(field);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(MESSAGE_UNEXPECTED_EXCEPTION, ex);
        }
    }

    public PropertySupplier(Method method) {
        if (!isMethodSupported(method)) {
            throw new IllegalArgumentException("Cannot create propertySupplier from: " + ReflectionUtility.getSimpleName(method));
        }
        //
        this.declaringClass = method.getDeclaringClass();
        this.propertyName = method.getAnnotation(Tag.class).name();
        //
        if (!method.trySetAccessible()) {
            throw new IllegalStateException("Cannot access: " + ReflectionUtility.getSimpleName(method));
        }
        //
        try {
            this.methodHandle = MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(MESSAGE_UNEXPECTED_EXCEPTION, ex);
        }
    }

    //

    private static void addPropertyTo(Collection<Property> properties, Class<?> clazz, String name, Object value) throws PropertySupplierException {
        if (value == null) {
            // DO NOTHING
        } else if (value instanceof String) {
            properties.add(new Property(clazz, name, (String) value));
        } else if (value instanceof Character) {
            properties.add(new Property(clazz, name, ((Character) value).toString()));
        } else if (value instanceof Number) {
            properties.add(new Property(clazz, name, ((Number) value).toString()));
        } else if (value instanceof Boolean) {
            properties.add(new Property(clazz, name, ((Boolean) value).toString()));
        } else if (value instanceof Enum) {
            properties.add(new Property(clazz, name, ((Enum<?>) value).name()));
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String childName = StringUtils.stripToNull(Objects.toString(entry.getKey()));
                Object childValue = entry.getValue();
                //
                if (StringUtils.isNotEmpty(childName)) {
                    addPropertyTo(properties, clazz, name + "." + childName, childValue);
                }
            }
        } else {
            throw new PropertySupplierException("Cannot create property from: " + value.getClass().getSimpleName());
        }
    }

    public void extractPropertiesTo(Collection<Property> properties, Object subject) throws PropertySupplierException {
        Object value;
        try {
            value = methodHandle.invoke(subject);
        } catch (Throwable ex) {
            throw new IllegalStateException(MESSAGE_UNEXPECTED_EXCEPTION, ex);
        }
        //
        addPropertyTo(properties, declaringClass, propertyName, value);
    }
}
