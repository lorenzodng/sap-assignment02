package gateway.infrastructure;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

public class DeliveryServiceCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceCircuitBreaker.class);
    private final CircuitBreaker circuitBreaker;

    public DeliveryServiceCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
        this.circuitBreaker = CircuitBreaker.of("delivery-service", config);
        this.circuitBreaker.getEventPublisher().onStateTransition(event -> log.info("Circuit Breaker state transition: {} -> {}", event.getStateTransition().getFromState(), event.getStateTransition().getToState()));
    }

    public CircuitBreaker get() {
        return circuitBreaker;
    }
}