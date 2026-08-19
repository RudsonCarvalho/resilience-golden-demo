package io.github.rudsoncarvalho.reof.web;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.rudsoncarvalho.reof.domain.FulfillmentResponse;
import io.github.rudsoncarvalho.reof.service.FulfillmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class FulfillmentController {

    private final FulfillmentService service;

    public FulfillmentController(FulfillmentService service) {
        this.service = service;
    }

    // REOF EE: the only external HTTP surface is protected by a semaphore bulkhead.
    @Bulkhead(name = "entryBulkhead", type = Bulkhead.Type.SEMAPHORE)
    @PostMapping("/{orderId}/fulfill")
    public ResponseEntity<FulfillmentResponse> fulfill(@PathVariable String orderId) {
        return ResponseEntity.accepted().body(service.fulfill(orderId));
    }
}
