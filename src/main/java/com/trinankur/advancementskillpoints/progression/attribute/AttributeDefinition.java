package com.trinankur.advancementskillpoints.progression.attribute;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.IntFunction;

public class AttributeDefinition {
	private final Identifier id;
	private final String displayName;
	private final String description;
	private final int maxRank;
	private final int defaultRank;
	private final IntFunction<Integer> costFunction;
	private final IntFunction<String> effectFormatter;
	private final boolean belongsToVanillaBudget;
	private final boolean visibleInGui;
	private final BiConsumer<ServerPlayer, Integer> applyHandler;

	public AttributeDefinition(
		Identifier id,
		String displayName,
		String description,
		int maxRank,
		int defaultRank,
		IntFunction<Integer> costFunction,
		IntFunction<String> effectFormatter,
		boolean belongsToVanillaBudget,
		boolean visibleInGui,
		BiConsumer<ServerPlayer, Integer> applyHandler
	) {
		this.id = id;
		this.displayName = displayName;
		this.description = description;
		this.maxRank = Math.max(1, maxRank);
		this.defaultRank = Math.clamp(defaultRank, 0, this.maxRank);
		this.costFunction = costFunction != null ? costFunction : rank -> rank;
		this.effectFormatter = effectFormatter != null ? effectFormatter : rank -> "Rank " + rank;
		this.belongsToVanillaBudget = belongsToVanillaBudget;
		this.visibleInGui = visibleInGui;
		this.applyHandler = applyHandler;
	}

	public Identifier getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public int getMaxRank() {
		return maxRank;
	}

	public int getDefaultRank() {
		return defaultRank;
	}

	public int getUpgradeCost(int currentRank) {
		if (currentRank >= maxRank) {
			return -1;
		}
		// nextRank = currentRank + 1
		int nextRank = currentRank + 1;
		return Math.max(1, costFunction.apply(nextRank));
	}

	public int getTotalCostToMax() {
		int total = 0;
		for (int r = defaultRank + 1; r <= maxRank; r++) {
			total += Math.max(1, costFunction.apply(r));
		}
		return total;
	}

	public String formatEffect(int rank) {
		return effectFormatter.apply(rank);
	}

	public boolean belongsToVanillaBudget() {
		return belongsToVanillaBudget;
	}

	public boolean isVisibleInGui() {
		return visibleInGui;
	}

	public void apply(ServerPlayer player, int rank) {
		if (applyHandler != null && player != null) {
			applyHandler.accept(player, rank);
		}
	}
}

