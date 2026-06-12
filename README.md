# AdventAI — Day 7

Android-приложение на **Jetpack Compose** для работы с AI-агентами. Учебный проект
**AI Advent Challenge #8** (агент создаётся поэтапно, по одному заданию в день).

Эта ветка — снимок **Day 7 «Контекст»**: история диалога **персистится в Room** и целиком
уходит в LLM, поэтому агент «помнит» беседу между запусками приложения.

## Что внутри (Day 7)

- Агент — отдельная доменная сущность (`AiAgent`); UI знает только про `ViewModel`.
- **Память диалога:** сообщения сохраняются в SQLite (Room), `AiAgent.ask(config, conversation)`
  получает всю историю и отправляет её в LLM с ролями `system`/`user`/`assistant`.
- У одного агента — **несколько диалогов («тем»)** с переключением (как чаты в ChatGPT).
- Нижняя навигация: **Чат · Документы · Готовность** (продуктовые поверхности под визовый сценарий).
- Провайдеры: **DeepSeek** и **OpenRouter** (в т.ч. бесплатные модели).

## Стек

Kotlin · Jetpack Compose · Material 3 · Hilt · Retrofit/OkHttp · **Room** · Gradle Kotlin DSL ·
DeepSeek / OpenRouter API.

## Карта модулей

| Модуль | Назначение |
|---|---|
| `app` | `MainActivity`, навигация (вкладки Чат/Документы/Готовность) |
| `feature:home` | главный экран, carousel агентов |
| `feature:chat` | экран диалога, переключение диалогов, настройки агента |
| `core:model` | модели данных (`AgentConfig`, `AgentLlmModel`, `Conversation`, …) |
| `core:domain` | `AiAgent`, `ChatHistoryRepository`, use case |
| `core:data` | репозитории, **Room** (`AdventAiDatabase`, DAO), мапперы |
| `core:network` | Retrofit API (`DeepSeekApi`, `OpenRouterApi`), DTO |
| `core:designsystem` | тема, цвета, общие UI-компоненты |
| `core:common` | `AppResult`, `AppError`, диспетчеры |

## Поток работы агента

```
ChatScreen → ChatViewModel.sendMessage()
          → AiAgent.ask(config, conversation)   // вся история + system prompt
          → AiChatRepository (core:data)
          → DeepSeekApi / OpenRouterApi (Retrofit, chat/completions)
          → AgentAnswer → история в Room → UI
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

> Дальше по дням: **Day 8** — подсчёт токенов (вход/история/ответ), лимит контекстного окна,
> sliding window и вкладка «Статистика».
