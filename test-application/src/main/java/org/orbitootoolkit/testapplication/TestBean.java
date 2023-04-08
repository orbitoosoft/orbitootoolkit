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
package org.orbitootoolkit.testapplication;

import org.orbitootoolkit.core.api.ServicePointReference;
import org.orbitootoolkit.core.api.ServicePointReferenceByName;
import org.orbitootoolkit.testapplication.animal.api.AnimalException;
import org.orbitootoolkit.testapplication.animal.api.AnimalService;
import org.orbitootoolkit.testapplication.animal.model.Cat;
import org.orbitootoolkit.testapplication.animal.model.Dog;
import org.orbitootoolkit.testapplication.animal.model.Fish;
import org.orbitootoolkit.testapplication.animal.model.Pokemon;
import org.orbitootoolkit.testapplication.animal.model.PokemonType;
import org.orbitootoolkit.testapplication.payment.api.LoanService;
import org.orbitootoolkit.testapplication.payment.api.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TestBean {
    @Autowired
    @ServicePointReference
    private AnimalService animalService;

    @Autowired
    @ServicePointReferenceByName("animalServicePoint")
    private AnimalService animalServiceByName;

    @Autowired
    private LoanService loadService;

    @Autowired
    private OrderService orderService;

    private void testInheritance() {
        Dog dog = new Dog("Buddy");
        Cat cat = new Cat("Tigger");
        Pokemon charizard = new Pokemon(PokemonType.CHARIZARD);
        Pokemon pikachu = new Pokemon(PokemonType.PIKACHU);
        Fish fish = new Fish();
        //
        try {
            animalService.makeSound(dog);
            animalService.makeSound(cat);
            animalService.makeSound(charizard);
            animalService.makeSound(pikachu);
            animalService.makeSound(fish);
        } catch (AnimalException ex) {
            log.info(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        //
        try {
            animalServiceByName.makeSound(dog);
            animalServiceByName.makeSound(cat);
            animalServiceByName.makeSound(charizard);
            animalServiceByName.makeSound(pikachu);
            animalServiceByName.makeSound(fish);
        } catch (AnimalException ex) {
            log.info(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private void testCallback() {
        loadService.loanPayment("LOAN-2023-01-01-0001");
        orderService.orderPayment("ORDER-2023-01-01-9999");
    }

    public void test() {
        testInheritance();
        testCallback();
    }
}
