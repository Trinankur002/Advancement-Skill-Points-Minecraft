package com.trinankur.advancementskillpoints.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeDefinition;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeRegistry;
import net.minecraft.resources.Identifier;

import java.util.*;

public class PlayerProgressionData {
	private int availableSkillPoints;
	private int totalEarnedPoints;
	private final Map<String, Integer> attributeRanks = new HashMap<>();
	private final Set<Identifier> claimedAdvancements = new HashSet<>();

	public static final Codec<PlayerProgressionData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.INT.optionalFieldOf("available_points", 0).forGetter(PlayerProgressionData::getAvailableSkillPoints),
			Codec.INT.optionalFieldOf("total_earned_points", 0).forGetter(PlayerProgressionData::getTotalEarnedPoints),
			Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("attribute_ranks", Map.of())
				.forGetter(PlayerProgressionData::getAttributeRanksMap),
			Identifier.CODEC.listOf().optionalFieldOf("claimed_advancements", List.of())
				.forGetter(data -> new ArrayList<>(data.getClaimedAdvancements()))
		).apply(instance, PlayerProgressionData::fromCodec)
	);

	public PlayerProgressionData() {
		this.availableSkillPoints = 0;
		this.totalEarnedPoints = 0;
	}

	public PlayerProgressionData(
		int availablePoints,
		int totalEarned,
		Map<String, Integer> ranks,
		Collection<Identifier> claimed
	) {
		this.availableSkillPoints = Math.max(0, availablePoints);
		this.totalEarnedPoints = Math.max(0, totalEarned);
		if (ranks != null) {
			this.attributeRanks.putAll(ranks);
		}
		if (claimed != null) {
			this.claimedAdvancements.addAll(claimed);
		}
	}

	private static PlayerProgressionData fromCodec(
		int availablePoints,
		int totalEarned,
		Map<String, Integer> stringRanks,
		List<Identifier> claimed
	) {
		return new PlayerProgressionData(availablePoints, totalEarned, stringRanks, claimed);
	}

	public int getAvailableSkillPoints() {
		return availableSkillPoints;
	}

	public void setAvailableSkillPoints(int points) {
		this.availableSkillPoints = Math.max(0, points);
	}

	public int getTotalEarnedPoints() {
		return totalEarnedPoints;
	}

	public Map<String, Integer> getAttributeRanksMap() {
		return Collections.unmodifiableMap(attributeRanks);
	}

	public int getRank(Identifier attributeId) {
		if (attributeId == null) return 0;
		// Support both full identifier (namespace:path) and short path for backwards migration
		int rank = attributeRanks.getOrDefault(attributeId.toString(), -1);
		if (rank < 0) {
			rank = attributeRanks.getOrDefault(attributeId.getPath(), 0);
		}
		return Math.max(0, rank);
	}

	public int getRank(String key) {
		if (key == null) return 0;
		return Math.max(0, attributeRanks.getOrDefault(key, 0));
	}

	public void setRank(Identifier attributeId, int rank) {
		if (attributeId == null) return;
		AttributeDefinition def = AttributeRegistry.get(attributeId);
		int maxRank = (def != null) ? def.getMaxRank() : 20;
		int clamped = Math.clamp(rank, 0, maxRank);
		this.attributeRanks.put(attributeId.toString(), clamped);
		this.attributeRanks.put(attributeId.getPath(), clamped);
	}

	public Set<Identifier> getClaimedAdvancements() {
		return Collections.unmodifiableSet(claimedAdvancements);
	}

	public boolean hasClaimed(Identifier advancementId) {
		return claimedAdvancements.contains(advancementId);
	}

	public boolean claimAdvancement(Identifier advancementId, int points) {
		if (claimedAdvancements.contains(advancementId)) {
			return false;
		}
		claimedAdvancements.add(advancementId);
		if (points > 0) {
			this.availableSkillPoints += points;
			this.totalEarnedPoints += points;
		}
		return true;
	}

	public boolean canUpgrade(AttributeDefinition def) {
		if (def == null) return false;
		int currentRank = getRank(def.getId());
		int cost = def.getUpgradeCost(currentRank);
		return cost > 0 && availableSkillPoints >= cost && currentRank < def.getMaxRank();
	}

	public boolean upgrade(AttributeDefinition def) {
		if (!canUpgrade(def)) {
			return false;
		}
		int currentRank = getRank(def.getId());
		int cost = def.getUpgradeCost(currentRank);
		this.availableSkillPoints -= cost;
		setRank(def.getId(), currentRank + 1);
		return true;
	}

	public void resetAttributes() {
		for (String key : this.attributeRanks.keySet()) {
			this.attributeRanks.put(key, 0);
		}
		this.availableSkillPoints = this.totalEarnedPoints;
	}

	public void copyFrom(PlayerProgressionData other) {
		if (other == null) return;
		this.availableSkillPoints = other.availableSkillPoints;
		this.totalEarnedPoints = other.totalEarnedPoints;
		this.attributeRanks.clear();
		this.attributeRanks.putAll(other.attributeRanks);
		this.claimedAdvancements.clear();
		this.claimedAdvancements.addAll(other.claimedAdvancements);
	}
}
