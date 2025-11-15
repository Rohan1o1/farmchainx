// src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { App } from './app/app';
import { appConfig } from './app/app.config';

import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { JwtInterceptor } from './app/interceptors/jwt.interceptor';

bootstrapApplication(App, {
  providers: [
    ...appConfig.providers,

    // Http client + JWT interceptor
    provideHttpClient(
      withInterceptors([JwtInterceptor])
    )
  ]
}).catch(err => console.error(err));
