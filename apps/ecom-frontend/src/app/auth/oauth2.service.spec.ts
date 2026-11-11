import { testProviders } from '../../testing/test-providers';
import { TestBed } from '@angular/core/testing';

import { Oauth2Service } from './oauth2.service';

describe('Oauth2Service', () => {
  let service: Oauth2Service;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: testProviders,
    });
    service = TestBed.inject(Oauth2Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
