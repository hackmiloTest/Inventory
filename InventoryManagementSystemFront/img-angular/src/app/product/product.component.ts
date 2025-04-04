import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { PaginationComponent } from '../pagination/pagination.component';
import { ApiService } from '../service/api.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms'; // <-- Agrega este import

@Component({
  selector: 'app-product',
  standalone: true,
  imports: [CommonModule, PaginationComponent, FormsModule], // <-- Agrega FormsModule aquí
  templateUrl: './product.component.html',
  styleUrl: './product.component.css',
})
export class ProductComponent implements OnInit {
  constructor(private apiService: ApiService, private router: Router) { }
  products: any[] = [];
  allProducts: any[] = []; // <-- Nueva propiedad para almacenar todos los productos
  message: string = '';
  currentPage: number = 1;
  totalPages: number = 0;
  itemsPerPage: number = 10;
  searchTerm: string = ''; // <-- Nueva propiedad para el término de búsqueda

  ngOnInit(): void {
    this.fetchProducts();
  }

  //FETCH PRODUCTS
  fetchProducts(): void {
    this.apiService.getAllProducts().subscribe({
      next: (res: any) => {
        this.allProducts = res.products || [];
        this.allProducts = this.sortProductsBySkuAscending(this.allProducts);
        this.applyFilters(); // <-- Cambia esto para usar el nuevo método
      },
      error: (error) => {
        this.showMessage(
          error?.error?.message ||
          error?.message ||
          'Unable to edit category' + error
        );
      },
    });
  }

  // Método para aplicar filtros de búsqueda
  applyFilters(): void {
    let filteredProducts = [...this.allProducts];

    // Aplicar filtro de búsqueda si hay 3 o más caracteres
    if (this.searchTerm.length >= 3) {
      const searchTermLower = this.searchTerm.toLowerCase();
      filteredProducts = filteredProducts.filter(product =>
        product.name.toLowerCase().includes(searchTermLower)
      );
    }

    // Actualizar paginación
    this.totalPages = Math.ceil(filteredProducts.length / this.itemsPerPage);

    // Asegurarse de que la página actual no exceda el total de páginas
    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = this.totalPages;
    }

    this.products = filteredProducts.slice(
      (this.currentPage - 1) * this.itemsPerPage,
      this.currentPage * this.itemsPerPage
    );
  }

  // Método llamado cuando cambia el término de búsqueda
  onSearchChange(): void {
    this.currentPage = 1; // Resetear a la primera página al buscar
    this.applyFilters();
  }


  // Método para ordenar productos por SKU ascendente
private sortProductsBySkuAscending(products: any[]): any[] {
  return products.sort((a, b) => {
    // Convertir a string para comparación segura
    const skuA = String(a.sku).toLowerCase();
    const skuB = String(b.sku).toLowerCase();
    
    if (skuA < skuB) return -1;
    if (skuA > skuB) return 1;
    return 0;
  });
}

  //DELETE A PRODUCT
  handleProductDelete(productId: string): void {
    if (window.confirm('Are you sure you want to delete this product?')) {
      this.apiService.deleteProduct(productId).subscribe({
        next: (res: any) => {
          if (res.status === 200) {
            this.showMessage('Product deleted successfully');
            this.fetchProducts(); //reload the products
          }
        },
        error: (error) => {
          this.showMessage(
            error?.error?.message ||
            error?.message ||
            'Unable to Delete product' + error
          );
        },
      });
    }
  }

  //HANDLE PAGE CHANGRTE. NAVIGATR TO NEXT< PREVIOUS OR SPECIFIC PAGE CHANGE
  onPageChange(page: number): void {
    this.currentPage = page;
    this.applyFilters(); // <-- Cambia esto de fetchProducts a applyFilters
  }

  //NAVIGATE TO ADD PRODUCT PAGE
  navigateToAddProductPage(): void {
    this.router.navigate(['/add-product']);
  }

  //NAVIGATE TO EDIT PRODUCT PAGE
  navigateToEditProductPage(productId: string): void {
    this.router.navigate([`/edit-product/${productId}`]);
  }

  //SHOW ERROR
  showMessage(message: string) {
    this.message = message;
    setTimeout(() => {
      this.message = '';
    }, 4000);
  }
}
