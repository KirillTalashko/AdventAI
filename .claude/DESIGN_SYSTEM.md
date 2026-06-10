# AdventAI Design System

## Visual Direction

AdventAI uses a clean 2026 product style: blue and white, soft surfaces, rounded geometry, readable typography, and calm motion.

The UI should feel like a polished AI product, not a demo or learning screen.

## Palette

Use `MaterialTheme.colorScheme` as the source of truth.

Current direction:
- primary: saturated blue;
- secondary: lighter product blue;
- tertiary: sky blue;
- background: very light blue-white;
- surface: white;
- surfaceContainer: pale blue;
- outlineVariant: soft blue-gray.

Avoid:
- random green, yellow, pink, purple, brown, orange accents;
- hardcoded colors in feature screens;
- gradients that introduce a new visual language.

If a hardcoded color is needed for a decorative effect, keep it in the blue-white range and explain why.

## Shape

Use rounded, modern geometry.

Rules:
- main cards: `RoundedCornerShape(30.dp)` or higher;
- inner colored visual blocks: rounded on all corners, never only top corners unless explicitly required;
- icon buttons: `CircleShape`;
- bottom sheets: rounded top corners, usually `34.dp`;
- text fields: `RoundedCornerShape(20.dp)` or higher.

Every colored panel inside a card must be clipped with the same or compatible rounded shape.

## Typography

Use Material typography.

Rules:
- screen headings should be short;
- compact panels should not use oversized hero typography;
- long titles must use `maxLines` and `TextOverflow.Ellipsis`;
- body text should stay readable and not overflow containers.

## Icons

Prefer standard Material-style vector drawables for common actions:
- back;
- close;
- settings;
- send;
- add.

Do not create custom Canvas icons for standard actions unless there is a specific design reason.

## Motion

Motion should be smooth and subtle.

Carousel:
- use `LazyRow`;
- use `rememberSnapFlingBehavior`;
- calculate item focus from `LazyListState.layoutInfo`;
- animate scale, alpha, lift, and slight rotation with spring animations;
- center item should feel focused, side items should be smaller and calmer.

Avoid:
- abrupt jumps;
- layout shifts caused by text or keyboard;
- animations that make the UI feel playful when the screen is operational.

## Safe Areas

All top bars must respect system UI:
- use `statusBarsPadding()` for custom top bars;
- do not draw back buttons under system time or camera cutouts.

Bottom input areas:
- use `navigationBarsPadding()`;
- apply `imePadding()` to the bottom composer only, not to the entire screen, unless the whole layout is intentionally designed for it.

## Compose UI Rules

Use:
- `MaterialTheme.colorScheme`;
- `BoxWithConstraints` for responsive sizing;
- `maxLines` and `TextOverflow.Ellipsis`;
- `clip(shape)` before drawing colored rounded blocks;
- adaptive dimensions based on screen width/height.

Avoid:
- fixed dimensions that break on small screens;
- nested cards inside cards;
- visible labels explaining how to use basic UI;
- default rectangular Material components when the rest of the screen uses custom rounded product surfaces.
