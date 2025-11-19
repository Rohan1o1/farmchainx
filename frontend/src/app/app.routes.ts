import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/home/home').then(m => m.Home)
  },

  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login').then(m => m.Login)
  },

  {
    path: 'register',
    loadComponent: () =>
      import('./pages/register/register').then(m => m.Register)
  },

{
  path: 'verify/:uuid',
  loadComponent: () => import('./components/verify-product/verify-product').then(m => m.VerifyProduct)
},

  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./pages/dashboard/dashboard').then(m => m.Dashboard)
  },
  {
    path: 'upload',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./pages/upload-product/upload-product').then(m => m.UploadProduct)
  },
{
    path: 'scanner',
    canActivate: [AuthGuard],
    loadComponent: () => import('./components/qr-scanner/qr-scanner/qr-scanner')
      .then(m => m.QrScannerComponent)
  },

  {
  path: 'products/my',
  canActivate: [AuthGuard],
  loadComponent: () => import('./pages/my-products/my-products').then(m => m.MyProducts)
},

  { path: '**', redirectTo: '' }
];
