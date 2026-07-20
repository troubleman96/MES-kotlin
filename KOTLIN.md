# MES — Kotlin Android Architecture & Design Document
### Medical Equipment Supply — B2B Equipment Rental, Built as a Full E-Commerce Native App

**Status:** Implementation blueprint, adapted from the MES SRS (Academic Year 2025/2026)
**Deviation from SRS on record:** the SRS specifies React Native for the mobile client. This document
formally supersedes that with a **native Kotlin / Jetpack Compose** client, and expands "browse and
book equipment" into a **full shopping-cart e-commerce flow** — browse, cart, checkout, order, track,
notify — because that is the deliverable the assignment is being graded against. The backend stays
Django + DRF with domain-separated apps, per the SRS's own "Expected Outcomes" section, so nothing
in the original scope is lost — the mobile layer is what changes.

---

## 0. How This Maps Back to the SRS

| SRS says | This document does | Why |
|---|---|---|
| React Native, Android + iOS | Kotlin + Jetpack Compose, **Android only** | SRS's own "Expected Outcome" section already scopes the deliverable to Android; native gives us Material 3, proper background work (WorkManager), and a cleaner story for offline-first, which the CamelTech stack always leans on |
| "Browse and book equipment" | Browse → **Cart** → Checkout → Order → Contract → Delivery tracking | The rental flow is reframed as a commerce funnel. A "booking" *is* an order line; a rental period *is* the SKU's fulfillment window instead of a quantity |
| Africa's Talking directly for SMS/OTP | **SendAfrica** (`api.sendafrica.online`) as the SMS layer, server-side only | SendAfrica already wraps Africa's Talking, gives Django a clean JSON envelope, idempotency keys, delivery status, and credit accounting for free — no reason to hit AT raw when CamelTech owns the layer in front of it |
| Firebase FCM for push | Kept — FCM for push, SendAfrica for SMS, MES's own `notifications` domain fans out to both | Two channels, one domain owning the fan-out logic (see §7) |
| AzamPay/Selcom for payments | **Snippe** (`api.snippe.sh`) is the payment processor for checkout — mobile money (M-Pesa, Airtel Money, Mixx by Yas, Halotel), card, and dynamic QR, all in TZS | Snippe is a real, working payment API with a genuine USSD-push mobile-money flow and signed webhooks — it lets the checkout demo trigger an actual phone-authorized payment instead of a mocked "payment successful" screen, which matters when this has to be *shown*, not just described |
| Bilingual SW/EN | Kept, and pushed further — the welcome flow is now where language is chosen, not buried in settings | See §4.1 |

Everything else — domain-separated Django apps (`accounts`, `equipment`, `bookings`, `payments`,
`contracts`, `notifications`), PostgreSQL, Redis+Celery, MinIO/R2, Docker+Nginx — is unchanged from
the SRS and is treated here as a given the mobile client talks to.

### 0.1 Alignment with the "Employ Scripting Languages to Develop Shopping Carts" course material

The course notes define a shopping cart by six modules: Product Catalogue, Product Selection, Price
Calculator, **Customer Account** (profile, shipping address, billing address, purchase history),
Checkout System (delivery information, payment, order confirmation), and Database. This document maps
every one of those onto MES 1:1, which is the point — the app isn't just "an SRS demo," it's the
textbook shopping-cart model, fully built out:

| Course module | Where it lives in this doc |
|---|---|
| Product Catalogue | §4.3 Browse, §4.4 Product Detail |
| Product Selection (add/remove/save/update quantity) | §4.4 rental-period picker, §4.5 Cart |
| Price Calculator (subtotal/tax/discount/shipping/total) | §4.4 live total, §4.5 per-merchant subtotals, §4.6 checkout grand total |
| Customer Account (profile, shipping/billing address, purchase history) | §4.9 Profile & Address Book |
| Checkout System (delivery info, payment, confirmation) | §4.6 Checkout |
| Database (products, customers, orders, payments, inventory) | Django `equipment`/`accounts`/`bookings`/`payments` apps, PostgreSQL — unchanged from SRS |

The course's "E-Marketplace" topic (multi-seller, B2B type, seller/buyer portals) is also directly
represented — MES *is* a B2B e-marketplace per that topic's own definition, and §1's merchant/buyer
split plus §6.2's per-merchant sub-order splitting is exactly that model in practice.

---

## 1. Product Reframing: MES as E-Commerce

The SRS's domain objects map cleanly onto a commerce vocabulary. This mapping is the spine of the
whole app — every screen, ViewModel, and Django serializer should use this vocabulary consistently
so the "e-commerce" framing isn't cosmetic.

| SRS / rental term | Commerce term used in this doc | Notes |
|---|---|---|
| Equipment listing | **Product** | Has photos, specs, a daily/weekly rate, and a live availability calendar instead of stock count |
| Supplier | **Merchant** | Owns a storefront (their own listings, their own dashboard) |
| Healthcare facility | **Buyer** | The role that browses, carts, checks out |
| Rental request | **Cart line item** | `product_id`, `rental_start`, `rental_end`, `quantity` — quantity is usually 1 for large equipment, >1 for consumables/small items |
| Booking confirmation | **Order** | One order can contain line items from multiple merchants — splits into per-merchant **sub-orders** at checkout, same pattern as any multi-vendor marketplace |
| Rental agreement | **Order contract** | Auto-generated per sub-order, attached to the order detail screen |
| Delivery/pickup tracking | **Fulfillment status** | `dispatched → in_transit → delivered → returned` |
| Payment | **Checkout payment** | Processed via **Snippe**, charged per sub-order (each merchant is settled independently, since Snippe's `settlement` object on every payment already breaks out gross/fees/net) |

Two roles, two home experiences, one codebase — role is resolved at login and drives which
Compose navigation graph loads (see §5).

---

## 2. High-Level Architecture

Clean architecture, three layers, feature-modularized. This mirrors the Go domain/usecase/repository/
delivery convention CamelTech already uses on the backend — just renamed for Kotlin idiom:

```
presentation (Compose UI + ViewModel)
        ↓ calls
domain (UseCase + Repository interface + domain models — no Android deps, pure Kotlin)
        ↓ implemented by
data (Repository impl + Retrofit API + Room DAO + DTO↔domain mappers)
```

### 2.1 Module graph

```
MES/
├── app/                          # Application class, NavHost, DI graph root, splash
├── core/
│   ├── core-designsystem/        # Theme, typography, color tokens, shared Composables
│   ├── core-network/             # Retrofit client, envelope parsing, auth interceptor, idempotency
│   ├── core-database/            # Room database, shared DAOs (cart, cache)
│   ├── core-datastore/           # Proto DataStore — session, language pref, onboarding-seen flag
│   ├── core-domain/              # Shared domain primitives: Result<T>, Money, RentalPeriod
│   ├── core-notifications/       # FCM token registration, local notification channel setup
│   └── core-testing/             # Fakes, test doubles shared across feature modules
├── feature/
│   ├── feature-onboarding/       # Welcome pager, role selection, language selection
│   ├── feature-auth/             # Register, login, OTP, forgot password
│   ├── feature-catalog/          # Browse, search, filters, product detail
│   ├── feature-cart/             # Cart screen, per-merchant grouping, quantity/date editing
│   ├── feature-checkout/         # Address/delivery details, payment method, review, pay
│   ├── feature-orders/           # Order list, order detail, contract viewer/e-sign, fulfillment tracking
│   ├── feature-notifications/    # In-app notification center, unread badge
│   ├── feature-merchant/         # Merchant dashboard: listing CRUD, incoming orders, inventory
│   └── feature-profile/          # Profile, settings, language toggle, logout
└── build-logic/                  # Gradle convention plugins (shared compile config per module type)
```

Every `feature-*` module depends only on `core-*` modules, never on another `feature-*` module directly
— cross-feature navigation goes through `app`'s NavHost using route contracts defined in
`core-domain`. This keeps merchant-side and buyer-side flows independently buildable/testable, which
matters because the two roles will diverge more over time (merchant gets inventory management, buyer
never does).

### 2.2 Tech stack

| Concern | Choice | Why |
|---|---|---|
| UI | Jetpack Compose + Material 3 | SRS asks for a modern, premium interface — Compose is where Material 3's dynamic color, motion, and adaptive layout live |
| DI | Hilt | Standard, works cleanly with the module graph above |
| Async | Kotlin Coroutines + Flow | Matches Django's async posture; Flow is the natural fit for "cart changes → UI updates" and for polling unread notification counts |
| Networking | Retrofit + OkHttp + kotlinx.serialization | Django's response envelope (`{success, data, error, meta}`) is parsed once in `core-network` into a sealed `ApiResult<T>`, so every repository gets the same error shape |
| Local persistence | Room | Cart survives process death and works offline; also caches the last-fetched catalog page for offline browse |
| Preferences | Jetpack DataStore (proto) | Session tokens, selected language, "has seen onboarding" flag |
| Images | Coil | Compose-native, handles MinIO/R2-hosted equipment photos with placeholder/crossfade |
| Push | Firebase Cloud Messaging | Per SRS |
| Navigation | Compose Navigation with type-safe routes (`kotlinx.serialization`-backed) | Avoids stringly-typed route bugs across 9 feature modules |
| Animations | Compose animation APIs + Lottie-Compose for onboarding illustrations | "Premium" onboarding needs motion, not static screens |

---

## 3. Design System — "Premium" Made Concrete

"Premium" is a design-tokens problem, not a vibe. Concrete tokens below so the design is reproducible,
not left to whoever's implementing a given screen.

### 3.1 Color

Two palettes, one identity — light and dark are not just inverted, dark mode gets its own elevation
tints (Material 3 surface-tint approach) so cards don't look like grey rectangles at night.

```kotlin
// core-designsystem/theme/Color.kt
object MesColor {
    // Brand — clinical trust + Tanzanian warmth, deliberately not "generic health teal"
    val PrimaryTeal      = Color(0xFF0E7C7B)   // primary actions, brand mark
    val PrimaryTealDark  = Color(0xFF5FBFBE)   // primary in dark theme
    val AccentAmber      = Color(0xFFE8A33D)   // CTAs that need to pop: "Add to Cart", "Pay Now"
    val Success          = Color(0xFF2E9E5B)   // delivered, payment confirmed
    val Warning          = Color(0xFFDB8A22)   // return due soon
    val Danger           = Color(0xFFD64545)   // overdue, payment failed

    // Neutrals — warm greys, not blue-greys, to avoid the "generic SaaS" look
    val Ink900 = Color(0xFF1B1B1D)
    val Ink600 = Color(0xFF55555A)
    val Ink300 = Color(0xFFADADB2)
    val Surface0 = Color(0xFFFFFFFF)
    val Surface1 = Color(0xFFF7F6F4)   // warm off-white, not cold #FAFAFA
    val SurfaceDark0 = Color(0xFF121214)
    val SurfaceDark1 = Color(0xFF1C1C1F)
}
```

### 3.2 Typography

Two type families: a geometric sans for headings (confidence, "storefront" energy) and a humanist
sans for body/data (legibility for spec sheets, TZS amounts, dates).

```kotlin
val MesTypography = Typography(
    displayLarge = TextStyle(fontFamily = ClashDisplay, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = ClashDisplay, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = InterTight, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = InterTight, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    labelLarge = TextStyle(fontFamily = InterTight, fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.1.sp),
    // ... bodyMedium, bodySmall, labelMedium/Small for prices, timestamps, badges
)
```

`ClashDisplay` and `InterTight` bundled as variable fonts in `res/font/` — both are free/open-license,
no runtime download needed (matters for low-connectivity Tanzanian networks — nothing about the app's
core chrome should depend on a network call succeeding).

### 3.3 Elevation & shape language

- Corner radius scale: `4 / 8 / 16 / 24` — cards use 16, bottom sheets use 24 (top corners only),
  chips/badges use 8, input fields use 4 (deliberately less rounded than cards, for a "form" feel that
  reads as trustworthy for contract/payment screens).
- Shadows are soft and colored (a faint teal-tinted shadow on the primary CTA, not a generic black
  drop shadow) — this alone is most of what makes Material 3 apps stop looking like Material 3
  templates.
- One signature component: **the merchant trust card** — every product detail and cart line shows a
  small merchant chip (avatar-initial, name, star rating placeholder, "Verified Supplier" badge if
  `is_verified`) — this is the single component that most sells the "formalized B2B marketplace"
  positioning from the SRS problem statement.

### 3.4 Motion

- Screen transitions: shared-element transition on the product image between catalog grid → product
  detail → cart line (Compose `SharedTransitionLayout`) — this is the single highest-leverage motion
  choice for making browsing feel premium instead of flat.
- Cart badge: spring-animated count bump on add-to-cart, plus a brief flying-image-to-cart-icon
  animation on the catalog grid (skippable, respects `Animatable` durations under 400ms so it never
  feels like it's blocking the user).
- Loading state: skeleton screens (shimmer) everywhere data loads from network — never a bare
  `CircularProgressIndicator` centered on an empty screen, which is the fastest way to look like a
  student project instead of a product.

---

## 4. Screen-by-Screen Specification

### 4.1 Welcome / Onboarding (this is the "excellent premium design" ask — spelled out in full)

Five screens, `HorizontalPager`, swipeable + auto-timed CTA nudges. This sequence is the first thing
a grader sees, so it carries disproportionate weight — treat it as the demo reel.

**Screen 0 — Splash**
Brand mark (camel-motif logomark, per CamelTech's visual identity) center-scales in with a spring
animation over a `PrimaryTeal → PrimaryTealDark` diagonal gradient. Duration capped at 900ms even if
session-restore is still running underneath — never block on network here; splash always resolves to
either onboarding or home.

**Screen 1 — Language select**
Not buried in settings — it's the very first decision. Two large tappable cards side-by-side:
"Kiswahili" / "English", each with a small flag-free, text-based preview of how the next screen looks
in that language (avoids using national flags for a language choice, which conflates language with
nationality — Swahili is spoken well beyond Tanzania). Selection persists immediately to DataStore and
re-composes every subsequent onboarding screen in that language — no restart needed.

**Screen 2 — Value prop 1: "Discover equipment in minutes, not days"**
Full-bleed Lottie illustration (isometric style: a phone with a catalog grid floating above it,
equipment icons — ventilator, hospital bed, infusion pump — orbiting in). Headline in `displayLarge`,
one line of supporting copy in `bodyLarge`, `Ink600`. Progress dots at the bottom, not a "skip" link
buried top-right — instead a persistent "Ruka" / "Skip" text button top-right, low-emphasis, so it's
available without competing with the illustration.

**Screen 3 — Value prop 2: "Pay the way you already do — M-Pesa, Tigo Pesa, Airtel Money"**
Illustration shows a stylized phone-to-phone mobile money transfer. This screen exists specifically to
pre-empt the buyer's first objection ("do I need a card?") before they ever hit checkout — answering it
in onboarding measurably reduces checkout abandonment in mobile-money markets.

**Screen 4 — Value prop 3: "Every rental, a real contract"**
Illustration: a document with a signature flourish animating in. Directly sells the SRS's "formalizing
informal procurement" pitch back to the user in one screen.

**Screen 5 — Role selection (terminal screen, not a modal later)**
Two large cards: **"I need equipment"** (Buyer) vs **"I supply equipment"** (Merchant), each with a
2-line description and a distinct icon. This is a first-class onboarding step, not a settings toggle,
because the entire nav graph forks here (§5). Tapping a card triggers a shared-element transition
straight into that role's auth flow — no separate "Continue" button needed, the card tap *is* the CTA.

```kotlin
// feature-onboarding/OnboardingPager.kt
@Composable
fun OnboardingRoute(onFinished: (UserRole) -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val pagerState = rememberPagerState(pageCount = { 6 })
    val language by viewModel.language.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, userScrollEnabled = true) { page ->
            when (page) {
                0 -> SplashPage()
                1 -> LanguageSelectPage(current = language, onSelect = viewModel::setLanguage)
                2 -> ValuePropPage(OnboardingCopy.discover(language))
                3 -> ValuePropPage(OnboardingCopy.payments(language))
                4 -> ValuePropPage(OnboardingCopy.contracts(language))
                5 -> RoleSelectPage(onRoleChosen = { role ->
                    viewModel.markOnboardingComplete(role)
                    onFinished(role)
                })
            }
        }
        if (pagerState.currentPage in 2..4) {
            OnboardingProgressDots(
                pageCount = 3,
                currentPage = pagerState.currentPage - 2,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
            )
        }
    }
}
```

Onboarding is shown exactly once (`DataStore` flag `has_seen_onboarding`), but language and role
remain editable later from Profile → Settings.

### 4.2 Auth

Register / Login / OTP verify / Forgot password — one flow, role-aware (the `role` chosen in
onboarding is passed through and submitted as part of registration so Django's `accounts` app can set
up the right profile type immediately). Phone-first: email is collected too (Django's `accounts`
domain in the SRS is email+password), but the hero field on the register screen is phone number, with
inline "we'll text you a code to confirm this" microcopy — sets expectation for the OTP step and
foreshadows SendAfrica's role.

Fields, validation, and copy are bilingual from the language chosen in onboarding — every string in
`feature-auth` is resolved through `strings.xml` / `strings-sw.xml`, never hardcoded.

### 4.3 Home / Browse (Buyer)

- Top app bar: location-agnostic (this is B2B, not consumer-local — no "near me" pretense), search
  bar prominent, cart icon with animated badge, notification bell with unread-count badge.
- Horizontal category rail: Diagnostic, Rehabilitation, Life Support, Mobility, Sterilization,
  Monitoring — pulled from `equipment.category`.
- Featured/sponsored row: surfaces `is_featured` listings (this is literally the SRS's "Featured
  Listings" revenue stream — the UI needs a slot for it to exist as a product, not just a backend flag).
- Main grid: 2-column product cards — image, product name, merchant name (small, muted), rate per
  day in TZS, availability chip (`Available now` / `Available from 12 Aug`).
- Pull-to-refresh, infinite scroll (paged through Django's `page`/`per_page` pattern), skeleton grid on
  first load, cached grid shown instantly on subsequent opens (Room-backed) while a background refresh
  runs — this is the offline-first reflex CamelTech applies everywhere, applied here too.

### 4.4 Product Detail

- Full-bleed image carousel (shared element from the grid thumbnail).
- Merchant trust card (§3.3) directly under the title — tapping it opens a lightweight merchant profile
  sheet (other listings from this merchant, join date).
- Spec table — collapsible, technical specs from the listing (model, manufacturer, power requirements,
  included accessories).
- **Rental period picker** — this replaces a simple "quantity" stepper. A date-range picker
  (`start_date` / `end_date`) plus a live-computed total: `daily_rate × days`, shown before add-to-cart
  so there's no surprise at checkout. Unavailable date ranges (from the listing's availability
  calendar) are greyed out and unselectable, not just validated after the fact.
- Sticky bottom bar: computed total + **"Add to Cart"** (`AccentAmber`, full width on phones). No
  separate "Buy Now" — MES's checkout is short enough that a bypass button isn't worth the complexity
  it'd add to the cart-merge logic.

### 4.5 Cart

This is the screen the assignment is explicitly graded on, so it gets the most structural detail.

- Grouped by merchant, not a flat list — each merchant section has its own subtotal and its own
  delivery-note field, because sub-orders fulfill independently (§6).
- Each line: thumbnail, product name, rental period (editable inline via a bottom sheet — reopens the
  same date-range picker from product detail, pre-filled), per-day rate × days = line total, remove
  action (swipe-to-delete with undo snackbar, not an instant destructive delete).
- Empty state: not a generic "your cart is empty" — an illustration + a "Browse equipment" CTA that
  routes straight back to catalog, and (if the buyer has order history) a "Reorder from your last
  supplier" shortcut.
- Bottom summary card: subtotal, "Delivery arranged directly with merchant" note (SRS explicitly
  excludes physical logistics from platform scope — the UI needs to say this plainly so it doesn't
  imply MES handles trucking), grand total, **"Proceed to Checkout"**.
- Cart state lives in Room (`CartDao`) so it survives app kill, and is synced to a lightweight Django
  cart endpoint on every mutation (debounced 500ms) so a buyer's cart follows them across devices —
  this is the one piece of state the SRS didn't originally have and that this document adds, because a
  cart that doesn't survive a device switch isn't really e-commerce.

### 4.6 Checkout

Three-step wizard (Compose `Scaffold` + a top `LinearProgressIndicator` as the step tracker, not a
full-screen stepper — keeps momentum, doesn't feel like a form marathon):

1. **Review & delivery address** — per-merchant sub-order review, then a proper address step, not a
   single free-text field:
   - If the buyer has saved addresses (facility addresses, since this is B2B — a hospital may have a
     main receiving address and a separate pharmacy/stores address), they're shown as selectable cards:
     label, facility name, address lines, contact phone, with the default marked. Buyer taps one to use
     it for this order — no retyping on repeat orders, which matters for a facility that orders
     equipment regularly.
   - **"Add new address" / "Edit"** opens the same address form inline (bottom sheet, not a separate
     screen — keeps checkout momentum): facility name, address line 1/2, ward/district, city, delivery
     contact name + phone, notes (e.g. "deliver to loading dock, ask for stores officer"). Saving here
     does two things at once — applies it to *this* checkout **and** writes it back to the buyer's
     Customer Account (`GET/POST/PUT/DELETE /api/v1/addresses`) so it's available as a saved option on
     the next order. This mirrors the "Customer Account → shipping address / billing address" module
     from the course's shopping-cart component breakdown — the address book is account-level state, not
     a one-off checkout field.
   - Billing address defaults to "same as delivery," with an explicit toggle to use a separate billing
     address (relevant for facilities where procurement/finance sits at a different office than the
     receiving ward) — sets `billing_address_id` separately from `delivery_address_id` on the order.
   - Editing the address **at checkout, after selecting it**, updates that saved address for future
     orders too (not a throwaway edit) — a confirmation snackbar ("Saved to your addresses") makes that
     persistence visible rather than silent.
   - Special instructions field, per sub-order, separate from the address (equipment-specific handling
     notes shouldn't overwrite the address's own delivery notes).
2. **Payment method** — mobile money via **Snippe**, buyer's verified phone pre-filled (same trust
   logic as everywhere else in this doc: never let someone type an arbitrary number into a payment
   field). Network is auto-detected from the phone prefix (M-Pesa / Airtel Money / Mixx by Yas /
   Halotel) and shown as a small badge next to the number so the buyer can confirm it's right before
   the push fires. Tapping **"Pay {total} TZS"** does the following, in order — this is a real push, not
   a mocked confirmation screen:
   1. Django creates the sub-order in `pending_payment` and calls `POST /v1/payments` on Snippe
      (`payment_type: "mobile"`, the sub-order total, the buyer's verified phone, a `webhook_url`
      pointing at Django's own signed webhook endpoint, and `metadata.order_id` set to the sub-order
      ID) — server-side only, Snippe's API key never touches the Kotlin app.
   2. Django returns Snippe's `reference` to the client immediately; the screen switches to a
      **"Check your phone"** state — a full-screen animated illustration of a phone with a lock-screen
      USSD prompt, plus the merchant name and amount, so the buyer isn't left staring at a spinner not
      knowing what they're waiting for.
   3. The customer's phone genuinely receives the USSD push from their network (Snippe's mobile-money
      rails, not a simulation) and they enter their PIN on their own device to authorize — this is the
      literal "real money top-up push" the flow is built around.
   4. Snippe POSTs a signed `payment.completed` (or `payment.failed`/`payment.expired`) webhook to
      Django (§8). Django flips the sub-order to `paid` (or `payment_failed`) and pushes the result to
      the client over the same channel §7 uses for other order events.
   5. The Kotlin client is **not** left polling blind — it observes a `Flow` backed by a short-interval
      `GET /v1/payments/{reference}` poll (every 3s, capped at 2 minutes) as a fallback in case the
      webhook round-trip to the client is slow, but the primary signal is the server-pushed event.
      Whichever arrives first wins; the poll is redundancy, not the main path.
   6. On success: haptic tap, a checkmark animation, then auto-advance to the contract step. On failure
      or the 4-hour Snippe expiry window: a clear retry button that creates a fresh payment intent
      (Snippe payments aren't reusable past `pending` — a new `reference` is created, not resent).
3. **Contract review** — the auto-generated rental agreement PDF preview (rendered inline via a PDF
   Compose view, not a forced external-app handoff) per sub-order, with an e-signature pad
   (`androidx.ink` or a simple Canvas-based signature capture) before final "Confirm Order".

### 4.7 Orders (post-purchase — "the user should be able to browse... everything")

- **Order list**: tabs — Active, Awaiting Return, Completed, Cancelled. Each row: merchant, product
  thumbnail(s), date range, status chip (color-coded via `MesColor.Success/Warning/Danger`).
- **Order detail**: fulfillment timeline (stepper: Confirmed → Dispatched → Delivered → In Use →
  Return Due → Returned), the signed contract (downloadable), payment receipt, a "Contact merchant"
  action, and — critically — the **return countdown**: a prominent card showing days remaining before
  the rental period ends, because the SRS's whole "reduce missed returns" objective lives or dies on
  this being visible, not buried.

### 4.8 Notifications (in-app center)

Bell icon → full-screen list, grouped by day (Today / Yesterday / Earlier). Each item: icon by type
(order confirmed, payment received, return due tomorrow, merchant message), title, body, relative
timestamp, unread = filled dot + slightly tinted background. Swipe or tap-through marks read. Pull to
refresh calls unread-count + list. See §7 for exactly how this is populated — it is **not** a direct
mirror of SendAfrica's own dashboard notifications; it's MES's own notification domain.

### 4.9 Profile & Customer Account — Address Book

This screen was implicit before; the checkout redesign above makes it load-bearing, so it gets its own
spec. Matches the course notes' "Customer Account" module (profile, shipping address, billing address,
purchase history) directly — this is the account-level home for everything checkout reads from.

- **Profile header**: buyer/facility name, email/phone verification badges (reusing the same
  verified-phone trust pattern from §4.6), role.
- **My Addresses**: the address book itself — list of saved addresses as cards (same component used in
  the checkout address picker, so it only needs to be built once), each with label, default toggle,
  edit, delete. "Add address" opens the identical bottom-sheet form used at checkout. This list is the
  single source of truth — checkout reads from it, checkout edits write back to it, nothing forks.
- **Order history**: shortcut into the Orders tab (§4.7), filtered view.
- **Payment methods**: verified mobile money number (the one Snippe's USSD push is sent to), with a
  "change number" flow that re-triggers phone OTP verification before it takes effect — never let a
  payment number change silently.
- **Language & role**: the two onboarding choices, editable here post-onboarding.

```kotlin
// core-database/entity/AddressEntity.kt
@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val label: String,               // "Main receiving", "Pharmacy stores"
    val facilityName: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val ward: String? = null,
    val district: String? = null,
    val city: String,
    val contactName: String,
    val contactPhone: String,
    val deliveryNotes: String? = null,
    val isDefault: Boolean = false,
    val addressType: AddressType = AddressType.DELIVERY   // DELIVERY / BILLING / BOTH
)
```

`AddressRepository` follows the exact same local-first-then-sync pattern as the cart (§6.1): write to
Room immediately, debounce-sync to Django's `api/v1/addresses`. Checkout's address picker and the
Profile address book both observe the same Room `Flow<List<AddressEntity>>`, so editing an address in
one place is instantly reflected in the other with no extra plumbing.

### 4.10 Merchant side (parallel app experience, same codebase)

- **Merchant dashboard home**: today's incoming rental requests, low-availability alerts, quick stats
  (active rentals, pending returns, this month's revenue).
- **Listing management**: CRUD for products — photo upload (multi-image, MinIO/R2-backed), spec form,
  daily rate, availability blackout dates.
- **Incoming orders**: accept/reject rental requests, mark dispatched/delivered, initiate return
  confirmation.
- Bottom nav differs entirely from buyer's (Dashboard / Listings / Orders / Notifications / Profile) —
  this is why role forks the nav graph at the root instead of hiding merchant screens behind buyer
  screens with visibility flags.

---

## 5. Navigation

Root-level fork by role, each role gets its own bottom-nav-driven graph:

```kotlin
@Serializable sealed interface MesRoute {
    @Serializable data object Onboarding : MesRoute
    @Serializable data object Auth : MesRoute
    @Serializable data object BuyerHome : MesRoute      // hosts its own nested NavHost + bottom bar
    @Serializable data object MerchantHome : MesRoute   // ditto
}

// Buyer's nested graph
@Serializable sealed interface BuyerRoute {
    @Serializable data object Catalog : BuyerRoute
    @Serializable data class ProductDetail(val productId: String) : BuyerRoute
    @Serializable data object Cart : BuyerRoute
    @Serializable data object Checkout : BuyerRoute
    @Serializable data object Orders : BuyerRoute
    @Serializable data class OrderDetail(val orderId: String) : BuyerRoute
    @Serializable data object Notifications : BuyerRoute
    @Serializable data object Profile : BuyerRoute
    @Serializable data object AddressBook : BuyerRoute
}
```

Buyer bottom nav: **Browse · Cart (badge) · Orders · Notifications (badge) · Profile** — five items,
each mapping directly to the SRS's core objective list, which is a deliberate choice: the nav bar
itself is a checklist a grader can visually match against the SRS's specific objectives.

---

## 6. Data Layer — Cart & Order Contracts

### 6.1 Cart (client-authoritative, server-synced)

```kotlin
// core-database/entity/CartLineEntity.kt
@Entity(tableName = "cart_lines")
data class CartLineEntity(
    @PrimaryKey val id: String,             // client-generated UUID, stable across syncs
    val productId: String,
    val merchantId: String,
    val merchantName: String,
    val productName: String,
    val thumbnailUrl: String,
    val dailyRateTzs: Long,
    val rentalStart: LocalDate,
    val rentalEnd: LocalDate,
    val quantity: Int = 1,
    val addedAt: Instant,
    val syncState: SyncState = SyncState.PENDING  // PENDING / SYNCED / FAILED
)
```

```kotlin
// domain/usecase/AddToCartUseCase.kt
class AddToCartUseCase(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(
        product: Product,
        rentalPeriod: RentalPeriod
    ): Result<Unit> {
        val line = CartLine(
            id = Uuid.random().toString(),
            productId = product.id,
            merchantId = product.merchantId,
            merchantName = product.merchantName,
            productName = product.name,
            thumbnailUrl = product.primaryImageUrl,
            dailyRate = product.dailyRate,
            rentalPeriod = rentalPeriod,
            quantity = 1
        )
        return cartRepository.addLine(line)   // writes Room immediately, enqueues debounced sync
    }
}
```

`CartRepository.addLine` writes to Room synchronously (so the UI updates instantly, offline included),
then enqueues a `WorkManager` one-off job with a 500ms debounce tag that PATCHes the full cart to
Django's cart endpoint — using the same `Idempotency-Key` pattern SendAfrica's SMS API uses, keyed on
`cart_id + last_mutation_id`, so a retried sync after a dropped connection never double-applies.

### 6.2 Checkout → sub-orders

At "Confirm Order", the cart is grouped by `merchantId` client-side for display, but the **actual
split into sub-orders happens server-side** in Django's `bookings` app — the client sends one checkout
payload (`cart_id`, `delivery_details`, `payment_method`), and Django returns an `order_group_id` plus
an array of `sub_orders`, each with its own `contract_id`, `payment_status`, and `fulfillment_status`.
The client never computes the split itself — this keeps pricing/commission logic (the SRS's "Commission
per Rental" and "Contract Processing Fee" revenue streams) entirely server-side, where it belongs.

---

## 7. Notifications — SendAfrica Integration, Precisely Scoped

This is the piece most likely to be built wrong, so it's worth being explicit about the boundary.

**What SendAfrica is, in this context:** MES's Django backend holds **one SendAfrica account** (created
under CamelTech, an `X-API-Key` stored server-side in Django's settings/secrets — never in the Kotlin
app, never in any client-visible config). This is exactly the "developer / integration" auth mode
SendAfrica's own docs describe — MES is a SendAfrica *customer*, the same way any other business would
be.

**What SendAfrica is not, in this context:** the SendAfrica account's own dashboard notifications
(`GET /v1/notifications`, low-balance alerts, payment-confirmed toasts) belong to *CamelTech's*
SendAfrica account — they're about MES's own SMS credit balance, not about a buyer's rental order.
Those two notification systems must never be conflated in the UI. MES buyers and merchants never see
SendAfrica's dashboard; they see MES's own notification center, which MES's own Django `notifications`
app owns end to end.

### 7.1 Flow

```
Domain event in Django (e.g. bookings.services.confirm_order)
        │
        ▼
notifications.services.notify(event, recipient, context)
        │
        ├──▶ writes a row to MES's own `notifications` table  ──▶  surfaced via
        │                                                          GET /api/v1/notifications
        │                                                          (MES's own endpoint, buyer/merchant JWT)
        │
        ├──▶ Firebase FCM push, if recipient has a registered device token
        │
        └──▶ SMS via SendAfrica, only for the subset of events that warrant it
             (order confirmed, payment received, return due tomorrow, return overdue)
             POST https://api.sendafrica.online/v1/sms/
             Headers: X-API-Key: <CamelTech's server-side key>
             Idempotency-Key: "order-{order_id}-{event_type}"   ← prevents double-SMS on retry
             Body: { "to": "<recipient phone, E.164>", "message": "<templated, bilingual>" }
```

```python
# Django — notifications/services.py (sketch, not full implementation)
def notify_order_confirmed(order: Order) -> None:
    Notification.objects.create(
        account=order.buyer,
        type="order_confirmed",
        title=_("Order confirmed"),
        body=_("Your rental of {product} is confirmed.").format(product=order.product_name),
    )
    fcm.send_to_account(order.buyer, title=..., body=...)
    sendafrica_client.send_sms(
        to=order.buyer.phone,
        message=render_sms_template("order_confirmed", order, lang=order.buyer.preferred_language),
        idempotency_key=f"order-{order.id}-confirmed",
    )
```

### 7.2 Why route SMS through SendAfrica instead of calling Africa's Talking directly (as the original SRS specifies)

- One less credential to manage in Django's secrets (Africa's Talking creds live inside SendAfrica,
  not duplicated in MES).
- Idempotency keys and automatic credit refund-on-gateway-rejection are already solved by SendAfrica —
  re-implementing that logic against raw AT would be pure duplicated effort.
- Delivery status (`sent/delivered/failed`) is queryable via SendAfrica's message logs if MES ever
  wants a "was the SMS actually delivered" indicator on the order timeline.
- It's dogfooding CamelTech's own revenue-generating product from another CamelTech product — every
  SMS MES sends is billed through SendAfrica's own credit system, which is a clean internal accounting
  story if these ever need to be billed against each other.

### 7.3 Kotlin side — what the client actually does

The Kotlin app **never talks to SendAfrica directly** — no SendAfrica SDK, no API key, nothing. The
client only ever calls MES's own Django endpoints:

```kotlin
// feature-notifications/data/NotificationApi.kt
interface NotificationApi {
    @GET("api/v1/notifications")
    suspend fun list(@Query("page") page: Int, @Query("unread") unreadOnly: Boolean?): Envelope<NotificationPage>

    @GET("api/v1/notifications/unread-count")
    suspend fun unreadCount(): Envelope<UnreadCount>

    @PATCH("api/v1/notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): Envelope<MessageResponse>
}
```

Note this is deliberately shaped **the same way** as SendAfrica's own `/v1/notifications` surface
(`unread-count`, `mark-one-read`, `mark-all-read`, `type` enum) — not because the client talks to it,
but because it's a proven, already-designed shape, and reusing it means less API-contract debate
during the Django implementation phase.

---

## 8. Payments — Snippe Integration

Same discipline as §7's SendAfrica boundary: **the Kotlin app never talks to Snippe directly.** No
Snippe SDK, no API key, nothing payment-credential-shaped ships in the APK. Django holds one Snippe
account (API key in server-side secrets), is the only thing that calls `api.snippe.sh`, and the client
only ever calls MES's own `/api/v1/payments/*` endpoints — which proxy the parts of Snippe's shape the
client actually needs (a reference to poll, a status to render) without ever exposing the key.

### 8.1 Why Snippe, and why it's simulate-able with real money movement

Snippe's mobile-money payment type is a genuine USSD push against Airtel Money, M-Pesa, Mixx by Yas,
and Halotel — `POST /v1/payments` with `payment_type: "mobile"` puts a real prompt on the payer's
phone. For a graded demo this is the difference between "trust me, payment works" and actually pulling
out a phone, entering a PIN, and watching TZS move — while still being safe to rehearse repeatedly,
since Snippe's minimum amount is TZS 500 and sandbox/test-mode top-ups are cheap to run over and over
during UAT.

### 8.2 Create payment (Django → Snippe)

```python
# Django — payments/services.py (sketch)
def create_snippe_payment(sub_order: SubOrder) -> SnippePayment:
    idempotency_key = f"so-{sub_order.id}"[:30]   # Snippe caps Idempotency-Key at 30 chars
    response = snippe_client.post(
        "/v1/payments",
        headers={"Idempotency-Key": idempotency_key},
        json={
            "payment_type": "mobile",
            "details": {"amount": sub_order.total_tzs, "currency": "TZS"},
            "phone_number": sub_order.buyer.verified_phone_e164.lstrip("+"),
            "customer": {
                "firstname": sub_order.buyer.first_name,
                "lastname": sub_order.buyer.last_name,
                "email": sub_order.buyer.email,
            },
            "webhook_url": settings.SNIPPE_WEBHOOK_URL,   # https://api.mes.co.tz/webhooks/snippe
            "metadata": {"order_id": str(sub_order.id)},
        },
    )
    data = response.json()["data"]
    sub_order.payment_reference = data["reference"]        # e.g. "9015c155-9e29-4e8e-8fe6-d5d81553c8e6"
    sub_order.payment_status = "pending"
    sub_order.payment_expires_at = data["expires_at"]       # 4-hour Snippe expiry
    sub_order.save()
    return sub_order
```

### 8.3 Webhook — signature verification is not optional

Snippe signs every webhook with HMAC-SHA256 over `{timestamp}.{raw_body}`, using a per-account signing
key from **Settings → Webhook Secret**. Django must verify this on every request, using the *raw* body
— not a re-serialized `json.dumps(json.loads(body))`, which can reorder keys and silently break the
signature:

```python
# Django — payments/webhooks.py (sketch)
@csrf_exempt
def snippe_webhook(request):
    raw_body = request.body  # bytes, exactly as received — never re-parse-then-reserialize before this
    timestamp = request.headers.get("X-Webhook-Timestamp", "")
    signature = request.headers.get("X-Webhook-Signature", "")

    if abs(int(time.time()) - int(timestamp)) > 300:
        return HttpResponseBadRequest("stale webhook")   # reject anything older than 5 minutes

    expected = hmac.new(
        settings.SNIPPE_WEBHOOK_SECRET.encode(), f"{timestamp}.{raw_body.decode()}".encode(), hashlib.sha256
    ).hexdigest()
    if not hmac.compare_digest(expected, signature):
        return HttpResponseBadRequest("invalid signature")

    event = json.loads(raw_body)
    event_id = event["id"]
    if WebhookEvent.objects.filter(provider="snippe", event_id=event_id).exists():
        return HttpResponse("OK")  # already processed — Snippe may deliver the same event twice

    WebhookEvent.objects.create(provider="snippe", event_id=event_id)

    match event["type"]:
        case "payment.completed":
            handle_payment_completed(event["data"])   # marks sub-order paid, triggers §7 notifications
        case "payment.failed" | "payment.expired":
            handle_payment_failed(event["data"])

    return HttpResponse("OK")   # 2xx within 30s — process the rest async if it's ever heavier than this
```

`handle_payment_completed` is exactly where §7's `notify_order_confirmed`-style fan-out gets triggered
— payment success is the event that turns a `pending_payment` sub-order into a confirmed order, writes
the in-app notification row, sends the FCM push, and fires the SendAfrica SMS. One webhook, one
source of truth, no client-side "mark as paid" path exists anywhere — a paid order is only ever paid
because Snippe said so.

### 8.4 Client-facing endpoints (what the Kotlin app actually calls)

```kotlin
// feature-checkout/data/PaymentApi.kt
interface PaymentApi {
    @POST("api/v1/orders/{subOrderId}/pay")
    suspend fun initiatePayment(@Path("subOrderId") id: String): Envelope<PaymentIntentResponse>

    @GET("api/v1/orders/{subOrderId}/payment-status")
    suspend fun paymentStatus(@Path("subOrderId") id: String): Envelope<PaymentStatusResponse>
}

@Serializable
data class PaymentIntentResponse(
    val reference: String,
    val status: String,          // pending / completed / failed / expired
    val expiresAt: Instant,
    val network: String?         // "mpesa" / "airtel" / "mixx" / "halotel" — for the confirmation badge
)
```

```kotlin
// feature-checkout/presentation/PaymentViewModel.kt — poll-as-fallback pattern from §4.6 step 2.5
fun observePaymentStatus(subOrderId: String): Flow<PaymentUiState> = flow {
    val deadline = Clock.System.now() + 2.minutes
    while (Clock.System.now() < deadline) {
        val result = paymentRepository.checkStatus(subOrderId)
        when (result) {
            is ApiResult.Success -> {
                emit(result.data.toUiState())
                if (result.data.status != "pending") return@flow   // terminal state reached
            }
            else -> { /* transient network error mid-poll — keep trying, don't fail the whole flow */ }
        }
        delay(3.seconds)
    }
    emit(PaymentUiState.TimedOut)   // client-side timeout ≠ Snippe's 4h expiry; this just stops polling
}.combine(serverPushedPaymentEvents(subOrderId)) { polled, pushed -> pushed ?: polled }
```

### 8.5 Failure modes worth designing for explicitly

- **`payment.expired`** (4-hour Snippe window) — realistically never hit in a live demo, but the retry
  button (§4.6 step 6) has to exist for grading credibility: "what happens if they don't confirm" is a
  fair question to expect.
- **`payment.failed`** with `failure_reason` ("Transaction declined by user", insufficient mobile-money
  balance, etc.) — surfaced verbatim-but-localized under the retry button, not swallowed into a generic
  "payment failed" message; Snippe already gives a real reason, no need to throw it away.
- **Duplicate webhook delivery** — handled by the `WebhookEvent` idempotency table in §8.3; Snippe's own
  docs are explicit that the same event may arrive more than once.
- **Idempotent payment creation** — if a buyer double-taps "Pay", the `Idempotency-Key` (`so-{sub_order_id}`,
  kept under Snippe's 30-character cap) means the second `POST /v1/payments` call returns the *same*
  cached payment instead of creating a second USSD push to the buyer's phone.

---

## 9. Networking & Error Handling

Django's response envelope is parsed once, centrally:

```kotlin
// core-network/Envelope.kt
@Serializable
data class Envelope<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
    val meta: PageMeta? = null
)

@Serializable
data class ApiError(val code: String, val message: String)

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val code: String, val message: String) : ApiResult<Nothing>
    data object NetworkError : ApiResult<Nothing>
}

suspend fun <T> safeApiCall(block: suspend () -> Envelope<T>): ApiResult<T> = try {
    val envelope = block()
    if (envelope.success && envelope.data != null) ApiResult.Success(envelope.data)
    else ApiResult.Failure(envelope.error?.code ?: "unknown", envelope.error?.message ?: "Unknown error")
} catch (e: IOException) {
    ApiResult.NetworkError
} catch (e: HttpException) {
    // parse Django's error envelope out of the error body even on non-2xx
    ApiResult.Failure(code = parseErrorCode(e), message = parseErrorMessage(e))
}
```

Every repository function returns `ApiResult<T>`, every ViewModel exposes a `UiState` sealed class
built from it — no raw exceptions ever reach a Composable. Error codes (`insufficient_credits`,
`invalid_phone`, etc. — same taxonomy SendAfrica itself uses backend-side) map to localized,
user-facing copy in a single `ErrorMessageMapper`, not scattered `when` blocks per screen.

---

## 10. Phased Build Plan

Following the same phased-with-test-gates methodology used on Camel Accounts: a `context/` folder
(`agents.md`, `architecture.md`, `glossary.md`, `conventions.md`, `api-contracts.md`, `decisions.md`,
`testing.md`, `ui-design.md`) as the canonical reference for whoever (human or agent) builds each
phase, git commit+push gated on that phase's tests passing.

| Phase | Scope | Acceptance gate |
|---|---|---|
| 0 | Inventory/analysis pass over this doc + Django SRS scope → populate `context/` folder | `context/` folder complete and reviewed |
| 1 | `core-*` modules: design system, network envelope parsing, Room schema, DataStore session | Unit tests on envelope parsing + Room DAOs green |
| 2 | Onboarding + Auth (register/login/OTP/role select) | Manual QA: full onboarding→login round trip, bilingual toggle verified on every screen |
| 3 | Catalog browse + product detail + rental period picker | Screenshot tests on product grid + detail; offline-cache-then-refresh verified |
| 4 | Cart (local + synced) | Instrumented test: add/edit/remove survives process death; sync retried after simulated network drop |
| 5 | Checkout (address → payment via Snippe → contract e-sign) | End-to-end test against Django staging with a **real Snippe test payment**: cart → USSD push actually received on a test phone → PIN entered → webhook verified and processed → confirmed order with signed contract. Not mocked — this phase isn't done until a real TZS 500 test payment completes the loop. |
| 6 | Orders list/detail + fulfillment timeline | Verify return-countdown renders correctly across all fulfillment states |
| 7 | Notifications (FCM + in-app center + SendAfrica SMS trigger, Django-side) | Trigger each event type in staging, verify push + in-app row + SMS all fire exactly once (idempotency key check) |
| 8 | Merchant side: dashboard, listing CRUD, incoming orders | Full merchant round trip: create listing → buyer books it → merchant marks dispatched |
| 9 | Polish: dark mode, motion pass, empty/error states, accessibility (TalkBack pass) | Manual design QA against §3 tokens |
| 10 | UAT with ≥2 clinics + 1 supplier (per SRS) | Signed-off UAT feedback incorporated |

---

## 11. What Stays Explicitly Out of Scope

Carried over unchanged from the SRS, restated here so the Kotlin build doesn't quietly grow beyond it:
no physical logistics/dispatch execution (only status *display*), no equipment maintenance/repair
workflows, no DHIS2 or other government health-system integration, no patient-facing clinical
features. The app is the commercial/administrative layer only — browsing, cart, ordering, contracts,
payment, and notification about all of the above.
