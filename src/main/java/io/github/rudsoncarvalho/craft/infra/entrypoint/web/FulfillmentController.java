package io.github.rudsoncarvalho.craft.infra.entrypoint.web;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.rudsoncarvalho.craft.application.service.FulfillmentService;
import io.github.rudsoncarvalho.craft.domain.FulfillmentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry adapter protected by a semaphore bulkhead before application orchestration begins. */
@RestController
@RequestMapping("/api/orders")
public class FulfillmentController {

    private final FulfillmentService service;

    public FulfillmentController(FulfillmentService service) {
        this.service = service;
    }

    @Bulkhead(name = "entryBulkhead", type = Bulkhead.Type.SEMAPHORE)
    @PostMapping("/{orderId}/fulfill")
    public ResponseEntity<FulfillmentResponse> fulfill(@PathVariable String orderId) {
        return ResponseEntity.accepted().body(service.fulfill(orderId));
    }
}
