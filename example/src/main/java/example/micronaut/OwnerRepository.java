package example.micronaut;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;

@Repository
public interface OwnerRepository extends CrudRepository<Owner, Long> {
    @NonNull
    @Override
    Collection<Owner> findAll();
}
