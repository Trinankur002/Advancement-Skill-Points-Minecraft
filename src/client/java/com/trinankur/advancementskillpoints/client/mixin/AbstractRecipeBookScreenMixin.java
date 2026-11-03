package com.trinankur.advancementskillpoints.client.mixin;

import com.trinankur.advancementskillpoints.client.screen.SkillPointsScreenHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin {
	@Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
	private void advancementSkillPoints$onHasClickedOutside(
		double mouseX,
		double mouseY,
		int left,
		int top,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (this instanceof SkillPointsScreenHolder holder && holder.advancementSkillPoints$isOpen()) {
			var panel = holder.advancementSkillPoints$getPanel();
			if (panel != null && panel.isMouseOver(mouseX, mouseY)) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void advancementSkillPoints$onMouseClicked(
		MouseButtonEvent event,
		boolean doubleClick,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (this instanceof SkillPointsScreenHolder holder && holder.advancementSkillPoints$isOpen()) {
			var panel = holder.advancementSkillPoints$getPanel();
			if (panel != null && panel.isMouseOver(event.x(), event.y())) {
				if (panel.mouseClicked(Minecraft.getInstance(), event.x(), event.y(), event.button())) {
					cir.setReturnValue(true);
				}
			}
		}
	}
}

