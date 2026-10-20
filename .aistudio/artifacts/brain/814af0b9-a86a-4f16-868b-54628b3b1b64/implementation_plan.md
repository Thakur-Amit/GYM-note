# Implementation Plan: Body Measurements Experience & App-Wide KG Unit Standardization

Transform body tracking into a visual, record-keeping experience matching the user-provided design (`image_d3c75c30.png`). The new system provides visual body figure mapping with progress delta callouts, historical records timeline, metric trend charts with sparklines, dedicated measurement logging, exclusive `kg` unit standardization across the app, and full data export capabilities.

---

## 1. User Review Required

> [!IMPORTANT]
> **Key Decisions & Confirmations:**
> - **Comparison baseline:** Delta indicators show difference compared to the **last logged record** (as confirmed).
> - **Entry flow:** Dedicated **Add New** tab inside the Body Measurements screen for clean input and live composition preview.
> - **Unit standardization:** Weight unit is permanently locked to **`kg`** throughout the app (removing all `lb` toggles, converters, and selectors).
> - **Body figure visual:** High-fidelity neon-wireframe athletic silhouette with anchor dots and callout pointer lines for all 10+ body parts (Neck, Chest, Waist, Hips, Arms, Shoulders, Thighs, Calves).

---

## 2. Proposed Changes

### A. Body Measurements Screen (`BodyMeasurementsScreen.kt`)
Create a new primary destination matching the 2-screen reference UI with 4 tab destinations:
1. **Tabs Header:** `[ Overview ]`, `[ History ]`, `[ Charts ]`, `[ Add New ]`.
2. **Overview Tab:**
   - **Header Card:** Last updated date badge and "+ Add New" action button.
   - **3 Top KPI Cards:**
     - Weight: Value in `kg` with delta (`↓ -1.2 kg vs last record`).
     - Body Fat (Est.): Value in `%` (U.S. Navy formula) with delta (`↓ -2.4% vs last record`).
     - BMI: Value with delta (`↓ -0.6 vs last record`).
   - **Body Figure Visual Mapping (`BodyFigureOverview.kt`):**
     - Dark-themed athletic silhouette with glowing purple/cyan outline.
     - Pulsing anchor dots positioned at anatomical sites: Neck, Shoulders, Chest, Left/Right Arm, Waist, Hips, Left/Right Thigh, Left/Right Calf.
     - Callout labels with pointer lines showing current circumference in `cm` and delta vs previous record (`↓ -0.5 cm` in green for reduction/gain according to fitness context).
     - Informational card: "Compare with previous records to see your progress over time."
   - **Recent Records Section:**
     - Timeline of recent dated entries with summary (`72.5 kg • 18.6% BF • 10 measurements`).
     - "View All >" quickly jumps to the History tab.
3. **History Tab:**
   - Full reverse-chronological list of all logged measurement entries.
   - Expandable card per session displaying every measured circumference, notes, and deltas.
   - Options to edit or delete past entries.
4. **Charts Tab:**
   - Metric selector dropdown (Weight, Body Fat %, BMI, Neck, Chest, Waist, Hips, Arms, Thighs, Calves).
   - Time-range pills: `[ 1M ]`, `[ 3M ]`, `[ 6M ]`, `[ 1Y ]`, `[ All ]`.
   - Smooth curved trend chart with gradient area fill, data point markers, and interactive date/value tooltip callout.
   - **"All Measurements Progress" Section:**
     - List of all tracked metrics with current value, overall time-range change, and custom canvas-drawn sparkline mini-trend graph for each body part.
5. **Add New Tab:**
   - Structured entry form with sections:
     - Weight (`kg`)
     - Core Circumferences (`cm`): Neck, Waist, Hips
     - Upper Body (`cm`): Chest, Shoulders, Left/Right Biceps, Left/Right Forearms
     - Lower Body (`cm`): Left/Right Thighs, Left/Right Calves
     - Date picker & notes
     - Live auto-calculated U.S. Navy Body Fat %, BMI, and Fat-Free Mass card preview
     - "Save Record" button that persists to Room and updates all deltas.

---

### B. Profile Screen & Navigation Integration
- In `ProfileScreen.kt`:
  - Add a dedicated **"Body Measurements"** feature card/button right under the Profile header:
    - Title: "Body Measurements & Progress"
    - Subtitle: "Track circumferences, body figure map, and composition trends"
    - Icon: Body figure / ruler icon with ChevronRight.
    - Tapping opens the new `BodyMeasurementsScreen`.
  - **Remove weight unit selector (`kg / lb` toggle)** from Profile. Set profile unit permanently to `"kg"`.
- In `GymNavGraph.kt` & `Screen.kt`:
  - Add `Screen.BodyMeasurements` route.
  - Connect navigation from Profile and Home reminder card to `BodyMeasurementsScreen`.

---

### C. App-Wide `kg` Unit Standardization
- Remove `preferredWeightUnit` selector and `lb` switching logic across all screens:
  - `HomeScreen.kt`: Permanently display `kg`, remove unit toggles in quick weight dialog.
  - `ProfileScreen.kt`: Remove `lb / kg` segmented control.
  - `BodyWeightScreen.kt` & `AddWeightDialog`: Hardcode and enforce `kg`.
  - `WorkoutSet` & `QuickSetInput`: Enforce `kg` as default and only unit.
  - Clean up any unused unit conversions while preserving database compatibility.

---

### D. Export & Backup Enhancement
- In `BackupManager.kt`:
  - Include full `body_measurement_entries` and `body_composition_snapshots` in JSON backup and restore pipelines.
  - Add dedicated method `exportBodyMeasurementsToCsv()`:
    - Columns: `Date, Weight (kg), BMI, Body Fat %, Fat-Free Mass (kg), Neck (cm), Shoulders (cm), Chest (cm), Waist (cm), Hips (cm), Left Arm (cm), Right Arm (cm), Left Thigh (cm), Right Thigh (cm), Left Calf (cm), Right Calf (cm), Notes`.
- In `BackupRestoreScreen.kt`:
  - Add an action button: **"Export Body Measurements (CSV)"** with share/save intent.

---

## 3. Verification Plan

### Automated Tests
1. **Robolectric & Unit Tests (`GymTrackerComprehensiveTest.kt`):**
   - Test measurement entry insertion, delta calculation vs previous record.
   - Test CSV export generation format for body measurements.
   - Test historical timeline ordering and metric trend data generation.
   - Execute: `gradle :app:testDebugUnitTest`.

### Visual & Interactive Verification
1. Open Profile screen -> verify "Body Measurements" entry button is present and `lb` toggle is gone.
2. Open Body Measurements screen -> verify:
   - Overview tab displays 3 top KPI cards with delta vs last record.
   - Neon body figure graphic with callouts for Neck, Chest, Waist, Hips, Arms, Thighs, Calves.
   - Charts tab displays metric dropdown, time-range filters, and sparkline mini-charts for all metrics.
   - Add New tab allows entering measurements in kg/cm and saving.
3. Open Backup & Restore -> verify "Export Body Measurements (CSV)" generates valid CSV data.
