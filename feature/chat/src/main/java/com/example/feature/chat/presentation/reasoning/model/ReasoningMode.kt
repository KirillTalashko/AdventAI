package com.example.feature.chat.presentation.reasoning.model

import androidx.annotation.StringRes
import com.example.feature.chat.R

enum class ReasoningMode(
    @param:StringRes val titleResId: Int,
    @param:StringRes val chipTitleResId: Int
) {
    Direct(
        titleResId = R.string.reasoning_direct_answer_title,
        chipTitleResId = R.string.reasoning_mode_direct
    ),
    StepByStep(
        titleResId = R.string.reasoning_step_by_step_answer_title,
        chipTitleResId = R.string.reasoning_mode_step_by_step
    ),
    PromptGenerated(
        titleResId = R.string.reasoning_generated_prompt_title,
        chipTitleResId = R.string.reasoning_mode_prompt_generated
    ),
    ExpertGroup(
        titleResId = R.string.reasoning_expert_answer_title,
        chipTitleResId = R.string.reasoning_mode_expert_group
    )
}
