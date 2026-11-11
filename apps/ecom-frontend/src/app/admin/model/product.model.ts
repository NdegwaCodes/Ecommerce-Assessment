import { FormControl } from '@angular/forms';

export interface PowerBand {
  key: string;
  label: string;
  minWattage?: number;
  maxWattage?: number;
}

export const powerBands: PowerBand[] = [
  { key: 'small', label: 'Up to 100 W', maxWattage: 100 },
  { key: 'medium', label: '100 W to 500 W', minWattage: 100, maxWattage: 500 },
  { key: 'large', label: '500 W to 2 kW', minWattage: 500, maxWattage: 2000 },
  { key: 'xlarge', label: 'Over 2 kW', minWattage: 2000 },
];

export interface ProductCategory {
  publicId?: string;
  name?: string;
}

export interface ProductPicture {
  file: File;
  mimeType: string;
}

export interface BaseProduct {
  brand: string;
  warrantyYears: number;
  description: string;
  name: string;
  price: number;
  wattage: number;
  category: ProductCategory;
  featured: boolean;
  pictures: ProductPicture[];
  nbInStock: number;
}

export interface Product extends BaseProduct {
  publicId: string;
}

export type CreateCategoryFormContent = {
  name: FormControl<string>;
};

export type CreateProductFormContent = {
  brand: FormControl<string>;
  warrantyYears: FormControl<number>;
  description: FormControl<string>;
  name: FormControl<string>;
  price: FormControl<number>;
  wattage: FormControl<number>;
  category: FormControl<string>;
  featured: FormControl<boolean>;
  pictures: FormControl<ProductPicture[]>;
  stock: FormControl<number>;
};

export interface ProductFilter {
  power?: string;
  category?: string | null;
  sort: string[];
}

export type FilterProductsFormContent = {
  sort: FormControl<string>;
  power: FormControl<string>;
}

export interface ProductFilterForm {
  power?: string;
  sort: string;
}
