package example.micronaut;


import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.Collection;

@JdbcRepository(dialect = Dialect.H2)
public interface PetRepository extends CrudRepository<Pet, Long> {
    @Join("owner")
    Collection<Pet> findByOwnerName(String owner);

    @Join("owner")
    Pet findByNameAndOwnerName(String pet, String owner);

    @Join("owner")
    Collection<Pet> findByOwnerNameAndHealth(String owner, Pet.PetHealth health);

    io.reactivex.Single<Boolean> existsByHealth(Pet.PetHealth health);

    void updatePet(@Id Long id, Pet.PetHealth health);
}
