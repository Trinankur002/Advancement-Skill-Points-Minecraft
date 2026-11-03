package com.trinankur.advancementskillpoints.progression.reward;

import com.trinankur.advancementskillpoints.AdvancementSkillPoints;
import com.trinankur.advancementskillpoints.config.ModConfig;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeRegistry;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;

import java.util.*;

public final class AdvancementRewardRegistry {
	private static final Map<Identifier, Integer> REWARD_SNAPSHOT = new HashMap<>();
	private static int totalVanillaPointsAllocated = 0;
	private static boolean initialized = false;

	private AdvancementRewardRegistry() {}

	public static synchronized void recalculate(ServerAdvancementManager manager) {
		if (manager == null) return;
		REWARD_SNAPSHOT.clear();

		Collection<AdvancementHolder> all = manager.getAllAdvancements();
		ModConfig config = ModConfig.get();

		// Separate vanilla vs modded advancements that have valid DisplayInfo
		List<AdvancementHolder> vanillaAdvancements = new ArrayList<>();
		Map<String, List<AdvancementHolder>> moddedNamespaces = new HashMap<>();

		for (AdvancementHolder holder : all) {
			if (holder.value().display().isEmpty()) {
				// Skip recipe unlocks and internal non-display triggers
				continue;
			}

			String namespace = holder.id().getNamespace();
			if ("minecraft".equals(namespace)) {
				vanillaAdvancements.add(holder);
			} else {
				if (!config.excludedNamespaces.contains(namespace)) {
					moddedNamespaces.computeIfAbsent(namespace, k -> new ArrayList<>()).add(holder);
				}
			}
		}

		// 1. Allocate Vanilla Budget using Largest-Remainder Algorithm
		int vanillaBudget = AttributeRegistry.calculateVanillaProgressionBudget();
		Map<Identifier, Integer> vanillaRewards = allocateLargestRemainder(vanillaAdvancements, vanillaBudget, config);
		REWARD_SNAPSHOT.putAll(vanillaRewards);

		totalVanillaPointsAllocated = 0;
		for (int pts : vanillaRewards.values()) {
			totalVanillaPointsAllocated += pts;
		}

		// 2. Allocate Modded Namespaces using Diminishing Budget Formula
		for (Map.Entry<String, List<AdvancementHolder>> entry : moddedNamespaces.entrySet()) {
			List<AdvancementHolder> list = entry.getValue();
			int n = list.size();
			int modBudget = calculateModdedBudget(n, config);
			Map<Identifier, Integer> modRewards = allocateLargestRemainder(list, modBudget, config);
			REWARD_SNAPSHOT.putAll(modRewards);
		}

		initialized = true;
		AdvancementSkillPoints.LOGGER.info(
			"Advancement reward allocation complete. Vanilla budget: {} SP allocated across {} advancements (Total: {} SP). Modded namespaces: {}",
			vanillaBudget, vanillaAdvancements.size(), totalVanillaPointsAllocated, moddedNamespaces.size()
		);
	}

	public static int calculateModdedBudget(int n, ModConfig config) {
		if (n <= 0) return 0;
		double scale = config.moddedBudgetScale;
		double halfLife = config.moddedBudgetHalfLife;
		return (int) Math.floor(scale * (double) n / ((double) n + halfLife));
	}

	public static int getReward(Identifier id) {
		return REWARD_SNAPSHOT.getOrDefault(id, 0);
	}

	public static int getTotalVanillaPointsAllocated() {
		return totalVanillaPointsAllocated;
	}

	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * Distributes an exact integer budget across a list of advancements using the
	 * Largest-Remainder (Hare-Niemeyer) method with deterministic tie-breaking.
	 */
	public static Map<Identifier, Integer> allocateLargestRemainder(
		List<AdvancementHolder> advancements,
		int totalBudget,
		ModConfig config
	) {
		Map<Identifier, Integer> result = new HashMap<>();
		if (advancements.isEmpty() || totalBudget <= 0) {
			for (AdvancementHolder h : advancements) {
				result.put(h.id(), 0);
			}
			return result;
		}

		// Calculate total weight
		int totalWeight = 0;
		Map<Identifier, Integer> weights = new HashMap<>();
		for (AdvancementHolder h : advancements) {
			int w = getWeight(h, config);
			weights.put(h.id(), w);
			totalWeight += w;
		}

		if (totalWeight <= 0) {
			for (AdvancementHolder h : advancements) {
				result.put(h.id(), 0);
			}
			return result;
		}

		// Calculate integer base and fractional remainder
		class Candidate {
			final Identifier id;
			final int weight;
			int baseReward;
			final double remainder;

			Candidate(Identifier id, int weight, int baseReward, double remainder) {
				this.id = id;
				this.weight = weight;
				this.baseReward = baseReward;
				this.remainder = remainder;
			}
		}

		List<Candidate> candidates = new ArrayList<>();
		int allocatedSum = 0;

		for (AdvancementHolder h : advancements) {
			int w = weights.get(h.id());
			double exactQuota = ((double) totalBudget * (double) w) / (double) totalWeight;
			int base = (int) Math.floor(exactQuota);
			double rem = exactQuota - (double) base;
			candidates.add(new Candidate(h.id(), w, base, rem));
			allocatedSum += base;
		}

		int surplus = totalBudget - allocatedSum;

		// Deterministic sort:
		// 1. Remainder descending
		// 2. Weight descending (favor challenges/goals)
		// 3. Identifier alphabetical ascending (guarantees identical result everywhere)
		candidates.sort((c1, c2) -> {
			int cmp = Double.compare(c2.remainder, c1.remainder);
			if (cmp != 0) return cmp;
			int wcmp = Integer.compare(c2.weight, c1.weight);
			if (wcmp != 0) return wcmp;
			return c1.id.toString().compareTo(c2.id.toString());
		});

		for (int i = 0; i < surplus && i < candidates.size(); i++) {
			candidates.get(i).baseReward += 1;
		}

		for (Candidate c : candidates) {
			result.put(c.id, c.baseReward);
		}

		return result;
	}

	private static int getWeight(AdvancementHolder holder, ModConfig config) {
		DisplayInfo display = holder.value().display().orElse(null);
		if (display == null) return config.taskFrameWeight;
		AdvancementType type = display.getType();
		if (type == AdvancementType.CHALLENGE) return config.challengeFrameWeight;
		if (type == AdvancementType.GOAL) return config.goalFrameWeight;
		return config.taskFrameWeight;
	}
}

