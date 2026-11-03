package com.trinankur.advancementskillpoints.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.trinankur.advancementskillpoints.config.ModConfig;
import com.trinankur.advancementskillpoints.progression.ProgressionService;
import com.trinankur.advancementskillpoints.progression.attribute.ModAttributes;
import com.trinankur.advancementskillpoints.progression.attribute.StaminaFoodData;
import com.trinankur.advancementskillpoints.progression.weapon.WeaponRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
	@Shadow
	protected FoodData foodData;

	@Shadow
	public abstract ItemStack getWeaponItem();

	@ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
	private float advancementSkillPoints$reduceExhaustion(float exhaustion) {
		if ((Object) this instanceof ServerPlayer serverPlayer) {
			int enduranceRank = ProgressionService.getRank(serverPlayer, ModAttributes.ENDURANCE_ID);
			if (enduranceRank > 0) {
				ModConfig config = ModConfig.get();
				double rate = config != null ? config.enduranceReductionPerRank : 0.03;
				double maxRed = config != null ? config.enduranceMaxReduction : 0.60;
				float reduction = (float) Math.min(maxRed, Math.max(0.0, enduranceRank * rate));
				return Math.max(0.0f, exhaustion * (1.0f - reduction));
			}
		}
		return exhaustion;
	}

	@WrapOperation(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		)
	)
	private boolean advancementSkillPoints$wrapAttackDamage(
		Entity target,
		DamageSource damageSource,
		float vanillaDamage,
		Operation<Boolean> original
	) {
		if ((Object) this instanceof ServerPlayer serverPlayer) {
			int strengthRank = ProgressionService.getRank(serverPlayer, ModAttributes.STRENGTH_ID);
			ItemStack weapon = this.getWeaponItem();

			// 1. Maces retain vanilla smash attack behavior and vanilla damage
			if (WeaponRegistry.isMace(weapon)) {
				return original.call(target, damageSource, vanillaDamage);
			}

			// 2. Registered scaling weapons (Swords, Spears, Tridents)
			if (WeaponRegistry.isRegisteredWeapon(weapon)) {
				if (strengthRank > 0) {
					float multiplier = WeaponRegistry.getWeaponMultiplier(strengthRank);
					return original.call(target, damageSource, vanillaDamage * multiplier);
				}
				return original.call(target, damageSource, vanillaDamage);
			}

			// 3. Universal Player Base Damage: Any other item (tools, axes, blocks, etc.) or empty hand
			float playerBase = WeaponRegistry.getUnarmedDamage(strengthRank);
			float totalAttr = (float) ((Player) (Object) this).getAttributeValue(Attributes.ATTACK_DAMAGE);
			float ratio = playerBase / Math.max(1.0f, totalAttr);
			float finalDamage = Math.max(0.0f, vanillaDamage * ratio);
			return original.call(target, damageSource, finalDamage);
		}
		return original.call(target, damageSource, vanillaDamage);
	}

	@WrapOperation(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;F)V"
		)
	)
	private void advancementSkillPoints$wrapSweepAttackDamage(
		Player player,
		Entity target,
		float baseDamage,
		DamageSource damageSource,
		float cooldown,
		Operation<Void> original
	) {
		if ((Object) this instanceof ServerPlayer serverPlayer) {
			int strengthRank = ProgressionService.getRank(serverPlayer, ModAttributes.STRENGTH_ID);
			if (strengthRank > 0) {
				ItemStack weapon = this.getWeaponItem();
				if (WeaponRegistry.isRegisteredWeapon(weapon)) {
					float multiplier = WeaponRegistry.getWeaponMultiplier(strengthRank);
					original.call(player, target, baseDamage * multiplier, damageSource, cooldown);
					return;
				}
			}
		}
		original.call(player, target, baseDamage, damageSource, cooldown);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void advancementSkillPoints$syncStaminaCap(CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayer serverPlayer && this.foodData instanceof StaminaFoodData staminaFood) {
			int staminaRank = ProgressionService.getRank(serverPlayer, ModAttributes.STAMINA_ID);
			ModConfig config = ModConfig.get();
			int base = config != null ? config.staminaFoodBase : 20;
			int targetMax = base + (staminaRank / 2);
			int targetBonus = staminaRank / 5;
			if (staminaFood.advancementSkillPoints$getMaxFoodLevel() != targetMax) {
				staminaFood.advancementSkillPoints$setMaxFoodLevel(targetMax);
			}
			if (staminaFood.advancementSkillPoints$getFoodRestorationBonus() != targetBonus) {
				staminaFood.advancementSkillPoints$setFoodRestorationBonus(targetBonus);
			}
		}
	}
}
