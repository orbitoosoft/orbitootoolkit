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
package org.orbitootoolkit.core.method;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.apache.commons.lang3.ArrayUtils;
import org.orbitootoolkit.core.api.Subject;
import org.orbitootoolkit.core.util.ReflectionUtility;

public class MethodDelegate {
    private Method method = null;
    private MethodHandle methodHandle = null;
    private int subjectIndex = -1;

    //

    public static int getMethodSubjectIndex(Method method) {
        Parameter[] parameters = ArrayUtils.nullToEmpty(method.getParameters(), Parameter[].class);
        for (int index = 0; index < parameters.length; index++) {
            if (parameters[index].getAnnotation(Subject.class) != null) {
                return index;
            }
        }
        return -1;
    }

    //

    public MethodDelegate(Method method) {
        if (!method.trySetAccessible()) {
            throw new IllegalStateException("Cannot access: " + ReflectionUtility.getSimpleName(method));
        }
        //
        this.method = method;
        this.subjectIndex = getMethodSubjectIndex(method);
        //
        try {
            this.methodHandle = MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Unexpected exception: ", ex);
        }
    }

    //

    public Method getMethod() {
        return method;
    }

    public Object getSubject(Object[] args) {
        return ArrayUtils.get(args, subjectIndex, null);
    }

    public Object invoke(Object service, Object[] args) throws Throwable {
        Object[] finalArgs = ArrayUtils.nullToEmpty(args);
        finalArgs = ArrayUtils.insert(0, finalArgs, service);
        return methodHandle.invokeWithArguments(finalArgs);
    }
}
