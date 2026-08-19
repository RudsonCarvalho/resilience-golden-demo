package io.github.rudsoncarvalho.reof.service;

import io.github.rudsoncarvalho.reof.client.CatalogClient;
import io.github.rudsoncarvalho.reof.client.FulfillmentWebhookClient;
import io.github.rudsoncarvalho.reof.data.ResilientOrderStateStore;
import io.github.rudsoncarvalho.reof.domain.CatalogSnapshot;
import io.github.rudsoncarvalho.reof.domain.FulfillmentEvent;
import io.github.rudsoncarvalho.reof.domain.FulfillmentResponse;
import io.github.rudsoncarvalho.reof.domain.OrderState;
import io.github.rudsoncarvalho.reof.messaging.FulfillmentEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class FulfillmentService {

    private final ResilientOrderStateStore stateStore;
    private final CatalogClient catalogClient;
    private final FulfillmentWebhookClient webhookClient;
    private final FulfillmentEventPublisher eventPublisher;

    public FulfillmentService(
            ResilientOrderStateStore stateStore,
            CatalogClient catalogClient,
            FulfillmentWebhookClient webhookClient,
            FulfillmentEventPublisher eventPublisher) {
        this.stateStore = stateStore;
        this.catalogClient = catalogClient;
        this.webhookClient = webhookClient;
        this.eventPublisher = eventPublisher;
    }

    // One cohesive business capability: fulfill one order. This intentionally keeps REOF D = 1.
    public FulfillmentResponse fulfill(String orderId) {
        OrderState state = stateStore.loadOrCreate(orderId);
        CatalogSnapshot catalog = catalogClient.consult(orderId);
        FulfillmentEvent event = FulfillmentEvent.accepted(orderId);

        var webhook = webhookClient.dispatch(orderId, event);
        var kafka = eventPublisher.publish(event);

        stateStore.store(new OrderState(orderId, "ACCEPTED", state.source()));

        return new FulfillmentResponse(
                orderId,
                catalog.source(),
                state.source(),
                webhook.isCompletedExceptionally() ? "FALLBACK" : "DISPATCHED",
                kafka.isCompletedExceptionally() ? "BUFFERED_FAILURE" : "PUBLISHING");
    }
}
