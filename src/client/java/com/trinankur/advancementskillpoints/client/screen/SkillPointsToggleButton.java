package com.trinankur.advancementskillpoints.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.BooleanSupplier;

public class SkillPointsToggleButton extends Button {
	private static final ItemStack SWORD_STACK = new ItemStack(Items.GOLDEN_SWORD);
	private final BooleanSupplier isOpenSupplier;

	public SkillPointsToggleButton(int x, int y, OnPress onPress, BooleanSupplier isOpenSupplier) {
		super(x, y, 20, 18, Component.literal("Skill Points"), onPress, DEFAULT_NARRATION);
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
		boolean hovered = this.isHoveredOrFocused() || (mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh);
		boolean open = isOpen();

		// 1. Draw vanilla-style 20x18 button frame with rounded black border (matching recipe book button frame)
		graphics.fill(bx + 1, by, bx + bw - 1, by + 1, 0xFF000000);
		graphics.fill(bx + 1, by + bh - 1, bx + bw - 1, by + bh, 0xFF000000);
		graphics.fill(bx, by + 1, bx + 1, by + bh - 1, 0xFF000000);
		graphics.fill(bx + bw - 1, by + 1, bx + bw, by + bh - 1, 0xFF000000);

		// 2. Beveled highlight / shadow and button body fill
		int highlight = 0xFFFFFFFF;
		int fill = hovered ? 0xFFDBDBDB : 0xFFC6C6C6;
		int shadow = hovered ? 0xFF5B5B5B : 0xFF373737;

		// Top & Left highlight bevel
		graphics.fill(bx + 1, by + 1, bx + bw - 1, by + 2, highlight);
		graphics.fill(bx + 1, by + 1, bx + 2, by + bh - 1, highlight);

		// Bottom & Right shadow bevel
		graphics.fill(bx + 1, by + bh - 2, bx + bw - 1, by + bh - 1, shadow);
		graphics.fill(bx + bw - 2, by + 1, bx + bw - 1, by + bh - 1, shadow);

		// Button background fill
		graphics.fill(bx + 2, by + 2, bx + bw - 2, by + bh - 2, fill);

		// 3. When open / active, apply glowing gold outline frame
		if (open) {
			graphics.outline(bx, by, bw, bh, 0xFFFFD700);
			graphics.outline(bx + 1, by + 1, bw - 2, bh - 2, 0xFFFFE57F);
		}

		// 4. Render Golden Sword item icon
		graphics.item(SWORD_STACK, bx + 2, by + 1);

		// 5. Draw sparkle stars beside the sword (matching design in reference UI)
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
