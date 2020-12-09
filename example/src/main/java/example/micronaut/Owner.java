package example.micronaut;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@MappedEntity
public class Owner {


    @Id
    @GeneratedValue(GeneratedValue.Type.IDENTITY)
    private Long id;

    @NotBlank
    private final String name;

    @Min(18)
    private final int age;

    public Owner(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}