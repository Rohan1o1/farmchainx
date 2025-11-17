import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-upload-product',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './upload-product.html'
})
export class UploadProduct {

  cropName = '';
  soilType = '';
  pesticides = '';
  harvestDate = '';
  gpsLocation = '';
  imageFile: File | null = null;
  previewUrl: string | ArrayBuffer | null = null;

  loading = false;

  constructor(private http: HttpClient, private router: Router) {}

  onFileSelected(event: any) {
    const file = event.target.files && event.target.files[0];
    if (!file) return;

    this.imageFile = file;
    const reader = new FileReader();
    reader.onload = () => this.previewUrl = reader.result;
    reader.readAsDataURL(file);
  }

  detectGPS() {
    if (!navigator.geolocation) {
      alert("GPS not supported");
      return;
    }

    navigator.geolocation.getCurrentPosition(position => {
      const lat = position.coords.latitude;
      const lng = position.coords.longitude;
      this.gpsLocation = `${lat},${lng}`;
    });
  }

  uploadProduct() {
    if (!this.imageFile) {
      alert("Please select an image");
      return;
    }

    this.loading = true;

    const formData = new FormData();
    formData.append('cropName', this.cropName);
    formData.append('soilType', this.soilType);
    formData.append('pesticides', this.pesticides);
    formData.append('harvestDate', this.harvestDate);
    formData.append('gpsLocation', this.gpsLocation);
    formData.append('image', this.imageFile);

    this.http.post<any>('/api/products/upload', formData)
      .subscribe({
        next: (res) => {
          this.loading = false;
          alert(`Product uploaded! ID = ${res.id}`);
          this.router.navigate(['/dashboard']);
        },
       error: (err) => {
  this.loading = false;
  console.error('Upload error full:', err);
  console.error('Status:', err?.status);
  console.error('Error body:', err?.error);
  const serverMsg = err?.error?.message || err?.error?.error || err?.statusText || (err?.message ? err.message : 'Upload failed!');
  alert(`Upload failed: ${serverMsg}`);
}
      });
  }
}
