import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { brand } from '../../shared/brand';

@Component({
  selector: 'ecom-footer',
  standalone: true,
  imports: [CommonModule, RouterLink, FaIconComponent],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
})
export class FooterComponent {
  protected readonly brand = brand;
}
