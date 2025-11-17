
import { Component, inject } from '@angular/core';

@Component({
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.html'
})
export class Home {

  get isLoggedIn(): boolean {
    return !!localStorage.getItem('fcx_token');
  }

  get isFarmer(): boolean {
    return localStorage.getItem('fcx_role') === 'ROLE_FARMER';
  }
}
