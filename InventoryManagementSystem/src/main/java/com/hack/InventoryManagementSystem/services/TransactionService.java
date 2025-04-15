package com.hack.InventoryManagementSystem.services;

import com.hack.InventoryManagementSystem.dto.Response;
import com.hack.InventoryManagementSystem.dto.TransactionsRequest;
import com.hack.InventoryManagementSystem.enums.TransactionStatus;

public interface TransactionService {
    Response restockInventory(TransactionsRequest transactionsRequest);

    Response sell(TransactionsRequest transactionsRequest);

    Response returnToSupplier(TransactionsRequest transactionsRequest);

    Response getAllTransactions(int page, int size, String searchText);

    Response getTransactionsById(Long id);

    Response getAllTransactionsByMonthAndYear(int month, int year);

    Response updateTransactions(Long transactionId, TransactionStatus transactionStatus);
    Response returnSaleTransaction(TransactionsRequest transactionsRequest, Long originalSaleId);
}
