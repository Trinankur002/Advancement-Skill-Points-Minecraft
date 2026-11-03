package com.trinankur.advancementskillpoints.client;

import com.trinankur.advancementskillpoints.network.SyncProgressionPayload;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeDefinition;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class ClientProgressionData {
	private static int availablePoints = 0;
	private static int totalEarnedPoints = 0;
	private static final Map<String, Integer> RANKS = new HashMap<>();

	private ClientProgressionData() {}

	public static void update(SyncProgressionPayload payload) {
		availablePoints = payload.availablePoints();
		totalEarnedPoints = payload.totalEarnedPoints();
		RANKS.clear();
		if (payload.ranks() != null) {
			RANKS.putAll(payload.ranks());
		}
	}

	public static int getAvailablePoints() {
		return availablePoints;
	}

	public static int getTotalEarnedPoints() {
		return totalEarnedPoints;
	}

	public static int getRank(Identifier id) {
		if (id == null) return 0;
		int rank = RANKS.getOrDefault(id.toString(), -1);
		if (rank < 0) {
			rank = RANKS.getOrDefault(id.getPath(), 0);
		}
		return Math.max(0, rank);
	}

	public static int getRank(String key) {
		if (key == null) return 0;
		return Math.max(0, RANKS.getOrDefault(key, 0));
	}

	public static boolean canUpgrade(AttributeDefinition def) {
		if (def == null) return false;
		int currentRank = getRank(def.getId());
		int cost = def.getUpgradeCost(currentRank);
		return cost > 0 && availablePoints >= cost && currentRank < def.getMaxRank();
	}
}
