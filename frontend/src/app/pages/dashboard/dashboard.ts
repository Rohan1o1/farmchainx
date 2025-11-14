import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  imports: [CommonModule]
})
export class Dashboard {
  name: string | null = localStorage.getItem('fcx_name') || localStorage.getItem('fcx_email');
  role: string | null = localStorage.getItem('fcx_role');

  constructor(private router: Router) {}

  logout() {
    localStorage.removeItem('fcx_token');
    localStorage.removeItem('fcx_role');
    localStorage.removeItem('fcx_email');
    localStorage.removeItem('fcx_name');
    this.router.navigate(['/login']);
  }
}
