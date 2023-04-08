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
package org.orbitootoolkit.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.support.BeanNameGenerator;

public class BeanNameGeneratorUtility {
    private BeanNameGeneratorUtility() {
    }

    //

    private static final String PROPERTY_INSTANCE = "INSTANCE";

    private static BeanNameGenerator getBeanNameGeneratorInstance(Class<? extends BeanNameGenerator> beanNameGeneratorClass) {
        try {
            Field beanNameGeneratorInstanceField = beanNameGeneratorClass.getField(PROPERTY_INSTANCE);
            return (BeanNameGenerator) beanNameGeneratorInstanceField.get(null);
        } catch (NoSuchFieldException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static BeanNameGenerator createBeanNameGenerator(Class<? extends BeanNameGenerator> beanNameGeneratorClass) {
        try {
            Constructor<? extends BeanNameGenerator> beanNameGeneratorConstructor = beanNameGeneratorClass.getConstructor();
            return beanNameGeneratorConstructor.newInstance();
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }

    //

    public static BeanNameGenerator getOrCreateBeanNameGenerator(Class<?> beanNameGeneratorClass) {
        Class<? extends BeanNameGenerator> clazz;
        try {
            clazz = beanNameGeneratorClass.asSubclass(BeanNameGenerator.class);
        } catch (ClassCastException ex) {
            throw new IllegalStateException("Class [" + beanNameGeneratorClass.getSimpleName() + "] is not BeanNameGenerator.");
        }
        //
        BeanNameGenerator beanNameGenerator = ObjectUtils.firstNonNull(getBeanNameGeneratorInstance(clazz), createBeanNameGenerator(clazz));
        if (beanNameGenerator != null) {
            return beanNameGenerator;
        } else {
            throw new IllegalStateException("Cannot obtain beanNameGenerator: " + clazz.getSimpleName());
        }
    }
}
