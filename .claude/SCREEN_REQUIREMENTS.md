# AdventAI Screen Requirements

## General Requirements

All screens must:
- work on small and large Android devices;
- avoid overlap with system bars;
- avoid text overflow;
- use the shared blue-white design system;
- keep business logic outside Compose UI;
- compile after changes with `.\gradlew.bat :app:compileDebugKotlin --console=plain`.

Do not restore old learning-day screens to the visible navigation unless explicitly requested.

## Home Screen

Module:
- `feature:home`

Primary file:
- `feature/home/src/main/java/com/example/feature/home/presentation/screen/HomeScreen.kt`

Purpose:
- show available AI agents;
- let the user open an existing agent;
- let the user start creating a new agent.

Current requirements:
- horizontal carousel based on `LazyRow`;
- centered card is larger than side cards;
- snap behavior with `rememberSnapFlingBehavior`;
- scale is calculated from item distance to viewport center;
- cards must be adaptive to screen width and height;
- first and last cards should center correctly through content padding;
- no visible `AdventAI` title in a top bar;
- no `Day 6` label;
- old challenge days must not be visible.

Cards:
- `Визовый специалист`;
- `Добавить агента`.

Card design:
- blue-white palette;
- no rectangular gray corners;
- no standard `Card` artifacts if they create visible rectangular edges;
- inner visual blocks must be rounded on all corners;
- text must use `maxLines` and ellipsis.

## Chat Screen

Module:
- `feature:chat`

Primary file:
- `feature/chat/src/main/java/com/example/feature/chat/presentation/screen/ChatScreen.kt`

Purpose:
- let the user chat with the selected AI agent;
- show agent responses;
- open agent settings.

Top panel requirements:
- custom top panel inside `ChatScreen`;
- must use `statusBarsPadding()`;
- back button on the left;
- settings button on the top right;
- settings icon should be a standard Material-style vector icon;
- no self-made Canvas settings icon;
- no overlap with system time/status bar.

Message list:
- should fill available space between top panel and input composer;
- should scroll normally;
- should keep latest message visible after sending.

Input composer:
- anchored at the bottom;
- app activity uses `android:windowSoftInputMode="adjustNothing"` so Compose owns IME positioning;
- uses `navigationBarsPadding()`;
- uses `imePadding()` only on the composer/bottom area;
- message list may use `imeNestedScroll()` for smoother keyboard animation;
- must not make the whole screen jump when keyboard opens;
- send action should be available from keyboard IME action and send button.

Settings bottom sheet:
- opens from the settings icon;
- height around 80% of screen;
- rounded top corners;
- includes:
  - agent name;
  - dialog theme;
  - model selector;
  - system prompt.

Model selector:
- should use rounded selectable cards;
- should not be a plain dropdown unless explicitly requested.

## Agent Logic

Agent logic should stay separate from UI.

Relevant files:
- `core/model/src/main/java/com/example/core/model/ai/AgentModels.kt`
- `core/domain/src/main/java/com/example/core/domain/agent/AiAgent.kt`
- `feature/chat/src/main/java/com/example/feature/chat/presentation/viewmodel/ChatViewModel.kt`

Requirements:
- UI sends user input to `ViewModel`;
- `ViewModel` delegates to `AiAgent`;
- `AiAgent` builds request context from agent config;
- repository/network layer performs API calls.

Do not move API calls directly into Compose.

## Navigation

Relevant files:
- `app/src/main/java/com/example/adventai/navigation/AppNavHost.kt`
- `feature/home/src/main/java/com/example/feature/home/presentation/navigation/HomeNavigation.kt`
- `feature/chat/src/main/java/com/example/feature/chat/presentation/navigation/ChatNavigation.kt`

Requirements:
- home is the start screen;
- selecting an agent navigates to chat route;
- chat route receives agent id;
- back from chat returns to home;
- global top bar should not be shown on home or chat if custom screen bars are used.
