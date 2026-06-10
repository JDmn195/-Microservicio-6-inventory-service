package com.microshop.inventory.controller;

import com.microshop.inventory.model.Inventory;
import com.microshop.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getStock(@PathVariable Long productId) {
        return inventoryService.getStockByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Inventory> updateStock(@PathVariable Long productId, @RequestBody Integer quantity) {
        return ResponseEntity.ok(inventoryService.updateStock(productId, quantity));
    }

    @PostMapping("/{productId}/reduce")
    public ResponseEntity<Inventory> reduceStock(@PathVariable Long productId, @RequestParam Integer amount) {
        try {
            return ResponseEntity.ok(inventoryService.reduceStock(productId, amount));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{productId}/restore")
    public ResponseEntity<Inventory> restoreStock(@PathVariable Long productId, @RequestParam Integer amount) {
        try {
            return ResponseEntity.ok(inventoryService.restoreStock(productId, amount));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockAlerts() {
        return ResponseEntity.ok(inventoryService.getLowStockAlerts());
    }
}
