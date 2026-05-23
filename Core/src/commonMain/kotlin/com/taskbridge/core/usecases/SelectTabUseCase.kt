package com.taskbridge.core.usecases

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.AppTab

/**
 * Use case for selecting a tab.
 * Uses the assembler to access needed stories.
 */
public class SelectTabUseCase internal constructor(
    private val assembler: CoreAssembler
) {
    suspend operator fun invoke(tab: AppTab) {
        val story = assembler.stories.selectTab(assembler)
        story(tab)
    }
}
