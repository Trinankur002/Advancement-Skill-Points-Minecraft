package com.trinankur.advancementskillpoints.progression.attribute;

import com.trinankur.advancementskillpoints.AdvancementSkillPoints;
import net.minecraft.resources.Identifier;

import java.util.*;

public final class AttributeRegistry {
	private static final Map<Identifier, AttributeDefinition> ATTRIBUTES = new LinkedHashMap<>();

	private AttributeRegistry() {}

	public static synchronized void register(AttributeDefinition attribute) {
		Objects.requireNonNull(attribute, "AttributeDefinition cannot be null");
		ATTRIBUTES.put(attribute.getId(), attribute);
		AdvancementSkillPoints.LOGGER.debug("Registered attribute: {}", attribute.getId());
	}

	public static AttributeDefinition get(Identifier id) {
		return ATTRIBUTES.get(id);
	}

	public static AttributeDefinition getByPath(String path) {
		if (path == null) return null;
		for (Map.Entry<Identifier, AttributeDefinition> entry : ATTRIBUTES.entrySet()) {
			if (entry.getKey().getPath().equalsIgnoreCase(path) || entry.getKey().toString().equalsIgnoreCase(path)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public static Collection<AttributeDefinition> getAll() {
		return Collections.unmodifiableCollection(ATTRIBUTES.values());
	}

	public static List<AttributeDefinition> getGuiAttributes() {
		List<AttributeDefinition> list = new ArrayList<>();
		for (AttributeDefinition def : ATTRIBUTES.values()) {
			if (def.isVisibleInGui()) {
				list.add(def);
			}
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Dynamically calculates the total Skill Point budget required to max all default
	 * attributes that belong to the vanilla progression budget.
	 * Under default settings (4 attributes with maxRank 20 and cost=rank):
	 * 210 * 4 = 840 SP.
	 */
	public static int calculateVanillaProgressionBudget() {
		int budget = 0;
		for (AttributeDefinition def : ATTRIBUTES.values()) {
			if (def.belongsToVanillaBudget()) {
				budget += def.getTotalCostToMax();
			}
		}
		return Math.max(1, budget);
	}

	public static void clear() {
		ATTRIBUTES.clear();
	}
}

