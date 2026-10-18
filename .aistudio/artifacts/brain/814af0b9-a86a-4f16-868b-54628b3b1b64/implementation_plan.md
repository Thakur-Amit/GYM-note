# Home Screen Overhaul & Navigation Fix

A comprehensive plan to fix the Home tab opening bug, scale up card sizes and typography to be bold and prominent, introduce high-impact top hero workout artwork matching the UI, and eliminate all dummy data in favor of real reactive database state.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed based on your preferences:
> - **Fitness Hero Artwork**: A vibrant, high-contrast fitness hero banner positioned at the top of the Home screen above workout routines.
> - **Card Sizing & Typography**: Maximum card width with enlarged, extra-bold titles and metrics for maximum impact and visual legibility.
> - **Data Fidelity**: All hardcoded numbers (such as dummy 3,200 kg volume and placeholder routine strings) replaced with genuine Room DB state and clean zero-state indicators.

---

### 1. Overview & Core Concept

- **What It Does**: Resolves the navigation bug that prevented opening or returning to the Home tab, transforms the Home screen into an ultra-bold, high-visibility dashboard with an expansive 16:9 workout hero illustration, magnifies card geometry and typography, and binds all stats strictly to real local Room Database records.
- **Target Audience / Persona**: Dedicated gym-goers who want instant, bold glanceability of their workout metrics and immediate one-tap workout logging without artificial mock numbers.
- **Key Value**: Reliable navigation between tabs, visual punch matching the reference aesthetic, and authentic workout analytics.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. *Open / Switch to Home Tab*: Users tap "Home" on the bottom navigation bar from any screen; the app smoothly lands on the Home dashboard without backstack deadlock.
  2. *Hero Inspiration & Quick Start*: Users view the top hero graphic displaying their current training streak and a bold "Start Workout" action button.
  3. *Routine & Body Weight Cards*: Enlarged side-by-side or stacked bento cards showing active split routines with glowing neon progress rings and real logged weight.
  4. *Activity Matrix*: Visual 3-month dot matrix showing actual completed session days.
  5. *Real Volume Analytics*: Real calculated 7-day cumulative volume lifted (showing 0 kg when clean, automatically increasing with completed workout sets).

- **Visual Identity & Theme**:
  - *Hero Art*: Sleek 16:9 athletic artwork with dark background and vibrant purple/electric cyan highlights.
  - *Typography*: Scaled display metrics (36–40sp extra-bold), primary card headers (20–22sp bold), and high-contrast labels.
  - *Cards & Borders*: Full-width container surfaces (`#161619`) with defined neon borders and 20–24dp rounded corners.
  - *Zero State*: Clean and motivating empty states with active prompts to record the first session or log current body weight.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Navigation Graph Stabilization**:
  - *Chosen Approach*: Streamline `GymNavGraph` bottom navigation dispatch. Replace strict nested `popUpTo` state-saving that can deadlock single-top root destinations with clean, standard Navigation Compose route matching.
  - *Why*: Guarantees the Home tab opens reliably from any screen, sub-screen, or editor without freeze or unhandled backstack state.
- **Decision 2: Elimination of Dummy Data vs. Starter Experience**:
  - *Chosen Approach*: Remove all hardcoded `3200.0`, static `0.65f` progress rings, and fake routine dates from `HomeViewModel`. Read directly from Room's `allTemplates`, `allBodyWeightEntries`, and `completedSessions`. If no workouts exist, display genuine `0` with a clean "No workouts logged yet" indicator and quick-start button.
  - *Why*: Builds trust and ensures accurate personal fitness tracking.
- **Decision 3: Hero Banner Presentation**:
  - *Chosen Approach*: Generate and integrate a dedicated 16:9 fitness hero banner at the top of the Home LazyColumn with an overlaid gradient and quick session launch action.
  - *Why*: Immediately elevates visual polish and matches the user's reference mockup.

---

### 4. Technical Architecture & Data Strategy

```
┌───────────────────────────────────────────────────────────────┐
│                       GymAppRoot                              │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │              Bottom Navigation (5 Tabs)                 │  │
│  │   [Home]     [Workout]   [Exercises]  [Progress] [Profile]│  │
│  └──────┬──────────────────────────────────────────────────┘  │
│         │ (Safe navigation without state deadlock)            │
│         ▼                                                     │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                     HomeScreen                          │  │
│  │  ┌───────────────────────────────────────────────────┐  │  │
│  │  │ 1. Workouts Header + Action Buttons               │  │  │
│  │  ├───────────────────────────────────────────────────┤  │  │
│  │  │ 2. Top Hero Card (16:9 Illustration + Quick CTA) │  │  │
│  │  ├───────────────────────────────────────────────────┤  │  │
│  │  │ 3. Bigger Bento Cards: Routine 1 & Body Weight    │  │  │
│  │  ├───────────────────────────────────────────────────┤  │  │
│  │  │ 4. Activity Dot Matrix + Routine 2                │  │  │
│  │  ├───────────────────────────────────────────────────┤  │  │
│  │  │ 5. Real 7-Day Volume Card (Real Room Sum)         │  │  │
│  │  ├───────────────────────────────────────────────────┤  │  │
│  │  │ 6. Full-Width Quick Add Tile                      │  │  │
│  │  └───────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────┬──────────────────────────┘  │
│                                 │ Reactive Flow               │
│                                 ▼                             │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                    HomeViewModel                        │  │
│  │     - UserProfile (Room)      - BodyWeight (Room)       │  │
│  │     - ActiveSession (Room)    - CompletedSessions (Room)│  │
│  │     - Real Volume Calculation - Real Streak Data        │  │
│  └─────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘
```

- **Execution Steps**:
  1. Fix navigation listener & backstack handling in `GymNavGraph.kt` to ensure Home tab opens smoothly.
  2. Generate and package the top fitness hero artwork drawable (`img_home_hero.jpg`).
  3. Refactor `HomeViewModel.kt` to strip all mock fallback numbers and emit authentic Room DB metrics.
  4. Redesign `HomeScreen.kt` with enlarged card geometry, bolder typography, top hero artwork, and clean zero states.
  5. Validate with `compile_applet` and Robolectric unit tests.
