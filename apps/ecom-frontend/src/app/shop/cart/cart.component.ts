import { KesPipe } from '../../shared/kes.pipe';
import {
  Component,
  DestroyRef,
  effect,
  inject,
  OnInit,
  PLATFORM_ID,
  signal,
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { CartService } from '../cart.service';
import { Oauth2Service } from '../../auth/oauth2.service';
import { ToastService } from '../../shared/toast/toast.service';
import {
  CartItem,
  CartItemAdd,
  MpesaOrderStatus,
  StripeSession,
} from '../cart.model';
import {
  injectMutation,
  injectQuery,
} from '@tanstack/angular-query-experimental';
import { lastValueFrom, Subscription, switchMap, takeWhile, timer } from 'rxjs';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { StripeService } from 'ngx-stripe';

@Component({
  selector: 'ecom-cart',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, KesPipe],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss',
})
export class CartComponent implements OnInit {
  cartService = inject(CartService);
  oauth2Service = inject(Oauth2Service);
  toastService = inject(ToastService);
  stripeService = inject(StripeService);
  router = inject(Router);
  destroyRef = inject(DestroyRef);

  mpesaPhone = '';
  mpesaWaiting = signal(false);
  mpesaStarting = signal(false);
  mpesaMessage = signal<string | null>(null);
  private mpesaPolling?: Subscription;

  cart: Array<CartItem> = [];

  platformId = inject(PLATFORM_ID);

  labelCheckout = 'Login to checkout';

  action: 'login' | 'checkout' = 'login';

  isInitPaymentSessionLoading = false;

  cartQuery = injectQuery(() => ({
    queryKey: ['cart'],
    queryFn: () => lastValueFrom(this.cartService.getCartDetail()),
  }));

  initPaymentSession = injectMutation(() => ({
    mutationFn: (cart: Array<CartItemAdd>) =>
      lastValueFrom(this.cartService.initPaymentSession(cart)),
    onSuccess: (result: StripeSession) => this.onSessionCreateSuccess(result),
  }));

  constructor() {
    this.extractListToUpdate();
    this.checkUserLoggedIn();
  }

  private extractListToUpdate() {
    effect(() => {
      if (this.cartQuery.isSuccess()) {
        this.cart = this.cartQuery.data().products;
      }
    });
  }

  private checkUserLoggedIn() {
    effect(() => {
      const connectedUserQuery = this.oauth2Service.connectedUserQuery;
      if (connectedUserQuery?.isError()) {
        this.labelCheckout = 'Login to checkout';
        this.action = 'login';
      } else if (connectedUserQuery?.isSuccess()) {
        this.labelCheckout = 'Checkout';
        this.action = 'checkout';
      }
    });
  }

  ngOnInit(): void {
    this.cartService.addedToCart.subscribe((cart) => this.updateQuantity(cart));
  }

  private updateQuantity(cartUpdated: Array<CartItemAdd>) {
    for (const cartItemToUpdate of this.cart) {
      const itemToUpdate = cartUpdated.find(
        (item) => item.publicId === cartItemToUpdate.publicId
      );
      if (itemToUpdate) {
        cartItemToUpdate.quantity = itemToUpdate.quantity;
      } else {
        this.cart.splice(this.cart.indexOf(cartItemToUpdate), 1);
      }
    }
  }

  addQuantityToCart(publicId: string) {
    this.cartService.addToCart(publicId, 'add');
  }

  removeQuantityToCart(publicId: string, quantity: number) {
    if (quantity > 1) {
      this.cartService.addToCart(publicId, 'remove');
    }
  }

  removeItem(publicId: string) {
    const itemToRemoveIndex = this.cart.findIndex(
      (item) => item.publicId === publicId
    );
    if (itemToRemoveIndex !== -1) {
      this.cart.splice(itemToRemoveIndex, 1);
    }
    this.cartService.removeFromCart(publicId);
  }

  computeTotal() {
    return this.cart.reduce((acc, item) => acc + item.price * item.quantity, 0);
  }

  checkIfEmptyCart(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      return (
        this.cartQuery.isSuccess() &&
        this.cartQuery.data().products.length === 0
      );
    } else {
      return false;
    }
  }

  checkout() {
    if (this.action === 'login') {
      this.oauth2Service.login();
    } else if (this.action === 'checkout') {
      this.isInitPaymentSessionLoading = true;
      const cartItemsAdd = this.cart.map(
        (item) =>
          ({ publicId: item.publicId, quantity: item.quantity } as CartItemAdd)
      );
      this.initPaymentSession.mutate(cartItemsAdd);
    }
  }

  payWithMpesa() {
    if (this.action === 'login') {
      this.oauth2Service.login();
      return;
    }
    this.mpesaMessage.set(null);
    this.mpesaStarting.set(true);
    const items = this.cart.map(
      (item) => ({ publicId: item.publicId, quantity: item.quantity } as CartItemAdd)
    );
    this.cartService.payWithMpesa(items, this.mpesaPhone).subscribe({
      next: ({ orderPublicId }) => {
        this.mpesaStarting.set(false);
        this.cartService.storeSessionId(orderPublicId);
        this.waitForMpesa(orderPublicId);
      },
      error: (error: HttpErrorResponse) => {
        this.mpesaStarting.set(false);
        this.mpesaMessage.set(
          error.error?.message ?? 'Could not start the M-Pesa payment. Try again.'
        );
      },
    });
  }

  private waitForMpesa(orderPublicId: string) {
    this.mpesaWaiting.set(true);
    const startedAt = Date.now();
    this.mpesaPolling?.unsubscribe();
    this.mpesaPolling = timer(4000, 4000)
      .pipe(
        switchMap(() => this.cartService.getMpesaOrder(orderPublicId)),
        takeWhile(
          (order) => order.status === 'PENDING' && Date.now() - startedAt < 120000,
          true
        )
      )
      .subscribe({
        next: (order) => this.onMpesaStatus(order, startedAt),
        error: () => {
          this.mpesaWaiting.set(false);
          this.mpesaMessage.set('We lost contact with the payment. Check your orders before paying again.');
        },
      });
    this.destroyRef.onDestroy(() => this.mpesaPolling?.unsubscribe());
  }

  private onMpesaStatus(order: MpesaOrderStatus, startedAt: number) {
    if (order.status === 'PAID') {
      this.mpesaWaiting.set(false);
      this.router.navigate(['/cart/success'], {
        queryParams: { session_id: order.orderPublicId, receipt: order.receiptNumber },
      });
    } else if (order.status === 'FAILED') {
      this.mpesaWaiting.set(false);
      this.mpesaMessage.set('The M-Pesa payment did not go through. You can try again.');
    } else if (Date.now() - startedAt >= 120000) {
      this.mpesaWaiting.set(false);
      this.mpesaMessage.set(
        'We have not heard back from M-Pesa yet. If you paid, the order will show as paid in your orders.'
      );
    }
  }

  private onSessionCreateSuccess(sessionId: StripeSession) {
    this.cartService.storeSessionId(sessionId.id);
    this.stripeService
      .redirectToCheckout({ sessionId: sessionId.id })
      .subscribe((results) => {
        this.isInitPaymentSessionLoading = false;
        this.toastService.show(`Order error ${results.error.message}`, 'ERROR');
      });
  }
}
