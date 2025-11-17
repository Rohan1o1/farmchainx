import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.html'
})
export class Navbar {
  private router = inject(Router);
  private auth = inject(AuthService);

  get isLoggedIn() {
    return !!localStorage.getItem('fcx_token');
  }

  get userRole() {
    return localStorage.getItem('fcx_role');
  }

  get userName() {
    return localStorage.getItem('fcx_name') || localStorage.getItem('fcx_email');
  }

  get isFarmer() {
    return this.userRole === 'ROLE_FARMER';
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
