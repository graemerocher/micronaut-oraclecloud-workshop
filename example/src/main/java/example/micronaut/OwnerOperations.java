package example.micronaut;

import java.util.Collection;

public interface OwnerOperations {
    @Logged
    Collection<Owner> getInitialOwners();

    void addOwner(Owner owner);

    Pet getPet(String owner, String pet);

    Collection<Pet> getPets(String owner);

    Collection<Pet> getPetsWithHeath(String owner, Pet.PetHealth health);
}
