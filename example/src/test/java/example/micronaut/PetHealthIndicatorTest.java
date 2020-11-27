package example.micronaut;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MicronautTest(transactional = false)
public class PetHealthIndicatorTest {

    @Inject @Client("/")
    HttpClient httpClient;

    @Inject
    PetRepository petRepository;

    @Test
    void testPetHealth() {
        HttpResponse<?> response = httpClient.toBlocking().exchange("/health");
        assertEquals(HttpStatus.OK, response.status());

        Pet pet = petRepository.findByNameAndOwnerName("Hoppy", "Barney");

        petRepository.updatePet(
                pet.getId(),
                Pet.PetHealth.REQUIRES_VACCINATION
        );

        response  = assertThrows(HttpClientResponseException.class, () ->
                httpClient.toBlocking().exchange("/health")
        ).getResponse();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.status());
    }
}
