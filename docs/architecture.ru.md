# Архитектура TaskBridge

TaskBridge - пример Kotlin Multiplatform приложения, где бизнес-логика находится в общем модуле Core, а Android и iOS остаются тонкими нативными оболочками. Проект показывает общие доменные модели, реактивное состояние, платформенные обработчики возможностей и нативные UI-слои.

## Модули

| Модуль | Ответственность |
| --- | --- |
| `Core` | Общая Kotlin Multiplatform логика: модели, сервисы, use cases, stories, interactors, storage, network и event buses. |
| `Android` | Нативное Android-приложение на Jetpack Compose. Адаптирует Core interactors в Android repositories и отображает экраны. |
| `iOS` | Нативное SwiftUI-приложение. Адаптирует Core interactors в Swift repositories и превращает Core flows в Swift async streams. |
| `Platform_Handlers` | Платформенный модуль для возможностей, которые Core не может реализовать одинаково на всех платформах. |
| `docs` | Статическая документация и mock API данные, включая JSON шаблонов задач. |

## Общий поток

```text
Android Compose UI / SwiftUI
        |
Platform repositories
        |
Core interactors
        |
Use cases
        |
Stories
        |
Stateful and stateless services
        |
Storage, network clients, platform handlers, event buses
```

UI не обращается напрямую к storage, network или платформенным notification API. Он работает через repositories, а repositories делегируют операции Core interactors.

## Точка входа Core

`TaskBridge` - публичная точка входа в общую логику. Платформенные приложения создают ее с:

- `PlatformDependencies`, которые дают Core доступ к платформенной инфраструктуре, например к созданию базы данных.
- `CorePlatformHandlers`, которые дают Core доступ к возможностям с нативной реализацией, например к reminders.

После создания платформенный код получает interactors из `TaskBridge`:

- `navigationInteractor()`
- `templatesInteractor()`
- `tasksInteractor()`
- `remindersInteractor()`

Точка входа также предоставляет `eventEmitter`, через который платформенные handlers сообщают Core об изменениях на стороне платформы.

## Композиция

`CoreAssembler` - внутренний composition root. Он владеет:

- `CoreServiceLocator`, который лениво создает и хранит сервисы.
- `UseCaseContainer`, который резолвит public-to-Core use cases.
- `UserStoriesContainer`, который создает небольшие внутренние story operations.
- `CoreEventBuses` и `CoreEventEmitter`, которые возвращают платформенные события обратно в общую логику.

Так детали сборки остаются внутри Core, а Android и iOS получают компактный interactor API.

## Слои Core

### Interactors

Interactors - стабильный фасад для платформ. Они предоставляют flows и suspend functions, которые repositories могут вызывать без знания внутренней сборки Core.

### Use cases

Use cases координируют доменные операции и переводят данные сервисов в state models, удобные для платформ. Примеры: наблюдение за navigation state, операции с задачами, загрузка templates и управление reminders.

### Stories

Stories - небольшие внутренние операции. Они инкапсулируют конкретные действия: загрузить задачи, заменить поддерево задачи, выбрать tab, загрузить remote resource или запланировать reminder.

### Services

Services владеют долгоживущим состоянием или обрабатывают запросы:

- Stateful services используют реактивное состояние и обработку команд.
- Stateless services обрабатывают requests через последовательный mailbox loop.
- `AppStateService` владеет navigation state.
- `TasksService` владеет деревом задач и сохраняет изменения через `TaskStorageManager`.
- `RemindersService` синхронизирует reminder state Core с платформенными reminder handlers.
- `NetworkService` выполняет JSON network requests.
- `RemoteResourceService` хранит состояние remote resources, ошибки загрузки, timestamps, TTL metadata и информацию о запросах в процессе.

## Данные и платформенные возможности

Задачи сохраняются локально через общий Room database abstraction в Core. Платформенные database builders передаются через `PlatformDependencies`.

Шаблоны задач загружаются из JSON через общий network layer. Текущий template URL указывает на `docs/mock-api/templates.json` в GitHub repository, а Core применяет TTL перед повторной загрузкой.

Reminders - платформенная возможность. Core определяет общую модель reminders и orchestration, а Android и iOS предоставляют нативные reminder handlers. Handlers отправляют обновления обратно в Core через `CoreEventEmitter`.

## Платформенные слои

### Android

Android-приложение инициализирует `TaskBridge` в `MainActivity`, создает Android repository implementations вокруг Core interactors и отображает экраны на Jetpack Compose. Compose screens наблюдают Kotlin flows и вызывают repository methods для пользовательских действий.

### iOS

iOS-приложение инициализирует `TaskBridge` в `TaskBridgeApp`, создает Swift repository implementations вокруг Core interactors, внедряет их через SwiftUI environment values и превращает Core flows в `AsyncStream` values для SwiftUI view models.

## Замысел архитектуры

Архитектура оставляет доменные решения в общем Kotlin-коде, но позволяет каждой платформе иметь нативный UI и нативные integrations. Core владеет правилами, state transitions, storage orchestration, network loading и event coordination. Android и iOS фокусируются на presentation, lifecycle и platform APIs.
