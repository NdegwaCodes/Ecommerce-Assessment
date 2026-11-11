import { testProviders } from '../../../testing/test-providers';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductCardComponent } from './product-card.component';
import { Product } from '../../admin/model/product.model';

const product: Product = {
  publicId: 'f3b6a0f2-0000-4000-8000-000000000001',
  brand: 'Jua Cell',
  warrantyYears: 25,
  description: 'Monocrystalline panel',
  name: 'Mono Panel 450 W',
  price: 18500,
  wattage: 450,
  category: { publicId: 'c1', name: 'Solar panels' },
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

  it('shows the brand, name, power, warranty and price in shillings', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Jua Cell');
    expect(text).toContain('Mono Panel 450 W');
    expect(text).toContain('450 W');
    expect(text).toContain('25-year warranty');
    expect(text).toContain('KSh 18,500');
  });

  it('links to the product page', () => {
    const link = (fixture.nativeElement as HTMLElement).querySelector('a');
    expect(link?.getAttribute('href')).toBe(`/product/${product.publicId}`);
  });
});
