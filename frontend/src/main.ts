// src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { App } from './app/app';
import { appConfig } from './app/app.config';
import { provideHttpClient } from '@angular/common/http';

bootstrapApplication(App, {
  providers: [
    ...appConfig.providers,
    provideHttpClient()               // required for AuthService
  ]
}).catch(err => console.error(err));