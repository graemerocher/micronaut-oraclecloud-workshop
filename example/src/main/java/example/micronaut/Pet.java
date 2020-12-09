package example.micronaut;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;

@MappedEntity
public class Pet {

    @Id
    @GeneratedValue
    private Long id;

    private final String name;

    @Relation(Relation.Kind.MANY_TO_ONE)
    private final Owner owner;

    private PetHealth health = PetHealth.VACCINATED;

    public Pet(String name, @NonNull Owner owner) {
        this.name = name;
        this.owner = owner;
    }

    public Owner getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PetHealth getHealth() {
        return health;
    }

    public void setHealth(@NonNull PetHealth health) {
        this.health = health;
    }

    public enum PetHealth {
        VACCINATED,
        REQUIRES_VACCINATION
    }
}