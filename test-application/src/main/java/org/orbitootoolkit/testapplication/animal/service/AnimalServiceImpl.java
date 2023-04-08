/*-
 * ========================LICENSE_START=================================
 * orbitoo-toolkit-test-application
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
package org.orbitootoolkit.testapplication.animal.service;

import org.orbitootoolkit.core.api.DomainService;
import org.orbitootoolkit.testapplication.animal.api.AnimalException;
import org.orbitootoolkit.testapplication.animal.api.AnimalService;
import org.orbitootoolkit.testapplication.animal.model.Animal;
import org.springframework.stereotype.Service;

@Service
@DomainService(servicePointName = "animalServicePoint", subjectClass = Animal.class)
public class AnimalServiceImpl implements AnimalService {
    @Override
    public void makeSound(Animal animal) throws AnimalException {
        throw new AnimalException("fish doesn't make sound");
    }
}
