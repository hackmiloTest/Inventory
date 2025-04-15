package com.hack.InventoryManagementSystem.services.impl;

import com.hack.InventoryManagementSystem.dto.Response;
import com.hack.InventoryManagementSystem.dto.TransactionDTO;
import com.hack.InventoryManagementSystem.dto.TransactionsRequest;
import com.hack.InventoryManagementSystem.entity.Product;
import com.hack.InventoryManagementSystem.entity.Supplier;
import com.hack.InventoryManagementSystem.entity.Transaction;
import com.hack.InventoryManagementSystem.entity.User;
import com.hack.InventoryManagementSystem.enums.TransactionStatus;
import com.hack.InventoryManagementSystem.enums.TransactionType;
import com.hack.InventoryManagementSystem.exceptions.NameValueRequiredException;
import com.hack.InventoryManagementSystem.exceptions.NotFoundException;
import com.hack.InventoryManagementSystem.repository.ProductRepository;
import com.hack.InventoryManagementSystem.repository.SupplierRepository;
import com.hack.InventoryManagementSystem.repository.TransactionRepository;
import com.hack.InventoryManagementSystem.services.TransactionService;
import com.hack.InventoryManagementSystem.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;
    private final SupplierRepository supplierRepository;
    private final UserService userService;
    private final ProductRepository productRepository;


    @Override
    public Response restockInventory(TransactionsRequest transactionsRequest) {
        Long productId = transactionsRequest.getProductId();
        Long supplierId = transactionsRequest.getSupplierId();
        Integer quantity = transactionsRequest.getQuantity();

        if (supplierId == null) throw new NameValueRequiredException("Supplier Id Is Required");

        Product product = productRepository.findById(productId).orElseThrow(
                () -> new NotFoundException("Product Not found"));
        Supplier supplier = supplierRepository.findById(supplierId).orElseThrow(
                () -> new NotFoundException("Supplier Not found"));
        User user = userService.getCurrentLoggedInUser();

        //Update the stock quantity and resave
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        //Create transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.COMPLETED)
                .product(product)
                .user(user)
                .supplier(supplier)
                .totalProducts(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .description(transactionsRequest.getDescription())
                .build();

        transactionRepository.save(transaction);
        return Response.builder()
                .status(200)
                .message("Transaction Made Successfully")
                .build();
    }

    @Override
    public Response sell(TransactionsRequest transactionsRequest) {
        Long productId = transactionsRequest.getProductId();
        Integer quantity = transactionsRequest.getQuantity();

        Product product = productRepository.findById(productId).orElseThrow(
                () -> new NotFoundException("Product Not found"));

        User user = userService.getCurrentLoggedInUser();

        //Update the stock quantity and resave
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        //Create transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .product(product)
                .user(user)
                .totalProducts(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .description(transactionsRequest.getDescription())
                .build();

        transactionRepository.save(transaction);
        return Response.builder()
                .status(200)
                .message("Transaction Sold Successfully")
                .build();
    }

    @Override
    public Response returnToSupplier(TransactionsRequest transactionsRequest) {
        Long productId = transactionsRequest.getProductId();
        Long supplierId = transactionsRequest.getSupplierId();
        Integer quantity = transactionsRequest.getQuantity();

        if (supplierId == null) throw new NameValueRequiredException("Supplier Id is Required");

        Product product = productRepository.findById(productId).orElseThrow(
                () -> new NotFoundException("Product Not found"));
        Supplier supplier = supplierRepository.findById(supplierId).orElseThrow(
                () -> new NotFoundException("Supplier Not found"));
        User user = userService.getCurrentLoggedInUser();

        //Update the stock quantity and resave
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        //Create transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.RETURN_TO_SUPPLIER)
                .status(TransactionStatus.PROCESSING)
                .product(product)
                .user(user)
                .supplier(supplier)
                .totalProducts(quantity)
                .totalPrice(BigDecimal.ZERO)
                .description(transactionsRequest.getDescription())
                .build();

        transactionRepository.save(transaction);
        return Response.builder()
                .status(200)
                .message("Transaction Returned Successfully Initialized")
                .build();
    }

    @Override
    public Response getAllTransactions(int page, int size, String searchText) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Transaction> transactionPage = transactionRepository.searchTransactions(searchText, pageable);
        List<TransactionDTO> transactionDTOS =
                modelMapper.map(transactionPage.getContent(), new TypeToken<List<TransactionDTO>>() {
                }.getType());
        transactionDTOS.forEach(transactionDTOItem -> {
            transactionDTOItem.setUser(null);
            transactionDTOItem.setSupplier(null);
        });
        return Response.builder()
                .status(200)
                .message("Success")
                .transactions(transactionDTOS)
                .build();
    }

    @Override
    public Response getTransactionsById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction Not found"));

        TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
        transactionDTO.getUser().setTransactions(null); // removing the user transaction list
        return Response.builder()
                .status(200)
                .message("Success")
                .transaction(transactionDTO)
                .build();
    }

    @Override
    public Response getAllTransactionsByMonthAndYear(int month, int year) {
        List<Transaction> transactions = transactionRepository.findAllByMonthAndYear(month, year);

        List<TransactionDTO> transactionDTOS = modelMapper
                .map(transactions, new TypeToken<List<TransactionDTO>>() {
                }.getType());

        transactionDTOS.forEach(transactionDTOItem -> {
            transactionDTOItem.setUser(null);
            transactionDTOItem.setProduct(null);
            transactionDTOItem.setSupplier(null);
        });
        return Response.builder()
                .status(200)
                .message("Success")
                .transactions(transactionDTOS)
                .build();
    }

    @Override
    public Response updateTransactions(Long transactionId, TransactionStatus transactionStatus) {
        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction Not found"));

        existingTransaction.setStatus(transactionStatus);
        existingTransaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(existingTransaction);

        return Response.builder()
                .status(200)
                .message("Transaction Status Successfully Updated")
                .build();
    }

    public Response returnSaleTransaction(TransactionsRequest transactionsRequest, Long originalSaleId) {
        Transaction originalSale = transactionRepository.findById(originalSaleId)
                .orElseThrow(() -> new NotFoundException("Original Sale Transaction not found"));

        Product product = originalSale.getProduct();
        Supplier supplier = transactionsRequest.getSupplierId() != null
                ? supplierRepository.findById(transactionsRequest.getSupplierId())
                .orElseThrow(() -> new NotFoundException("Supplier not found"))
                : null;

        User user = userService.getCurrentLoggedInUser();
        Integer quantity = originalSale.getTotalProducts();

        // Devolver el producto al inventario
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        // Crear transacción de tipo RETURN
        Transaction returnTransaction = Transaction.builder()
                .transactionType(TransactionType.RETURN)
                .status(TransactionStatus.COMPLETED)
                .product(product)
                .user(user)
                .supplier(supplier)
                .totalProducts(quantity)
                .totalPrice(originalSale.getTotalPrice().negate()) // Opcional: mostrar valor negativo si quieres
                .description("Devolución de venta ID " + originalSaleId)
                .originalSaleId(originalSaleId)
                .build();

        transactionRepository.save(returnTransaction);

        return Response.builder()
                .status(200)
                .message("Venta devuelta exitosamente")
                .build();
    }

}
