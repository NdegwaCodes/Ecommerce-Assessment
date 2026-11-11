import { testProviders } from '../../../testing/test-providers';
import { TestBed } from '@angular/core/testing';

import { UserProductService } from './user-product.service';

describe('UserProductService', () => {
  let service: UserProductService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: testProviders,
    });
    service = TestBed.inject(UserProductService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
