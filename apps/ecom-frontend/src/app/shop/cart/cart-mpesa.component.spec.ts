import { testProviders } from '../../../testing/test-providers';
import {
  ComponentFixture,
  discardPeriodicTasks,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { CartComponent } from './cart.component';
import { CartItem } from '../cart.model';

const battery: CartItem = {
  publicId: 'battery-1',
  name: 'Lithium Battery 2.4 kWh',
  brand: 'Jua Cell',
  price: 9000,
  quantity: 2,
  picture: { file: 'aGVsbG8=' as unknown as File, mimeType: 'image/png' },
};

describe('CartComponent M-Pesa checkout', () => {
  let fixture: ComponentFixture<CartComponent>;
  let component: CartComponent;
  let http: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CartComponent],
      providers: testProviders,
    }).compileComponents();

    fixture = TestBed.createComponent(CartComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    component.action = 'checkout';
    component.cart = [{ ...battery }];
  });

  it('sends an STK push and opens the success page once the order is paid', fakeAsync(() => {
    const navigate = jest.spyOn(router, 'navigate').mockResolvedValue(true);
    component.mpesaPhone = '0712 345 678';

    component.payWithMpesa();

    const start = http.expectOne(
      (req) => req.method === 'POST' && req.url.endsWith('/orders/mpesa')
    );
    expect(start.request.body).toEqual({
      items: [{ publicId: 'battery-1', quantity: 2 }],
      phoneNumber: '0712 345 678',
    });
    start.flush({ orderPublicId: 'order-1' });
    expect(component.mpesaWaiting()).toBe(true);

    tick(4000);
    http
      .expectOne((req) => req.url.endsWith('/orders/mpesa/order-1'))
      .flush({ orderPublicId: 'order-1', status: 'PENDING', receiptNumber: null, amount: 18000 });
    expect(navigate).not.toHaveBeenCalled();

    tick(4000);
    http
      .expectOne((req) => req.url.endsWith('/orders/mpesa/order-1'))
      .flush({ orderPublicId: 'order-1', status: 'PAID', receiptNumber: 'NLJ7RT61SV', amount: 18000 });

    expect(navigate).toHaveBeenCalledWith(['/cart/success'], {
      queryParams: { session_id: 'order-1', receipt: 'NLJ7RT61SV' },
    });
    expect(component.mpesaWaiting()).toBe(false);
    discardPeriodicTasks();
  }));

  it('tells the customer when the payment fails', fakeAsync(() => {
    component.mpesaPhone = '0712345678';
    component.payWithMpesa();
    http
      .expectOne((req) => req.method === 'POST' && req.url.endsWith('/orders/mpesa'))
      .flush({ orderPublicId: 'order-2' });

    tick(4000);
    http
      .expectOne((req) => req.url.endsWith('/orders/mpesa/order-2'))
      .flush({ orderPublicId: 'order-2', status: 'FAILED', receiptNumber: null, amount: 18000 });

    expect(component.mpesaWaiting()).toBe(false);
    expect(component.mpesaMessage()).toContain('did not go through');
    discardPeriodicTasks();
  }));

  it('shows the reason when the request is refused', () => {
    component.mpesaPhone = '12345';
    component.payWithMpesa();
    http
      .expectOne((req) => req.method === 'POST' && req.url.endsWith('/orders/mpesa'))
      .flush(
        { message: 'Enter a Safaricom number like 0712 345 678' },
        { status: 400, statusText: 'Bad Request' }
      );

    expect(component.mpesaStarting()).toBe(false);
    expect(component.mpesaMessage()).toBe('Enter a Safaricom number like 0712 345 678');
  });
});
