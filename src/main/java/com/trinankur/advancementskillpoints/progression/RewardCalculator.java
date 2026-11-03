package com.trinankur.advancementskillpoints.progression;

import com.trinankur.advancementskillpoints.progression.reward.AdvancementRewardRegistry;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;

/**
 * Retained for backwards compatibility with Phase 1 callers.
 * Delegates to the Phase 2 mathematical AdvancementRewardRegistry.
 */
public final class RewardCalculator {
	private RewardCalculator() {}

	public static int calculatePoints(AdvancementHolder holder, AdvancementTree tree) {
		if (holder == null) return 1;
		return AdvancementRewardRegistry.getReward(holder.id());
	}
}
