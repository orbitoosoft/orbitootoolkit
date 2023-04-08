# orbitoo-toolkit

The goal of the orbitoo-toolkit is to enhance spring-boot with [OOP](https://en.wikipedia.org/wiki/Object-oriented_programming).
The toolkit allows to work with entities as with objects: now they can contain both behavior and attributes.
In order to achieve it the toolkit allows to bind services (behavior) with entities (state). Upon receiving the request,
the toolkit selects the proper service based on the type of subject entity.

The toolkit is working with [Spring-Boot 3.x](https://spring.io/) and [Java 17](https://openjdk.org/).

## Application Startup
Before starting of spring-boot we need to:
* add package `org.orbitootoolkit.core` to `@ComponentScan`
* use `@ServicePointScan` to register `@ServicePoint` components
```java
@SpringBootApplication
@ComponentScan(basePackages = { "org.orbitootoolkit.core", "org.orbitootoolkit.testapplication" })
@ServicePointScan(basePackages = "org.orbitootoolkit.testapplication")
public class TestApplication {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(TestApplication.class, args);
    }
}
```

## Introduction
Let's start with classic OOP example.
Let's suppose that in the database we are storing information about animals:
```java
public abstract class Animal {
    ...
}

public class Dog extends Animal {
    ...
}

public class Fish extends Animal {
    ...
}
```

For each animal we want to know, what sound it makes.
So we will define `AnimalService` interface with:
* `@ServicePoint` ...    specifies servicepoint, which to which are registered domain services
* `@Subject` ...    specifies subject entity
```java
@ServicePoint("animalServicePoint")
public interface AnimalService {
    public void makeSound(@Subject Animal animal);
}
```

Then we can define spring services and bind the to the entities using `@DomainService`:
```java
@Service
@DomainService(servicePointName = "animalServicePoint", subjectClass = Animal.class)
public class AnimalServiceImpl implements AnimalService {
    @Override
    public void makeSound(Animal animal) {
        log.info(animal.getClass().getSimpleName() + " doesn't make sound.");
    }
}

@Service
@DomainService(servicePointName = "animalServicePoint", subjectClass = Dog.class)
public class DogServiceImpl implements AnimalService {
    @Override
    public void makeSound(Animal animal) {
        log.info("Dog [" + animal.getName() + "]: woof woof");
    }
}
```

Finally we can load animals from database and check, what sound they makes. We will send all
requests to servicepoint (injected using qualifier`@ServicePointReference`) and the toolkit
find dispatch the request to the proper domain service based on the type of subject entity.
```java
@Service
public class TestBean {
    @Autowired
    @ServicePointReference
    private AnimalService animalService;

    public void test() {
        Dog dog = new Dog("Buddy");
        Fish fish = new Fish();
        //
        animalService.makeSound(dog);
        animalService.makeSound(fish);
    }
}
```

Output:
```
Dog [Buddy]: woof woof
Fish doesn't make sound.
```

## Tags
Sometimes we need to be able to customize the behavior based on the specific property of the entity
(for example we have a special behavior for VIP customers). The allows to mark a such property using `Tag`
and bind the domain service to specific `TaggedValue`. For example:
```java
public enum PokemonType {
    PIKACHU, CHARIZARD, ...
}

public class Pokemon extends Animal {
    @Tag(name = "type")
    private PokemonType type = null;

    public Pokemon(PokemonType type) {
        this.type = type;
    }
}
```

Then we can define a special service for a specific `PokemonType`. For example:
```java
@Service
@DomainService(servicePointName = "animalServicePoint", subjectClass = Pokemon.class, //
        subjectTaggedValues = @TaggedValue(tag = "type", value = "PIKACHU"))
public class PikachuServiceImpl implements AnimalService {
    @Override
    public void makeSound(Animal animal) {
        log.info("Pikachu: hello");
    }
}
```

Finally we can check our code:
```java
@Service
public class TestBean {
    @Autowired
    @ServicePointReference
    private AnimalService animalService;

    public void test() {
        Pokemon pikachu = new Pokemon(PokemonType.PIKACHU);
        animalService.makeSound(pikachu);
    }
}
```

Output:
```
Pikachu: hello
```
