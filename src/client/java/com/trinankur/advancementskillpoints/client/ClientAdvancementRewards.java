package com.trinankur.advancementskillpoints.client;

import com.trinankur.advancementskillpoints.progression.reward.AdvancementRewardRegistry;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class ClientAdvancementRewards {
	private static final Map<Identifier, Integer> REWARDS = new HashMap<>();

	private ClientAdvancementRewards() {}

	public static void update(Map<String, Integer> stringRewards) {
		REWARDS.clear();
		if (stringRewards != null) {
			for (Map.Entry<String, Integer> entry : stringRewards.entrySet()) {
				Identifier id = Identifier.tryParse(entry.getKey());
				if (id != null) {
					REWARDS.put(id, entry.getValue());
				}
			}
		}
	}

	public static int getReward(AdvancementHolder holder) {
		if (holder == null) return 0;
		return getReward(holder.id(), holder.value().display().orElse(null));
	}

	public static int getReward(Identifier id, DisplayInfo display) {
		if (id == null) return 0;

		// 1. Check synced server rewards
		Integer synced = REWARDS.get(id);
		if (synced != null && synced > 0) {
			return synced;
		}

		// 2. Check AdvancementRewardRegistry (shared JVM in singleplayer)
		int regReward = AdvancementRewardRegistry.getReward(id);
		if (regReward > 0) {
			return regReward;
		}

		// 3. Fallback based on DisplayInfo type (Tasks, Goals, Challenges)
		if (display != null) {
			AdvancementType type = display.getType();
			if (type == AdvancementType.CHALLENGE) return 3;
			if (type == AdvancementType.GOAL) return 2;
			return 1;
		}

		return 0;
	}
}

