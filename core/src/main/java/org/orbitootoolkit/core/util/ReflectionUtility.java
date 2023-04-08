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

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class ReflectionUtility {
    private ReflectionUtility() {
    }

    public static String getSimpleName(Member member) {
        return member.getDeclaringClass().getSimpleName() + "." + member.getName();
    }

    public static List<Class<?>> getMethodParameterTypes(Method method) {
        Class<?>[] parameterTypes = ArrayUtils.nullToEmpty(method.getParameterTypes(), Class[].class);
        return Arrays.asList(parameterTypes);
    }
}
