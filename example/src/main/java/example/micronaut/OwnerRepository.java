package example.micronaut;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;

@JdbcRepository(dialect = Dialect.H2)
public interface OwnerRepository extends CrudRepository<Owner, Long> {
    @NonNull
    @Override
    Collection<Owner> findAll();
}