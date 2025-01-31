# E-commerce Application

A modern e-commerce platform built with Angular, featuring product management, shopping cart functionality, and secure checkout.

## 🚀 Features

### Customer Features
- **Product Browsing**
  - Browse featured products on homepage
  - Filter products by size and category
  - Sort products by date
  - View detailed product information
  - See related products

- **Shopping Cart**
  - Add/remove items
  - Adjust quantities
  - Persistent cart storage
  - Real-time price calculations

- **Checkout**
  - Secure Stripe integration
  - Order confirmation
  - Email notifications
  - Order history

### Admin Features
- **Product Management**
  - Create and edit products
  - Manage product categories
  - Set featured products
  - Control inventory

- **Order Management**
  - View and process orders
  - Update order status
  - Access order history

## 🛠 Technical Stack

- **Frontend**: Angular 14+
- **Styling**: Tailwind CSS
- **State Management**: TanStack Query
- **Authentication**: OIDC with Kinde
- **Payment Processing**: Stripe
- **Icons**: Font Awesome

## 📦 Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/ecommerce-app.git
```

2. Install dependencies:
```bash
pnpm install
```

3. Set up environment variables:
```bash
cp .env.example .env
```

Configure the following variables:
```env
KINDE_CLIENT_ID=your_kinde_client_id
KINDE_CLIENT_SECRET=your_kinde_client_secret
STRIPE_PUBLISHABLE_KEY=your_stripe_key
API_URL=your_api_url
```

4. Start the development server:
```bash
pnpm start
```

## 🏗 Project Structure

```
apps/
  ├── Ecommerce Assessment/
  │   ├── src/
  │   │   ├── app/
  │   │   │   ├── admin/       # Admin features
  │   │   │   ├── auth/        # Authentication
  │   │   │   ├── home/        # Homepage
  │   │   │   ├── layout/      # Shared layouts
  │   │   │   ├── shared/      # Shared components
  │   │   │   └── shop/        # Shopping features
  │   │   └── environments/    # Environment configs
  │   └── ...
  └── ...
```

## 🔒 Security Features

- OAuth 2.0 authentication
- Role-based access control
- Secure payment processing
- XSS protection
- CSRF protection

## 🧪 Testing

Run the test suite:
```bash
pnpm test
```

Run e2e tests:
```bash
pnpm e2e
```

## 🚀 Deployment

1. Build the production bundle:
```bash
pnpm build
```

2. The build artifacts will be stored in the `dist/` directory

## 🔄 API Integration

The application connects to a backend API with the following main endpoints:

- `/products` - Product management
- `/categories` - Category management
- `/orders` - Order processing
- `/cart` - Cart operations

## 🎨 Styling Guide

- Uses Tailwind CSS for styling
- Responsive design for all screen sizes
- Dark/light mode support
- Consistent color scheme and typography

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## 👥 Authors

- Your Name - *Initial work* - [YourGithub](https://github.com/yourusername)

## 🙏 Acknowledgments

- Angular team for the amazing framework
- TanStack Query for efficient data fetching
- Tailwind CSS for the styling system
- Stripe for payment processing
- Kinde for authentication services
