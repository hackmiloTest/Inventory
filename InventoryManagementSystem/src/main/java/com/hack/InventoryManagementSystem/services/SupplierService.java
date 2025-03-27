package com.hack.InventoryManagementSystem.services;

import com.hack.InventoryManagementSystem.dto.Response;
import com.hack.InventoryManagementSystem.dto.SupplierDTO;

public interface SupplierService {
    Response addSupplier(SupplierDTO supplierDTO);

    Response updateSupplier(Long id, SupplierDTO supplierDTO);

    Response getAllSuppliers();

    Response getSupplierById(Long id);

    Response deleteSupplier(Long id);
}
