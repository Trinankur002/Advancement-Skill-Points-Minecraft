package com.trinankur.advancementskillpoints.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.trinankur.advancementskillpoints.progression.ProgressionService;
import com.trinankur.advancementskillpoints.progression.attribute.ModAttributes;
import com.trinankur.advancementskillpoints.progression.weapon.WeaponRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {
	protected ThrownTridentMixin() {
		super(null, null);
	}

	@WrapOperation(
		method = "onHitEntity",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		)
	)
	private boolean advancementSkillPoints$scaleTridentDamage(
		Entity target,
		DamageSource damageSource,
		float vanillaDamage,
		Operation<Boolean> original
	) {
		if (this.getOwner() instanceof ServerPlayer player) {
			int strengthRank = ProgressionService.getRank(player, ModAttributes.STRENGTH_ID);
			if (strengthRank > 0) {
				float multiplier = WeaponRegistry.getWeaponMultiplier(strengthRank);
				return original.call(target, damageSource, vanillaDamage * multiplier);
			}
		}
		return original.call(target, damageSource, vanillaDamage);
	}
}

