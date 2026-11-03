package com.trinankur.advancementskillpoints.client.mixin;

import com.trinankur.advancementskillpoints.client.hud.EnhancedHudRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
	private void advancementSkillPoints$renderEnhancedHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		EnhancedHudRenderer.render(graphics, this.minecraft);
	}

	@Inject(method = "extractPlayerHealth", at = @At("HEAD"), cancellable = true)
	private void advancementSkillPoints$cancelVanillaPlayerHealth(GuiGraphicsExtractor graphics, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "extractVehicleHealth", at = @At("HEAD"), cancellable = true)
	private void advancementSkillPoints$cancelVanillaVehicleHealth(GuiGraphicsExtractor graphics, CallbackInfo ci) {
		ci.cancel();
	}

	@Redirect(
		method = "extractHotbarAndDecorations",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
		)
	)
	private void advancementSkillPoints$suppressVanillaXpBackground(
		ContextualBarRenderer renderer,
		GuiGraphicsExtractor graphics,
		DeltaTracker deltaTracker
	) {
		// Suppressed in favor of EnhancedHudRenderer
	}

	@Redirect(
		method = "extractHotbarAndDecorations",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
		)
	)
	private void advancementSkillPoints$suppressVanillaXpRenderState(
		ContextualBarRenderer renderer,
		GuiGraphicsExtractor graphics,
		DeltaTracker deltaTracker
	) {
		// Suppressed in favor of EnhancedHudRenderer
	}

	@Redirect(
		method = "extractHotbarAndDecorations",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"
		)
	)
	private void advancementSkillPoints$suppressVanillaXpLevel(
		GuiGraphicsExtractor graphics,
		Font font,
		int level
	) {
		// Suppressed in favor of EnhancedHudRenderer's compact Lv badge and numeric XP tracker
	}
}


