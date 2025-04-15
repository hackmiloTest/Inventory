import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { PaginationComponent } from '../pagination/pagination.component';
import { ApiService } from '../service/api.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms'; // <-- Agrega este import
@Component({
  selector: 'app-sell',
  standalone: true,
  imports: [CommonModule, PaginationComponent, FormsModule],
  templateUrl: './sell.component.html',
  styleUrl: './sell.component.css'
})
export class SellComponent implements OnInit {

  constructor(private apiService: ApiService, private router: Router){}


  products: any[] = []
  allProducts: any[] = [];
  productId:string = ''
  description:string = ''
  quantity:string = ''
  message:string = ''
  searchTerm: string = ''; 
  currentPage: number = 1;
  totalPages: number = 0;
  itemsPerPage: number = 3;
  productSearchTerm: string = '';
  filteredProducts: any[] = [];
  selectedProduct: any = null;



  ngOnInit(): void {
    this.fetchProducts();
  }

  fetchProducts(): void {
    this.apiService.getAllProducts().subscribe({
      next: (res: any) => {
        if (res.status === 200) {
          this.products = res.products;
          this.allProducts = [...res.products]; // Copia para búsqueda
        }
      },
      error: (error) => {
        this.showMessage('Error loading products');
      }
    });
  }

  onProductSearch(): void {
    if (this.productSearchTerm.length >= 2) {
      const searchTerm = this.productSearchTerm.toLowerCase();
      this.filteredProducts = this.allProducts
        .filter(product => 
          product.name.toLowerCase().includes(searchTerm) || 
          product.sku.toString().toLowerCase().includes(searchTerm)
        )
        .slice(0, 3); // Limita a 3 resultados
    } else {
      this.filteredProducts = [];
    }
  }

  selectProduct(product: any): void {
    this.productId = product.id;
    this.productSearchTerm = product.name; // Muestra el nombre seleccionado
    this.filteredProducts = []; // Oculta resultados
  }

  //Handle form submission
  handleSubmit():void{
    if (!this.productId || !this.quantity) {
      this.showMessage("Please select a product and specify quantity");
      return;
    }
    const body = {
      productId: this.productId,
      quantity:  parseInt(this.quantity, 10),
      description: this.description
    }

    this.apiService.sellProduct(body).subscribe({
      next: (res: any) => {
        if (res.status === 200) {
          this.showMessage(res.message)
          this.resetForm();
        }
      },
      error: (error) => {
        this.showMessage(
          error?.error?.message ||
            error?.message ||
            'Unable to sell a product' + error
        );
      },
    })

  }

  
  resetForm():void{
    this.productId = '';
    this.description = '';
    this.quantity = '';
  }


  showMessage(message: string) {
    this.message = message;
    setTimeout(() => {
      this.message = '';
    }, 4000);
  }
  

// Método llamado cuando cambia el término de búsqueda
  onSearchChange(): void {
    this.currentPage = 1; // Resetear a la primera página al buscar
    this.applyFilters();
  }

  navigateToBulkUploadPage(): void {
    this.router.navigate(['/products/bulk-upload']);
  }

   //NAVIGATE TO ADD PRODUCT PAGE
   navigateToAddProductPage(): void {
    this.router.navigate(['/add-product']);
  }

  // Método para aplicar filtros de búsqueda
  applyFilters(): void {
    let filteredProducts = [...this.allProducts];

    // Aplicar filtro de búsqueda si hay 3 o más caracteres
    if (this.searchTerm.length >= 3) {
      const searchTermLower = this.searchTerm.toLowerCase();
      filteredProducts = filteredProducts.filter(product =>
        product.name.toLowerCase().includes(searchTermLower) || 
      product.sku.toString().toLowerCase().includes(searchTermLower)
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
  

}

