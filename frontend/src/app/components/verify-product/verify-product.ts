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
  retailers: any[] = [];
  selectedRetailerId: number | null = null;
  currentUserId: number | null = null;
  displayLocation = '';
  resolvingLocation = false;
  isFinalHandover = false;

  userRole: 'Guest' | 'Farmer' | 'Distributor' | 'Retailer' | 'Admin' | 'Consumer' = 'Guest';

  feedbacks: any[] = [];
  consumerCanGiveFeedback = false;
  myRating = 5;
  myComment = '';

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit(): void {
    this.uuid = this.route.snapshot.paramMap.get('uuid')!;
    this.readRoleFromToken();
    this.loadProduct();
  }

  private readRoleFromToken() {
    const token = localStorage.getItem('fcx_token') || localStorage.getItem('token') || localStorage.getItem('jwt') || null;
    const storedRole = localStorage.getItem('fcx_role') || localStorage.getItem('role') || null;

    if (storedRole) {
      const rn = storedRole.toUpperCase().replace(/^ROLE_/, '');
      if (rn.includes('DISTRIBUTOR')) this.userRole = 'Distributor';
      else if (rn.includes('RETAILER')) this.userRole = 'Retailer';
      else if (rn.includes('FARMER')) this.userRole = 'Farmer';
      else if (rn.includes('ADMIN')) this.userRole = 'Admin';
      else if (rn.includes('CONSUMER')) this.userRole = 'Consumer';
      else this.userRole = 'Guest';
    }

    if (!token) return;

    try {
      const parts = token.split('.');
      if (parts.length < 2) return;
      const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));

      this.currentUserId = payload.userId || payload.id || payload.sub || payload.user?.id || null;
      if (this.userRole === 'Distributor') this.loadRetailers();
    } catch (e) {}
  }

  private loadProduct() {
    this.loading = true;
    this.http.get(`/api/verify/${this.uuid}`).subscribe({
      next: (data: any) => {
        this.product = data;
        this.canUpdate = (data && data.canUpdate === true) || (this.userRole === 'Distributor' || this.userRole === 'Retailer');
        this.consumerCanGiveFeedback = data.canGiveFeedback === true;
        this.loading = false;
        this.displayLocation = data.displayLocation || data.gpsLocation || '';
        this.loadFeedbacks();
      },
      error: () => {
        this.error = 'Invalid or expired QR code • Product not found';
        this.loading = false;
      }
    });
  }

  private loadRetailers() {
    this.http.get<any[]>('/api/track/users/retailers').subscribe({
      next: (data) => (this.retailers = data)
    });
  }

  private loadFeedbacks() {
    if (!this.product?.productId) return;
    this.http.get<any[]>(`/api/products/${this.product.productId}/feedbacks`).subscribe({
      next: (list) => (this.feedbacks = list || []),
      error: () => {}
    });
  }

  submitFeedback() {
    if (!this.product?.productId) {
      alert('Product not loaded');
      return;
    }
    const rating = Number(this.myRating);
    if (!rating || rating < 1 || rating > 5) {
      alert('Rating must be between 1 and 5');
      return;
    }
    const payload = { rating, comment: (this.myComment || '').trim() };
    this.http.post(`/api/products/${this.product.productId}/feedback`, payload).subscribe({
      next: () => {
        alert('Thanks for your feedback!');
        this.consumerCanGiveFeedback = false;
        this.myRating = 5;
        this.myComment = '';
        this.loadFeedbacks();
      },
      error: (err) => {
        alert(err.error?.error || 'Failed to submit feedback');
      }
    });
  }

  hasTakenFromFarmer(): boolean {
    if (!this.product?.trackingHistory?.length || !this.currentUserId) return false;
    const lastLog = this.product.trackingHistory[this.product.trackingHistory.length - 1];
    const lastTo = lastLog?.toUserId != null ? Number(lastLog.toUserId) : null;
    const myId = this.currentUserId != null ? Number(this.currentUserId) : null;
    const confirmed = typeof lastLog?.confirmed === 'boolean' ? lastLog.confirmed : false;
    return lastTo === myId && confirmed === true;
  }

  showUpdatePossible(): boolean {
    if (!this.canUpdate) return false;
    if (!this.product?.trackingHistory || this.product.trackingHistory.length === 0) {
      return this.userRole === 'Distributor';
    }
    const lastLog = this.product.trackingHistory[this.product.trackingHistory.length - 1];
    const lastFrom = lastLog?.fromUserId != null ? Number(lastLog.fromUserId) : null;
    const lastTo = lastLog?.toUserId != null ? Number(lastLog.toUserId) : null;
    const myId = this.currentUserId != null ? Number(this.currentUserId) : null;
    const confirmed = typeof lastLog?.confirmed === 'boolean' ? lastLog.confirmed : false;
    const rejected = typeof lastLog?.rejected === 'boolean' ? lastLog.rejected : false;
    if (this.userRole === 'Distributor') {
      if (lastFrom == null && lastTo == null) return true;
      if (lastTo === myId) return !rejected;
      return false;
    }
    if (this.userRole === 'Retailer') {
      return lastTo === myId && confirmed === false && rejected === false;
    }
    return false;
  }

  currentActionText(): string {
    if (this.userRole === 'Distributor') {
      if (!this.hasTakenFromFarmer() && this.showUpdatePossible()) return '1. Confirm Receipt from Farmer ✓';
      return this.isFinalHandover ? 'FINAL: Hand Over to Retailer →' : '+ Add Tracking Update';
    }
    if (this.userRole === 'Retailer') return 'Confirm Receipt ✓';
    return '';
  }

  submitChainUpdate() {
    if (!this.newLocation.trim()) {
      alert('Location is required');
      return;
    }
    const payload: any = { location: this.newLocation.trim(), note: this.newNote.trim() || undefined };
    if (this.isFinalHandover) {
      if (!this.selectedRetailerId) {
        alert('Please select a retailer');
        return;
      }
      payload.toUserId = this.selectedRetailerId;
    }
    this.http.post(`/api/verify/${this.uuid}/track`, payload).subscribe({
      next: () => {
        alert('Success!');
        this.resetForm();
        this.loadProduct();
      },
      error: (err) => alert(err.error?.error || 'Failed')
    });
  }

  resetForm() {
    this.newLocation = '';
    this.newNote = '';
    this.selectedRetailerId = null;
    this.showAddForm = false;
    this.isFinalHandover = false;
  }

  getCropEmoji(): string {
    const name = (this.product?.cropName || '').toLowerCase();
    const map: Record<string, string> = {
      onion: 'Onion', tomato: 'Tomato', mango: 'Mango', potato: 'Potato', rice: 'Rice',
      banana: 'Banana', apple: 'Apple', orange: 'Orange', grape: 'Grape', wheat: 'Wheat',
      corn: 'Corn', carrot: 'Carrot', cucumber: 'Cucumber', lettuce: 'Lettuce',
      strawberry: 'Strawberry', watermelon: 'Watermelon', coffee: 'Coffee', cotton: 'Cotton', sugarcane: 'Sugarcane'
    };
    const key = Object.keys(map).find(k => name.includes(k));
    return key ? map[key] : 'Seedling';
  }

  getImageUrl(path: string): string {
    return path?.startsWith('http') ? path : `http://localhost:8080${path}`;
  }
}
