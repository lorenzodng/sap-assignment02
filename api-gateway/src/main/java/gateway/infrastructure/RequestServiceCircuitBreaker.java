package gateway.infrastructure;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

//circuit braker per le chiamate a request-service
public class RequestServiceCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceCircuitBreaker.class);
    private final CircuitBreaker circuitBreaker;

    public RequestServiceCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) //se il 50% delle ultime chiamate fallisce, il circuito entra nello stato open
                .waitDurationInOpenState(Duration.ofSeconds(30)) //rimane aperto per 30 secondi prima di passare allo stato half-open
                .slidingWindowSize(10) //valuta le ultime 10 chiamate per calcolare il tasso di fallimento
                .permittedNumberOfCallsInHalfOpenState(3) //3 chiamate riuscite per passare allo stato closed
                .build();
        this.circuitBreaker = CircuitBreaker.of("request-service", config);
        this.circuitBreaker.getEventPublisher().onStateTransition(event -> log.info("Circuit Breaker state transition: {} -> {}", event.getStateTransition().getFromState(), event.getStateTransition().getToState()));
    }

    public CircuitBreaker get() {
        return circuitBreaker;
    }
}