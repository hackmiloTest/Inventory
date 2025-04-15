import { Component, OnInit } from '@angular/core';
import { PaginationComponent } from '../pagination/pagination.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../service/api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-transaction',
  standalone: true,
  imports: [PaginationComponent, FormsModule, CommonModule],
  templateUrl: './transaction.component.html',
  styleUrl: './transaction.component.css'
})
export class TransactionComponent implements OnInit {
  constructor(private apiService: ApiService, private router: Router) { }

  transactions: any[] = [];
  message: string = '';
  searchInput: string = '';
  valueToSearch: string = '';
  currentPage: number = 1;
  totalPages: number = 0;
  itemsPerPage: number = 10;
  originalTransactions: any[] = []; // Todas las transacciones sin filtrar
  selectedType: string = '';        // Filtro de tipo: SALE, PURCHASE o ''


  ngOnInit(): void {
    this.loadTransactions();
  }


  //FETCH Transactions

  loadTransactions(): void {
    this.apiService.getAllTransactions(this.valueToSearch).subscribe({
      next: (res: any) => {
        let transactions = res.transactions || [];
  
        // Ordenar por fecha (más reciente primero)
        transactions.sort((a: any, b: any) =>
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
  
        this.originalTransactions = transactions;
        this.applyFilters(); // Aplica filtro de tipo y paginación
      },
      error: (error) => {
        this.showMessage(
          error?.error?.message ||
          error?.message ||
          'Unable to Get all Transactions ' + error
        );
      },
    });
  }
  

  applyFilters(): void {
    let filtered = this.originalTransactions;
  
    // Si el filtro es SALE_NO_RETURN, hacemos lógica especial
    if (this.selectedType === 'SALE_NO_RETURN') {
      const saleTransactions = this.originalTransactions.filter(t => t.transactionType === 'SALE');
      const returnTransactions = this.originalTransactions.filter(t => t.transactionType === 'RETURN');
    
      const returnedSaleIds = new Set(returnTransactions.map(rt => rt.originalSaleId));
      filtered = saleTransactions.filter(sale => !returnedSaleIds.has(sale.id));
    } else if (this.selectedType) {
      // Filtro común por tipo
      filtered = filtered.filter(t => t.transactionType === this.selectedType);
    }
  
    // Paginación
    this.totalPages = Math.ceil(filtered.length / this.itemsPerPage);
  
    this.transactions = filtered.slice(
      (this.currentPage - 1) * this.itemsPerPage,
      this.currentPage * this.itemsPerPage
    );
  }
  

  filterByType(type: string): void {
    this.selectedType = type;
    this.currentPage = 1;
    this.applyFilters();
  }
  


  //HANDLE SERCH
  handleSearch(): void {
    this.currentPage = 1;
    this.loadTransactions(); // Carga de nuevo aplicando búsqueda
  }
  

  //NAVIGATE TGO TRANSACTIONS DETAILS PAGE
  navigateTOTransactionsDetailsPage(transactionId: string): void {
    this.router.navigate([`/transaction/${transactionId}`])
  }

  //HANDLE PAGE CHANGRTE. NAVIGATR TO NEXT< PREVIOUS OR SPECIFIC PAGE CHANGE
  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadTransactions();
  }


  handleReturn(transaction: any): void {
    const confirmReturn = confirm(`¿Deseas devolver el producto ${transaction.product?.name}?`);
    if (!confirmReturn) return;
  
    // Si ya tiene supplier, seguimos directo
    if (transaction.supplier?.id) {
      this.processReturn(transaction, transaction.supplier.id);
    } else {
      // Si no tiene supplier, buscamos uno válido desde el backend
      this.apiService.getAllSuppliers().subscribe({
        next: (res: any) => {
          const suppliers = res || [];
          if (suppliers.length === 0) {
            this.showMessage('No hay proveedores registrados. No se puede hacer la devolución.');
            return;
          }
  
          const validSupplierId = suppliers[0]?.id; // puedes aplicar lógica extra si quieres uno específico
  
          this.processReturn(transaction, validSupplierId);
        },
        error: (err) => {
          this.showMessage('Error al obtener proveedores para la devolución');
          console.error(err);
        }
      });
    }
  }
  
  
  private processReturn(transaction: any, supplierId: string): void {
    const body = {
      productId: transaction.product?.id,
      supplierId: supplierId,
      quantity: transaction.totalProducts,
      description: `Devolución de venta ID ${transaction.id}`
    };
  
    this.apiService.returnSale(transaction.id, body).subscribe({
      next: (res: any) => {
        if (res.status === 200) {
          const returnTransaction = {
            ...transaction,
            id: 'RETURN-' + transaction.id + '-' + Date.now(),
            transactionType: 'RETURN',
            status: 'COMPLETED',
            createdAt: new Date().toISOString(),
            isReturn: true,
            originalSaleId: transaction.id
          };
  
          this.originalTransactions.unshift(returnTransaction);
          this.applyFilters();
          this.showMessage('Devolución realizada con éxito');
        }
      },
      error: (error) => {
        this.showMessage(
          error?.error?.message ||
          error?.message ||
          'Error al devolver el producto: ' + error
        );
      }
    });
  }
  



  //SHOW ERROR
  showMessage(message: string) {
    this.message = message;
    setTimeout(() => {
      this.message = '';
    }, 4000);
  }
}
