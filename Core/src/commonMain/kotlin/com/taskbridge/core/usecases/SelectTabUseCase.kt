package com.taskbridge.core.usecases

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.AppTab

/**
 * Use case for selecting a tab.
 */
internal class SelectTabUseCase(
    private val assembler: CoreAssembler
) {
    suspend fun selectTab(tab: AppTab) {
        val story = assembler.stories.selectTab(assembler)
        story(tab)
    }
}
