# CLAUDE.md — AdventAI

Точка входа для Claude Code. Здесь — короткий обзор; детали в `.claude/*.md`.

## Роль

Ты Senior Android Developer и Product Designer уровня 2026 года. Работаешь над Android-приложением **AdventAI** для AI Advent Challenge #8 (агент создаётся поэтапно, по одному заданию в день).

## Что это за проект

Android-приложение на Jetpack Compose для работы с AI-агентами:
- главный экран — carousel со списком агентов;
- выбор агента → чат с агентом;
- настройки агента через bottom sheet (имя, тема диалога, модель, system prompt).

Ключевой принцип: **агент — отдельная сущность, а не голый вызов API из UI**. Логика запроса к LLM инкапсулирована в доменном слое (`AiAgent`).

## Стек

Kotlin · Jetpack Compose · Material 3 · Hilt · Retrofit/OkHttp · Gradle Kotlin DSL · DeepSeek / OpenRouter API.

## Карта модулей

- `app` — `MainActivity`, навигация (нижние вкладки: **Чат**, **Статистика**), экран статистики токенов и стоимости.
- `feature:home` — главный экран, carousel агентов.
- `feature:chat` — экран диалога (с индикатором контекстного окна и чипами токенов под ответами), настройки агента (включая демо-лимит окна и авто-обрезку истории), экраны-эксперименты (reasoning, temperature, model comparison, format).
- `core:model` — модели данных (`AgentConfig`, `AgentLlmModel` с лимитом окна и тарифом, `ChatRequestOptions`, `TokenUsage`, `ConversationTokenStat`, …).
- `core:domain` — `AiAgent`, use case, интерфейс репозитория.
- `core:data` — реализация репозитория, мапперы.
- `core:network` — Retrofit API (`DeepSeekApi`, `OpenRouterApi`), DTO, `NetworkModule`.
- `core:designsystem` — тема, цвета, общие UI-компоненты.
- `core:common` — `AppResult`, `AppError`, диспетчеры.

## Поток работы агента (Day 6 «Первый агент» ✅ · Day 7 «Контекст» ✅ · Day 8 «Токены» ✅)

```
ChatScreen → ChatViewModel.sendMessage()
          → AiAgent.ask(config, conversation)   // вся история, system prompt, оценка токенов + проверка окна
          → AiChatRepository (core:data)
          → DeepSeekApi / OpenRouterApi (Retrofit, chat/completions)
          → AgentAnswer(content, usage) → история и токены в Room → UI
```

Статус по дням:
- **Day 7** — история диалога персистится в Room и целиком уходит в LLM (агент «помнит» контекст).
- **Day 8** — подсчёт токенов: факт `usage` из API больше не теряется (сохраняется по сообщению), плюс локальная оценка до запроса (`TokenEstimator`), лимит контекстного окна и тариф у модели, демо-переполнение окна (ошибка `ContextOverflow`) и sliding-window авто-обрезка, вкладка «Статистика» (рост токенов и стоимости).

Ключевые файлы:
- `core/domain/src/main/java/com/example/core/domain/agent/AiAgent.kt`
- `core/domain/src/main/java/com/example/core/domain/agent/TokenEstimator.kt`
- `core/model/src/main/java/com/example/core/model/ai/AgentModels.kt`
- `feature/chat/src/main/java/com/example/feature/chat/presentation/viewmodel/ChatViewModel.kt`
- `app/src/main/java/com/example/adventai/ui/statistics/StatisticsScreen.kt`

## Правила

- Не переносить вызовы API напрямую в Compose — UI знает только про `ViewModel`.
- Не ломать модульные границы.
- Дизайн: сине-белый, скруглённая геометрия, цвета из `MaterialTheme.colorScheme`, без hardcoded-палитры. Подробности — `.claude/DESIGN_SYSTEM.md`.
- Не возвращать старые «дни заданий» в видимую навигацию.
- Не хранить API-ключи и секреты в md-файлах и в репозитории (ключи — в `local.properties`, оно в `.gitignore`).

## Проверка после изменений

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

## Справочные документы (`.claude/`)

- `PROJECT_CONTEXT.md` — расширенный контекст проекта, UI и правила работы.
- `DESIGN_SYSTEM.md` — дизайн-система (палитра, формы, типографика, motion).
- `SCREEN_REQUIREMENTS.md` — требования к экранам Home/Chat и слою агента.
- `AI_ADVENT_CHALLENGE_GUIDE.md` — формат и правила AI Advent Challenge #8.
- `AI_AGENT_TOOLS_AND_LLM_NOTES.md` — практический гайд: концепты LLM/агентов (контекст, память, токены, инструменты) применительно к коду AdventAI + приоритетный план улучшений.
