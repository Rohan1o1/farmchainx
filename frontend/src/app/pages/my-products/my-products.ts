// src/app/pages/my-products/my-products.component.ts

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
      error: () => {
        alert('Failed to load products');
        this.loading = false;
      }
    });
  }

  // AUTO-DOWNLOAD + PERFECT FILENAME
  generateQr(id: number) {
    this.http.post<any>(`/api/products/${id}/qrcode`, {}).subscribe({
      next: (res) => {
        const product = this.products.find(p => p.id === id)!;
        const url = res.qrPath.startsWith('http') ? res.qrPath : `http://localhost:8080${res.qrPath}`;
        const filename = this.generateFilename(product);

        // Trigger auto-download
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        alert(`QR Generated & Downloaded: ${filename}`);
        this.load(); // refresh preview
      },
      error: (err) => alert(err.error?.message || 'Failed to generate QR')
    });
  }

  downloadQr(id: number) {
    const product = this.products.find(p => p.id === id);
    if (!product?.qrCodePath) return;

    const url = this.getQrUrl(product.qrCodePath);
    const filename = this.generateFilename(product);

    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  // Perfect filename: QR_Red-Onion_47.png
  private generateFilename(product: any): string {
    const cleanName = (product.cropName || 'Product')
      .replace(/[^a-zA-Z0-9]/g, '-')
      .replace(/-+/g, '-')
      .replace(/^-|-$/g, '');
    return `QR_${cleanName}_${product.id}.png`;
  }

  // Helpers
  getImageUrl(path: string): string {
    return path?.startsWith('http') ? path : `http://localhost:8080${path}`;
  }

  getQrUrl(path: string): string {
    return path?.startsWith('http') ? path : `http://localhost:8080${path}`;
  }

  formatDate(date: string | null): string {
    if (!date) return 'Unknown Date';
    return new Date(date).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
  }

  getCropEmoji(name: string): string {
    const n = (name || '').toLowerCase();
    const map: Record<string, string> = {
      onion: 'Onion', tomato: 'Tomato', mango: 'Mango', potato: 'Potato',
      rice: 'Ear of Rice', banana: 'Banana', apple: 'Apple', orange: 'Orange',
      grape: 'Grapes', wheat: 'Wheat', corn: 'Ear of Corn', carrot: 'Carrot',
      cucumber: 'Cucumber', strawberry: 'Strawberry', watermelon: 'Watermelon'
    };
    return Object.keys(map).find(k => n.includes(k)) ? map[Object.keys(map).find(k => n.includes(k))!] : 'Seedling';
  }
}