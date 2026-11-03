package com.trinankur.advancementskillpoints.progression.weapon;

import com.trinankur.advancementskillpoints.config.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class WeaponRegistry {
	private static final Set<Identifier> REGISTERED_WEAPONS = new HashSet<>();
	private static final Set<Identifier> EXCLUDED_WEAPONS = new HashSet<>();

	private WeaponRegistry() {}

	public static void registerWeapon(Identifier itemId) {
		if (itemId != null) {
			REGISTERED_WEAPONS.add(itemId);
		}
	}

	public static void excludeWeapon(Identifier itemId) {
		if (itemId != null) {
			EXCLUDED_WEAPONS.add(itemId);
		}
	}

	public static boolean isExplicitlyExcluded(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return false;
		Item item = stack.getItem();

		// Hard rules per Section 3.1:
		// Maces must remain completely vanilla
		if (item instanceof MaceItem) return true;
		// Axes are not considered weapons within this mod's attribute system
		if (stack.is(ItemTags.AXES)) return true;
		// Other tools excluded
		if (stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES)) return true;

		Identifier id = BuiltInRegistries.ITEM.getKey(item);
		if (EXCLUDED_WEAPONS.contains(id)) return true;

		ModConfig config = ModConfig.get();
		if (config != null && config.customExcludedWeapons.contains(id.toString())) {
			return true;
		}

		return false;
	}

	public static boolean isRegisteredWeapon(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return false;
		if (isExplicitlyExcluded(stack)) return false;

		Item item = stack.getItem();

		// Built-in categories per Section 3.1:
		// Swords, Bows, Crossbows, Tridents, Spears
		if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.SPEARS) ||
			item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) {
			return true;
		}

		Identifier id = BuiltInRegistries.ITEM.getKey(item);
		if (REGISTERED_WEAPONS.contains(id)) return true;

		// Spear / weapon detection fallback (naming/class conventions)
		String path = id.getPath().toLowerCase();
		if (path.contains("spear") || path.contains("sword") || item.getClass().getSimpleName().toLowerCase().contains("spear")) {
			return true;
		}

		ModConfig config = ModConfig.get();
		if (config != null && config.customRegisteredWeapons.contains(id.toString())) {
			return true;
		}

		return false;
	}

	public static boolean isUnarmed(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return true;
		if (isExplicitlyExcluded(stack) || isRegisteredWeapon(stack)) return false;

		AtomicReference<Float> damage = new AtomicReference<>(0.0f);
		stack.forEachModifier(EquipmentSlot.MAINHAND, (holder, modifier) -> {
			if (holder.is(Attributes.ATTACK_DAMAGE)) {
				damage.set((float) modifier.amount());
			}
		});
		return damage.get() <= 0.0f;
	}

	public static float getBaseWeaponDamage(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 1.0f; // unarmed base
		}

		Item item = stack.getItem();
		if (item instanceof TridentItem) {
			return 9.0f; // Trident base weapon damage = 9.0
		}

		// Read base damage modifier from the item's mainhand attributes
		AtomicReference<Float> damageModifier = new AtomicReference<>(0.0f);
		stack.forEachModifier(EquipmentSlot.MAINHAND, (attributeHolder, modifier) -> {
			if (attributeHolder.is(Attributes.ATTACK_DAMAGE)) {
				damageModifier.set((float) modifier.amount());
			}
		});

		// In vanilla, player base unarmed is 1.0; a diamond sword has modifier +6.0, giving base 7.0
		float modAmount = damageModifier.get();
		if (modAmount > 0.0f) {
			return 1.0f + modAmount;
		}

		if (stack.is(ItemTags.SWORDS)) {
			return 4.0f; // Generic baseline sword fallback
		}

		return 1.0f;
	}

	public static float getUnarmedDamage(int strengthRank) {
		ModConfig config = ModConfig.get();
		double base = config != null ? config.strengthUnarmedBase : 1.0;
		double perRank = config != null ? config.strengthUnarmedPerRank : 0.30;
		return (float) (base + (perRank * strengthRank));
	}

	public static float getWeaponMultiplier(int strengthRank) {
		ModConfig config = ModConfig.get();
		double perRank = config != null ? config.strengthWeaponMultiplierPerRank : 0.15;
		return (float) (1.0 + (perRank * strengthRank));
	}

	public static boolean isMace(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return false;
		Item item = stack.getItem();
		if (item instanceof MaceItem) return true;
		Identifier id = BuiltInRegistries.ITEM.getKey(item);
		return id != null && id.getPath().toLowerCase().contains("mace");
	}

	public static float getItemAttackDamageModifier(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 0.0f;
		}
		AtomicReference<Float> totalMod = new AtomicReference<>(0.0f);
		stack.forEachModifier(EquipmentSlot.MAINHAND, (attributeHolder, modifier) -> {
			if (attributeHolder.is(Attributes.ATTACK_DAMAGE) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
				totalMod.updateAndGet(v -> v + (float) modifier.amount());
			}
		});
		return totalMod.get();
	}

	public static float calculateMeleeBaseDamage(boolean isMace, boolean isRegisteredWeapon, float weaponBaseDamage, int strengthRank) {
		if (isMace) {
			return -1.0f; // Preserves vanilla mace damage
		}
		if (isRegisteredWeapon) {
			return weaponBaseDamage * getWeaponMultiplier(strengthRank);
		}
		return getUnarmedDamage(strengthRank);
	}

	public static float calculateMeleeBaseDamage(ItemStack stack, int strengthRank) {
		if (isMace(stack)) {
			return -1.0f; // Preserves vanilla mace damage
		}
		if (isRegisteredWeapon(stack)) {
			return getScaledWeaponDamage(stack, strengthRank);
		}
		return getUnarmedDamage(strengthRank);
	}

	public static float getScaledWeaponDamage(ItemStack stack, int strengthRank) {
		float baseDamage = getBaseWeaponDamage(stack);
		float multiplier = getWeaponMultiplier(strengthRank);
		return baseDamage * multiplier;
	}
}
