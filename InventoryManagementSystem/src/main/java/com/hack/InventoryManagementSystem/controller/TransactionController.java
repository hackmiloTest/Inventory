package com.hack.InventoryManagementSystem.controller;

import com.hack.InventoryManagementSystem.dto.Response;
import com.hack.InventoryManagementSystem.dto.TransactionsRequest;
import com.hack.InventoryManagementSystem.enums.TransactionStatus;
import com.hack.InventoryManagementSystem.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/purchase")
    public ResponseEntity<Response> purchaseInventory(@RequestBody @Valid TransactionsRequest transactionsRequest) {
        return ResponseEntity.ok(transactionService.restockInventory(transactionsRequest));
    }

    @PostMapping("/sell")
    public ResponseEntity<Response> sell(@RequestBody @Valid TransactionsRequest transactionsRequest) {
        return ResponseEntity.ok(transactionService.sell(transactionsRequest));
    }

    @PostMapping("/return")
    public ResponseEntity<Response> returnToSupplier(@RequestBody @Valid TransactionsRequest transactionsRequest) {
        return ResponseEntity.ok(transactionService.returnToSupplier(transactionsRequest));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size,
            @RequestParam(required = false) String searchText
    ) {
        return ResponseEntity.ok(transactionService.getAllTransactions(page, size, searchText));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getTransactionId(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionsById(id));
    }

    @GetMapping("/by-month-year")
    public ResponseEntity<Response> getAllTransactionsByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year
    ) {
        return ResponseEntity.ok(transactionService.getAllTransactionsByMonthAndYear(month, year));
    }

    @PutMapping("/update/{transactionId}")
    public ResponseEntity<Response> updateTransaction(
            @PathVariable Long transactionId,
            @RequestBody @Valid TransactionStatus status
    ) {
        return ResponseEntity.ok(transactionService.updateTransactions(transactionId, status));
    }

    @PostMapping("/return-sale/{saleId}")
    public ResponseEntity<Response> returnSale(
            @PathVariable Long saleId,
            @RequestBody TransactionsRequest request) {
        return ResponseEntity.ok(transactionService.returnSaleTransaction(request, saleId));
    }



}
