# Project "Trend" Enhancement Plan

This file tracks the step-by-step improvements to make the app a 10/10 user experience and optimize monetization.

---

### Phase 1: Immediate UI/UX Wins

- [x] **Fix jarring screen refresh on favorite:** Modify `songsAdapter` to use `notifyItemChanged()` instead of `recreate()` in `MainActivity` when a song is marked as a favorite. This will provide a smooth, instant visual update.
- [x] **Analyze and redesign the main Player screen (`Play.java`):** Improve the layout, add a more visually appealing progress bar, and ensure all controls are intuitive and modern.
- [x] **Implement Smarter Interstitial Ads:** Changed the ad display logic to show interstitial ads upon returning to the main activity after a song has finished playing, creating a more natural and less intrusive user experience.

---

### Phase 2: Advanced Monetization & UI Polish

- [ ] **Implement Material Design 3:** Update the project's Material dependency and refactor layouts to use modern components like `MaterialCardView` for a polished, professional look.
- [x] **Implement Rewarded Ads:** Introduce a feature where users can watch a rewarded ad to get a period of ad-free listening (e.g., 30 minutes).

---

### Phase 3: Architectural Refactoring for a 10/10 App

- [ ] **Refactor MainActivity with MVVM:** Create a `ViewModel` to move data handling (the song list) and business logic out of `MainActivity`, making the code cleaner and easier to maintain.
- [ ] **Migrate to Room Database:** Replace the raw `SQLite` implementation with the modern Room persistence library for a safer, more robust, and easier-to-manage favorites database.
- [ ] **Decouple Song Data:** Move the hardcoded song list from `MainActivity` into a separate data source that the ViewModel can load, such as a JSON file in the `assets` folder.
