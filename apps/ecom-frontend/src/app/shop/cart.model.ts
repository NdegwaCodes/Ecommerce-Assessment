import { ProductPicture } from '../admin/model/product.model';

export interface CartItemAdd {
  publicId: string;
  quantity: number;
}

export interface Cart {
  products: CartItem[];
}

export interface CartItem {
  name: string;
  price: number;
  brand: string;
  picture: ProductPicture;
  quantity: number;
  publicId: string;
}

export interface StripeSession {
  id: string;
}

export interface MpesaCheckout {
  orderPublicId: string;
}

export type MpesaOrderState = 'PENDING' | 'PAID' | 'FAILED';

export interface MpesaOrderStatus {
  orderPublicId: string;
  status: MpesaOrderState;
  receiptNumber: string | null;
  amount: number;
}
