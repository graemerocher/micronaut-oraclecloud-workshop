package example.micronaut;

import io.micronaut.context.annotation.EachProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.List;

@EachProperty("owners")
public class OwnerConfiguration {
    @NotBlank
    private String name;
    @Min(18)
    private int age;
    private List<String> pets = Collections.emptyList();

    public List<String> getPets() {
        return pets;
    }

    public void setPets(List<String> pets) {
        this.pets = pets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    Owner create() {
        Owner owner = new Owner();
        owner.setName(name);
        owner.setAge(age);
        return owner;
    }
}
