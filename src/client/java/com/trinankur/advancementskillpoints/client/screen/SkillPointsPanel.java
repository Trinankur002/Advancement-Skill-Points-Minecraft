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

		String availStr = "Available SP: §e" + availSP;
		String divStr = "§8|";
		String spentStr = "§7Spent: §f" + spentSP;

		int availW = font.width(availStr);
		int divW = font.width(divStr);
		int spentW = font.width(spentStr);
		int totalSummaryTextW = availW + 8 + divW + 8 + spentW;
		int summaryTextX = summaryX + (summaryW - totalSummaryTextW) / 2;

		graphics.text(font, availStr, summaryTextX, summaryY + 3, 0xFFFFFFFF, true);
		graphics.text(font, divStr, summaryTextX + availW + 8, summaryY + 3, 0xFF888888, true);
		graphics.text(font, spentStr, summaryTextX + availW + 8 + divW + 8, summaryY + 3, 0xFFFFFFFF, true);

		// 4. Four Attribute Rows
		List<AttributeDefinition> attributes = AttributeRegistry.getGuiAttributes();
		int rowStartY = y + 36;
		int rowH = 26;
		int rowGap = 2;

		AttributeDefinition hoveredDef = null;

		for (int i = 0; i < attributes.size() && i < 4; i++) {
			AttributeDefinition def = attributes.get(i);
			int rowY = rowStartY + (i * (rowH + rowGap));
			int rowX = x + 5;
			int rowW = 137;

			boolean isRowHovered = mouseX >= rowX && mouseX < rowX + rowW && mouseY >= rowY && mouseY < rowY + rowH;
			if (isRowHovered) {
				hoveredDef = def;
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

			// Upgrade [+] button (15x15)
			int btnX = rowX + 118;
			int btnY = rowY + 5;
			int btnW = 15;
			int btnH = 15;
			boolean isBtnHovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
			boolean canUpgrade = ClientProgressionData.canUpgrade(def);

			drawButton(graphics, btnX, btnY, btnW, btnH, isBtnHovered, canUpgrade);
			int plusCol = canUpgrade ? (isBtnHovered ? 0xFFFFFF55 : 0xFFFFFFFF) : 0xFF777777;
			graphics.text(font, "+", btnX + 5, btnY + 3, plusCol, canUpgrade);
		}

		// 5. Bottom Controls (Creative Mode: Reset + Navigation | Survival: Centered Navigation)
		int bottomY = y + 148;
		boolean isCreative = mc.player != null && mc.player.isCreative();

		if (isCreative) {
			// [ Reset ] button
			int rBtnX = x + 6;
			int rBtnY = bottomY;
			int rBtnW = 34;
			int rBtnH = 14;
			boolean isResetHovered = mouseX >= rBtnX && mouseX < rBtnX + rBtnW && mouseY >= rBtnY && mouseY < rBtnY + rBtnH;

			drawButton(graphics, rBtnX, rBtnY, rBtnW, rBtnH, isResetHovered, true);
			graphics.text(font, "Reset", rBtnX + 5, rBtnY + 3, isResetHovered ? 0xFFFFFF55 : 0xFFFFFFFF, true);

			// ◄  ATTRIBUTES  ► Navigation
			int navX = x + 44;
			int navW = 97;
			drawNavigation(graphics, font, navX, bottomY, navW, 14, mouseX, mouseY);
		} else {
			// Survival / Adventure: Centered Navigation
			int navX = x + 15;
			int navW = 117;
			drawNavigation(graphics, font, navX, bottomY, navW, 14, mouseX, mouseY);
		}

		// 6. Tooltip (Rendered on top if hovering row)
		if (hoveredDef != null) {
			int currentRank = ClientProgressionData.getRank(hoveredDef.getId());
			int nextRank = currentRank + 1;
			int cost = hoveredDef.getUpgradeCost(currentRank);
			boolean isMax = currentRank >= hoveredDef.getMaxRank();

			List<Component> tooltipLines = new ArrayList<>();
			tooltipLines.add(Component.literal("§6✦ " + hoveredDef.getDisplayName() + " §7(Rank " + currentRank + "/" + hoveredDef.getMaxRank() + ")"));
			tooltipLines.add(Component.literal("§7" + hoveredDef.getDescription()));
			tooltipLines.add(Component.literal("§8----------------------"));
			String curEffect = (currentRank > 0) ? hoveredDef.formatEffect(currentRank) : "None (Base Rank 0)";
			tooltipLines.add(Component.literal("§aCurrent: §f" + curEffect));
			if (!isMax) {
				tooltipLines.add(Component.literal("§eNext (Lv " + nextRank + "): §f" + hoveredDef.formatEffect(nextRank)));
				tooltipLines.add(Component.literal("§bUpgrade Cost: §e" + cost + " Skill Point" + (cost > 1 ? "s" : "")));
			} else {
				tooltipLines.add(Component.literal("§6MAXIMUM RANK REACHED"));
			}

			List<FormattedCharSequence> formattedTooltip = tooltipLines.stream().map(Component::getVisualOrderText).toList();
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

	private static void drawNavigation(
		GuiGraphicsExtractor graphics,
		Font font,
		int navX, int navY, int navW, int navH,
		int mouseX, int mouseY
	) {
		// Left arrow ◄ button (12x14)
		int leftArrowX = navX;
		drawButton(graphics, leftArrowX, navY, 12, navH, false, false);
		graphics.text(font, "§7◄", leftArrowX + 3, navY + 3, 0xFF888888, false);

		// "ATTRIBUTES" centered text
		String attrText = "ATTRIBUTES";
		int textW = font.width(attrText);
		int textX = navX + (navW - textW) / 2;
		graphics.text(font, attrText, textX, navY + 3, 0xFFCCCCCC, true);

		// Right arrow ► button (12x14)
		int rightArrowX = navX + navW - 12;
		drawButton(graphics, rightArrowX, navY, 12, navH, false, false);
		graphics.text(font, "§7►", rightArrowX + 4, navY + 3, 0xFF888888, false);
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
