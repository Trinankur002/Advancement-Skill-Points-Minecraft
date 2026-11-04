package com.trinankur.advancementskillpoints.client.mixin;

import com.trinankur.advancementskillpoints.client.ClientAdvancementRewards;
import com.trinankur.advancementskillpoints.client.ClientProgressionData;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementWidget.class)
public abstract class AdvancementWidgetMixin {
	@Shadow @Final private AdvancementNode advancementNode;
	@Shadow @Final private DisplayInfo display;
	@Shadow private AdvancementProgress progress;
	@Shadow @Final private int x;
	@Shadow @Final private int y;

	@Redirect(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/advancements/DisplayInfo;getDescription()Lnet/minecraft/network/chat/Component;"
		)
	)
	private Component advancementSkillPoints$appendRewardToDescription(DisplayInfo display) {
		Component original = display.getDescription();
		if (this.advancementNode == null || this.advancementNode.holder() == null) {
			return original;
		}

		int sp = ClientAdvancementRewards.getReward(this.advancementNode.holder());
		if (sp <= 0) {
			return original;
		}

		String rewardText = sp == 1 ? "1 Skill Point" : sp + " Skill Points";
		return Component.empty()
			.append(original)
			.append(Component.literal("\n\n§e✦ Reward: §6+" + rewardText));
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void advancementSkillPoints$onExtractRenderState(GuiGraphicsExtractor graphics, int tabX, int tabY, CallbackInfo ci) {
		if (this.display == null || this.advancementNode == null || this.advancementNode.holder() == null) return;
		if (this.display.isHidden() && (this.progress == null || !this.progress.isDone())) {
			return;
		}

		int sp = ClientAdvancementRewards.getReward(this.advancementNode.holder());
		if (sp <= 0) return;

		boolean isDone = this.progress != null && this.progress.isDone();
		boolean isClaimed = ClientProgressionData.isClaimed(this.advancementNode.holder().id());
		boolean isCollected = isDone || isClaimed;

		int nodeX = tabX + this.x + 3;
		int nodeY = tabY + this.y;

		// Render bottom-right SP reward badge on the node
		Font font = Minecraft.getInstance().font;
		String badgeText = (isCollected ? "✔" : "✦") + sp;
		int textW = font.width(badgeText);
		int badgeW = textW + 3;
		int badgeH = 8;
		int bx = nodeX + 26 - badgeW;
		int by = nodeY + 26 - badgeH;

		int bgColor = isCollected ? 0xDD0E2214 : 0xDD24180A;
		int borderColor = isCollected ? 0xFF2E7D32 : 0xFFFFB300;
		int textColor = isCollected ? 0xFF55FF55 : 0xFFFFD700;

		graphics.fill(bx, by, bx + badgeW, by + badgeH, bgColor);
		graphics.outline(bx, by, badgeW, badgeH, borderColor);
		graphics.text(font, badgeText, bx + 2, by + 1, textColor, true);
	}
}
