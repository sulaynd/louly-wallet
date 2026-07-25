import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const authHeader = auth.authHeader();

  if (authHeader['Authorization']) {
    req = req.clone({ setHeaders: authHeader });
  }

  return next(req);
};
