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
- **Day 9** — сжатие истории (summary) — подробно в
  [docs/DAY_09_CONTEXT_COMPRESSION.md](docs/DAY_09_CONTEXT_COMPRESSION.md).

### Текущий день — Day 9: управление контекстом через сжатие

- **Rolling summary**: последние N сообщений уходят «как есть», старшее сворачивается в summary
  пачками (отдельным запросом к DeepSeek Flash) и подставляется в запрос вместо полной истории.
- Summary хранится **отдельно** от сообщений (Room v5), история на экране остаётся полной — сжимается
  только то, что реально уходит в модель; в промпте суммаризатора требуем сохранять факты.
- В чате: тумблер сжатия + карточка «Сжатая память» + индикатор экономии «сжато: −P%».
- **A/B-экран**: один сценарий прогоняется без сжатия и со сжатием — токены и «вспомнил ли имя»
  показываются рядом (качество сохранено, токенов меньше).

Полное описание и сценарии для демо:
**[docs/DAY_09_CONTEXT_COMPRESSION.md](docs/DAY_09_CONTEXT_COMPRESSION.md)**.

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
- [`docs/DAY_09_CONTEXT_COMPRESSION.md`](docs/DAY_09_CONTEXT_COMPRESSION.md) — задание Day 9 и его реализация.
