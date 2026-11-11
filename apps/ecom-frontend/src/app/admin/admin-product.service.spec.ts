import { testProviders } from '../../testing/test-providers';
import { TestBed } from '@angular/core/testing';

import { AdminProductService } from './admin-product.service';

describe('AdminProductService', () => {
  let service: AdminProductService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: testProviders,
    });
    service = TestBed.inject(AdminProductService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
