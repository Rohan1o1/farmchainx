import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

interface User {
  id: number;
  name: string;
  email: string;
  roles: string[];
  isAdmin: boolean;
}

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.scss'
})
export class AdminUsers implements OnInit {
  users: User[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.http.get<any[]>('/api/admin/users').subscribe(data => {
      this.users = data.map(u => ({
        id: u.id,
        name: u.name,
        email: u.email,
        roles: u.roles.map((r: any) => r.name || r),
        isAdmin: u.roles.some((r: any) => (r.name || r) === 'ROLE_ADMIN')
      }));
    });
  }

  promote(userId: number): void {
    if (!confirm('Promote this user to Admin?')) return;

    this.http.post(`/api/admin/promote/${userId}`, {}).subscribe({
      next: () => {
        alert('User promoted to Admin successfully!');
        this.loadUsers();
      },
      error: (err) => alert(err.error?.message || 'Promotion failed')
    });
  }
}