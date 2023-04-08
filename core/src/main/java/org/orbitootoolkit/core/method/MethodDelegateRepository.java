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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class MethodDelegateRepository {
    private Map<Method, MethodDelegate> methodDelegateMap = new ConcurrentHashMap<Method, MethodDelegate>();

    public MethodDelegate get(Method method) {
        if (methodDelegateMap.containsKey(method)) {
            return methodDelegateMap.get(method);
        } else {
            MethodDelegate methodDelegate = new MethodDelegate(method);
            methodDelegateMap.put(method, methodDelegate);
            return methodDelegate;
        }
    }
}
