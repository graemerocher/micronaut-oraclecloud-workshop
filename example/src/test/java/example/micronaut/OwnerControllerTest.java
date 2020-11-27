package example.micronaut;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class OwnerControllerTest  {
    @Inject OwnerClient ownerClient;

    @Test
    void testAddOwnerInvalid() {
        HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () ->
                {
                    Owner owner = new Owner();
                    owner.setName("Bob");
                    owner.setAge(10);
                    ownerClient.add(owner).blockingGet();
                }
        );
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("owner.age: must be greater than or equal to 18", e.getMessage());

    }

    @Test
    void testAddOwnerValid() {
        Owner owner = new Owner();
        owner.setName("Bob");
        owner.setAge(35);
        Owner bob = ownerClient.add(owner).blockingGet();
        assertNotNull(bob);
        assertEquals("Bob", bob.getName());
        assertEquals(35, bob.getAge());

        assertEquals(3, ownerClient.getOwners().toList().blockingGet().size());
    }

    @Test
    void testGetHealthPets() {
        Collection<Pet> pets = ownerClient.getPets("Barney", Pet.PetHealth.VACCINATED);
        assertEquals(
                1,
                pets.size()
        );
    }

    @Test
    void testGetAllPets() {
        Collection<Pet> pets = ownerClient.getPets("Barney", null);
        assertEquals(
                1,
                pets.size()
        );
    }

    @Client("/owners")
    interface OwnerClient {
        @Get("/")
        Flowable<Owner> getOwners();

        @Post("/")
        Single<Owner> add(@Body Owner owner);

        @Get("/{owner}/pets{?health}")
        Collection<Pet> getPets(String owner, @Nullable Pet.PetHealth health);

        @Get("/{owner}/pets/{pet}")
        Pet getPet(String owner, String pet);
    }
}
