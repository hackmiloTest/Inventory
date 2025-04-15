package com.hack.InventoryManagementSystem.services.impl;

import com.hack.InventoryManagementSystem.dto.ProductDTO;
import com.hack.InventoryManagementSystem.dto.Response;
import com.hack.InventoryManagementSystem.entity.Category;
import com.hack.InventoryManagementSystem.entity.Product;
import com.hack.InventoryManagementSystem.exceptions.NotFoundException;
import com.hack.InventoryManagementSystem.repository.CategoryRepository;
import com.hack.InventoryManagementSystem.repository.ProductRepository;
import com.hack.InventoryManagementSystem.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    private static final String IMAGE_DIRECTORY = System.getProperty("user.dir") + "/product-image/";

    @Override
    public Response saveProduct(ProductDTO productDTO) {
        Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow(() -> new NotFoundException("Category Not Found"));

        //map out product dto to product entity
        Product productToSave = Product.builder().name(productDTO.getName()).sku(productDTO.getSku()).price(productDTO.getPrice()).stockQuantity(productDTO.getStockQuantity()).description(productDTO.getDescription()).category(category).build();

        //save the product to our database
        productRepository.save(productToSave);
        return Response.builder().status(200).message("Product Successfully Saved").build();
    }

    @Override
    public Response updateProduct(ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(productDTO.getProductId()).orElseThrow(() -> new NotFoundException("Product Not Found"));

        //Check if category is to be changed for the product
        if (productDTO.getCategoryId() != null && productDTO.getCategoryId() > 0) {
            Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow(() -> new NotFoundException("Category Not Found"));
            existingProduct.setCategory(category);
        }
        //Check and update fields
        if (productDTO.getName() != null && !productDTO.getName().isBlank()) {
            existingProduct.setName(productDTO.getName());
        }

        if (productDTO.getSku() != null && !productDTO.getSku().isBlank()) {
            existingProduct.setSku(productDTO.getSku());
        }

        if (productDTO.getDescription() != null && !productDTO.getDescription().isBlank()) {
            existingProduct.setDescription(productDTO.getDescription());
        }

        if (productDTO.getPrice() != null && productDTO.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
            existingProduct.setPrice(productDTO.getPrice());
        }

        if (productDTO.getStockQuantity() != null && productDTO.getStockQuantity() >= 0) {
            existingProduct.setStockQuantity(productDTO.getStockQuantity());
        }
        //Update the product

        productRepository.save(existingProduct);
        return Response.builder().status(200).message("Product Successfully Updated").build();
    }

    @Override
    public Response getAllProducts() {
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<ProductDTO> productDTOS = modelMapper.map(products, new TypeToken<List<ProductDTO>>() {
        }.getType());

        return Response.builder().status(200).message("success").products(productDTOS).build();
    }

    @Override
    public Response getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product Not Found"));

        return Response.builder().status(200).message("success").product(modelMapper.map(product, ProductDTO.class)).build();
    }

    @Override
    public Response deleteProduct(Long id) {
        productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product Not Found"));

        productRepository.deleteById(id);

        return Response.builder().status(200).message("Product Success Deleted").build();
    }


    private String saveImage(MultipartFile imageFile) {
        //validate check image
        if (!imageFile.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
        //create the directory to store images if it doesn't exist
        File directory = new File(IMAGE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
            log.info("Directory was created");
        }
        //Generate unique file name for the image
        String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        //Get the absolute path of the image
        String imagePath = IMAGE_DIRECTORY + uniqueFileName;

        try {
            File destinationFile = new File(imagePath);
            imageFile.transferTo(destinationFile); // We are transferring(writing to this folder)
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error occurred while saving image" + ex.getMessage());
        }
        return imagePath;
    }


    @Override
    public Response bulkSaveProducts(MultipartFile file) {
        List<Product> productsToSave = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try {
            // Leer el archivo Excel
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0); // Suponemos que los datos están en la primera hoja

            // Iterar sobre las filas del Excel (empezando desde la fila 1 para omitir los encabezados)
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                System.out.println("Procesando fila " + (i + 1) + ": " + row); // Debug
                try {
                    // Asegurarse de que la fila no esté vacía
                    if (row == null) {
                        System.out.println("Fila " + (i + 1) + " está vacía");
                        continue;
                    }

                    ProductDTO dto = new ProductDTO();
                    dto.setName(getCellValue(row.getCell(0)));
                    dto.setSku((getCellValue(row.getCell(1))));
                    dto.setPrice(new BigDecimal(getCellValue(row.getCell(2))));
                    dto.setStockQuantity((int) Double.parseDouble(getCellValue(row.getCell(3))));
                    dto.setDescription(getCellValue(row.getCell(4)));
                    String categoryIdStr = getCellValue(row.getCell(5));
                    dto.setCategoryId((long) Double.parseDouble(categoryIdStr));

                    // Validaciones básicas
                    if (dto.getName() == null || dto.getName().isBlank()) {
                        throw new IllegalArgumentException("El nombre es requerido");
                    }

                    if (dto.getSku() == null || dto.getSku().isBlank()) {
                        throw new IllegalArgumentException("El SKU es requerido");
                    }

                    Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new NotFoundException("Categoría no encontrada para el producto: " + dto.getName()));

                    // En tu servicio (antes del mapeo):
                    modelMapper.getConfiguration().setAmbiguityIgnored(true); // Opcional: ignora conflictos
                    modelMapper.typeMap(ProductDTO.class, Product.class).addMappings(mapper -> {
                        mapper.<Long>map(ProductDTO::getCategoryId, (dest, v) -> dest.getCategory().setId(v));
                    });

// Luego usa el mapeo normal:
                    Product product = modelMapper.map(dto, Product.class);

                    productsToSave.add(product);
                    successCount++;
                } catch (Exception e) {
                    System.out.println("Error en fila " + (i + 1) + ": " + e.getMessage());
                    errors.add("Error en línea " + (i + 1) + ": " + e.getMessage());
                }
            }

            if (!productsToSave.isEmpty()) {
                productRepository.saveAll(productsToSave);
            }

        } catch (Exception e) {
            errors.add("Error al procesar el archivo: " + e.getMessage());
        }

        return Response.builder().status(errors.isEmpty() ? 200 : 207) // 207 Multi-Status si hay errores
                .message(successCount + " productos creados exitosamente").build();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

}
