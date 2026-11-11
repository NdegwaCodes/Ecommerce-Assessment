import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {
  APP_INITIALIZER,
  EnvironmentProviders,
  inject,
  Provider,
} from '@angular/core';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { fontAwesomeIcons } from '../app/shared/font-awesome-icons';
import { provideRouter } from '@angular/router';
import {
  provideQueryClient,
  QueryClient,
} from '@tanstack/angular-query-experimental';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { provideNgxStripe } from 'ngx-stripe';
import { of } from 'rxjs';

export const oidcSecurityServiceStub = {
  checkAuth: () => of({ isAuthenticated: false }),
  authorize: () => undefined,
  logoff: () => of(null),
  logoffLocal: () => undefined,
  getAccessToken: () => of(''),
  isAuthenticated$: of({ isAuthenticated: false }),
};

export const testProviders: (Provider | EnvironmentProviders)[] = [
  provideHttpClient(),
  provideHttpClientTesting(),
  provideRouter([]),
  provideQueryClient(
    new QueryClient({ defaultOptions: { queries: { retry: false } } })
  ),
  provideNgxStripe('pk_test_unit'),
  { provide: OidcSecurityService, useValue: oidcSecurityServiceStub },
  {
    provide: APP_INITIALIZER,
    multi: true,
    useFactory: () => {
      const library = inject(FaIconLibrary);
      return () => library.addIcons(...fontAwesomeIcons);
    },
  },
];
