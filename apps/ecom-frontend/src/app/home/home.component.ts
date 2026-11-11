import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FeaturedComponent } from './featured/featured.component';

@Component({
  selector: 'ecom-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FeaturedComponent, FaIconComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent {
  protected readonly rays = [0, 45, 90, 135, 180, 225, 270, 315];
}
