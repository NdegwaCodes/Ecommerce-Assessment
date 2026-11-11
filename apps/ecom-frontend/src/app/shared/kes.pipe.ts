import { Pipe, PipeTransform } from '@angular/core';

const formatter = new Intl.NumberFormat('en-KE', { maximumFractionDigits: 0 });

@Pipe({
  name: 'kes',
  standalone: true,
})
export class KesPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    if (value === null || value === undefined || Number.isNaN(value)) {
      return '';
    }
    return `KSh ${formatter.format(Math.round(value))}`;
  }
}
