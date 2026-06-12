# AdventAI

Android-приложение на **Jetpack Compose** для работы с AI-агентами. Учебный проект
**AI Advent Challenge #8**: агент создаётся поэтапно, по одному заданию в день.

Ключевой принцип: **агент — отдельная доменная сущность (`AiAgent`), а не голый вызов API из UI**.
Логика запроса к LLM инкапсулирована в доменном слое, UI знает только про `ViewModel`.

## Что внутри

- Список агентов → чат с агентом → настройки агента (bottom sheet: имя, тема, модель, system prompt).
- Две нижние вкладки: **Чат** и **Статистика** (токены и стоимость диалогов).
- История диалога персистится в Room и целиком уходит в LLM (агент «помнит» контекст).

## Стек

Kotlin · Jetpack Compose · Material 3 · Hilt · Retrofit/OkHttp · Room · Gradle Kotlin DSL ·
DeepSeek / OpenRouter API.

## Карта модулей

| Модуль | Назначение |
|---|---|
| `app` | `MainActivity`, навигация (вкладки Чат/Статистика), экран статистики токенов |
| `feature:home` | главный экран, carousel агентов |
| `feature:chat` | экран диалога, настройки агента, экраны-эксперименты |
| `core:model` | модели данных (`AgentConfig`, `AgentLlmModel`, `TokenUsage`, …) |
| `core:domain` | `AiAgent`, `TokenEstimator`, use case, интерфейсы репозиториев |
| `core:data` | реализация репозиториев, Room, мапперы |
| `core:network` | Retrofit API (`DeepSeekApi`, `OpenRouterApi`), DTO |
| `core:designsystem` | тема, цвета, общие UI-компоненты |
| `core:common` | `AppResult`, `AppError`, диспетчеры |

## Прогресс по дням

- **Day 6** — первый агент (вопрос → ответ через доменный слой).
- **Day 7** — контекст: история диалога персистится в Room и целиком уходит в LLM.
- **Day 8** — токены и контекстное окно — подробно в [docs/DAY_08_TOKENS.md](docs/DAY_08_TOKENS.md).

### Текущий день — Day 8: работа с токенами

- Подсчёт токенов: факт `usage` из API (вход/ответ/всего + cache hit/miss) сохраняется по сообщению,
  плюс локальная оценка до запроса (`TokenEstimator`).
- Контекстное окно: лимит и тариф у модели, демо-лимит + sliding-window авто-обрезка, ошибка
  переполнения `ContextOverflow`.
- Визуализация: индикатор заполнения окна, приглушение сообщений «вне контекста» с разделителем,
  вкладка «Статистика» (рост токенов и стоимости).

Полное описание и сценарии для демо: **[docs/DAY_08_TOKENS.md](docs/DAY_08_TOKENS.md)**.

## Сборка

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

API-ключи не хранятся в репозитории — задаются в `local.properties` (в `.gitignore`),
например `DEEPSEEK_API_KEY=...`.

## Документация

- [`CLAUDE.md`](CLAUDE.md) — точка входа, краткий обзор проекта.
- [`.claude/`](.claude) — контекст проекта, дизайн-система, требования к экранам, гайд по LLM/агентам.
- [`docs/DAY_08_TOKENS.md`](docs/DAY_08_TOKENS.md) — задание Day 8 и его реализация.
