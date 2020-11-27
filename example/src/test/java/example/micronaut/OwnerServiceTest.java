package example.micronaut;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OwnerServiceTest implements TestPropertyProvider {
    @Inject OwnerService ownerService;

    @Override
    public Map<String, String> getProperties() {
        return Map.of(
                "owners.bob.name", "Bob",
                "owners.bob.age", "25"
        );
    }

    @Test
    void testOwners() {
        Collection<Owner> initialOwners = ownerService.getInitialOwners();
        assertEquals(
                3,
                initialOwners.size()
        );

        assertTrue(
                initialOwners.stream().anyMatch(o -> o.getName().equals("Bob"))
        );
    }

    @Test
    void testFindPetsForOwner() {
        Collection<Pet> pets = ownerService.getPets("Barney");
        assertEquals(
                1,
                pets.size()
        );
        assertEquals(
                "Hoppy",
                pets.iterator().next().getName()
        );
    }

    @Test
    void testFindPetsForOwnerAndHealth() {
        Collection<Pet> pets = ownerService.getPetsWithHeath("Barney", Pet.PetHealth.VACCINATED);
        assertEquals(
                1,
                pets.size()
        );
        assertEquals(
                "Hoppy",
                pets.iterator().next().getName()
        );
    }
}
