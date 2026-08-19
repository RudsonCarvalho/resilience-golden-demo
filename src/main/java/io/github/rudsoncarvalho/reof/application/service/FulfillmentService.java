package io.github.rudsoncarvalho.reof.application.service;

import io.github.rudsoncarvalho.reof.application.port.out.CatalogPort;
import io.github.rudsoncarvalho.reof.application.port.out.FulfillmentEventPort;
import io.github.rudsoncarvalho.reof.application.port.out.FulfillmentWebhookPort;
import io.github.rudsoncarvalho.reof.application.port.out.OrderStatePort;
import io.github.rudsoncarvalho.reof.domain.CatalogSnapshot;
import io.github.rudsoncarvalho.reof.domain.FulfillmentEvent;
import io.github.rudsoncarvalho.reof.domain.FulfillmentResponse;
import io.github.rudsoncarvalho.reof.domain.OrderState;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the single business capability exposed by the demo: fulfilling an order.
 *
 * <p>This class knows only domain objects and outbound ports. HTTP, Redis, Kafka, pooling, retries and circuit
 * breakers are implementation details supplied by the infrastructure layer.</p>
 */
@Service
public class FulfillmentService {

    private final OrderStatePort stateStore;
    private final CatalogPort catalog;
    private final FulfillmentWebhookPort webhook;
    private final FulfillmentEventPort events;

    public FulfillmentService(
            OrderStatePort stateStore,
            CatalogPort catalog,
            FulfillmentWebhookPort webhook,
            FulfillmentEventPort events) {
        this.stateStore = stateStore;
        this.catalog = catalog;
        this.webhook = webhook;
        this.events = events;
    }

    /**
     * Executes one cohesive order-fulfillment flow. Keeping this service focused on one capability intentionally
     * preserves a REOF domain count of {@code D = 1}.
     */
    public FulfillmentResponse fulfill(String orderId) {
        OrderState state = stateStore.loadOrCreate(orderId);
        CatalogSnapshot catalogSnapshot = catalog.consult(orderId);
        FulfillmentEvent event = FulfillmentEvent.accepted(orderId);

        var webhookDispatch = webhook.dispatch(orderId, event);
        var eventPublication = events.publish(event);

        stateStore.store(new OrderState(orderId, "ACCEPTED", state.source()));

        return new FulfillmentResponse(
                orderId,
                catalogSnapshot.source(),
                state.source(),
                webhookDispatch.isCompletedExceptionally() ? "FALLBACK" : "DISPATCHED",
                eventPublication.isCompletedExceptionally() ? "BUFFERED_FAILURE" : "PUBLISHING");
    }
}
