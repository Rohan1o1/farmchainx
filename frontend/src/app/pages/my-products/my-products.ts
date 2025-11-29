import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, delay, retryWhen, scan, throwError } from 'rxjs';
import { AuthService } from '../../services/auth.service';

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-products.html'
})
export class MyProducts {
  products: any[] = [];
  loading = true;
  retryMessage = '';
  page = 0;
  size = 9;
  totalPages = 0;
  viewMode: 'owned' | 'pickup' = 'owned'; // For distributors to switch between views

  constructor(private http: HttpClient, private router: Router, private authService: AuthService) {
    this.load();
  }

  isDistributor(): boolean {
    return this.authService.hasRole('ROLE_DISTRIBUTOR');
  }

  isAdmin(): boolean {
    return this.authService.hasRole('ROLE_ADMIN');
  }

  viewProduct(product: any): void {
    if (product.publicUuid) {
      console.log('Navigating to product verification page:', product.publicUuid);
      this.router.navigate(['/verify', product.publicUuid]);
    } else {
      console.warn('Product does not have a publicUuid:', product);
      alert('This product cannot be verified yet. Please try again later.');
    }
  }

  switchToOwnedProducts(): void {
    console.log('Switching to owned products...');
    this.viewMode = 'owned';
    this.products = []; // Clear current products
    this.load(0);
  }

  switchToPickupProducts(): void {
    console.log('Switching to pickup products...');
    this.viewMode = 'pickup';
    this.products = []; // Clear current products
    this.load(0);
  }

  load(page: number = 0) {
    this.loading = true;
    this.retryMessage = '';
    this.page = page;

    // Get the correct endpoint based on user role and view mode
    const endpoint = this.getProductEndpoint();
    const fullUrl = `${endpoint}?page=${page}&size=${this.size}&sort=id,desc`;
    
    console.log('Loading products...', {
      role: this.authService.getRole(),
      viewMode: this.viewMode,
      endpoint,
      fullUrl,
      page,
      size: this.size
    });
    
    this.http
      .get<any>(fullUrl)
      .pipe(
        retryWhen(errors =>
          errors.pipe(
            scan((retryCount) => {
              retryCount++;
              if (retryCount > 3) throw errors;
              this.retryMessage = `🔁 Reconnecting... (Attempt ${retryCount} of 3)`;
              console.log(`Retry attempt ${retryCount} for ${fullUrl}`);
              return retryCount;
            }, 0),
            delay(1000) // exponential backoff could be delay(500 * Math.pow(2, retryCount))
          )
        ),
        catchError(err => {
          this.retryMessage = '';
          this.loading = false;
          console.error('API Error:', err);
          console.error('Failed endpoint:', fullUrl);
          
          const errorMsg = err.status === 404 
            ? `❌ Endpoint not found: ${endpoint}`
            : err.status === 403
            ? `❌ Access denied to ${endpoint}`
            : `❌ Failed to load products: ${err.message || 'Unknown error'}`;
            
          alert(errorMsg);
          return throwError(() => err);
        })
      )
      .subscribe({
        next: (res) => {
          console.log('✅ Products API Response:', {
            endpoint: fullUrl,
            response: res,
            contentLength: res?.content?.length || 0,
            totalElements: res?.totalElements || 0
          });
          
          this.products = res?.content || [];
          this.page = res?.number || 0;
          this.totalPages = res?.totalPages || 1;
          this.loading = false;
          this.retryMessage = '';
          
          console.log(`✅ Loaded ${this.products.length} products for ${this.viewMode} mode`);
          
          if (this.products.length === 0) {
            console.log('ℹ️ No products found for current view mode');
          }
        },
        error: (err) => {
          console.error('❌ Failed to load products:', {
            error: err,
            endpoint: fullUrl,
            role: this.authService.getRole(),
            viewMode: this.viewMode
          });
          this.loading = false;
        }
      });
  }

  nextPage() {
    if (this.page < this.totalPages - 1) this.load(this.page + 1);
  }

  prevPage() {
    if (this.page > 0) this.load(this.page - 1);
  }

  generateQr(id: number) {
    this.http.post<any>(`/api/products/${id}/qrcode`, {}).subscribe({
      next: (res) => {
        const product = this.products.find(p => p.id === id)!;
        const url = res.qrPath.startsWith('http')
          ? res.qrPath
          : `http://localhost:8080${res.qrPath}`;
        const filename = this.generateFilename(product);

        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        alert(`QR Generated & Downloaded: ${filename}`);
        this.load(this.page);
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

  private generateFilename(product: any): string {
    const cleanName = (product.cropName || 'Product')
      .replace(/[^a-zA-Z0-9]/g, '-')
      .replace(/-+/g, '-')
      .replace(/^-|-$/g, '');
    return `QR_${cleanName}_${product.id}.png`;
  }

  getImageUrl(path: string): string {
    return path?.startsWith('http') ? path : `http://localhost:8080${path}`;
  }

  getQrUrl(path: string): string {
    return path?.startsWith('http') ? path : `http://localhost:8080${path}`;
  }

  formatDate(date: string | null): string {
    if (!date) return 'Unknown Date';
    return new Date(date).toLocaleDateString('en-US', {
      month: 'long',
      day: 'numeric',
      year: 'numeric'
    });
  }

  getCropEmoji(name: string): string {
    const n = (name || '').toLowerCase();
    const map: Record<string, string> = {
      onion: '🧅', tomato: '🍅', mango: '🥭', potato: '🥔', rice: '🌾',
      banana: '🍌', apple: '🍎', orange: '🍊', grape: '🍇', wheat: '🌿',
      corn: '🌽', carrot: '🥕', cucumber: '🥒', strawberry: '🍓', watermelon: '🍉'
    };
    return Object.keys(map).find(k => n.includes(k))
      ? map[Object.keys(map).find(k => n.includes(k))!]
      : '🌱';
  }

  private getProductEndpoint(): string {
    const role = this.authService.getRole();
    
    console.log('🔍 Getting endpoint for:', {
      role,
      viewMode: this.viewMode,
      isDistributor: this.isDistributor()
    });
    
    switch (role) {
      case 'ROLE_FARMER':
        console.log('📱 Using farmer endpoint');
        return '/api/products/my';
      case 'ROLE_DISTRIBUTOR':
        // Distributors can switch between:
        // - 'owned': products they currently own/possess (/api/products/available)
        // - 'pickup': products available for pickup from farmers (/api/products/pickup)
        const endpoint = this.viewMode === 'pickup' ? '/api/products/pickup' : '/api/products/available';
        console.log('🚚 Using distributor endpoint:', endpoint, 'for view mode:', this.viewMode);
        return endpoint;
      case 'ROLE_RETAILER':
        console.log('🏪 Using retailer endpoint');
        return '/api/products/pending';
      case 'ROLE_CONSUMER':
        console.log('🛒 Using consumer endpoint');
        return '/api/products/retail';
      case 'ROLE_ADMIN':
        console.log('👑 Using admin endpoint to view all products');
        return '/api/admin/products'; // Admin uses dedicated admin endpoint with pagination
      default:
        // Fallback to farmer endpoint for unknown roles
        console.warn('⚠️ Unknown role, using fallback endpoint:', role);
        return '/api/products/my';
    }
  }
}
