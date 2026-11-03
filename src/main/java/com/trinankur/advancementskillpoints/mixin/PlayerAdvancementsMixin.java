package com.trinankur.advancementskillpoints.mixin;

import com.trinankur.advancementskillpoints.progression.ProgressionService;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {
	@Shadow
	private ServerPlayer player;

	@Shadow
	@Final
	private Map<AdvancementHolder, AdvancementProgress> progress;

	@Shadow
	public abstract void save();

	@Inject(method = "award", at = @At("RETURN"))
	private void advancementSkillPoints$onAdvancementAward(
		AdvancementHolder holder,
		String criterion,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (cir.getReturnValue() && this.player != null && holder != null) {
			AdvancementProgress advProgress = this.progress.get(holder);
			if (advProgress != null && advProgress.isDone()) {
				ProgressionService.handleAdvancementAward(this.player, holder);
				this.save();
			}
		}
	}
}
