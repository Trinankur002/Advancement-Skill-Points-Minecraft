package com.trinankur.advancementskillpoints.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.BooleanSupplier;

public class SkillPointsToggleButton extends ImageButton {
	private static final ItemStack SWORD_STACK = new ItemStack(Items.GOLDEN_SWORD);
	private final BooleanSupplier isOpenSupplier;

	public SkillPointsToggleButton(int x, int y, OnPress onPress, BooleanSupplier isOpenSupplier) {
		super(x, y, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, onPress, Component.literal("Skill Points"));
		this.isOpenSupplier = isOpenSupplier;
		this.setTooltip(Tooltip.create(Component.literal("Skill Points")));
	}

	public boolean isOpen() {
		return isOpenSupplier != null && isOpenSupplier.getAsBoolean();
	}

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int bx = this.getX();
		int by = this.getY();
		int bw = this.getWidth();
		int bh = this.getHeight();
		boolean open = isOpen();

		// 1. Draw vanilla recipe book button box (normal / hovered states)
		super.extractContents(graphics, mouseX, mouseY, delta);

		// 2. When open, apply active glowing gold outline frame
		if (open) {
			graphics.outline(bx, by, bw, bh, 0xFFFFD700);
			graphics.outline(bx + 1, by + 1, bw - 2, bh - 2, 0xFFFFE57F);
		}

		// 3. Render authentic Minecraft Golden Sword item icon (centered in 20x18 button)
		graphics.item(SWORD_STACK, bx + 2, by + 1);

		// 4. Draw sparkle stars beside the sword (matching design in reference UI)
		drawSparkles(graphics, bx, by);
	}

	private static void drawSparkles(GuiGraphicsExtractor graphics, int bx, int by) {
		int W = 0xFFFFFFFF; // Bright white center
		int Y = 0xFFFFEE88; // Soft yellow star glow
		int G = 0xFFFFD700; // Gold sparkle

		// Top-right 4-pointed sparkle star (at bx + 14, by + 3)
		graphics.fill(bx + 15, by + 2, bx + 16, by + 3, Y);
		graphics.fill(bx + 14, by + 3, bx + 17, by + 4, W);
		graphics.fill(bx + 15, by + 4, bx + 16, by + 5, Y);

		// Bottom-left small sparkle dot (at bx + 3, by + 13)
		graphics.fill(bx + 3, by + 13, bx + 5, by + 14, G);
		graphics.fill(bx + 4, by + 12, bx + 5, by + 15, Y);
	}
}
