package com.trinankur.advancementskillpoints.client.mixin;

import com.trinankur.advancementskillpoints.client.screen.SkillPointsPanel;
import com.trinankur.advancementskillpoints.client.screen.SkillPointsScreenHolder;
import com.trinankur.advancementskillpoints.client.screen.SkillPointsToggleButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> implements SkillPointsScreenHolder {
	@Unique
	private static boolean skillPointsOpen = true;

	@Unique
	private SkillPointsPanel advancementSkillPoints$panel;

	@Unique
	private SkillPointsToggleButton advancementSkillPoints$toggleButton;

	public InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
		super(menu, null, inventory, title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void advancementSkillPoints$onInit(CallbackInfo ci) {
		RecipeBookComponent<?> book = ((AbstractRecipeBookScreenAccessor) this).advancementSkillPoints$getRecipeBookComponent();

		// Mutual exclusivity check on opening:
		if (skillPointsOpen) {
			if (book.isVisible()) {
				book.toggleVisibility();
			}
			boolean widthTooNarrow = this.width < 379;
			if (!widthTooNarrow) {
				this.leftPos = 177 + (this.width - this.imageWidth - 200) / 2;
			} else {
				this.leftPos = (this.width - this.imageWidth) / 2;
			}
		} else if (book.isVisible()) {
			skillPointsOpen = false;
		}

		this.advancementSkillPoints$panel = new SkillPointsPanel(this);
		this.advancementSkillPoints$panel.updatePosition(this.leftPos - 147 - 2, this.topPos);

		int buttonY = this.height / 2 - 22;
		this.advancementSkillPoints$toggleButton = new SkillPointsToggleButton(
			this.leftPos + 126,
			buttonY,
			btn -> this.advancementSkillPoints$onToggleButtonPress(),
			() -> skillPointsOpen
		);
		this.addRenderableWidget(this.advancementSkillPoints$toggleButton);

		// Synchronize recipe book button position
		advancementSkillPoints$updateButtonPositions();
	}

	@Inject(method = "onRecipeBookButtonClick", at = @At("HEAD"))
	private void advancementSkillPoints$onRecipeBookButtonClick(CallbackInfo ci) {
		if (skillPointsOpen) {
			skillPointsOpen = false;
		}
		advancementSkillPoints$updateButtonPositions();
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void advancementSkillPoints$onExtractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (skillPointsOpen && this.advancementSkillPoints$panel != null) {
			this.advancementSkillPoints$panel.render(graphics, this.minecraft, mouseX, mouseY);
		}
	}

	@Unique
	private void advancementSkillPoints$onToggleButtonPress() {
		skillPointsOpen = !skillPointsOpen;
		RecipeBookComponent<?> book = ((AbstractRecipeBookScreenAccessor) this).advancementSkillPoints$getRecipeBookComponent();

		if (skillPointsOpen) {
			if (book.isVisible()) {
				book.toggleVisibility();
			}
			boolean widthTooNarrow = this.width < 379;
			if (!widthTooNarrow) {
				this.leftPos = 177 + (this.width - this.imageWidth - 200) / 2;
			} else {
				this.leftPos = (this.width - this.imageWidth) / 2;
			}
		} else {
			this.leftPos = (this.width - this.imageWidth) / 2;
		}

		if (this.advancementSkillPoints$panel != null) {
			this.advancementSkillPoints$panel.updatePosition(this.leftPos - 147 - 2, this.topPos);
		}

		advancementSkillPoints$updateButtonPositions();
	}

	@Unique
	private void advancementSkillPoints$updateButtonPositions() {
		int buttonY = this.height / 2 - 22;

		if (this.advancementSkillPoints$toggleButton != null) {
			this.advancementSkillPoints$toggleButton.setPosition(this.leftPos + 126, buttonY);
		}

		ScreenPosition bookPos = this.getRecipeBookButtonPosition();
		for (var child : this.children()) {
			if (child instanceof AbstractWidget widget && widget != this.advancementSkillPoints$toggleButton) {
				if (widget.getWidth() == 20 && widget.getHeight() == 18) {
					widget.setPosition(bookPos.x(), bookPos.y());
				}
			}
		}
	}

	@Override
	public SkillPointsPanel advancementSkillPoints$getPanel() {
		return this.advancementSkillPoints$panel;
	}

	@Override
	public boolean advancementSkillPoints$isOpen() {
		return skillPointsOpen;
	}
}
