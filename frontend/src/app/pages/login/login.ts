import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-login',
  templateUrl: './login.html',
  imports: [CommonModule, FormsModule, RouterModule]
})
export class Login {
  email = '';
  password = '';

  constructor(private http: HttpClient, private router: Router) {}

  login() {
    this.http.post<any>('/api/auth/login', {
      email: this.email,
      password: this.password
    }).subscribe({
      next: (res) => {
        // store keys with fcx_ prefix
        localStorage.setItem('fcx_token', res?.token || '');
        localStorage.setItem('fcx_role', res?.role || '');
        localStorage.setItem('fcx_email', res?.email || '');
        localStorage.setItem('fcx_name', res?.name || '');

        alert('Login successful ✅');
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        const msg = err?.error?.error || 'Invalid email or password';
        alert(msg);
      }
    });
  }
}
