# AdventAI — Day 6

Android-приложение на **Jetpack Compose** для работы с AI-агентами. Учебный проект
**AI Advent Challenge #8** (агент создаётся поэтапно, по одному заданию в день).

Эта ветка — снимок **Day 6 «Первый агент»**: агент оформлен как отдельная доменная сущность
(`AiAgent`) и отвечает на запрос (вопрос → ответ), а не дёргает API напрямую из UI.

## Что внутри (Day 6)

- Главный экран — carousel агентов → чат с агентом → настройки агента (bottom sheet: имя, тема,
  модель, system prompt).
- Агент — **отдельная сущность в доменном слое**; UI знает только про `ViewModel`.
- Провайдеры: **DeepSeek** и **OpenRouter** (в т.ч. бесплатные модели).
- Диалог одно-ходовый: история ещё не сохраняется (это появляется в Day 7).

## Стек

Kotlin · Jetpack Compose · Material 3 · Hilt · Retrofit/OkHttp · Gradle Kotlin DSL ·
DeepSeek / OpenRouter API.

## Карта модулей

| Модуль | Назначение |
|---|---|
| `app` | `MainActivity`, навигация |
| `feature:home` | главный экран, carousel агентов |
| `feature:chat` | экран диалога, настройки агента, экраны-эксперименты |
| `core:model` | модели данных (`AgentConfig`, `AgentLlmModel`, `ChatRequestOptions`, …) |
| `core:domain` | `AiAgent`, use case, интерфейс репозитория |
| `core:data` | реализация репозитория, мапперы |
| `core:network` | Retrofit API (`DeepSeekApi`, `OpenRouterApi`), DTO |
| `core:designsystem` | тема, цвета, общие UI-компоненты |
| `core:common` | `AppResult`, `AppError`, диспетчеры |

## Поток работы агента

```
ChatScreen → ChatViewModel.sendMessage()
          → AiAgent.ask(config, userRequest)   // валидация, system prompt, выбор провайдера
          → AiChatRepository (core:data)
          → DeepSeekApi / OpenRouterApi (Retrofit, chat/completions)
          → AgentAnswer → UI
```

## Сборка

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

API-ключи не хранятся в репозитории — задаются в `local.properties` (в `.gitignore`),
например `DEEPSEEK_API_KEY=...`.

## Документация

- [`CLAUDE.md`](CLAUDE.md) — точка входа, краткий обзор проекта.
- [`.claude/`](.claude) — контекст проекта, дизайн-система, требования к экранам, гайд по LLM/агентам.

> Дальше по дням: **Day 7** — персист истории диалога (Room, контекст), **Day 8** — подсчёт токенов
> и контекстное окно.
