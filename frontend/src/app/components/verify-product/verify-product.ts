import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-verify-product',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './verify-product.html',
  styleUrl: './verify-product.scss'
})
export class VerifyProduct implements OnInit {
  product: any = null;
  loading = true;
  error = '';
  uuid = '';
  canUpdate = false;
  showAddForm = false;
  newNote = '';
  newLocation = '';

  displayLocation = '';
  resolvingLocation = false;
  private locationCache = new Map<string, string>();

  userRole: 'Guest' | 'Farmer' | 'Distributor' | 'Retailer' | 'Admin' = 'Guest';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.uuid = this.route.snapshot.paramMap.get('uuid')!;
    this.loadProduct();
    this.detectUserRole();
  }

  private loadProduct() {
    this.loading = true;
    this.http.get(`/api/verify/${this.uuid}`).subscribe({
      next: (data: any) => {
        this.product = data;
        this.canUpdate = !!data.canUpdate;  // ONLY from backend — 100% secure
        this.loading = false;

        if (data.gpsLocation?.includes(',')) {
          this.resolveLocation(data.gpsLocation.trim());
        } else {
          this.displayLocation = 'Protected Farm Location';
        }
      },
      error: () => {
        this.error = 'Invalid or expired QR code • Product not found';
        this.loading = false;
      }
    });
  }

  private detectUserRole() {
    this.http.get<any>('/api/auth/me').subscribe({
      next: (user) => {
        const roles = user?.roles || [];
        if (roles.some((r: string) => r.includes('DISTRIBUTOR'))) this.userRole = 'Distributor';
        else if (roles.some((r: string) => r.includes('RETAILER'))) this.userRole = 'Retailer';
        else if (roles.some((r: string) => r.includes('FARMER'))) this.userRole = 'Farmer';
        else if (roles.some((r: string) => r.includes('ADMIN'))) this.userRole = 'Admin';
        else this.userRole = 'Guest';
      },
      error: () => this.userRole = 'Guest'
    });
  }

  private async resolveLocation(coords: string) {
    if (this.locationCache.has(coords)) {
      this.displayLocation = this.locationCache.get(coords)!;
      return;
    }
    this.resolvingLocation = true;
    try {
      const [lat, lng] = coords.split(',').map(parseFloat);
      const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=10&addressdetails=1`, {
        headers: { 'User-Agent': 'FarmChainX/1.0 (your-email@example.com)' }
      });
      const data = await res.json();
      const parts = [
        data.address?.village || data.address?.town || data.address?.city || data.address?.hamlet,
        data.address?.state_district || data.address?.county,
        data.address?.state,
        data.address?.country
      ].filter(Boolean);
      this.displayLocation = parts.length ? parts.join(', ') : 'Protected Farm Location';
      this.locationCache.set(coords, this.displayLocation);
    } catch {
      this.displayLocation = 'Protected Farm Location';
    } finally {
      this.resolvingLocation = false;
    }
  }

  addTrackingUpdate() {
    if (!this.newNote.trim() || !this.newLocation.trim()) {
      alert('Please fill both location and note');
      return;
    }
    this.http.post(`/api/verify/${this.uuid}/track`, {
      note: this.newNote.trim(),
      location: this.newLocation.trim()
    }).subscribe({
      next: () => {
        this.newNote = this.newLocation = '';
        this.showAddForm = false;
        this.loadProduct();
        alert('Tracking update saved on blockchain!');
      },
      error: (err) => alert(err.error?.error || 'Failed to save update')
    });
  }

  getCropEmoji(): string {
    const name = (this.product?.cropName || '').toLowerCase();
    const map: Record<string, string> = {
      onion: '🧅', tomato: '🍅', mango: '🥭', potato: '🥔', rice: '🌾',
      banana: '🍌', apple: '🍎', orange: '🍊', grape: '🍇', wheat: '🌾',
      corn: '🌽', maize: '🌽', carrot: '🥕', cucumber: '🥒', lettuce: '🥬',
      strawberry: '🍓', watermelon: '🍉', coffee: '☕', cotton: '👕', sugarcane: '🌱'
    };
    return Object.keys(map).find(k => name.includes(k)) ? map[Object.keys(map).find(k => name.includes(k))!] : '🌱';
  }

  // Helper for image URL (no pipe!)
  getImageUrl(path: string): string {
    return path?.startsWith('http') ? path : `http://localhost:8080${path}`;
  }
}