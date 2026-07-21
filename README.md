# MES — Medical Equipment Supply Android App

A native Kotlin / Jetpack Compose client for the MES B2B marketplace.

## Getting Started

### 1. Environment Configuration
The app uses a `.env` file in the root directory to manage environment-specific variables.

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Adjust `API_BASE_URL` in `.env` if needed. For local development with a running Django backend on your host machine, use:
   ```
   API_BASE_URL=http://10.0.2.2:8000/api/v1/
   ```

### 2. Building the project
Run a Gradle sync in Android Studio. The `core-network` module will automatically read the `.env` file and generate a `BuildConfig.API_BASE_URL` constant.

## Architecture
The project follows Clean Architecture principles with a modular structure:
- **`app`**: The main entry point and navigation host.
- **`core-*`**: Shared modules for networking, database, design system, etc.
- **`feature-*`**: Domain-specific feature modules (Auth, Catalog, Cart, Merchant, etc.).

For detailed architecture and design decisions, see [KOTLIN.md](KOTLIN.md).

## API Integration
The app integrates with the [MES-API](https://github.com/cameltech/MES-API) (Django/DRF).
- **Authentication**: JWT-based (Access/Refresh tokens).
- **Response Envelope**: All responses are wrapped in a standard `{success, data, error, meta}` structure.
- **Payments**: Real mobile money push via Snippe.
- **Notifications**: In-app center + Firebase Cloud Messaging + SMS via SendAfrica.
