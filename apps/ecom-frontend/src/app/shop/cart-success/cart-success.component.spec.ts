import { testProviders } from '../../../testing/test-providers';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CartSuccessComponent } from './cart-success.component';

describe('CartSuccessComponent', () => {
  let component: CartSuccessComponent;
  let fixture: ComponentFixture<CartSuccessComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CartSuccessComponent],
      providers: testProviders,
    }).compileComponents();

    fixture = TestBed.createComponent(CartSuccessComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
