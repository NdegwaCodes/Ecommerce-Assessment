import { testProviders } from '../../../testing/test-providers';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductCardComponent } from './product-card.component';
import { Product } from '../../admin/model/product.model';

const product: Product = {
  publicId: 'f3b6a0f2-0000-4000-8000-000000000001',
  brand: 'Kikoy Co',
  color: 'Navy',
  description: 'Cotton t-shirt',
  name: 'Classic Tee',
  price: 1500,
  size: 'M',
  category: { publicId: 'c1', name: 'T-shirts' },
  featured: true,
  pictures: [{ file: 'aGVsbG8=' as unknown as File, mimeType: 'image/png' }],
  nbInStock: 4,
};

describe('ProductCardComponent', () => {
  let fixture: ComponentFixture<ProductCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductCardComponent],
      providers: testProviders,
    }).compileComponents();

    fixture = TestBed.createComponent(ProductCardComponent);
    fixture.componentRef.setInput('product', product);
    fixture.detectChanges();
  });

  it('shows the brand, name and size', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Kikoy Co');
    expect(text).toContain('Classic Tee - M');
  });

  it('links to the product page', () => {
    const link = (fixture.nativeElement as HTMLElement).querySelector('a');
    expect(link?.getAttribute('href')).toBe(`/product/${product.publicId}`);
  });
});
