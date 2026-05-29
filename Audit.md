# TaskBridge — Code Audit (Core / iOS / Android)

Scope: the Kotlin `Core` module (`Core/src/commonMain/**`) and the platform *app*
code (`iOS/TaskBridge/**`, `Android/src/androidMain/**`). `Platform_Handlers` and
build/config are out of scope.

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

Every finding currently carries `🔓 Open`. A short summary is at the bottom.

---

## Common (iOS + Android)

These are the same defect on both platforms; fix them in lockstep to keep the apps
aligned.

- **Common-1 🟠 · 🔓 Open — Asymmetric “already here?” check when routing app messages.**
  Both platforms handle the `reminderCreated` / `taskAdded` app messages in the same
  place (iOS `ContentView.handleNavigationMessage`, Android
  `MainActivity`’s message-collect block). The two branches use *different*
  granularity: the reminder branch early-returns on `isCurrentRoot(.reminders)`, but
  the task branch early-returns on merely `fetchCurrentTab() == .tasks`. So if you are
  on the Tasks tab but inside `TaskDetails` (not the root) when a root task is added
  elsewhere, the task branch returns early, never sets the navigation-destination
  message, and the “scroll & blink” highlight is lost when you return to the root. The
  task branch should test the *root* (`isCurrentRoot(.tasks)` /
  `isCurrentRoot(AppTab.TASKS)`), matching the reminder branch.
  → Platform entries: **iOS-4**, **And-8**.

- **Common-2 🟡 · 🔓 Open — `forceLoadTemplates()` is dead; “refresh” just calls `loadTemplates()`.**
  On both platforms the templates repository/interactor exposes `forceLoadTemplates()`
  but nothing calls it, and the view model’s `refreshTemplates()` just calls
  `loadTemplates()` — which is TTL-gated in Core (`RemoteResourceCommand.Load` skips
  when fresh, see Core), so a user-triggered “refresh” can be a silent no-op within the
  10-minute TTL. Wire refresh to `forceLoadTemplates()` (→ `RemoteResourceCommand.ForceLoad`)
  or remove the unused API on both sides.
  → Platform entries: **iOS-5**, **And-5**.

- **Common-3 🟠 · 🔓 Open — Tasks *root* and *details* screens are ~90% duplicated.**
  On each platform the root list screen and the details screen carry near-identical
  logic: observe-task-created, consume-pending-navigation-message, wait/scroll/blink to
  a row, and the rename + reminder (+ subtask) dialog/sheet wiring. iOS:
  `TasksRootView` vs `TaskDetailsView`. Android: `TasksRootScreen` vs
  `TaskDetailsScreen` (the scroll/blink + header-offset helper is duplicated into
  `RemindersRootScreen` too). Extract a shared highlight/scroll coordinator and a
  shared dialog/sheet block per platform, parameterized by scope id and a “find item”
  closure.
  → Platform entries: **iOS-7**, **And-7**.

---

## iOS

### Retain cycles & memory / object lifetime

- **iOS-1 🟠 · 🔓 Open — `RepositoriesStorage` caches repositories with `weak var`.**
  `RepositoriesStorage.swift` stores every repository in a `private weak var …Ref`
  and recreates it in the getter when the ref is `nil`. Repositories are therefore
  *not* stable singletons: as soon as no view/VM strongly holds one (e.g. you leave
  the Tasks screen and the `TasksViewModel` deinits), the impl is deallocated, and the
  next access builds a brand-new `…RepositoryImpl` with a fresh `createStream`
  bridge (a new `Task` + `Collector` + `AsyncStream`). The Core source flows are hot
  conflated `StateFlow`s (`BaseStatefulService` exposes `MutableStateFlow.asStateFlow()`),
  so the *re-subscription itself is cheap* — it just replays the current value, with no
  recomputation at the service. The real problem is therefore identity instability and
  churn, not performance: object identity silently changes, every recreate tears down
  and rebuilds the Swift⇄Kotlin bridge, and this diverges from Android (`by lazy` =
  strong singletons, see `RepositoriesStorage.kt`). Recommendation: hold the
  repositories strongly (lazy `let`-style cache) so identity is stable.

- **iOS-2 🟡 · 🔓 Open — `ScrollBlinkHighlighter.uncancellableSleep` outlives its `.task`.**
  `scrollToAndBlink` is launched from `.task` modifiers in `TasksRootView` /
  `TaskDetailsView` / `RemindersRootView`, but it `await`s `uncancellableSleep`
  (`FlowUtils.swift`), which by design ignores cancellation. If the user navigates
  away mid-blink, the owning task is cancelled yet the highlighter keeps mutating its
  `@Published highlightedId` / `opacity` for several more seconds. Harmless visually,
  but it is state churn on a view that is gone and defeats structured cancellation.

### Race conditions

- **iOS-3 🟠 · 🔓 Open — Two-way tab binding feedback loop in `ContentView`.**
  `.onChange(of: selectedTab)` pushes `navRepo.selectTab(tab:)` (async) while a
  separate `.task` observes `navRepo.currentTab` and writes `selectedTab` back. The
  loop is only broken by the `selectedTab != tab` guard. Because both directions hop
  through async boundaries, rapid tab taps can interleave (Core emits an older tab
  after a newer local change), producing visible flicker or a “bounce” to the previous
  tab. A single source of truth (drive the `TabView` selection directly from
  `currentTab`) would remove the loop.

### Logical problems

- **iOS-4 🟠 · 🔓 Open — Asymmetric “already here?” check in `handleNavigationMessage`.**
  → See **Common-1**. iOS location: `ContentView.handleNavigationMessage`
  (`isCurrentRoot(.reminders)` vs `fetchCurrentTab() == .tasks`).

- **iOS-5 🟡 · 🔓 Open — `forceLoadTemplates()` is dead; `refreshTemplates()` is mislabeled.**
  → See **Common-2**. iOS location: `TaskTemplatesRepository` (protocol + impl) and
  `TaskTemplatesViewModel.refreshTemplates()` (pull-to-refresh).

- **iOS-6 🟡 · 🔓 Open — Checkbox `Toggle` binding ignores its new value.**
  In `TaskDetailsView.detailsCard`, the toggle is
  `Binding(get: { task.isChecked }, set: { _ in viewModel.toggleCheckbox(task:) })`.
  The setter discards the incoming value and unconditionally toggles. This relies on
  SwiftUI never invoking the setter with the current value; any extra setter call (or
  a stale `task` capture) flips the state the wrong way.

### Reuse / duplication (possible generics)

- **iOS-7 🟠 · 🔓 Open — `TasksRootView` and `TaskDetailsView` are ~90% duplicated.**
  → See **Common-3**. iOS location: `TasksRootView` vs `TaskDetailsView`
  (`observeTaskCreatedMessages`, `consumePendingNavigationMessage`, `waitForTaskRow`,
  the rename/reminder sheets, the highlight `onChange` block).

- **iOS-8 🟡 · 🔓 Open — Duplicated `TaskItem` construction.**
  `TasksViewModel.newTask(...)` and `TaskTemplatesViewModel.makeTask(...)` both
  switch over the type and rebuild a `TaskItem` per case. Additionally,
  `toggleCheckbox` / `renameTask` rebuild the *entire* `TaskItem` field-by-field
  (Android uses `task.copy(...)`), which is verbose and breaks silently whenever a
  field is added. Extract a single `TaskItem` factory + a Swift `copy`-style helper.

- **iOS-9 🟡 · 🔓 Open — `TaskTreeRowsView` is a misleading no-op wrapper.**
  It forwards every argument to `TaskRowView` and adds nothing, and despite the
  “Tree”/`depth` naming it never recurses into `task.children` (`depth` is always 0).
  Either make it actually render the subtree or delete it and use `TaskRowView`
  directly.

### Bad practices

- **iOS-10 🟡 · 🔓 Open — `print()` logging in production code.**
  `iOSReminderHandler`, `RemindersViewModel`, and `TasksViewModel` log via `print`.
  Use `os.Logger`/`OSLog` with categories so logs are filterable and stripped in
  release.

- **iOS-11 🟡 · 🔓 Open — Type name `iOSReminderHandler` violates UpperCamelCase.**
  Swift type names should be `IOSReminderHandler` (or `AppleReminderHandler`).

- **iOS-12 🟡 · 🔓 Open — Inconsistent VM conventions.**
  `TaskTemplatesViewModel` is a non-`final class` while the other VMs are `final`, and
  it omits the `hasLoaded` guard used by `loadTasks` / `loadReminders`, so its
  `loadTemplates()` can fire redundantly. Align the conventions.

---

## Android

### Object lifetime / lifecycle

- **And-1 🔴 · 🔓 Open — Dependency graph rebuilt on every configuration change.**
  `MainActivity.onCreate` constructs `PlatformDependencies`, `TaskBridge`, and
  `RepositoriesStorage.create(taskBridge, lifecycleScope)` directly. On rotation /
  config change `onCreate` runs again, so the *entire* Core graph, all repositories,
  and all flow subscriptions are torn down and rebuilt, and the `stateIn` scope
  (`lifecycleScope`) is cancelled. This drops in-memory state and re-does Core
  bootstrap work on every rotation. Hoist the graph into `Application` (or a retained
  holder / DI scope) and only read it from the Activity.

### Race conditions / state sharing

- **And-2 🟠 · 🔓 Open — Inconsistent state-sharing strategy across view models.**
  `TasksViewModel` and `TaskTemplatesViewModel` wrap the repository flow with
  `stateIn(viewModelScope, WhileSubscribed(5000))`, but `RemindersViewModel` exposes
  the raw `repository.remindersState` — which `RemindersRepositoryImpl` already shared
  with `stateIn(scope = <Activity lifecycleScope>, WhileSubscribed(5000))`. The result
  is three different sharing lifetimes for three equivalent screens (VM scope vs
  Activity scope). Pick one pattern; tying reminder sharing to the Activity scope also
  feeds And-1. (Note: the divergence is *not* cosmetic — the Core `StateFlow` is hot,
  but each interactor re-wraps it with cold `.map { … }.distinctUntilChanged()`
  operators (see `TasksInteractor.kt`), so the cold chain is re-collected per collector
  and the choice of where to `stateIn` genuinely changes the collection lifetime.)

- **And-3 🟠 · 🔓 Open — Hand-counted header offsets for scroll targeting are fragile.**
  `tasksListHeaderOffset(state)` / `remindersListHeaderOffset(state)` re-derive the
  number of leading `item {}`s (header + loading + error + empty) to convert a data
  index into a `LazyColumn` index for `animateScrollToItem`. This duplicates the
  conditional layout in the composable and silently drifts the moment the list’s
  header items change → scroll-to-wrong-row. Prefer stable item `key`s + a key-based
  scroll, or build the index from the same list that renders the items.

### Logical problems

- **And-4 🟠 · 🔓 Open — Progress and Container tasks share the same icon.**
  In `TaskRow.kt`, `taskIcon` maps both `TaskType.PROGRESS` and `TaskType.CONTAINER`
  to `Icons.AutoMirrored.Filled.List`, so the two types are visually
  indistinguishable. iOS distinguishes them (`chart.bar` vs `folder`). Give each type
  a distinct icon.

- **And-5 🟡 · 🔓 Open — `refreshTemplates()` duplicates `loadTemplates()`; `forceLoadTemplates()` is dead.**
  → See **Common-2**. Android location: `TaskTemplatesViewModel.refreshTemplates()`
  (identical `try/catch` body to `loadTemplates()`, wired to the “Refresh” button) and
  the unused `TaskTemplatesRepository.forceLoadTemplates()`.

- **And-8 🟠 · 🔓 Open — Same asymmetric navigation check as iOS-4.**
  → See **Common-1**. Android location: `MainActivity` message-collect block
  (`isCurrentRoot(AppTab.REMINDERS)` vs `fetchCurrentTab() == AppTab.TASKS`).

- **And-11 🟡 · 🔓 Open — `state.findTask(...)` runs over the whole tree on each recomposition.**
  `TaskDetailsScreen` calls `state.findTask(TaskId(taskId))` directly in the composable
  body, re-walking the tree on every recomposition. Hoist it behind
  `remember(state, taskId) { … }`.

### Reuse / duplication (possible generics)

- **And-6 🟠 · 🔓 Open — `ViewModelProvider.Factory` boilerplate copy-pasted 4×.**
  The same anonymous `object : ViewModelProvider.Factory { … as T }` block appears in
  `TasksRootScreen`, `TaskDetailsScreen`, `RemindersRootScreen`, and
  `TemplatesRootDestination`. Replace with a single reusable helper, e.g.
  `viewModelFactory { TasksViewModel(...) }` (androidx) or a small inline generic.

- **And-7 🟠 · 🔓 Open — Screen-level scroll/blink/dialog wiring duplicated.**
  → See **Common-3**. Android location: `TasksRootScreen` vs `TaskDetailsScreen`
  (rename/reminder/subtask dialogs); the `scrollToAndBlink` + `findItemIndex` +
  header-offset pattern is also duplicated into `RemindersRootScreen`.

### Bad practices

- **And-9 🟡 · 🔓 Open — Inline fully-qualified names despite existing imports.**
  `MainActivity` matches `com.taskbridge.core.models.messages.AppMessage.ReminderCreated`
  inline even though `AppMessage` is imported, and `TaskDetailsScreen` uses
  `com.taskbridge.core.models.tasks.TaskId(taskId)` inline. Import and use short names.

- **And-10 🟠 · 🔓 Open — `AndroidReminderHandler` does no real scheduling.**
  It persists reminders to `SharedPreferences` and emits updates, but the actual
  `AlarmManager`/`WorkManager` scheduling is left as `TODO`, so scheduled reminders
  never fire on Android (iOS uses real `UNUserNotificationCenter`). JSON
  encode/decode also happens on the calling coroutine’s thread. Implement real
  scheduling and move (de)serialization off the main dispatcher.

---

## Core

Analysis of the shared Kotlin module only (`Core/src/commonMain/**`).

### Race conditions

- **Core-1 🔴 · 🔓 Open — Non-atomic `updateState` raced by two coroutines in `RemindersService`.**
  `BaseStatefulService.updateState` does `_data.value = reducer(_data.value)` — a
  non-atomic read-modify-write. `TasksService`, `AppStateService`, and
  `RemoteResourceService` only mutate from the single command-loop coroutine, so they
  are safe. **`RemindersService` is not:** it calls `updateState` both from the command
  loop (`performLoad` / `performSchedule` / `performAction`) *and* from the separate
  `reminderEvents.events().collect { … }` coroutine, both running on
  `Dispatchers.Default` (multi-threaded). Two concurrent RMWs lose updates — e.g. a
  command’s `it.copy(isLoading = true)` built from a stale snapshot clobbers a
  freshly-arrived `reminders` list, so reminders transiently vanish or `isLoading`
  sticks. Fix: make `updateState` atomic with `MutableStateFlow.update { }`.

- **Core-2 🟠 · 🔓 Open — Initial reminders sync can be dropped (replay=0 bus race).**
  `RemindersService.init` launches two coroutines on the same scope: one collects
  `reminderEvents.events()`, the other emits the initial `RemindersUpdated`.
  `CoreEventBus` is a `MutableSharedFlow(replay = 0, …)`, so if the emitter runs before
  the collector has subscribed, the initial event is lost and the reminder list stays
  empty until a manual `loadReminders()`. It self-heals because the view models call
  `loadReminders()` on screen appear, but it is a real startup ordering race. Give the
  bus `replay = 1` for this use, or seed initial state directly instead of
  round-tripping through the bus.

- **Core-3 🟠 · 🔓 Open — Non-atomic read-then-clear in `consumeNavigationDestinationMessage`.**
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
  `handleCommand` invokes `command.loader()` (the network fetch) inline, and
  `BaseStatefulService` pulls commands one-at-a-time from its channel. While one URL is
  loading, every other `RemoteResource` command — other URLs, force-loads, even the
  fresh-cache metadata update — waits behind it. Only the templates URL is used today,
  so it is latent, but the per-URL `LOADING` guard implies a concurrency the design
  does not actually deliver. Launch each load in a child coroutine keyed by URL rather
  than awaiting it in the command loop.

- **Core-5 🟡 · 🔓 Open — `createdAt` is reset on every edit.**
  `TaskItem.toEntities` stamps `createdAtMillis = now` (and `updatedAtMillis = now`) for
  *every* node on *every* upsert, so the original creation time is lost whenever a tree
  is replaced (a TODO acknowledges this). Harmless while timestamps are unused in the
  UI, but the persisted field is misleading. Preserve existing `createdAtMillis` in the
  storage layer.

- **Core-6 🟡 · 🔓 Open — Tree reconstruction/deletion is not cycle-safe and not range-safe.**
  `List<TaskEntity>.toTaskTree` (`buildTask`) and `TaskStorageManager.collectSubtreeIds`
  (`collect`) recurse into children without a visited-guard, so a malformed DB with a
  parent cycle recurses infinitely / stack-overflows. `toTaskTree` also silently drops
  children of non-`CONTAINER` rows, and `TaskProgress(it)`’s `require(value in 0..100)`
  throws on an out-of-range persisted progress (the `TaskType.valueOf` fallback covers
  bad types but not bad progress). Add visited-guards and clamp/validate on read.

### Reuse / generics / type-safety

- **Core-7 🟠 · 🔓 Open — Unchecked generic casts erase type safety around remote resources.**
  `RemoteResourceEntry.data` is `Any?`, so `TemplatesInteractor` must do
  `entry.data as? List<TaskTemplateDto>` (erased — only checks “is a `List`”), and
  `JsonRequestManager` stores `Deferred<Any?>` keyed by URL alone: two callers
  requesting the same URL with different reified `T` share one deferred, and the second
  `await() as T` casts the first’s result → `ClassCastException`. Fine for the single
  templates URL today, but it is a latent erasure bug. Consider a typed resource key
  (or storing the decoder alongside the URL) so the resource cache is type-safe.

- **Core-8 🟡 · 🔓 Open — “Containers” don’t cache, and the layering is mostly pass-through.**
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
  `CoreServiceLocator` creates a `CoroutineScope(SupervisorJob() + Dispatchers.Default)`
  and `HttpJsonClient` creates a Ktor `HttpClient`, but neither `TaskBridge` nor the
  assembler exposes a way to cancel/close them. Each `TaskBridge` instance leaks a live
  scope (with the services’ command loops and collectors) and an unclosed `HttpClient`.
  Benign for a single app-lifetime instance, but it compounds **And-1** (a new
  `TaskBridge` per Android rotation) into a per-config-change scope + HttpClient leak.
  Add a `close()` that cancels the scope and closes the client.

- **Core-10 🟡 · 🔓 Open — `println` logging in production Core.**
  `RemindersService` (and others) log via `println`, which on Android lands in
  logcat/stdout unfiltered and is not strippable in release. Route through an injectable
  logger / `expect` logging API. (Mirrors iOS-10.)

- **Core-11 🟡 · 🔓 Open — User-facing strings built in Core with a raw timestamp.**
  `MessagesInteractor.toAppMessage` composes the toast text in Core
  (`"Reminder created: … • ${triggerAtMillis.formatAsInstantText()}"`) using
  `Instant.toString()` — a raw ISO-8601 UTC string (e.g. `2026-05-29T12:00:00Z`) — which
  leaks an unformatted timestamp into the UI and bakes English copy into Core. A TODO
  already plans a `LocalizationHandler`; until then, format on the platform or pass
  structured data up.

---

## Summary

**iOS.** The biggest structural issue is **iOS-1**: repositories are cached with
`weak var`, so they are not stable singletons and silently re-subscribe to Core
flows. The two-task tab feedback loop (**iOS-3**) and the asymmetric navigation guard
(**iOS-4**) are the most likely user-visible bugs. The rest is cleanup: heavy
duplication between the Tasks root/details screens (**iOS-7**), duplicated/clumsy
`TaskItem` construction (**iOS-8**), a misleading no-op `TaskTreeRowsView` (**iOS-9**),
`print` logging (**iOS-10**), and minor naming/convention drift (**iOS-11/12**, plus
dead `forceLoadTemplates` **iOS-5**).

**Android.** The standout is **And-1**: the whole dependency graph is rebuilt in
`MainActivity.onCreate`, so it is recreated on every rotation — this should move to
`Application`/a retained scope, and it interacts with the inconsistent state-sharing
in **And-2**. **And-3** (hand-counted scroll offsets) and **And-4** (progress and
container share an icon) are concrete defects, and **And-10** means Android reminders
never actually fire. Cleanup parallels iOS: `ViewModelProvider.Factory` boilerplate
(**And-6**), duplicated scroll/dialog wiring (**And-7**), the duplicate
`refreshTemplates`/dead `forceLoadTemplates` (**And-5**), inline FQNs (**And-9**), and
a per-recomposition tree walk (**And-11**).

**Core.** The headline is **Core-1**: `RemindersService` mutates a non-atomic
`updateState` from two concurrent coroutines on `Dispatchers.Default`, a genuine data
race that can drop reminder updates — the one-line fix is `MutableStateFlow.update {}`
in the base service. The other concurrency issues are **Core-2** (initial reminder
sync lost on a replay-0 bus race) and **Core-3** (read-then-clear of the navigation
message bypasses the actor and isn’t atomic). **Core-4** (remote loads serialized by
the command loop) and **Core-9** (no `close()` → scope + `HttpClient` leak, which
compounds And-1) are the notable design issues; the rest is type-safety around the
untyped `RemoteResourceEntry.data` (**Core-7**), over-eager “containers”/layering
(**Core-8**), and cleanup (**Core-5/6/10/11**).

**Common (cross-platform).** Three findings are the *same* defect on both apps and are
described once in the **Common** section: **Common-1** (asymmetric nav-message guard,
= iOS-4 / And-8), **Common-2** (dead `forceLoadTemplates` + a “refresh” that just calls
`loadTemplates`, = iOS-5 / And-5), and **Common-3** (root-vs-details screen
duplication, = iOS-7 / And-7). Fix each once per platform but in lockstep so the two
stay aligned; note Common-2 also ties into Core’s TTL-gated `RemoteResourceCommand.Load`.
