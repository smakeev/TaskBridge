# TaskBridge — Backlog (Epics + Code Audit)

This file is the project's work tracker. It holds **two sets of items**:

- **Triaged items** — concrete findings already located in a platform/Core list
  (`Common-n` / `iOS-n` / `And-n` / `Core-n`). These came out of the code audit; their
  scope is the Kotlin `Core` module (`Core/src/commonMain/**`) and the platform *app*
  code (`iOS/TaskBridge/**`, `Android/src/androidMain/**`) — `Platform_Handlers` and
  build/config are out of scope for the audit.
- **Epics** — product-level work items (`TASK-n` / `STORY-n` / `BUG-n`). An epic is not
  tied to a platform; it groups the triaged items (and/or sub-epics) related to it.

**Everything is driven by editing this file** — the viewer (`backlog.html`) only parses,
links, filters, and sorts what is written here.

---

## Epics (how this works)

An **epic** is a product-level work item. It has a stable id, a **type**, and a
**status**, and it *contains* the triaged items and/or sub-epics related to it.

**Epic types:**

- **🛠️ Task** — an action that isn't a feature or a bug fix: an audit, a tooling /
  build / CI-CD change, or a non-behavioural code change (e.g. an indentation fix).
- **✨ Story** — a feature implementation.
- **🐞 Bug** — a problem we found. We open it as an epic *before* we know where it
  lives (Core? a platform?); the triaged items under it pin down the actual fix
  location(s). (A no-bug "we just want to refactor/enhance this" can be a Task **or** a
  Story — decide case by case.)

**Epic status:** **🔓 Open** / **🔒 Closed**. Closing an epic is independent of its
contents: closing an epic does **not** close anything inside it, and an epic whose
children are *all* closed does **not** become closed automatically. You close an epic
only by editing its status here.

**Membership is stored on the child, once.** A triaged item or a sub-epic that belongs
to an epic carries a single reference line in its body:

```
  → Epic: <PARENT-EPIC-ID>
```

That is the only place the relationship is written — an epic's contents are *derived*
from the children that point at it, so nothing can drift. A child has at most one parent
epic. A triaged item may have **no** `→ Epic:` line at all, in which case it belongs to
no epic and shows no ref. To move an item between epics, edit its one ref line.

**Platform binding** for an epic (which platform[s] it ultimately touches) lives only in
the epic's prose description — epics are product-level; the triaged children are where
the platform-specific work actually happens.

In `backlog.html`, every epic reference is an active link that **opens that epic in a new
tab** (the epic as a header, with its triaged items and sub-epics beneath it); sub-epics
in that view are themselves links that open in further tabs.

**Machine-readable epic format.** Each epic is a top-level bullet whose title line is
exactly:

```
- **<TYPE>-<n> <type-emoji> · <status-emoji> <Open|Closed> — <Title>**
```

where `<TYPE>` is `TASK` / `STORY` / `BUG`. Its description follows on the body lines; a
sub-epic adds a `→ Epic: <PARENT-EPIC-ID>` line like any other child.

---

## The audit findings

Each finding has a stable id, a **severity** hint, and a **status**.

**Id namespaces:**

- **`Common-n`** — one issue that is present *identically* on both iOS and Android.
  The per-platform `iOS-n` / `And-n` entries for it are kept (so the numbering is
  stable) but reduced to a pointer + the platform-specific location; the full
  description lives in **Common**.
- **`iOS-n` / `And-n`** — platform-app findings.
- **`Core-n`** — findings in the shared Kotlin module.

**Severity:** 🔴 high · 🟠 medium · 🟡 low.

**Status** (shown in each finding title, after the severity):

- **🔓 Open** — not yet addressed.
- **✅ Closed-Fixed** — code was changed to fix it.
- **☑️ Closed-Done** — work item completed (e.g. intentionally implemented / handled).
- **🗒️ Closed-Resolution "…"** — closed without a code fix, with a resolution note
  (e.g. *won't fix*, *by design*, *deferred*); the reason is quoted inline.

**Machine-readable format** (so `backlog.html` can parse, filter, and sort these). Each
finding is a top-level bullet whose title line is exactly:

```
- **<ID> <severity-emoji> · <status-emoji> <Status>[ "<resolution>"] — <Title>**
```

where `<ID>` matches `(Common|iOS|And|Core)-<n>` (the prefix is the type; `And` =
Android). The body lines that follow may use two markers, rendered in bold:
**`Fix:`** for what was changed, and **`Comment:`** for rationale/notes. A finding that
belongs to an epic also carries a `→ Epic: <PARENT-EPIC-ID>` line. Open
`backlog.html` in this folder to read it with type/status filters, sorting, and the
Triaged / Epics / All views.

A short summary is at the bottom.

---

## Epics

- **TASK-1 🛠️ · 🔓 Open — Make audit.**
  Full code audit of the Kotlin `Core` module and the iOS/Android app layers, captured
  as the triaged `Common-` / `iOS-` / `And-` / `Core-` findings below. Product-level and
  cross-platform by nature (it spans all three targets); the actual fixes happen in the
  triaged children. **Always open** — the audit is an ongoing activity: new findings are
  added under it over time, and it is not closed just because individual findings are.
  All current findings belong to this epic.

---

## Common (iOS + Android)

These are the same defect on both platforms; fix them in lockstep to keep the apps
aligned.

- **Common-1 🟠 · 🗒️ Closed-Resolution "By design: while the user is on the Tasks tab we never force-navigate on an external add — only toast; blink-in-place is the visible screen's job. The proposed isCurrentRoot symmetry would wrongly yank the user out of TaskDetails." — Asymmetric “already here?” check when routing app messages.**
  → Epic: TASK-1
  Both platforms handle the `reminderCreated` / `taskAdded` app messages in the same
  place (iOS `ContentView.handleNavigationMessage`, Android
  `MainActivity`’s message-collect block). The two branches use *different*
  granularity: the reminder branch early-returns on `isCurrentRoot(.reminders)`, but
  the task branch early-returns on merely `fetchCurrentTab() == .tasks`. So if you are
  on the Tasks tab but inside `TaskDetails` (not the root) when a root task is added
  elsewhere, the task branch returns early, never sets the navigation-destination
  message, and the “scroll & blink” highlight is lost when you return to the root.
  **Comment:** This coarser whole-tab check is *intentional*, not a defect — do **not**
  "fix" it to `isCurrentRoot` in a future audit. The `handleNavigationMessage` branch
  governs only *forced* navigation (`pullToRoot` + `selectTab`); the in-place blink is a
  separate mechanism. The rule we want: **while the user is anywhere on the Tasks tab —
  root or a (possibly deep) `TaskDetails` — an external add must never force-navigate;
  we only toast.** Pulling a user out of a details view to the root on every external
  add would be disruptive. Blink-in-place, *when the added task's parent scope is the
  screen currently shown*, is already handled by each screen's live
  `observeTaskCreatedMessages` (root: `parentPath.isEmpty`; details:
  `parentPath.last == taskId`, so it already works at arbitrary depth). Adopting
  `isCurrentRoot(.tasks)` would set the pending message and `pullToRoot` + `selectTab`
  from `TaskDetails`, yanking the user to the root — the opposite of what we want.
  Forced navigation-with-blink is reserved for *actions* (the not-on-Tasks-tab `else`
  path, e.g. applying a template from the Templates tab). The structural reason the
  asymmetry is fine: the Tasks tab has root **+ deep details**, whereas reminder
  *creation* always surfaces at the reminders root, so for reminders `isCurrentRoot` is
  effectively a whole-tab check anyway. (Next step, out of scope here: when templates
  can target a non-root parent, the *action* will carry full-path navigation to that
  parent's details — still not this passive handler.)
  → Platform entries: **iOS-4**, **And-8**.

- **Common-2 🟡 · 🔓 Open — `forceLoadTemplates()` is dead; “refresh” just calls `loadTemplates()`.**
  → Epic: TASK-1
  On both platforms the templates repository/interactor exposes `forceLoadTemplates()`
  but nothing calls it, and the view model’s `refreshTemplates()` just calls
  `loadTemplates()` — which is TTL-gated in Core (`RemoteResourceCommand.Load` skips
  when fresh, see Core), so a user-triggered “refresh” can be a silent no-op within the
  10-minute TTL. Wire refresh to `forceLoadTemplates()` (→ `RemoteResourceCommand.ForceLoad`)
  or remove the unused API on both sides.
  → Platform entries: **iOS-5**, **And-5**.

- **Common-3 🟠 · ✅ Closed-Fixed — Tasks *root* and *details* screens are ~90% duplicated.**
  → Epic: TASK-1
  On each platform the root list screen and the details screen carried near-identical
  logic: observe-task-created, consume-pending-navigation-message, wait/scroll/blink to
  a row, and the rename + reminder (+ subtask) dialog/sheet wiring. iOS:
  `TasksRootView` vs `TaskDetailsView`. Android: `TasksRootScreen` vs
  `TaskDetailsScreen` (the scroll/blink + header-offset helper was duplicated into
  `RemindersRootScreen` too).
  **Fix:** extracted a *domain-neutral* highlight/scroll coordinator shared by the
  Tasks root/details **and** the Reminders screens (the blink behaviour is generic —
  it is not task-specific), plus a task-only dialog/sheet block. The coordinator is
  parameterized by scope id, a created-message → target-id mapper (also where a screen
  filters to its own scope / gates on "am I current"), a pending-message → target-id
  mapper, and a "find item" closure.
  *iOS*: `UIComponents/ScrollBlinkBinding.swift` — a `ScrollBlinkBinding` config + a
  `.scrollBlinkHighlighting(…)` `View` modifier (the two `.task` observers feeding
  `ScrollBlinkHighlighter`); `TasksRootView`, `TaskDetailsView`, and `RemindersRootView`
  each just supply a `blinkBinding`. The task-only rename/reminder sheets live in
  `TaskActionSheets.swift` (`.taskActionSheets(…)`).
  *Android*: `ScrollBlinkEffects` + `rememberScrollToAndBlink` + `leadingItemsOffset`
  (all in `components/ScrollBlinkSupport.kt`), with the task-only `TaskActionDialogs`
  in its own file; `TasksRootScreen`, `TaskDetailsScreen`, and `RemindersRootScreen`
  all drive the shared `ScrollBlinkEffects`. Builds: `:Android:` and the iOS
  `TaskBridge` scheme both compile. (The hand-counted header offsets the find-item
  closures still use are tracked separately as **And-3**.)
  → Platform entries: **iOS-7**, **And-7**.

- **Common-4 🟡 · 🔓 Open — Domain-object lifetime model: only services are singletons; everything above is on-demand; caches live in Core managers.**
  → Epic: TASK-1
  Agreed model for the whole stack: the Core **services** (the AppState boxes in
  `CoreServiceLocator`) are process-lifetime singletons; **repositories, interactors,
  use cases, and stories** are stateless and created on demand, living only as long as
  a screen/VM needs them. Any **cache or mutable state belongs in a Core
  manager/service, never in a repository**, so repositories stay freely recreatable —
  this is what makes the iOS weak-share (**iOS-1**) safe.
  **Comment:** Rollout status — the Core foundation is done (interactors/use cases are
  no longer pinned, **Core-12**), and iOS already conforms via its weak
  `RepositoriesStorage`. **Remaining:** Android still pins repositories with `by lazy`
  in `RepositoriesStorage.kt`, so it cannot release them; give it the same
  release-on-demand sharing as iOS (a weak / ref-counted cache, or per-VM ownership)
  to finish the model. The "caches → Core manager" rule should also be documented so a
  future caching repository doesn't reintroduce per-repository state.

---

## iOS

### Retain cycles & memory / object lifetime

- **iOS-1 🟠 · 🗒️ Closed-Resolution "By design: repositories are stateless scoped wrappers; weak sharing is intentional and safe. See Common-4." — `RepositoriesStorage` caches repositories with `weak var`.**
  → Epic: TASK-1
  `RepositoriesStorage.swift` stores every repository in a `private weak var …Ref`
  and recreates it in the getter when the ref is `nil`. A repository is therefore
  shared while at least one view/VM strongly holds it, and is deallocated and rebuilt
  on next demand once all holders are gone (with a fresh `createStream` bridge — a new
  `Task` + `Collector` + `AsyncStream`). The Core source flows are hot conflated
  `StateFlow`s (`BaseStatefulService` exposes `MutableStateFlow.asStateFlow()`), so
  re-subscription just replays the current value with no recomputation at the service.
  **Comment:** This is the intended scoped-lifetime model, not a defect. Repositories —
  and now their interactors / use cases / stories (see **Core-12**) — are stateless
  wrappers; all real state lives in the long-lived Core services/managers. So an
  instance carries nothing worth preserving: recreating one is free and observably
  identical, and object identity is never relied upon (every instance delegates to the
  same singleton service). Weak caching gives exactly the wanted behaviour — share one
  instance while screens are alive, drop it when none are, recreate on demand — so a
  repository used only by a pushed screen dies with that screen instead of being pinned
  forever. The one rule that keeps this safe is that **caches/state never live in a
  repository; they live in a Core manager** (see **Common-4**): a repository that needs
  state holds a reference to that manager, so it still owns nothing that recreation
  could lose.

- **iOS-2 🟡 · 🔓 Open — `ScrollBlinkHighlighter.uncancellableSleep` outlives its `.task`.**
  → Epic: TASK-1
  `scrollToAndBlink` is launched from `.task` modifiers in `TasksRootView` /
  `TaskDetailsView` / `RemindersRootView`, but it `await`s `uncancellableSleep`
  (`FlowUtils.swift`), which by design ignores cancellation. If the user navigates
  away mid-blink, the owning task is cancelled yet the highlighter keeps mutating its
  `@Published highlightedId` / `opacity` for several more seconds. Harmless visually,
  but it is state churn on a view that is gone and defeats structured cancellation.

### Race conditions

- **iOS-3 🟠 · 🔓 Open — Two-way tab binding feedback loop in `ContentView`.**
  → Epic: TASK-1
  `.onChange(of: selectedTab)` pushes `navRepo.selectTab(tab:)` (async) while a
  separate `.task` observes `navRepo.currentTab` and writes `selectedTab` back. The
  loop is only broken by the `selectedTab != tab` guard. Because both directions hop
  through async boundaries, rapid tab taps can interleave (Core emits an older tab
  after a newer local change), producing visible flicker or a “bounce” to the previous
  tab. A single source of truth (drive the `TabView` selection directly from
  `currentTab`) would remove the loop.

### Logical problems

- **iOS-4 🟠 · 🗒️ Closed-Resolution "By design — see Common-1." — Asymmetric “already here?” check in `handleNavigationMessage`.**
  → Epic: TASK-1
  → See **Common-1**. iOS location: `ContentView.handleNavigationMessage`
  (`isCurrentRoot(.reminders)` vs `fetchCurrentTab() == .tasks`).

- **iOS-5 🟡 · 🔓 Open — `forceLoadTemplates()` is dead; `refreshTemplates()` is mislabeled.**
  → Epic: TASK-1
  → See **Common-2**. iOS location: `TaskTemplatesRepository` (protocol + impl) and
  `TaskTemplatesViewModel.refreshTemplates()` (pull-to-refresh).

- **iOS-6 🟡 · 🔓 Open — Checkbox `Toggle` binding ignores its new value.**
  → Epic: TASK-1
  In `TaskDetailsView.detailsCard`, the toggle is
  `Binding(get: { task.isChecked }, set: { _ in viewModel.toggleCheckbox(task:) })`.
  The setter discards the incoming value and unconditionally toggles. This relies on
  SwiftUI never invoking the setter with the current value; any extra setter call (or
  a stale `task` capture) flips the state the wrong way.

### Reuse / duplication (possible generics)

- **iOS-7 🟠 · ✅ Closed-Fixed — `TasksRootView` and `TaskDetailsView` are ~90% duplicated.**
  → Epic: TASK-1
  → See **Common-3**. iOS location: `TasksRootView` vs `TaskDetailsView`
  (`observeTaskCreatedMessages`, `consumePendingNavigationMessage`, `waitForTaskRow`,
  the rename/reminder sheets, the highlight `onChange` block). Now share the generic
  `ScrollBlinkBinding` / `.scrollBlinkHighlighting` (also used by `RemindersRootView`)
  plus task-only `.taskActionSheets`.

- **iOS-8 🟡 · 🔓 Open — Duplicated `TaskItem` construction.**
  → Epic: TASK-1
  `TasksViewModel.newTask(...)` and `TaskTemplatesViewModel.makeTask(...)` both
  switch over the type and rebuild a `TaskItem` per case. Additionally,
  `toggleCheckbox` / `renameTask` rebuild the *entire* `TaskItem` field-by-field
  (Android uses `task.copy(...)`), which is verbose and breaks silently whenever a
  field is added. Extract a single `TaskItem` factory + a Swift `copy`-style helper.

- **iOS-9 🟡 · 🔓 Open — `TaskTreeRowsView` is a misleading no-op wrapper.**
  → Epic: TASK-1
  It forwards every argument to `TaskRowView` and adds nothing, and despite the
  “Tree”/`depth` naming it never recurses into `task.children` (`depth` is always 0).
  Either make it actually render the subtree or delete it and use `TaskRowView`
  directly.

### Bad practices

- **iOS-10 🟡 · 🔓 Open — `print()` logging in production code.**
  → Epic: TASK-1
  `iOSReminderHandler`, `RemindersViewModel`, and `TasksViewModel` log via `print`.
  Use `os.Logger`/`OSLog` with categories so logs are filterable and stripped in
  release.

- **iOS-11 🟡 · 🔓 Open — Type name `iOSReminderHandler` violates UpperCamelCase.**
  → Epic: TASK-1
  Swift type names should be `IOSReminderHandler` (or `AppleReminderHandler`).

- **iOS-12 🟡 · 🔓 Open — Inconsistent VM conventions.**
  → Epic: TASK-1
  `TaskTemplatesViewModel` is a non-`final class` while the other VMs are `final`, and
  it omits the `hasLoaded` guard used by `loadTasks` / `loadReminders`, so its
  `loadTemplates()` can fire redundantly. Align the conventions.

---

## Android

### Object lifetime / lifecycle

- **And-1 🔴 · ✅ Closed-Fixed — Dependency graph rebuilt on every configuration change.**
  → Epic: TASK-1
  `MainActivity.onCreate` constructs `PlatformDependencies`, `TaskBridge`, and
  `RepositoriesStorage.create(taskBridge, lifecycleScope)` directly. On rotation /
  config change `onCreate` runs again, so the *entire* Core graph, all repositories,
  and all flow subscriptions are torn down and rebuilt, and the `stateIn` scope
  (`lifecycleScope`) is cancelled — dropping in-memory state and re-doing Core
  bootstrap on every rotation.
  **Fix:** the graph now lives in a new `TaskBridgeApplication : Application` (registered
  via `android:name=".TaskBridgeApplication"`), which builds `TaskBridge` +
  `RepositoriesStorage` once per process behind a `by lazy`, using a process-lifetime
  `appScope` (`SupervisorJob() + Dispatchers.Default`) instead of the Activity
  `lifecycleScope`. `MainActivity.onCreate` now just reads
  `(application as TaskBridgeApplication).repositoriesStorage`, so the graph and its
  state survive Activity recreation. Passing the `Application` as the `Context` also
  drops the previous Activity-context capture in
  `PlatformDependencies`/`AndroidReminderHandler`. Builds + assembles
  (`:Android:assembleDebug`); the Application is present in the merged manifest. (One
  process-lifetime graph is still never disposed — that is **Core-9**.)

### Race conditions / state sharing

- **And-2 🟠 · 🔓 Open — Inconsistent state-sharing strategy across view models.**
  → Epic: TASK-1
  `TasksViewModel` and `TaskTemplatesViewModel` wrap the repository flow with
  `stateIn(viewModelScope, WhileSubscribed(5000))`, but `RemindersViewModel` exposes
  the raw `repository.remindersState` — which `RemindersRepositoryImpl` already shared
  with `stateIn(scope, WhileSubscribed(5000))`. The result is two different sharing
  lifetimes for three equivalent screens (per-VM scope vs the shared graph scope —
  since And-1 that shared scope is the process-lifetime `appScope`, no longer the
  Activity `lifecycleScope`). Pick one pattern. (Note: the divergence is *not*
  cosmetic — the Core `StateFlow` is hot,
  but each interactor re-wraps it with cold `.map { … }.distinctUntilChanged()`
  operators (see `TasksInteractor.kt`), so the cold chain is re-collected per collector
  and the choice of where to `stateIn` genuinely changes the collection lifetime.)

- **And-3 🟠 · 🔓 Open — Hand-counted header offsets for scroll targeting are fragile.**
  → Epic: TASK-1
  `tasksListHeaderOffset(state)` / `remindersListHeaderOffset(state)` re-derive the
  number of leading `item {}`s (header + loading + error + empty) to convert a data
  index into a `LazyColumn` index for `animateScrollToItem`. This duplicates the
  conditional layout in the composable and silently drifts the moment the list’s
  header items change → scroll-to-wrong-row. Prefer stable item `key`s + a key-based
  scroll, or build the index from the same list that renders the items.

### Logical problems

- **And-4 🟠 · 🔓 Open — Progress and Container tasks share the same icon.**
  → Epic: TASK-1
  In `TaskRow.kt`, `taskIcon` maps both `TaskType.PROGRESS` and `TaskType.CONTAINER`
  to `Icons.AutoMirrored.Filled.List`, so the two types are visually
  indistinguishable. iOS distinguishes them (`chart.bar` vs `folder`). Give each type
  a distinct icon.

- **And-5 🟡 · 🔓 Open — `refreshTemplates()` duplicates `loadTemplates()`; `forceLoadTemplates()` is dead.**
  → Epic: TASK-1
  → See **Common-2**. Android location: `TaskTemplatesViewModel.refreshTemplates()`
  (identical `try/catch` body to `loadTemplates()`, wired to the “Refresh” button) and
  the unused `TaskTemplatesRepository.forceLoadTemplates()`.

- **And-8 🟠 · 🗒️ Closed-Resolution "By design — see Common-1." — Same asymmetric navigation check as iOS-4.**
  → Epic: TASK-1
  → See **Common-1**. Android location: `MainActivity` message-collect block
  (`isCurrentRoot(AppTab.REMINDERS)` vs `fetchCurrentTab() == AppTab.TASKS`).

- **And-11 🟡 · 🔓 Open — `state.findTask(...)` runs over the whole tree on each recomposition.**
  → Epic: TASK-1
  `TaskDetailsScreen` calls `state.findTask(TaskId(taskId))` directly in the composable
  body, re-walking the tree on every recomposition. Hoist it behind
  `remember(state, taskId) { … }`.

### Reuse / duplication (possible generics)

- **And-6 🟠 · 🔓 Open — `ViewModelProvider.Factory` boilerplate copy-pasted 4×.**
  → Epic: TASK-1
  The same anonymous `object : ViewModelProvider.Factory { … as T }` block appears in
  `TasksRootScreen`, `TaskDetailsScreen`, `RemindersRootScreen`, and
  `TemplatesRootDestination`. Replace with a single reusable helper, e.g.
  `viewModelFactory { TasksViewModel(...) }` (androidx) or a small inline generic.

- **And-7 🟠 · ✅ Closed-Fixed — Screen-level scroll/blink/dialog wiring duplicated.**
  → Epic: TASK-1
  → See **Common-3**. Android location: `TasksRootScreen` vs `TaskDetailsScreen`
  (rename/reminder/subtask dialogs); the `scrollToAndBlink` + `findItemIndex` +
  header-offset pattern is also duplicated into `RemindersRootScreen`. Now share the
  generic `ScrollBlinkEffects` + `rememberScrollToAndBlink` + `leadingItemsOffset`
  (`ScrollBlinkSupport.kt`, used by all three screens) and task-only `TaskActionDialogs`.

### Bad practices

- **And-9 🟡 · 🔓 Open — Inline fully-qualified names despite existing imports.**
  → Epic: TASK-1
  `MainActivity` matches `com.taskbridge.core.models.messages.AppMessage.ReminderCreated`
  inline even though `AppMessage` is imported, and `TaskDetailsScreen` uses
  `com.taskbridge.core.models.tasks.TaskId(taskId)` inline. Import and use short names.

- **And-10 🟠 · 🔓 Open — `AndroidReminderHandler` does no real scheduling.**
  → Epic: TASK-1
  It persists reminders to `SharedPreferences` and emits updates, but the actual
  `AlarmManager`/`WorkManager` scheduling is left as `TODO`, so scheduled reminders
  never fire on Android (iOS uses real `UNUserNotificationCenter`). JSON
  encode/decode also happens on the calling coroutine’s thread. Implement real
  scheduling and move (de)serialization off the main dispatcher.

---

## Core

Analysis of the shared Kotlin module only (`Core/src/commonMain/**`).

### Race conditions

- **Core-1 🔴 · ✅ Closed-Fixed — Non-atomic `updateState` raced by two coroutines in `RemindersService`.**
  → Epic: TASK-1
  `BaseStatefulService.updateState` does `_data.value = reducer(_data.value)` — a
  non-atomic read-modify-write. `TasksService`, `AppStateService`, and
  `RemoteResourceService` only mutate from the single command-loop coroutine, so they
  are safe. **`RemindersService` is not:** it calls `updateState` both from the command
  loop (`performLoad` / `performSchedule` / `performAction`) *and* from the separate
  `reminderEvents.events().collect { … }` coroutine, both running on
  `Dispatchers.Default` (multi-threaded). Two concurrent RMWs lose updates — e.g. a
  command’s `it.copy(isLoading = true)` built from a stale snapshot clobbers a
  freshly-arrived `reminders` list, so reminders transiently vanish or `isLoading`
  sticks.
  **Fix:** `updateState` now uses `MutableStateFlow.update { }` (an atomic
  compare-and-set loop) instead of `_data.value = reducer(_data.value)`, so concurrent
  read-modify-write callers can no longer drop updates. Single-call site change in
  `BaseStatefulService.kt`; `:Core:compileDebugKotlinAndroid` passes.

- **Core-2 🟠 · ✅ Closed-Fixed — Initial reminders sync can be dropped (replay=0 bus race).**
  → Epic: TASK-1
  `RemindersService.init` used to launch two coroutines on the same scope: one collected
  `reminderEvents.events()`, the other emitted the initial `RemindersUpdated`.
  `CoreEventBus` is a `MutableSharedFlow(replay = 0, …)`, so if the emitter ran before
  the collector had subscribed, the initial event was lost and the reminder list stayed
  empty until a manual `loadReminders()`. It self-healed because the view models call
  `loadReminders()` on screen appear, but it was a real startup ordering race.
  **Fix:** took the second remedy this entry proposed — `init` now runs a single
  coroutine that calls `runInitialSync()` (fetch `getAllReminders()` and write state
  *directly* via `handleRemindersUpdate`, no bus round-trip) and *then* subscribes to
  `reminderEvents.events()`. The internal initial emit is gone, so there is no event to
  lose; `runInitialSync` keeps a `try/catch` (rethrowing `CancellationException`) so a
  failed initial fetch surfaces to `errorMessage` and the collector still subscribes. The
  now-redundant `hasCompletedInitialSync` flag was removed — initial sync no longer goes
  through the message-publishing collector, so there is nothing to suppress, and the
  first real event diffs against the seeded list. (Note: the eager seed makes Core
  authoritative on startup regardless of UI; the on-appear `loadReminders()` is kept
  deliberately as the re-poll path for out-of-band changes the handler never emits for —
  e.g. an iOS notification firing and dropping out of `pendingNotificationRequests()`.)

- **Core-3 🟠 · 🔓 Open — Non-atomic read-then-clear in `consumeNavigationDestinationMessage`.**
  → Epic: TASK-1
  `PushNavigationUseCase.consumeNavigationDestinationMessage` reads the pending message
  via `FetchNavigationDestinationMessageStory` (which reads `appStateService.data`
  directly, *bypassing* the command channel) and then clears it with a *separately
  queued* `SetNavigationDestinationMessage(null)` command. The read-check-clear is not
  atomic and skips the actor serialization the rest of `AppStateService` relies on: a
  `set(message)` that lands between the read and the clear is silently wiped, and two
  callers can observe the same message before either clear is processed. Make consume a
  single command whose reducer reads and clears atomically inside `AppStateService`.

### Logical problems

- **Core-4 🟠 · 🔓 Open — `RemoteResourceService` serializes all loads (head-of-line blocking).**
  → Epic: TASK-1
  `handleCommand` invokes `command.loader()` (the network fetch) inline, and
  `BaseStatefulService` pulls commands one-at-a-time from its channel. While one URL is
  loading, every other `RemoteResource` command — other URLs, force-loads, even the
  fresh-cache metadata update — waits behind it. Only the templates URL is used today,
  so it is latent, but the per-URL `LOADING` guard implies a concurrency the design
  does not actually deliver. Launch each load in a child coroutine keyed by URL rather
  than awaiting it in the command loop.

- **Core-5 🟡 · 🔓 Open — `createdAt` is reset on every edit.**
  → Epic: TASK-1
  `TaskItem.toEntities` stamps `createdAtMillis = now` (and `updatedAtMillis = now`) for
  *every* node on *every* upsert, so the original creation time is lost whenever a tree
  is replaced (a TODO acknowledges this). Harmless while timestamps are unused in the
  UI, but the persisted field is misleading. Preserve existing `createdAtMillis` in the
  storage layer.

- **Core-6 🟡 · 🔓 Open — Tree reconstruction/deletion is not cycle-safe and not range-safe.**
  → Epic: TASK-1
  `List<TaskEntity>.toTaskTree` (`buildTask`) and `TaskStorageManager.collectSubtreeIds`
  (`collect`) recurse into children without a visited-guard, so a malformed DB with a
  parent cycle recurses infinitely / stack-overflows. `toTaskTree` also silently drops
  children of non-`CONTAINER` rows, and `TaskProgress(it)`’s `require(value in 0..100)`
  throws on an out-of-range persisted progress (the `TaskType.valueOf` fallback covers
  bad types but not bad progress). Add visited-guards and clamp/validate on read.

### Reuse / generics / type-safety

- **Core-7 🟠 · 🔓 Open — Unchecked generic casts erase type safety around remote resources.**
  → Epic: TASK-1
  `RemoteResourceEntry.data` is `Any?`, so `TemplatesInteractor` must do
  `entry.data as? List<TaskTemplateDto>` (erased — only checks “is a `List`”), and
  `JsonRequestManager` stores `Deferred<Any?>` keyed by URL alone: two callers
  requesting the same URL with different reified `T` share one deferred, and the second
  `await() as T` casts the first’s result → `ClassCastException`. Fine for the single
  templates URL today, but it is a latent erasure bug. Consider a typed resource key
  (or storing the decoder alongside the URL) so the resource cache is type-safe.

- **Core-8 🟡 · 🔓 Open — “Containers” don’t cache, and the layering is mostly pass-through.**
  → Epic: TASK-1
  `UseCaseContainer.get(...)` and every `UserStoriesContainer` getter construct a **new**
  instance on each call despite the “Container” name implying memoization, and the path
  for one action is Repository → Interactor → UseCase → Story → Service where the
  UseCase/Story layers are typically one-line pass-throughs (e.g. `TasksUseCase.createTask`
  → `CreateTaskStory.createTask` → `tasksService.sendCommand`). The stateful singletons
  live in `CoreServiceLocator`, so this is functionally harmless but is a lot of
  allocation and indirection; memoize the containers or collapse the pure pass-through
  layers.

### Bad practices / lifecycle

- **Core-9 🟠 · 🔓 Open — No `close()`/dispose; scope and `HttpClient` leak.**
  → Epic: TASK-1
  `CoreServiceLocator` creates a `CoroutineScope(SupervisorJob() + Dispatchers.Default)`
  and `HttpJsonClient` creates a Ktor `HttpClient`, but neither `TaskBridge` nor the
  assembler exposes a way to cancel/close them. Each `TaskBridge` instance leaks a live
  scope (with the services’ command loops and collectors) and an unclosed `HttpClient`.
  Benign for a single app-lifetime instance, but it compounds **And-1** (a new
  `TaskBridge` per Android rotation) into a per-config-change scope + HttpClient leak.
  Add a `close()` that cancels the scope and closes the client.

- **Core-10 🟡 · 🔓 Open — `println` logging in production Core.**
  → Epic: TASK-1
  `RemindersService` (and others) log via `println`, which on Android lands in
  logcat/stdout unfiltered and is not strippable in release. Route through an injectable
  logger / `expect` logging API. (Mirrors iOS-10.)

- **Core-11 🟡 · 🔓 Open — User-facing strings built in Core with a raw timestamp.**
  → Epic: TASK-1
  `MessagesInteractor.toAppMessage` composes the toast text in Core
  (`"Reminder created: … • ${triggerAtMillis.formatAsInstantText()}"`) using
  `Instant.toString()` — a raw ISO-8601 UTC string (e.g. `2026-05-29T12:00:00Z`) — which
  leaks an unformatted timestamp into the UI and bakes English copy into Core. A TODO
  already plans a `LocalizationHandler`; until then, format on the platform or pass
  structured data up.

- **Core-12 🟠 · ✅ Closed-Fixed — Domain layer pinned for the whole process by the composition root.**
  → Epic: TASK-1
  `CoreAssembler` holds each interactor in a `lazy { … }` property and
  `InteractorDependencies` holds use-case *instances* (built once via the `by lazy`
  `coreAccess`), so interactors and use cases are process-lifetime singletons —
  preventing the on-demand domain-object lifetime the platforms want (see **Common-4**
  and **iOS-1**).
  **Fix:** `InteractorDependencies` now holds use-case *factories* (`() -> UseCase`),
  `CoreAccess` is a stateless factory bundle that can stay shared, the five
  `lazyXInteractor` properties are removed, and `coreRepositoryAssembler()` constructs
  a fresh interactor per call. Each interactor builds the use cases it needs and owns
  them, so an interactor + its use cases/stories live only as long as the repository
  that built it; services stay singletons. `:Core:` and `:Android:` compile.
  **Comment:** Safe because interactors are pure stateless wrappers — their `…State` /
  `activePath` properties are *cold* flow definitions with no construction-time side
  effects, so creating one starts nothing and re-creating one observes the same hot
  service `StateFlow`. `useCases.get(...)` and the story getters already build a fresh
  instance per call, so the whole chain above the services is now genuinely transient
  and released with the repository.

---

## Summary

**Resolved so far.** **Core-1** (reminder state data race) ✅, **Core-2** (initial
reminder sync lost on a replay-0 bus race) ✅, **Core-12** (domain
layer un-pinned for on-demand lifetime) ✅, **And-1** (graph rebuilt on rotation) ✅,
**iOS-1** 🗒️ closed as by-design (weak scoped lifetime; the model is now written
up in **Common-4**, with the Android side of it still open), **Common-1**
(= **iOS-4** / **And-8**) 🗒️ closed as by-design (the coarser whole-tab guard on the
task branch is intentional — on the Tasks tab we toast but never force-navigate; the
proposed `isCurrentRoot` symmetry would wrongly yank the user out of `TaskDetails`),
and **Common-3** (= **iOS-7** / **And-7**) ✅ (root/details screen duplication extracted
into shared highlight/scroll + dialog helpers per platform).

**iOS.** The remaining likely user-visible bug is the two-way tab feedback loop
(**iOS-3**) — the asymmetric navigation guard (**iOS-4** → **Common-1**) is now closed
as by-design. The rest is
cleanup: duplication between the Tasks root/details screens (**iOS-7** → **Common-3**)
is now fixed; remaining are duplicated/clumsy `TaskItem` construction (**iOS-8**), a misleading no-op
`TaskTreeRowsView` (**iOS-9**), `print` logging (**iOS-10**), and minor
naming/convention drift (**iOS-11/12**, plus dead `forceLoadTemplates` **iOS-5** →
**Common-2**).

**Android.** With **And-1** fixed (graph hoisted to `TaskBridgeApplication`), the
remaining concrete defects are **And-3** (hand-counted scroll offsets) and **And-4**
(progress and container share an icon), and **And-10** means Android reminders never
actually fire. The state-sharing inconsistency (**And-2**) is still open. Cleanup
parallels iOS: `ViewModelProvider.Factory` boilerplate (**And-6**); the duplicated
scroll/dialog wiring (**And-7** → **Common-3**) is now fixed; the duplicate `refreshTemplates`/dead
`forceLoadTemplates` (**And-5** → **Common-2**), inline FQNs (**And-9**), and a
per-recomposition tree walk (**And-11**).

**Core.** With **Core-1**, **Core-2**, and **Core-12** fixed, the open concurrency issue is
**Core-3**
(read-then-clear of the navigation message bypasses the actor and isn’t atomic).
**Core-4** (remote loads serialized by the command loop) and **Core-9** (no `close()`
→ scope + `HttpClient` leak) are the notable design issues; the rest is type-safety
around the untyped `RemoteResourceEntry.data` (**Core-7**), the pass-through layering
(**Core-8**), and cleanup (**Core-5/6/10/11**).

**Common (cross-platform).** Items described once in the **Common** section:
**Common-1** (asymmetric nav-message guard, = iOS-4 / And-8) 🗒️ closed as by-design —
on the Tasks tab we toast but never force-navigate, blink-in-place is the visible
screen's job; **Common-2** (dead
`forceLoadTemplates` + a “refresh” that just calls `loadTemplates`, = iOS-5 / And-5),
**Common-3** (root-vs-details screen duplication, = iOS-7 / And-7) ✅ now extracted into
shared per-platform helpers; and **Common-4**
(the domain-object lifetime model behind the iOS-1 resolution — services are the only
singletons, everything above is on-demand, caches live in Core managers; Core + iOS
done, Android weak-share remaining). Fix the duplications once per platform but in
lockstep; note Common-2 also ties into Core’s TTL-gated `RemoteResourceCommand.Load`.
