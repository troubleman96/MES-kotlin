# Conventions

## Code Style
- Kotlin idiomatic, no unnecessary comments
- Compose functions: PascalCase, prefixed with feature name
- ViewModels: `*ViewModel` suffix
- Screens: `*Screen` suffix
- Data classes for UI state: `*UiState`

## Module Structure
- `core-*` modules: shared, no feature dependencies
- `feature-*` modules: depend only on `core-*`, never on other features
- Navigation: defined in `app/MesNavHost.kt` using string routes

## Naming
- Package: `com.mes.{module}`
- Database entities: `*Entity` suffix
- API interfaces: `*Api` suffix
- Repository interfaces in domain, implementations in data

## Git
- Phase-based commits: "Phase N: Description"
- Push after each phase's tests pass
