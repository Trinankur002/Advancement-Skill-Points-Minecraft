package com.trinankur.advancementskillpoints.client.screen;

import com.trinankur.advancementskillpoints.client.ClientProgressionData;
import com.trinankur.advancementskillpoints.network.RequestResetPayload;
import com.trinankur.advancementskillpoints.network.RequestUpgradePayload;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeDefinition;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public final class SkillPointsPanel {
	public static final int PANEL_WIDTH = 147;
	public static final int PANEL_HEIGHT = 166;

	private int x;
	private int y;
	private final Screen parentScreen;

	public SkillPointsPanel(Screen parentScreen) {
		this.parentScreen = parentScreen;
	}

	public void updatePosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + PANEL_WIDTH && mouseY >= y && mouseY < y + PANEL_HEIGHT;
	}

	public void render(GuiGraphicsExtractor graphics, Minecraft mc, int mouseX, int mouseY) {
		Font font = mc.font;

		// 1. Panel Container Outer Frame (Matching Vanilla Inventory / Recipe Book aesthetic)
		drawPanelBox(graphics, x, y, PANEL_WIDTH, PANEL_HEIGHT);

		// 2. Header: ✦ SKILL POINTS
		drawSparkleStar(graphics, x + 15, y + 8);
		graphics.text(font, "SKILL POINTS", x + 27, y + 7, 0xFFFFFFFF, true);

		// 3. Points Summary Box: Available SP: 12  |  Spent: 28
		int summaryX = x + 5;
		int summaryY = y + 20;
		int summaryW = 137;
		int summaryH = 14;

		graphics.fill(summaryX, summaryY, summaryX + summaryW, summaryY + summaryH, 0xFF121318);
		graphics.outline(summaryX, summaryY, summaryW, summaryH, 0xFF2E303A);

		int availSP = ClientProgressionData.getAvailablePoints();
		int totalEarned = ClientProgressionData.getTotalEarnedPoints();
		int spentSP = Math.max(0, totalEarned - availSP);

		String fullSummary = "Available SP: §e" + availSP + " §8| §7Spent: §f" + spentSP;
		int totalSummaryTextW = font.width(fullSummary);
		int maxSummaryW = summaryW - 10; // 5px padding on both sides to guarantee zero border collision

		if (totalSummaryTextW > maxSummaryW) {
			float scale = (float) maxSummaryW / (float) totalSummaryTextW;
			float scaledW = totalSummaryTextW * scale;
			graphics.pose().pushMatrix();
			float drawX = summaryX + (summaryW - scaledW) / 2.0f;
			float drawY = summaryY + (summaryH - (font.lineHeight * scale)) / 2.0f;
			graphics.pose().translate(drawX, drawY);
			graphics.pose().scale(scale, scale);
			graphics.text(font, fullSummary, 0, 0, 0xFFFFFFFF, true);
			graphics.pose().popMatrix();
		} else {
			int summaryTextX = summaryX + (summaryW - totalSummaryTextW) / 2;
			int summaryTextY = summaryY + (summaryH - font.lineHeight) / 2;
			graphics.text(font, fullSummary, summaryTextX, summaryTextY, 0xFFFFFFFF, true);
		}

		// 4. Four Attribute Rows
		List<AttributeDefinition> attributes = AttributeRegistry.getGuiAttributes();
		int rowStartY = y + 36;
		int rowH = 26;
		int rowGap = 2;

		AttributeDefinition hoveredCardDef = null;
		AttributeDefinition hoveredBtnDef = null;

		for (int i = 0; i < attributes.size() && i < 4; i++) {
			AttributeDefinition def = attributes.get(i);
			int rowY = rowStartY + (i * (rowH + rowGap));
			int rowX = x + 5;
			int rowW = 137;

			int btnX = rowX + 118;
			int btnY = rowY + 5;
			int btnW = 15;
			int btnH = 15;

			boolean isBtnHovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
			boolean isRowHovered = mouseX >= rowX && mouseX < rowX + rowW && mouseY >= rowY && mouseY < rowY + rowH;
			boolean isCardHovered = isRowHovered && !isBtnHovered;

			if (isBtnHovered) {
				hoveredBtnDef = def;
			} else if (isCardHovered) {
				hoveredCardDef = def;
			}

			// Row container
			int rowBg = isRowHovered ? 0xFF1E2028 : 0xFF15161B;
			int rowBorder = isRowHovered ? 0xFF4A4E5C : 0xFF2D3038;
			graphics.fill(rowX, rowY, rowX + rowW, rowY + rowH, rowBg);
			graphics.outline(rowX, rowY, rowW, rowH, rowBorder);

			// Attribute Icon (14x14)
			drawAttributeIcon(graphics, def.getId().getPath(), rowX + 4, rowY + 6);

			// Attribute Name
			graphics.text(font, def.getDisplayName(), rowX + 24, rowY + 3, 0xFFFFFFFF, true);

			// Rank: <rank> / 20
			int currentRank = ClientProgressionData.getRank(def.getId());
			int maxRank = def.getMaxRank();
			graphics.text(font, "Rank: §e" + currentRank + " §7/ " + maxRank, rowX + 24, rowY + 12, 0xFFCCCCCC, true);

			// Rank Progress Bar (20 segments, green / dark gray)
			int barX = rowX + 24;
			int barY = rowY + 21;
			int barW = 75;
			int barH = 3;
			drawRankProgressBar(graphics, barX, barY, barW, barH, currentRank, maxRank);

			// Upgrade Cost & Button
			boolean isMax = currentRank >= maxRank;
			int cost = def.getUpgradeCost(currentRank);
			String costText = isMax ? "§6MAX" : "Cost: §e" + cost + " SP";
			int costW = font.width(costText);
			graphics.text(font, costText, rowX + 115 - costW, rowY + 12, 0xFFCCCCCC, true);

			boolean canUpgrade = ClientProgressionData.canUpgrade(def);
			drawButton(graphics, btnX, btnY, btnW, btnH, isBtnHovered, canUpgrade);
			int plusCol = canUpgrade ? (isBtnHovered ? 0xFFFFFF55 : 0xFFFFFFFF) : 0xFF777777;
			graphics.text(font, "+", btnX + 5, btnY + 3, plusCol, canUpgrade);
		}

		// 5. Bottom Controls (Creative Mode: Reset + Centered ATTRIBUTES | Survival: Centered ATTRIBUTES)
		int bottomY = y + 148;
		boolean isCreative = mc.player != null && mc.player.isCreative();

		String attrText = "ATTRIBUTES";
		int attrW = font.width(attrText);

		if (isCreative) {
			// [ Reset ] button
			int rBtnX = x + 6;
			int rBtnY = bottomY;
			int rBtnW = 34;
			int rBtnH = 14;
			boolean isResetHovered = mouseX >= rBtnX && mouseX < rBtnX + rBtnW && mouseY >= rBtnY && mouseY < rBtnY + rBtnH;

			drawButton(graphics, rBtnX, rBtnY, rBtnW, rBtnH, isResetHovered, true);
			graphics.text(font, "Reset", rBtnX + 5, rBtnY + 3, isResetHovered ? 0xFFFFFF55 : 0xFFFFFFFF, true);

			// ATTRIBUTES text centered in the remaining bottom area
			int rightAreaX = rBtnX + rBtnW + 4;
			int rightAreaW = (x + PANEL_WIDTH - 6) - rightAreaX;
			int textX = rightAreaX + (rightAreaW - attrW) / 2;
			graphics.text(font, attrText, textX, bottomY + 3, 0xFFCCCCCC, true);
		} else {
			// Survival / Adventure: Centered ATTRIBUTES across the panel
			int textX = x + (PANEL_WIDTH - attrW) / 2;
			graphics.text(font, attrText, textX, bottomY + 3, 0xFFCCCCCC, true);
		}

		// 6. Tooltips (Compact, neatly wrapped to avoid covering inventory)
		if (hoveredBtnDef != null) {
			int currentRank = ClientProgressionData.getRank(hoveredBtnDef.getId());
			boolean isMax = currentRank >= hoveredBtnDef.getMaxRank();
			int cost = hoveredBtnDef.getUpgradeCost(currentRank);
			boolean canUpgrade = ClientProgressionData.canUpgrade(hoveredBtnDef);

			List<FormattedCharSequence> btnTooltip = new ArrayList<>();
			if (isMax) {
				btnTooltip.add(Component.literal("§6Maximum Rank Reached").getVisualOrderText());
			} else if (canUpgrade) {
				btnTooltip.add(Component.literal("§aUpgrade " + hoveredBtnDef.getDisplayName() + " §7(+1)").getVisualOrderText());
				btnTooltip.add(Component.literal("§7Cost: §e" + cost + " SP").getVisualOrderText());
			} else {
				btnTooltip.add(Component.literal("§cCannot Afford Upgrade").getVisualOrderText());
				btnTooltip.add(Component.literal("§7Requires: §e" + cost + " SP").getVisualOrderText());
			}
			graphics.setTooltipForNextFrame(font, btnTooltip, mouseX, mouseY);
		} else if (hoveredCardDef != null) {
			int currentRank = ClientProgressionData.getRank(hoveredCardDef.getId());
			int nextRank = currentRank + 1;
			int cost = hoveredCardDef.getUpgradeCost(currentRank);
			boolean isMax = currentRank >= hoveredCardDef.getMaxRank();

			int maxTooltipWidth = 145;
			List<Component> rawLines = new ArrayList<>();
			rawLines.add(Component.literal("§6✦ " + hoveredCardDef.getDisplayName() + " §7(Rank " + currentRank + "/" + hoveredCardDef.getMaxRank() + ")"));
			rawLines.add(Component.literal("§7" + hoveredCardDef.getDescription()));

			String curEffect = (currentRank > 0) ? hoveredCardDef.formatEffect(currentRank) : "Base Rank 0";
			rawLines.add(Component.literal("§aCurrent: §f" + curEffect));

			if (!isMax) {
				rawLines.add(Component.literal("§eNext (Lv " + nextRank + "): §f" + hoveredCardDef.formatEffect(nextRank)));
				rawLines.add(Component.literal("§bUpgrade Cost: §e" + cost + " SP"));
			} else {
				rawLines.add(Component.literal("§6[MAXIMUM RANK REACHED]"));
			}

			List<FormattedCharSequence> formattedTooltip = new ArrayList<>();
			for (Component c : rawLines) {
				formattedTooltip.addAll(font.split(c, maxTooltipWidth));
			}
			graphics.setTooltipForNextFrame(font, formattedTooltip, mouseX, mouseY);
		}
	}

	public boolean mouseClicked(Minecraft mc, double mouseX, double mouseY, int button) {
		if (button != 0) return false;

		// 1. Check Upgrade [+] buttons
		List<AttributeDefinition> attributes = AttributeRegistry.getGuiAttributes();
		int rowStartY = y + 36;
		int rowH = 26;
		int rowGap = 2;

		for (int i = 0; i < attributes.size() && i < 4; i++) {
			AttributeDefinition def = attributes.get(i);
			int rowY = rowStartY + (i * (rowH + rowGap));
			int rowX = x + 5;
			int btnX = rowX + 118;
			int btnY = rowY + 5;
			int btnW = 15;
			int btnH = 15;

			if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
				if (ClientProgressionData.canUpgrade(def)) {
					mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
					ClientPlayNetworking.send(new RequestUpgradePayload(def.getId().toString()));
				} else {
					mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.DISPENSER_FAIL, 1.0F));
				}
				return true;
			}
		}

		// 2. Check [ Reset ] button (Creative Mode Only)
		boolean isCreative = mc.player != null && mc.player.isCreative();
		if (isCreative) {
			int rBtnX = x + 6;
			int rBtnY = y + 148;
			int rBtnW = 34;
			int rBtnH = 14;

			if (mouseX >= rBtnX && mouseX < rBtnX + rBtnW && mouseY >= rBtnY && mouseY < rBtnY + rBtnH) {
				mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				mc.setScreen(new ConfirmScreen(
					confirmed -> {
						if (confirmed) {
							ClientPlayNetworking.send(new RequestResetPayload());
						}
						mc.setScreen(this.parentScreen);
					},
					Component.literal("Reset Skill Points?"),
					Component.literal("This will reset all 4 attributes to Rank 0 and refund all invested Skill Points.\n(Creative Mode Utility)")
				));
				return true;
			}
		}

		// Absorb any click inside the panel
		return isMouseOver(mouseX, mouseY);
	}

	private static void drawPanelBox(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
		// Outer 1px black border with rounded corners
		graphics.fill(x + 1, y, x + w - 1, y + 1, 0xFF000000);
		graphics.fill(x + 1, y + h - 1, x + w - 1, y + h, 0xFF000000);
		graphics.fill(x, y + 1, x + 1, y + h - 1, 0xFF000000);
		graphics.fill(x + w - 1, y + 1, x + w, y + h - 1, 0xFF000000);

		// Vanilla Inventory Gray Bevel
		graphics.fill(x + 1, y + 1, x + w - 1, y + 3, 0xFFFFFFFF); // Top light highlight
		graphics.fill(x + 1, y + 1, x + 3, y + h - 1, 0xFFFFFFFF); // Left light highlight
		graphics.fill(x + 1, y + h - 3, x + w - 1, y + h - 1, 0xFF373737); // Bottom shadow
		graphics.fill(x + w - 3, y + 1, x + w - 1, y + h - 1, 0xFF373737); // Right shadow

		// Body fill (Vanilla gray)
		graphics.fill(x + 3, y + 3, x + w - 3, y + h - 3, 0xFFC6C6C6);

		// Inner dark panel cutout
		int innerX = x + 3;
		int innerY = y + 3;
		int innerW = w - 6;
		int innerH = h - 6;

		graphics.fill(innerX, innerY, innerX + innerW, innerY + 1, 0xFF373737); // Inner top shadow
		graphics.fill(innerX, innerY, innerX + 1, innerY + innerH, 0xFF373737); // Inner left shadow
		graphics.fill(innerX, innerY + innerH - 1, innerX + innerW, innerY + innerH, 0xFFFFFFFF); // Inner bot highlight
		graphics.fill(innerX + innerW - 1, innerY, innerX + innerW, innerY + innerH, 0xFFFFFFFF); // Inner right highlight

		// Deep dark container background
		graphics.fill(innerX + 1, innerY + 1, innerX + innerW - 1, innerY + innerH - 1, 0xFF14151B);
	}

	private static void drawButton(
		GuiGraphicsExtractor graphics,
		int x, int y, int w, int h,
		boolean hovered, boolean enabled
	) {
		// Outer 1px black outline
		graphics.outline(x, y, w, h, 0xFF000000);

		int highlight = hovered ? 0xFFFFFFFF : 0xFFDBDBDB;
		int shadow = hovered ? 0xFF5B5B5B : 0xFF373737;
		int fill = enabled ? (hovered ? 0xFFA5A5A5 : 0xFF8B8B8B) : 0xFF4A4A4A;

		// Button bevel
		graphics.fill(x + 1, y + 1, x + w - 1, y + 2, highlight);
		graphics.fill(x + 1, y + 1, x + 2, y + h - 1, highlight);
		graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, shadow);
		graphics.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, shadow);

		// Button body
		graphics.fill(x + 2, y + 2, x + w - 2, y + h - 2, fill);
	}

	private static void drawRankProgressBar(
		GuiGraphicsExtractor graphics,
		int x, int y, int w, int h,
		int currentRank, int maxRank
	) {
		int segments = Math.max(1, maxRank);
		int totalGaps = segments - 1;
		int availableWidth = w - totalGaps;

		for (int i = 0; i < segments; i++) {
			int segX = x + i + (i * availableWidth / segments);
			int nextSegX = x + (i + 1) + ((i + 1) * availableWidth / segments) - 1;
			int segW = Math.max(1, nextSegX - segX);

			boolean isFilled = i < currentRank;

			if (isFilled) {
				// Vibrant Minecraft green rank segment
				graphics.fill(segX, y, segX + segW, y + 1, 0xFF55FF55);
				graphics.fill(segX, y + 1, segX + segW, y + h - 1, 0xFF00AA00);
				graphics.fill(segX, y + h - 1, segX + segW, y + h, 0xFF005500);
			} else {
				// Dark gray unacquired slot
				graphics.fill(segX, y, segX + segW, y + 1, 0xFF3A3A3A);
				graphics.fill(segX, y + 1, segX + segW, y + h, 0xFF222222);
			}
		}
	}

	private static void drawSparkleStar(GuiGraphicsExtractor graphics, int x, int y) {
		int G = 0xFFFFD700; // Gold
		int W = 0xFFFFFFFF; // White center
		int[][] pixels = {
			{0, 0, 0, G, 0, 0, 0},
			{0, 0, G, W, G, 0, 0},
			{0, G, W, W, W, G, 0},
			{G, W, W, W, W, W, G},
			{0, G, W, W, W, G, 0},
			{0, 0, G, W, G, 0, 0},
			{0, 0, 0, G, 0, 0, 0}
		};
		drawPixelMatrix(graphics, x, y, pixels);
	}

	private static void drawAttributeIcon(GuiGraphicsExtractor graphics, String path, int x, int y) {
		if (path == null) return;
		switch (path.toLowerCase()) {
			case "strength" -> drawStrengthSword(graphics, x, y);
			case "vitality" -> drawVitalityHeart(graphics, x, y);
			case "endurance" -> drawEnduranceBoot(graphics, x, y);
			case "stamina" -> drawStaminaDrumstick(graphics, x, y);
		}
	}

	private static void drawStrengthSword(GuiGraphicsExtractor graphics, int x, int y) {
		int W = 0xFFFFFFFF; // Blade specular
		int S = 0xFFDCDCE5; // Blade silver
		int B = 0xFF707380; // Blade shadow
		int G = 0xFFFFD700; // Gold crossguard & pommel
		int H = 0xFF5C3317; // Brown leather hilt
		int[][] pixels = {
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, W, W, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, W, S, S, W},
			{0, 0, 0, 0, 0, 0, 0, 0, W, S, S, B, 0},
			{0, 0, 0, 0, 0, 0, 0, W, S, S, B, 0, 0},
			{0, 0, 0, 0, 0, 0, W, S, S, B, 0, 0, 0},
			{0, 0, 0, 0, 0, W, S, S, B, 0, 0, 0, 0},
			{0, 0, 0, 0, W, S, S, B, 0, 0, 0, 0, 0},
			{0, 0, 0, G, G, S, B, 0, 0, 0, 0, 0, 0},
			{0, 0, G, G, G, G, 0, 0, 0, 0, 0, 0, 0},
			{0, G, G, 0, 0, G, G, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, H, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, H, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, G, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
		};
		drawPixelMatrix(graphics, x, y, pixels);
	}

	private static void drawVitalityHeart(GuiGraphicsExtractor graphics, int x, int y) {
		int W = 0xFFFFFFFF; // White shine
		int R = 0xFFE51515; // Red heart
		int D = 0xFF8B0000; // Dark shadow
		int[][] pixels = {
			{0, 0, R, R, 0, 0, 0, R, R, 0, 0},
			{0, R, W, W, R, 0, R, R, R, R, 0},
			{R, W, W, R, R, R, R, R, R, R, R},
			{R, W, R, R, R, R, R, R, R, R, R},
			{R, R, R, R, R, R, R, R, R, R, R},
			{0, R, R, R, R, R, R, R, R, R, 0},
			{0, 0, D, R, R, R, R, R, D, 0, 0},
			{0, 0, 0, D, R, R, R, D, 0, 0, 0},
			{0, 0, 0, 0, D, R, D, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, D, 0, 0, 0, 0, 0}
		};
		drawPixelMatrix(graphics, x + 1, y + 2, pixels);
	}

	private static void drawEnduranceBoot(GuiGraphicsExtractor graphics, int x, int y) {
		int W = 0xFFFFFFFF; // White speed dash lines
		int B = 0xFF3D230F; // Dark leather outline
		int L = 0xFF9E5C2A; // Brown leather
		int S = 0xFF2A170A; // Sole
		int[][] pixels = {
			{0, 0, 0, 0, B, B, B, B, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, B, L, L, B, 0, 0, 0, 0, 0},
			{W, W, 0, 0, B, L, L, B, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, B, L, L, B, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, B, L, L, B, B, B, B, 0, 0},
			{0, W, W, 0, B, L, L, L, L, L, L, B, 0},
			{W, W, 0, 0, B, L, L, L, L, L, L, L, B},
			{0, 0, 0, 0, B, S, S, S, S, S, S, S, B},
			{0, 0, 0, 0, B, B, B, B, B, B, B, B, B}
		};
		drawPixelMatrix(graphics, x, y + 2, pixels);
	}

	private static void drawStaminaDrumstick(GuiGraphicsExtractor graphics, int x, int y) {
		int H = 0xFFFFD152; // Roast highlight
		int O = 0xFFFF9F1C; // Roast body
		int D = 0xFF994F00; // Brown shadow
		int W = 0xFFFFFFFF; // Bone
		int[][] pixels = {
			{0, 0, 0, H, O, O, 0, 0, 0, 0, 0, 0},
			{0, 0, H, O, O, O, O, 0, 0, 0, 0, 0},
			{0, H, O, O, O, O, O, O, 0, 0, 0, 0},
			{0, O, O, O, O, O, O, D, 0, 0, 0, 0},
			{0, O, O, O, O, O, D, 0, 0, 0, 0, 0},
			{0, 0, O, O, O, D, 0, W, 0, 0, 0, 0},
			{0, 0, 0, D, D, 0, W, W, W, 0, 0, 0},
			{0, 0, 0, 0, 0, W, W, 0, W, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, W, W, 0, 0, 0, 0}
		};
		drawPixelMatrix(graphics, x, y + 2, pixels);
	}

	private static void drawPixelMatrix(GuiGraphicsExtractor graphics, int startX, int startY, int[][] pixels) {
		for (int r = 0; r < pixels.length; r++) {
			for (int c = 0; c < pixels[r].length; c++) {
				int color = pixels[r][c];
				if (color != 0) {
					graphics.fill(startX + c, startY + r, startX + c + 1, startY + r + 1, color);
				}
			}
		}
	}
}
