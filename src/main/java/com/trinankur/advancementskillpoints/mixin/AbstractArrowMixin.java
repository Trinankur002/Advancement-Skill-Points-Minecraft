package com.trinankur.advancementskillpoints.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.trinankur.advancementskillpoints.progression.ProgressionService;
import com.trinankur.advancementskillpoints.progression.attribute.ModAttributes;
import com.trinankur.advancementskillpoints.progression.weapon.WeaponRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {
	protected AbstractArrowMixin() {
		super(null, null);
	}

	@Shadow
	public abstract ItemStack getWeaponItem();

	@WrapOperation(
		method = "onHitEntity",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		)
	)
	private boolean advancementSkillPoints$scaleArrowDamage(
		Entity target,
		DamageSource damageSource,
		float vanillaDamage,
		Operation<Boolean> original
	) {
		if (this.getOwner() instanceof ServerPlayer player) {
			ItemStack weapon = this.getWeaponItem();
			if (weapon != null && WeaponRegistry.isRegisteredWeapon(weapon)) {
				int strengthRank = ProgressionService.getRank(player, ModAttributes.STRENGTH_ID);
				if (strengthRank > 0) {
					float multiplier = WeaponRegistry.getWeaponMultiplier(strengthRank);
					return original.call(target, damageSource, vanillaDamage * multiplier);
				}
			}
		}
		return original.call(target, damageSource, vanillaDamage);
	}
}

