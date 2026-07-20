# MES - Medical Equipment Supply
## Project Context

### Overview
MES is a B2B Medical Equipment Rental Marketplace built as a native Kotlin Android app with Jetpack Compose. The app connects healthcare facilities (buyers) with medical equipment suppliers (merchants) for equipment rental.

### Architecture
- **Presentation**: Jetpack Compose + Material 3 + Hilt ViewModel
- **Domain**: Pure Kotlin models and use cases
- **Data**: Retrofit (network), Room (local), DataStore (preferences)

### Tech Stack
- Kotlin 2.1.0, Jetpack Compose, Material 3
- Hilt (DI), Coroutines + Flow (async)
- Retrofit + OkHttp (networking)
- Room (local database)
- DataStore (preferences)
- Coil (images)

### Key Features
1. **Onboarding**: Language selection (EN/SW), role selection (Buyer/Merchant)
2. **Auth**: Register/Login with OTP verification
3. **Catalog**: Browse equipment, search, filter by category
4. **Product Detail**: Image carousel, specs, rental period picker
5. **Cart**: Grouped by merchant, quantity editing, totals
6. **Checkout**: Address selection, payment (Snippe USSD), confirmation
7. **Orders**: List with tabs, detail with fulfillment timeline
8. **Notifications**: In-app notification center
9. **Profile**: Address book, settings, logout
10. **Merchant Dashboard**: Stats, incoming orders, listing management

### Color System
- Primary Teal: #0E7C7B (brand, trust)
- Accent Amber: #E8A33D (CTAs)
- Success: #2E9E5B (confirmed, delivered)
- Warning: #DB8A22 (return due)
- Danger: #D64545 (overdue, failed)

### Design Principles
- Premium feel with warm greys, not blue-greys
- Merchant trust cards on every product
- Skeleton loading screens, never bare spinners
- Bilingual (English/Swahili) throughout
