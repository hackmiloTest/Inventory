import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../service/api.service';
import { ActivatedRoute, Router } from '@angular/router';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-product-bulk-upload',
  templateUrl: './product-bulk-upload.component.html',
  imports: [FormsModule, CommonModule, MatIconModule, MatProgressBarModule],
  styleUrls: ['./product-bulk-upload.component.css']
})
export class ProductBulkUploadComponent {
  selectedFile: File | null = null;
  uploadProgress = 0;
  isUploading = false;
  successMessage = '';
  errorMessage = '';
  previewData: any[] = [];
  headers: string[] = [];

  constructor(private apiService: ApiService,
    private route: ActivatedRoute,
    private router: Router) { }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
    this.previewFile();
  }

  previewFile() {
    if (!this.selectedFile) return;

    const reader = new FileReader();
    reader.onload = (e: any) => {
      const data = new Uint8Array(e.target.result);
      const workbook = XLSX.read(data, { type: 'array' });
      const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
      const jsonData = XLSX.utils.sheet_to_json(firstSheet, { header: 1 });

      if (jsonData.length > 0) {
        this.headers = jsonData[0] as string[];
        this.previewData = jsonData.slice(1, 6); // Mostrar solo 5 filas
      }
    };
    reader.readAsArrayBuffer(this.selectedFile);
  }

  uploadFile() {
    if (!this.selectedFile) return;

    this.isUploading = true;
    this.uploadProgress = 0;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.apiService.bulkUploadExcel(formData).subscribe({
      next: (response) => {
        this.isUploading = false;
        this.successMessage = `${response.message}`;
        setTimeout(() => this.router.navigate(['/product']), 3000);
      },
      error: (error) => {
        this.isUploading = false;
        this.errorMessage = error.error?.message || 'Error al cargar productos';
      }
    });
  }

  downloadTemplate() {
    // Lógica para descargar plantilla Excel
    const template = [
      ['name', 'sku', 'price', 'stockQuantity', 'description', 'categoryId'],
      ['Ejemplo 1', 'SKU001', '19.99', '100', 'Descripción 1', '1'],
      ['Ejemplo 2', 'SKU002', '29.99', '50', 'Descripción 2', '2']
    ];

    const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(template);
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Plantilla');
    XLSX.writeFile(wb, 'plantilla_productos.xlsx');
  }
}