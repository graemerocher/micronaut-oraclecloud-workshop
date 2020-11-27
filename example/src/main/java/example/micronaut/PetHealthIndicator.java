package example.micronaut;

import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.util.Collections;

@Singleton
public class PetHealthIndicator implements HealthIndicator {
    private final PetRepository petRepository;

    public PetHealthIndicator(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Override
    public Publisher<HealthResult> getResult() {
        return petRepository.existsByHealth(Pet.PetHealth.REQUIRES_VACCINATION)
                .flatMapPublisher((petsNeedVaccine) -> {
                    String message = petsNeedVaccine ? "Pets Need Vaccine" : "All Pets Vaccinated";
                    HealthResult result = HealthResult.builder("pets")
                            .status(petsNeedVaccine ? HealthStatus.DOWN : HealthStatus.UP)
                            .details(Collections.singletonMap(
                                    "description", message
                            )).build();
                    return Flowable.just(result);
                });
    }
}
