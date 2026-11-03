package com.trinankur.advancementskillpoints.mixin;

import com.trinankur.advancementskillpoints.progression.attribute.StaminaFoodData;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FoodData.class)
public abstract class FoodDataMixin implements StaminaFoodData {
	@Unique
	private int advancementSkillPoints$maxFoodLevel = 20;

	@Unique
	private int advancementSkillPoints$foodRestorationBonus = 0;

	@Override
	public int advancementSkillPoints$getMaxFoodLevel() {
		return advancementSkillPoints$maxFoodLevel;
	}

	@Override
	public void advancementSkillPoints$setMaxFoodLevel(int max) {
		this.advancementSkillPoints$maxFoodLevel = Math.max(20, max);
	}

	@Override
	public int advancementSkillPoints$getFoodRestorationBonus() {
		return advancementSkillPoints$foodRestorationBonus;
	}

	@Override
	public void advancementSkillPoints$setFoodRestorationBonus(int bonus) {
		this.advancementSkillPoints$foodRestorationBonus = Math.max(0, bonus);
	}

	@ModifyArg(
		method = "eat(Lnet/minecraft/world/food/FoodProperties;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;add(IF)V"),
		index = 0
	)
	private int advancementSkillPoints$scaleFoodPropertiesNutrition(int nutrition) {
		if (nutrition > 0 && this.advancementSkillPoints$foodRestorationBonus > 0) {
			return nutrition + this.advancementSkillPoints$foodRestorationBonus;
		}
		return nutrition;
	}

	@ModifyArg(
		method = "eat(IF)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;add(IF)V"),
		index = 0
	)
	private int advancementSkillPoints$scaleRawNutrition(int nutrition) {
		if (nutrition > 0 && this.advancementSkillPoints$foodRestorationBonus > 0) {
			return nutrition + this.advancementSkillPoints$foodRestorationBonus;
		}
		return nutrition;
	}

	@ModifyConstant(method = "add", constant = @Constant(intValue = 20))
	private int advancementSkillPoints$modifyAddMaxFood(int constant) {
		return this.advancementSkillPoints$maxFoodLevel;
	}

	@ModifyConstant(method = "needsFood", constant = @Constant(intValue = 20))
	private int advancementSkillPoints$modifyNeedsFoodMax(int constant) {
		return this.advancementSkillPoints$maxFoodLevel;
	}
}

