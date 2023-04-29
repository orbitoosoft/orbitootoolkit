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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import org.orbitootoolkit.core.property.Property;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class DomainServiceKeyBuilder {
    private static final Comparator<DomainServiceKeyBuilder> SERVICE_POINT_NAME_COMPARATOR = //
            Comparator.comparing(DomainServiceKeyBuilder::getServicePointName, Comparator.naturalOrder());
    private static final Comparator<DomainServiceKeyBuilder> SUBJECT_CLASS_COMPARATOR = //
            Comparator.comparing(DomainServiceKeyBuilder::getSubjectClass, (class1, class2) -> {
                if (class1.equals(class2)) {
                    return 0;
                } else if (class1.isAssignableFrom(class2)) {
                    return -1;
                } else if (class2.isAssignableFrom(class1)) {
                    return 1;
                } else {
                    throw new IllegalStateException("Unrelated classes: [" + class1.getSimpleName() + ", " + class2.getSimpleName() + "]");
                }
            });
    private static final Comparator<DomainServiceKeyBuilder> PRIORITY_COMPARATOR = //
            Comparator.comparing(DomainServiceKeyBuilder::getTagPriority, Comparator.nullsFirst(Comparator.reverseOrder()));
    public static final Comparator<DomainServiceKeyBuilder> COMPARATOR = //
            SERVICE_POINT_NAME_COMPARATOR.thenComparing(SUBJECT_CLASS_COMPARATOR).thenComparing(PRIORITY_COMPARATOR);

    //

    private String servicePointName = null;
    private Class<?> subjectClass = null;
    private Integer tagPriority = null;

    public DomainServiceKeyBuilder(String servicePointName, Class<?> subjectClass, Integer tagPriority) {
        Objects.requireNonNull(servicePointName);
        Objects.requireNonNull(subjectClass);
        //
        this.servicePointName = servicePointName;
        this.subjectClass = subjectClass;
        this.tagPriority = tagPriority;
    }

    public DomainServiceKeyBuilder(String servicePointName, Class<?> subjectClass) {
        this(servicePointName, subjectClass, null);
    }

    //

    public DomainServiceKey filterAndBuild(Set<Property> properties) {
        Iterator<Property> iterator = properties.iterator();
        while (iterator.hasNext()) {
            Property property = iterator.next();
            boolean propertyHasSameClass = property.getDeclaringClass().equals(subjectClass);
            boolean propertyHasUnrelatedClass = !property.getDeclaringClass().isAssignableFrom(subjectClass);
            boolean propertyHasLowerPriority = (tagPriority == null) || (property.getPriority() < tagPriority);
            if (propertyHasUnrelatedClass || (propertyHasSameClass && propertyHasLowerPriority)) {
                iterator.remove();
            }
        }
        //
        return new DomainServiceKey(servicePointName, subjectClass, properties);
    }
}
