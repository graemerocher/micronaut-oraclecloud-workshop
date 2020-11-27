package example.micronaut;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import javax.validation.Valid;
import java.util.Collection;

@Controller("/owners")
public class OwnerController {
    private final OwnerOperations ownerOperations;

    public OwnerController(OwnerOperations ownerOperations) {
        this.ownerOperations = ownerOperations;
    }

    @Get("/")
    Collection<Owner> getOwners() {
        return ownerOperations.getInitialOwners();
    }

    @Post("/")
    Owner add(@Valid @Body Owner owner) {
        ownerOperations.addOwner(owner);
        return owner;
    }

    @Get("/{owner}/pets{?health}")
    Collection<Pet> getPets(String owner, @Nullable Pet.PetHealth health) {
        if (health != null) {
            return ownerOperations.getPetsWithHeath(owner, health);
        } else {
            return ownerOperations.getPets(owner);
        }
    }

    /**
     * Gets a Pet for the given Owner name and Pet name
     * @param owner The name of the Owner
     * @param pet The name of Pet
     * @return A pet if it exists
     */
    @Get("/{owner}/pets/{pet}")
    @ApiResponse(responseCode = "404", description = "If a pet is not found")
    Pet getPet(String owner, String pet) {
        return ownerOperations.getPet(owner, pet);
    }
}