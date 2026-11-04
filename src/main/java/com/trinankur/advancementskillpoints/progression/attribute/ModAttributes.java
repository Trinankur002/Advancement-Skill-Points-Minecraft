package com.trinankur.advancementskillpoints.progression.attribute;

import com.trinankur.advancementskillpoints.AdvancementSkillPoints;
import com.trinankur.advancementskillpoints.config.ModConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ModAttributes {
	public static final Identifier STRENGTH_ID = AdvancementSkillPoints.id("strength");
	public static final Identifier VITALITY_ID = AdvancementSkillPoints.id("vitality");
	public static final Identifier ENDURANCE_ID = AdvancementSkillPoints.id("endurance");
	public static final Identifier STAMINA_ID = AdvancementSkillPoints.id("stamina");

	public static final Identifier VITALITY_MODIFIER_ID = AdvancementSkillPoints.id("vitality_bonus");

	private ModAttributes() {}

	public static void init() {
		ModConfig config = ModConfig.get();
		int maxRank = config.defaultMaxRank;

		// 1. Strength
		AttributeRegistry.register(new AttributeDefinition(
			STRENGTH_ID,
			"Strength",
			"Increases unarmed damage and multiplies weapon attack power.",
			maxRank,
			0,
			rank -> rank,
			rank -> {
				double unarmed = config.strengthUnarmedBase + (config.strengthUnarmedPerRank * rank);
				double mult = 1.0 + (config.strengthWeaponMultiplierPerRank * rank);
				return String.format("Unarmed: %.1f | Weapons: %.1fx", unarmed, mult);
			},
			true,
			true,
			null // Strength melee/projectile damage is handled authoritatively via combat mixins
		));

		// 2. Vitality
		AttributeRegistry.register(new AttributeDefinition(
			VITALITY_ID,
			"Vitality",
			"Increases player maximum health and knockback resistance.",
			maxRank,
			0,
			rank -> rank,
			rank -> {
				int totalHp = (int) Math.round(config.vitalityHealthBase + (config.vitalityHealthPerRank * rank));
				int bonusHp = (int) Math.round(config.vitalityHealthPerRank * rank);
				return String.format("+%d HP (%d Total / %d Hearts)", bonusHp, totalHp, totalHp / 2);
			},
			true,
			true,
			(player, rank) -> {
				AttributeInstance healthInstance = player.getAttribute(Attributes.MAX_HEALTH);
				if (healthInstance != null) {
					float prevMax = player.getMaxHealth();
					float currentHealth = player.getHealth();
					healthInstance.removeModifier(VITALITY_MODIFIER_ID);
					if (rank > 0) {
						double amount = rank * config.vitalityHealthPerRank;
						healthInstance.addOrReplacePermanentModifier(
							new AttributeModifier(VITALITY_MODIFIER_ID, amount, AttributeModifier.Operation.ADD_VALUE)
						);
					}
					float newMax = player.getMaxHealth();
					if (Math.abs(newMax - prevMax) > 0.01f && prevMax > 0.0f && player.isAlive()) {
						float ratio = currentHealth / prevMax;
						player.setHealth(Math.min(newMax, newMax * ratio));
					} else if (currentHealth > newMax) {
						player.setHealth(newMax);
					}
				}
			}
		));

		// 3. Endurance
		AttributeRegistry.register(new AttributeDefinition(
			ENDURANCE_ID,
			"Endurance",
			"Reduces hunger exhaustion from sprinting, jumping, and actions.",
			maxRank,
			0,
			rank -> rank,
			rank -> {
				double reduction = Math.min(config.enduranceMaxReduction, config.enduranceReductionPerRank * rank);
				return String.format("-%d%% Exhaustion", (int) Math.round(reduction * 100));
			},
			true,
			true,
			null // Exhaustion reduction applied in PlayerMixin
		));

		// 4. Stamina
		AttributeRegistry.register(new AttributeDefinition(
			STAMINA_ID,
			"Stamina",
			"Expands max hunger pool and increases food restoration.",
			maxRank,
			0,
			rank -> rank,
			rank -> {
				int bonusCap = rank / 2;
				int totalFood = config.staminaFoodBase + bonusCap;
				int foodRestoreBonus = rank / 5;
				return String.format("+%d Max Food (%d Total) | +%d Restore", bonusCap, totalFood, foodRestoreBonus);
			},
			true,
			true,
			(player, rank) -> {
				if (player.getFoodData() instanceof StaminaFoodData staminaFood) {
					int maxFood = config.staminaFoodBase + (rank / 2);
					staminaFood.advancementSkillPoints$setMaxFoodLevel(maxFood);
					staminaFood.advancementSkillPoints$setFoodRestorationBonus(rank / 5);
				}
			}
		));
	}
}

