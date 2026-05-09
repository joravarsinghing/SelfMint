# Agent Instructions & Project Context

This document provides technical context and guidelines for AI agents and developers working on the **Habit Currency** project.

## Core Philosophy
- **Lean & Effective**: Prefer simple, readable solutions over complex abstractions.
- **Compose-First**: Prefer Jetpack Compose for all UI. Avoid adding new XML screens unless there’s a clear, unavoidable reason.
- **Single Source of Truth**: Keep data flowing from Room/DataStore through Repositories to ViewModels via Kotlin Flow.

## How to Use This Doc (Avoid Over-Constraint)
- Treat these as **guidelines**, not hard rules. Use good judgment and prioritize the user’s intent.
- Prefer changes that reduce future maintenance, avoid “AI tunnel vision,” and keep the app coherent.
- If a guideline conflicts with an explicit user request, follow the user request and keep the change minimal.

## Architecture Guidelines
- **Dependency Injection**: Prefer the `AppContainer` pattern for manual DI. Avoid introducing Dagger/Hilt unless there’s a strong payoff.
- **State Management**: Prefer `collectAsStateWithLifecycle()` in screens consuming `StateFlow` from ViewModels.
- **Business Logic**: Keep logic in Repositories or small use-case classes (e.g., `RolloverRepository`) rather than in Composables.

## Key Data Flows
1. **The Rollover**: Processed in `MainActivity` via `RolloverRepository`. It handles daily task regeneration and carry-forward logic for one-time tasks.
2. **The Wallet**: Managed via `WalletDataStore`. Task completion/repeat typically increments balance; reward redemption typically decrements it; undo/edit flows should keep balance consistent.
3. **Tags**: Many-to-many relationship between Tasks/Rewards and Tags using Room CrossRefs.

## UI Patterns
- **Colors**: Defined in `ui.theme.Color.kt`. Use `TasksBlue` for primary actions in the Task flow.
- **Components**: Prefer reusable small Composables.
- **Icons**: Use `Icons.Default` or `Icons.Rounded` from Material Icons Extended.

## Common Tasks for Agents
- **Adding a New Screen**: Register the route in `AppNavigation.kt` and add a `BottomNavItem` if it’s a top-level destination.
- **Database Changes**: Update entities in `data.local.entity`, update `AppDatabase`, and increment the version if schema changes.
- **Logic Updates**: Always check `RolloverRepository` if the change affects how tasks behave across days.
