package com.trinankur.advancementskillpoints.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;

public class SkillPointsToggleButton extends Button {
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

		// 1. Outer 1px black border with rounded corners (authentic vanilla button border)
		graphics.fill(bx + 1, by, bx + bw - 1, by + 1, 0xFF000000);
		graphics.fill(bx + 1, by + bh - 1, bx + bw - 1, by + bh, 0xFF000000);
		graphics.fill(bx, by + 1, bx + 1, by + bh - 1, 0xFF000000);
		graphics.fill(bx + bw - 1, by + 1, bx + bw, by + bh - 1, 0xFF000000);

		// 2. Beveled highlight / shadow and button body fill
		if (open) {
			// Pressed / Active state (authentic vanilla toggle depression)
			int shadow = 0xFF373737;
			int highlight = 0xFFFFFFFF;
			int fill = hovered ? 0xFFB8B8B8 : 0xFFA5A5A5;

			// Top & Left recessed shadow
			graphics.fill(bx + 1, by + 1, bx + bw - 1, by + 2, shadow);
			graphics.fill(bx + 1, by + 1, bx + 2, by + bh - 1, shadow);

			// Bottom & Right bevel highlight
			graphics.fill(bx + 1, by + bh - 2, bx + bw - 1, by + bh - 1, highlight);
			graphics.fill(bx + bw - 2, by + 1, bx + bw - 1, by + bh - 1, highlight);

			// Inner recessed fill
			graphics.fill(bx + 2, by + 2, bx + bw - 2, by + bh - 2, fill);

			// Subtle warm golden inner border accent indicating active selection
			graphics.outline(bx + 1, by + 1, bw - 2, bh - 2, 0xFFD4AF37);
		} else {
			// Normal unpressed state
			int highlight = 0xFFFFFFFF;
			int shadow = hovered ? 0xFF5B5B5B : 0xFF373737;
			int fill = hovered ? 0xFFDBDBDB : 0xFFC6C6C6;

			// Top & Left highlight
			graphics.fill(bx + 1, by + 1, bx + bw - 1, by + 2, highlight);
			graphics.fill(bx + 1, by + 1, bx + 2, by + bh - 1, highlight);

			// Bottom & Right shadow
			graphics.fill(bx + 1, by + bh - 2, bx + bw - 1, by + bh - 1, shadow);
			graphics.fill(bx + bw - 2, by + 1, bx + bw - 1, by + bh - 1, shadow);

			// Center fill
			graphics.fill(bx + 2, by + 2, bx + bw - 2, by + bh - 2, fill);
		}

		// 3. Render Crisp, Elegant Skill Point Star Icon (✦)
		int iconX = bx + bw / 2;
		int iconY = by + bh / 2 + (open ? 1 : 0); // Subtle 1px depression when toggled active
		drawSkillPointStarIcon(graphics, iconX, iconY);
	}

	/**
	 * Draws a radiant 11x11 golden Skill Point Star (✦) centered at (cx, cy).
	 */
	private static void drawSkillPointStarIcon(GuiGraphicsExtractor graphics, int cx, int cy) {
		int WHITE  = 0xFFFFFFFF; // Pure white core and tip shine
		int GLOW   = 0xFFFFFF88; // Bright yellow-white core glow
		int GOLD   = 0xFFFFD700; // Rich golden yellow
		int AMBER  = 0xFFD49000; // Outer amber border / shading

		// Outer amber silhouette for high contrast against gray button background
		graphics.fill(cx - 1, cy - 5, cx + 2, cy + 6, AMBER);
		graphics.fill(cx - 5, cy - 1, cx + 6, cy + 2, AMBER);
		graphics.fill(cx - 3, cy - 3, cx + 4, cy + 4, AMBER);

		// Golden inner star rays
		graphics.fill(cx - 1, cy - 4, cx + 2, cy + 5, GOLD);
		graphics.fill(cx - 4, cy - 1, cx + 5, cy + 2, GOLD);
		graphics.fill(cx - 2, cy - 2, cx + 3, cy + 3, GOLD);

		// Bright yellow core glow
		graphics.fill(cx, cy - 3, cx + 1, cy + 4, GLOW);
		graphics.fill(cx - 3, cy, cx + 4, cy + 1, GLOW);
		graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, GLOW);

		// Pure white center sparkle and sharp tips
		graphics.fill(cx, cy - 1, cx + 1, cy + 2, WHITE);
		graphics.fill(cx - 1, cy, cx + 2, cy + 1, WHITE);
		graphics.fill(cx, cy - 4, cx + 1, cy - 3, WHITE); // Top tip
		graphics.fill(cx, cy + 4, cx + 1, cy + 5, WHITE); // Bottom tip
		graphics.fill(cx - 4, cy, cx - 3, cy + 1, WHITE); // Left tip
		graphics.fill(cx + 4, cy, cx + 5, cy + 1, WHITE); // Right tip

		// Four micro diagonal sparkle glints
		graphics.fill(cx - 2, cy - 2, cx - 1, cy - 1, GLOW);
		graphics.fill(cx + 2, cy - 2, cx + 3, cy - 1, GLOW);
		graphics.fill(cx - 2, cy + 2, cx - 1, cy + 3, GLOW);
		graphics.fill(cx + 2, cy + 2, cx + 3, cy + 3, GLOW);
	}
}
