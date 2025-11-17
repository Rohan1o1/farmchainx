import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-products.html'
})
export class MyProducts {
  products: any[] = [];
  loading = true;
  error: string | null = null;

  constructor(private http: HttpClient, private router: Router) {
    this.load();
  }

  load() {
    this.loading = true;

    this.http.get<any[]>('/api/products/my').subscribe({
      next: (res) => {
        this.products = Array.isArray(res) ? res : [];
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load products';
        this.loading = false;
      }
    });
  }

  openUpload() {
    this.router.navigate(['/upload']);
  }

  generateQr(productId: number) {
    this.http.post(`/api/products/${productId}/qrcode`, {}).subscribe({
      next: () => {
        alert('QR generated');
        this.load();
      },
      error: (err) => alert(err?.error?.message || 'QR error')
    });
  }
}
