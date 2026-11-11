import { Component, effect, inject, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FilterProductsFormContent,
  ProductFilter,
  ProductFilterForm,
  powerBands,
} from '../../../admin/model/product.model';
import {
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

@Component({
  selector: 'ecom-products-filter',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './products-filter.component.html',
  styleUrl: './products-filter.component.scss',
})
export class ProductsFilterComponent {
  sort = input<string>('createdDate,asc');
  power = input<string>();

  productFilter = output<ProductFilter>();
  formBuilder = inject(FormBuilder);

  protected readonly powerBands = powerBands;

  constructor() {
    effect(() => this.updatePowerFormValue());
    effect(() => this.updateSortFormValue());
    this.formFilterProducts.valueChanges.subscribe(() =>
      this.onFilterChange(this.formFilterProducts.getRawValue())
    );
  }

  formFilterProducts =
    this.formBuilder.nonNullable.group<FilterProductsFormContent>({
      sort: new FormControl<string>(this.sort().split(',')[1], {
        nonNullable: true,
        validators: [Validators.required],
      }),
      power: new FormControl<string>('', { nonNullable: true }),
    });

  private onFilterChange(filter: Partial<ProductFilterForm>) {
    this.productFilter.emit({
      power: filter.power ?? '',
      sort: [`createdDate,${filter.sort}`],
    });
  }

  private updatePowerFormValue() {
    this.formFilterProducts.controls.power.setValue(this.power() ?? '', {
      emitEvent: false,
    });
  }

  private updateSortFormValue() {
    if (this.sort()) {
      this.formFilterProducts.controls.sort.setValue(
        this.sort().split(',')[1],
        { emitEvent: false }
      );
    }
  }
}
