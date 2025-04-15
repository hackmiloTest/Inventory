package com.hack.InventoryManagementSystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hack.InventoryManagementSystem.enums.TransactionType;
import com.hack.InventoryManagementSystem.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDTO {

    private Long id;

    private Integer totalProducts;

    private BigDecimal totalPrice;

    private TransactionType transactionType;

    private TransactionStatus status;

    private String description;

    private LocalDateTime updatedAt;

    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductDTO product;

    private UserDTO user;

    private SupplierDTO supplier;
    private Long originalSaleId;

}
