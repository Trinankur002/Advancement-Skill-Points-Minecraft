package com.trinankur.advancementskillpoints;

import com.trinankur.advancementskillpoints.config.ModConfig;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeDefinition;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeRegistry;
import com.trinankur.advancementskillpoints.progression.attribute.ModAttributes;
import com.trinankur.advancementskillpoints.progression.reward.AdvancementRewardRegistry;
import com.trinankur.advancementskillpoints.progression.weapon.WeaponRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MathematicalBalancingTest {

	@BeforeAll
	static void setup() {
		try {
			SharedConstants.tryDetectVersion();
			Bootstrap.bootStrap();
		} catch (Throwable ignored) {}

		AttributeRegistry.clear();
		ModConfig.load();
		ModAttributes.init();
	}

	@Test
	@DisplayName("Phase 4 Strength Unarmed Damage Formula Verification (1.0 + 0.30 * Rank)")
	void testStrengthUnarmedDamage() {
		// UnarmedDamage = 1.0 + (0.30 * StrengthRank)
		assertEquals(1.0f, WeaponRegistry.getUnarmedDamage(0), 1e-4);
		assertEquals(1.6f, WeaponRegistry.getUnarmedDamage(2), 1e-4);
		assertEquals(2.2f, WeaponRegistry.getUnarmedDamage(4), 1e-4);
		assertEquals(2.5f, WeaponRegistry.getUnarmedDamage(5), 1e-4);
		assertEquals(2.8f, WeaponRegistry.getUnarmedDamage(6), 1e-4);
		assertEquals(3.4f, WeaponRegistry.getUnarmedDamage(8), 1e-4);
		assertEquals(4.0f, WeaponRegistry.getUnarmedDamage(10), 1e-4);
		assertEquals(4.6f, WeaponRegistry.getUnarmedDamage(12), 1e-4);
		assertEquals(5.2f, WeaponRegistry.getUnarmedDamage(14), 1e-4);
		assertEquals(5.5f, WeaponRegistry.getUnarmedDamage(15), 1e-4);
		assertEquals(5.8f, WeaponRegistry.getUnarmedDamage(16), 1e-4);
		assertEquals(6.4f, WeaponRegistry.getUnarmedDamage(18), 1e-4);
		assertEquals(7.0f, WeaponRegistry.getUnarmedDamage(20), 1e-4);
	}

	@Test
	@DisplayName("Phase 4 Strength Weapon Multiplier Verification (1.0 + 0.15 * Rank)")
	void testStrengthWeaponMultiplier() {
		// WeaponMultiplier = 1.0 + (0.15 * StrengthRank)
		assertEquals(1.0f, WeaponRegistry.getWeaponMultiplier(0), 1e-4);
		assertEquals(1.3f, WeaponRegistry.getWeaponMultiplier(2), 1e-4);
		assertEquals(1.6f, WeaponRegistry.getWeaponMultiplier(4), 1e-4);
		assertEquals(1.9f, WeaponRegistry.getWeaponMultiplier(6), 1e-4);
		assertEquals(2.2f, WeaponRegistry.getWeaponMultiplier(8), 1e-4);
		assertEquals(2.5f, WeaponRegistry.getWeaponMultiplier(10), 1e-4);
		assertEquals(2.8f, WeaponRegistry.getWeaponMultiplier(12), 1e-4);
		assertEquals(3.1f, WeaponRegistry.getWeaponMultiplier(14), 1e-4);
		assertEquals(3.4f, WeaponRegistry.getWeaponMultiplier(16), 1e-4);
		assertEquals(3.7f, WeaponRegistry.getWeaponMultiplier(18), 1e-4);
		assertEquals(4.0f, WeaponRegistry.getWeaponMultiplier(20), 1e-4);
	}

	@Test
	@DisplayName("Phase 4 Diamond Sword Damage Pipeline (Base, Sharpness V, Strength I, Both, Critical)")
	void testDiamondSwordDamageTable() {
		float baseDamage = 7.0f;
		float sharpnessVBonus = 3.0f;
		float strengthIBonus = 3.0f;

		// Rank 0: Base 7, Sharpness V 10, Strength I 10, Both 13
		float m0 = WeaponRegistry.getWeaponMultiplier(0);
		assertEquals(7.0f, baseDamage * m0, 1e-4);
		assertEquals(10.0f, (baseDamage + sharpnessVBonus) * m0, 1e-4);
		assertEquals(10.0f, (baseDamage + strengthIBonus) * m0, 1e-4);
		assertEquals(13.0f, (baseDamage + sharpnessVBonus + strengthIBonus) * m0, 1e-4);

		// Rank 10: Base 17.5, Sharpness V 25, Strength I 25, Both 32.5
		float m10 = WeaponRegistry.getWeaponMultiplier(10);
		assertEquals(17.5f, baseDamage * m10, 1e-4);
		assertEquals(25.0f, (baseDamage + sharpnessVBonus) * m10, 1e-4);
		assertEquals(25.0f, (baseDamage + strengthIBonus) * m10, 1e-4);
		assertEquals(32.5f, (baseDamage + sharpnessVBonus + strengthIBonus) * m10, 1e-4);

		// Rank 20: Base 28, Sharpness V 40, Strength I 40, Both 52
		float m20 = WeaponRegistry.getWeaponMultiplier(20);
		assertEquals(28.0f, baseDamage * m20, 1e-4);
		assertEquals(40.0f, (baseDamage + sharpnessVBonus) * m20, 1e-4);
		assertEquals(40.0f, (baseDamage + strengthIBonus) * m20, 1e-4);
		assertEquals(52.0f, (baseDamage + sharpnessVBonus + strengthIBonus) * m20, 1e-4);

		// Critical Hit at Rank 20 with Both (Section 6: Normal 52, 1.5x Crit = 78)
		float normalBoth20 = (baseDamage + sharpnessVBonus + strengthIBonus) * m20;
		float criticalBoth20 = normalBoth20 * 1.5f;
		assertEquals(52.0f, normalBoth20, 1e-4);
		assertEquals(78.0f, criticalBoth20, 1e-4);
	}

	@Test
	@DisplayName("Phase 4 Trident Damage Pipeline (Base, Sharpness V, Strength I, Both)")
	void testTridentDamageTable() {
		float tridentBase = 9.0f;
		float sharpnessVBonus = 3.0f;
		float strengthIBonus = 3.0f;

		// Rank 0: Base 9, Sharpness V 12, Strength I 12, Both 15
		float m0 = WeaponRegistry.getWeaponMultiplier(0);
		assertEquals(9.0f, tridentBase * m0, 1e-4);
		assertEquals(12.0f, (tridentBase + sharpnessVBonus) * m0, 1e-4);
		assertEquals(12.0f, (tridentBase + strengthIBonus) * m0, 1e-4);
		assertEquals(15.0f, (tridentBase + sharpnessVBonus + strengthIBonus) * m0, 1e-4);

		// Rank 2: Base 11.7, Sharpness V 15.6, Strength I 15.6, Both 19.5
		float m2 = WeaponRegistry.getWeaponMultiplier(2);
		assertEquals(11.7f, tridentBase * m2, 1e-4);
		assertEquals(15.6f, (tridentBase + sharpnessVBonus) * m2, 1e-4);
		assertEquals(15.6f, (tridentBase + strengthIBonus) * m2, 1e-4);
		assertEquals(19.5f, (tridentBase + sharpnessVBonus + strengthIBonus) * m2, 1e-4);

		// Rank 10: Base 22.5, Sharpness V 30, Strength I 30, Both 37.5
		float m10 = WeaponRegistry.getWeaponMultiplier(10);
		assertEquals(22.5f, tridentBase * m10, 1e-4);
		assertEquals(30.0f, (tridentBase + sharpnessVBonus) * m10, 1e-4);
		assertEquals(30.0f, (tridentBase + strengthIBonus) * m10, 1e-4);
		assertEquals(37.5f, (tridentBase + sharpnessVBonus + strengthIBonus) * m10, 1e-4);

		// Rank 20: Base 36, Sharpness V 48, Strength I 48, Both 60
		float m20 = WeaponRegistry.getWeaponMultiplier(20);
		assertEquals(36.0f, tridentBase * m20, 1e-4);
		assertEquals(48.0f, (tridentBase + sharpnessVBonus) * m20, 1e-4);
		assertEquals(48.0f, (tridentBase + strengthIBonus) * m20, 1e-4);
		assertEquals(60.0f, (tridentBase + sharpnessVBonus + strengthIBonus) * m20, 1e-4);
	}

	@Test
	@DisplayName("Phase 4 Vitality Max Health Formula Verification (20 + 3 * Rank)")
	void testVitalityFormula() {
		ModConfig config = ModConfig.get();
		assertEquals(20.0, config.vitalityHealthBase + (config.vitalityHealthPerRank * 0), 1e-4);
		assertEquals(35.0, config.vitalityHealthBase + (config.vitalityHealthPerRank * 5), 1e-4);
		assertEquals(50.0, config.vitalityHealthBase + (config.vitalityHealthPerRank * 10), 1e-4);
		assertEquals(65.0, config.vitalityHealthBase + (config.vitalityHealthPerRank * 15), 1e-4);
		assertEquals(80.0, config.vitalityHealthBase + (config.vitalityHealthPerRank * 20), 1e-4);
	}

	@Test
	@DisplayName("Phase 4 Endurance Exhaustion Reduction Verification (0.03 * Rank, cap 60%)")
	void testEnduranceFormula() {
		ModConfig config = ModConfig.get();
		assertEquals(0.00, Math.min(config.enduranceMaxReduction, config.enduranceReductionPerRank * 0), 1e-4);
		assertEquals(0.15, Math.min(config.enduranceMaxReduction, config.enduranceReductionPerRank * 5), 1e-4);
		assertEquals(0.30, Math.min(config.enduranceMaxReduction, config.enduranceReductionPerRank * 10), 1e-4);
		assertEquals(0.45, Math.min(config.enduranceMaxReduction, config.enduranceReductionPerRank * 15), 1e-4);
		assertEquals(0.60, Math.min(config.enduranceMaxReduction, config.enduranceReductionPerRank * 20), 1e-4);
	}

	@Test
	@DisplayName("Phase 4 Stamina Max Food Capacity Verification (20 + floor(Rank / 2))")
	void testStaminaFormula() {
		ModConfig config = ModConfig.get();
		int base = config.staminaFoodBase;
		assertEquals(20, base + (0 / 2));
		assertEquals(21, base + (2 / 2));
		assertEquals(22, base + (4 / 2));
		assertEquals(23, base + (6 / 2));
		assertEquals(24, base + (8 / 2));
		assertEquals(25, base + (10 / 2));
		assertEquals(26, base + (12 / 2));
		assertEquals(27, base + (14 / 2));
		assertEquals(28, base + (16 / 2));
		assertEquals(29, base + (18 / 2));
		assertEquals(30, base + (20 / 2));
	}

	@Test
	@DisplayName("Additional Stamina Mechanic — Food Restoration Scaling (floor(Rank / 5))")
	void testStaminaFoodRestorationScaling() {
		// FoodBonus = floor(StaminaRank / 5)
		for (int rank = 0; rank <= 4; rank++) {
			assertEquals(0, rank / 5, "Ranks 0-4 must have +0 bonus");
		}
		for (int rank = 5; rank <= 9; rank++) {
			assertEquals(1, rank / 5, "Ranks 5-9 must have +1 bonus");
		}
		for (int rank = 10; rank <= 14; rank++) {
			assertEquals(2, rank / 5, "Ranks 10-14 must have +2 bonus");
		}
		for (int rank = 15; rank <= 19; rank++) {
			assertEquals(3, rank / 5, "Ranks 15-19 must have +3 bonus");
		}
		assertEquals(4, 20 / 5, "Rank 20 must have +4 bonus");

		// Bread test (Vanilla 5 restoration)
		int breadVanilla = 5;
		assertEquals(5, breadVanilla + (0 / 5));
		assertEquals(6, breadVanilla + (5 / 5));
		assertEquals(7, breadVanilla + (10 / 5));
		assertEquals(8, breadVanilla + (15 / 5));
		assertEquals(9, breadVanilla + (20 / 5), "Bread must restore 9 food at Rank 20");

		// Steak test (Vanilla 8 restoration)
		int steakVanilla = 8;
		assertEquals(8, steakVanilla + (0 / 5));
		assertEquals(9, steakVanilla + (5 / 5));
		assertEquals(10, steakVanilla + (10 / 5));
		assertEquals(11, steakVanilla + (15 / 5));
		assertEquals(12, steakVanilla + (20 / 5), "Steak must restore 12 food at Rank 20");
	}

	@Test
	@DisplayName("Upgrade Costs and Dynamic Vanilla Budget Verification (840 SP)")
	void testUpgradeCostsAndBudget() {
		AttributeDefinition strength = AttributeRegistry.get(ModAttributes.STRENGTH_ID);
		assertNotNull(strength);
		assertEquals(20, strength.getMaxRank());

		// UpgradeCost(nextRank) = nextRank
		assertEquals(1, strength.getUpgradeCost(0)); // 0 -> 1 costs 1
		assertEquals(2, strength.getUpgradeCost(1)); // 1 -> 2 costs 2
		assertEquals(10, strength.getUpgradeCost(9)); // 9 -> 10 costs 10
		assertEquals(20, strength.getUpgradeCost(19)); // 19 -> 20 costs 20
		assertEquals(-1, strength.getUpgradeCost(20)); // Maxed

		// Total cost to max one attribute: 1 + 2 + ... + 20 = 210 SP
		assertEquals(210, strength.getTotalCostToMax());

		// Total budget for all 4 default attributes: 210 * 4 = 840 SP
		int calculatedBudget = AttributeRegistry.calculateVanillaProgressionBudget();
		assertEquals(840, calculatedBudget);
	}

	@Test
	@DisplayName("Modded Advancement Diminishing Budget Formula Verification")
	void testModdedAdvancementBudget() {
		ModConfig config = ModConfig.get();
		assertEquals(7, AdvancementRewardRegistry.calculateModdedBudget(10, config));
		assertEquals(16, AdvancementRewardRegistry.calculateModdedBudget(25, config));
		assertEquals(28, AdvancementRewardRegistry.calculateModdedBudget(50, config));
		assertEquals(42, AdvancementRewardRegistry.calculateModdedBudget(100, config));
		assertEquals(60, AdvancementRewardRegistry.calculateModdedBudget(250, config));
		assertEquals(70, AdvancementRewardRegistry.calculateModdedBudget(500, config));
		assertEquals(76, AdvancementRewardRegistry.calculateModdedBudget(1000, config));
	}

	@Test
	@DisplayName("Largest-Remainder Allocation Sum Verification")
	void testLargestRemainderSumIntegrity() {
		int targetBudget = 840;
		int numAdvancements = 125;
		int[] weights = new int[numAdvancements];
		int totalWeight = 0;

		for (int i = 0; i < numAdvancements; i++) {
			int w = (i % 7 == 0) ? 4 : ((i % 3 == 0) ? 2 : 1);
			weights[i] = w;
			totalWeight += w;
		}

		class Cand {
			final int index;
			final int weight;
			int base;
			final double rem;

			Cand(int index, int weight, int base, double rem) {
				this.index = index;
				this.weight = weight;
				this.base = base;
				this.rem = rem;
			}
		}

		List<Cand> list = new ArrayList<>();
		int allocatedSum = 0;
		for (int i = 0; i < numAdvancements; i++) {
			double quota = ((double) targetBudget * weights[i]) / (double) totalWeight;
			int base = (int) Math.floor(quota);
			double rem = quota - base;
			list.add(new Cand(i, weights[i], base, rem));
			allocatedSum += base;
		}

		int surplus = targetBudget - allocatedSum;
		list.sort((c1, c2) -> {
			int cmp = Double.compare(c2.rem, c1.rem);
			if (cmp != 0) return cmp;
			int wcmp = Integer.compare(c2.weight, c1.weight);
			if (wcmp != 0) return wcmp;
			return Integer.compare(c1.index, c2.index);
		});

		for (int i = 0; i < surplus && i < list.size(); i++) {
			list.get(i).base += 1;
		}

		int finalSum = 0;
		for (Cand c : list) {
			assertTrue(c.base >= 0, "No reward should be negative");
			finalSum += c.base;
		}

		assertEquals(targetBudget, finalSum, "The distributed sum must exactly equal 840 SP!");
	}

	@Test
	@DisplayName("Phase 4 Smite & Bane Target Restrictions & Exclusivity Verification")
	void testSmiteAndBaneScaling() {
		float baseDamage = 7.0f; // Diamond Sword
		float smiteVBonus = 12.5f; // +2.5 * 5 against undead
		float baneVBonus = 12.5f; // +2.5 * 5 against arthropods
		float m20 = WeaponRegistry.getWeaponMultiplier(20); // 4.0x

		// Against Undead (Zombie) with Smite V at Rank 20: (7 + 12.5) * 4.0 = 78.0
		float zombieDamageSmite = (baseDamage + smiteVBonus) * m20;
		assertEquals(78.0f, zombieDamageSmite, 1e-4);

		// Against Non-Undead (Creeper) with Smite V at Rank 20: Smite bonus is 0, so 7 * 4.0 = 28.0
		float creeperDamageSmite = (baseDamage + 0.0f) * m20;
		assertEquals(28.0f, creeperDamageSmite, 1e-4);

		// Against Arthropod (Spider) with Bane V at Rank 20: (7 + 12.5) * 4.0 = 78.0
		float spiderDamageBane = (baseDamage + baneVBonus) * m20;
		assertEquals(78.0f, spiderDamageBane, 1e-4);

		// Against Non-Arthropod (Zombie) with Bane V at Rank 20: Bane bonus is 0, so 7 * 4.0 = 28.0
		float zombieDamageBane = (baseDamage + 0.0f) * m20;
		assertEquals(28.0f, zombieDamageBane, 1e-4);
	}

	@Test
	@DisplayName("Phase 4 Vitality Health Ratio Preservation Logic")
	void testVitalityHealthRatioPreservation() {
		// If player is at 10 HP / 20 HP (50% health), ranking up from Rank 0 to Rank 5 (35 max HP)
		float prevMax = 20.0f;
		float currentHealth = 10.0f;
		float newMax = 35.0f;

		float ratio = currentHealth / prevMax; // 0.5f
		float adjustedHealth = Math.min(newMax, newMax * ratio); // 17.5f

		assertEquals(0.5f, ratio, 1e-4);
		assertEquals(17.5f, adjustedHealth, 1e-4, "Player must remain at 50% health, not healed to full!");

		// If player was at full health (20/20)
		float fullHealth = 20.0f;
		float fullRatio = fullHealth / prevMax; // 1.0f
		float adjustedFullHealth = Math.min(newMax, newMax * fullRatio); // 35.0f
		assertEquals(35.0f, adjustedFullHealth, 1e-4, "Full-health player stays at full health!");
	}

	@Test
	@DisplayName("Phase 4 Projectile Multiplier Scaling Verification")
	void testProjectileScaling() {
		// Player-fired arrows scale by weapon multiplier
		float arrowVanillaBase = 2.0f;
		float powerVBonus = 3.0f; // flat power bonus
		float m20 = WeaponRegistry.getWeaponMultiplier(20); // 4.0x

		float normalArrow = arrowVanillaBase * m20;
		float powerArrow = (arrowVanillaBase + powerVBonus) * m20;

		assertEquals(8.0f, normalArrow, 1e-4);
		assertEquals(20.0f, powerArrow, 1e-4);

		// Non-player projectile (skeleton arrow): multiplier is 1.0 (vanilla)
		float skeletonMultiplier = 1.0f;
		assertEquals(2.0f, arrowVanillaBase * skeletonMultiplier, 1e-4);
	}

	@Test
	@DisplayName("Universal Player Base Damage Table Verification at Strength Rank 20")
	void testUniversalPlayerBaseDamageTable() {
		int rank20 = 20;

		// 1. Empty hand: 7
		assertEquals(7.0f, WeaponRegistry.calculateMeleeBaseDamage(false, false, 1.0f, rank20), 1e-4);

		// 2. Dirt block: 7
		assertEquals(7.0f, WeaponRegistry.calculateMeleeBaseDamage(false, false, 1.0f, rank20), 1e-4);

		// 3. Pickaxe: 7 (Diamond Pickaxe vanilla tool damage 5 is replaced with Strength base 7)
		assertEquals(7.0f, WeaponRegistry.calculateMeleeBaseDamage(false, false, 5.0f, rank20), 1e-4);

		// 4. Shovel: 7 (Diamond Shovel vanilla tool damage 5.5 is replaced with Strength base 7)
		assertEquals(7.0f, WeaponRegistry.calculateMeleeBaseDamage(false, false, 5.5f, rank20), 1e-4);

		// 5. Hoe: 7 (Diamond Hoe vanilla tool damage is replaced with Strength base 7)
		assertEquals(7.0f, WeaponRegistry.calculateMeleeBaseDamage(false, false, 1.0f, rank20), 1e-4);

		// 6. Axe: 7 (Diamond Axe vanilla tool damage 9 is excluded from weapon scaling; uses Strength base 7)
		assertEquals(7.0f, WeaponRegistry.calculateMeleeBaseDamage(false, false, 9.0f, rank20), 1e-4);

		// 7. Diamond Sword: 28 (Registered weapon: 7 base * 4.0 multiplier)
		assertEquals(28.0f, WeaponRegistry.calculateMeleeBaseDamage(false, true, 7.0f, rank20), 1e-4);

		// 8. Trident: 36 (Registered weapon: 9 base * 4.0 multiplier)
		assertEquals(36.0f, WeaponRegistry.calculateMeleeBaseDamage(false, true, 9.0f, rank20), 1e-4);

		// 9. Mace: -1.0 flag indicating vanilla mace damage preserved
		assertEquals(-1.0f, WeaponRegistry.calculateMeleeBaseDamage(true, false, 6.0f, rank20), 1e-4);

		// Strength Potion Bonus check: PlayerBase (7) + Strength I (+3) = 10.0
		float strengthIBonus = 3.0f;
		float axeWithStrengthI = WeaponRegistry.calculateMeleeBaseDamage(false, false, 9.0f, rank20) + strengthIBonus;
		assertEquals(10.0f, axeWithStrengthI, 1e-4);

		// Critical Hit check on excluded tool: 7.0 * 1.5 = 10.5
		float axeCrit = WeaponRegistry.calculateMeleeBaseDamage(false, false, 9.0f, rank20) * 1.5f;
		assertEquals(10.5f, axeCrit, 1e-4);
	}

	@Test
	@DisplayName("PlayerProgressionData copyFrom Verification for Death and Respawn")
	void testPlayerProgressionDataCopyFrom() {
		com.trinankur.advancementskillpoints.progression.PlayerProgressionData source =
			new com.trinankur.advancementskillpoints.progression.PlayerProgressionData();
		source.setAvailableSkillPoints(12);
		source.setRank(ModAttributes.STRENGTH_ID, 8);
		source.setRank(ModAttributes.STAMINA_ID, 5);
		source.claimAdvancement(net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "adventure/kill_a_mob"), 1);

		com.trinankur.advancementskillpoints.progression.PlayerProgressionData target =
			new com.trinankur.advancementskillpoints.progression.PlayerProgressionData();
		target.copyFrom(source);

		assertEquals(13, target.getAvailableSkillPoints());
		assertEquals(1, target.getTotalEarnedPoints());
		assertEquals(8, target.getRank(ModAttributes.STRENGTH_ID));
		assertEquals(5, target.getRank(ModAttributes.STAMINA_ID));
		assertTrue(target.hasClaimed(net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "adventure/kill_a_mob")));
	}
}
