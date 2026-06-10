package com.microshop.inventory.service;

import com.microshop.inventory.model.Inventory;
import com.microshop.inventory.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Optional<Inventory> getStockByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Transactional
    public Inventory updateStock(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(new Inventory(productId, 0, 10));
        
        inventory.setQuantity(quantity);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory reduceStock(Long productId, Integer amount) {
        // If the product has no inventory record yet, create one on-the-fly.
        // This handles products created before inventory-service was reachable.
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    log.warn("Producto {} no encontrado en inventario — creando registro con stock 0. " +
                             "Sincronizar manualmente si tiene stock real.", productId);
                    return inventoryRepository.save(new Inventory(productId, 0, 10));
                });

        if (inventory.getQuantity() < amount) {
            throw new RuntimeException("Stock insuficiente para el producto: " + productId);
        }

        inventory.setQuantity(inventory.getQuantity() - amount);

        if (inventory.getQuantity() <= inventory.getLowStockThreshold()) {
            log.warn("ALERTA: Stock bajo para el producto {}. Cantidad actual: {}", productId, inventory.getQuantity());
        }

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory restoreStock(Long productId, Integer amount) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en inventario: " + productId));

        inventory.setQuantity(inventory.getQuantity() + amount);
        return inventoryRepository.save(inventory);
    }

    public List<Inventory> getLowStockAlerts() {
        return inventoryRepository.findLowStockItems();
    }
}
