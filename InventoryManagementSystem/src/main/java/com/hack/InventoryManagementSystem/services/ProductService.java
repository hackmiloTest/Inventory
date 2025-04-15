package com.hack.InventoryManagementSystem.services;

import com.hack.InventoryManagementSystem.dto.ProductDTO;
import com.hack.InventoryManagementSystem.dto.Response;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    Response saveProduct(ProductDTO productDTO);

    Response updateProduct(ProductDTO productDTO);

    Response getAllProducts();

    Response getProductById(Long id);

    Response deleteProduct(Long id);

    Response bulkSaveProducts(MultipartFile file);
}
