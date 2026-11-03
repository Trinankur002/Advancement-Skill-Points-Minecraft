package com.trinankur.advancementskillpoints.client.hud;

import com.trinankur.advancementskillpoints.client.ClientProgressionData;
import com.trinankur.advancementskillpoints.progression.attribute.ModAttributes;
import com.trinankur.advancementskillpoints.progression.attribute.StaminaFoodData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.food.FoodData;

import java.util.ArrayList;
import java.util.List;

public final class EnhancedHudRenderer {
	private static final int TOTAL_WIDTH = 182;
	private static final int CARD_WIDTH = 44;
	private static final int CARD_HEIGHT = 15;
	private static final int CARD_GAP = 2;
	private static final int XP_ROW_HEIGHT = 12;

	private record CardInfo(
		int iconType,
		String text,
		float fillRatio,
		int topCol, int bodyCol, int botCol,
		int emptyTop, int emptyBot
	) {}

	private EnhancedHudRenderer() {}

	public static void render(GuiGraphicsExtractor graphics, Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null || mc.options.hideGui || player.isSpectator()) {
			return;
		}

		Font font = mc.font;
		int screenWidth = graphics.guiWidth();
		int screenHeight = graphics.guiHeight();
		int centerX = screenWidth / 2;
		int startX = centerX - (TOTAL_WIDTH / 2);

		// Anchor directly above the hotbar (which is at screenHeight - 22)
		int hotbarY = screenHeight - 22;
		int xpRowY = hotbarY - XP_ROW_HEIGHT - 1;     // screenHeight - 35
		int statRowY = xpRowY - CARD_HEIGHT - 1;      // screenHeight - 51

		// -------------------------------------------------------------
		// 1. TOP ROW: DYNAMIC STAT PANELS (Health, Armor, Hunger, Air)
		// -------------------------------------------------------------
		List<CardInfo> cards = new ArrayList<>(4);

		// Panel 1: Health (Always shown)
		int curHealth = (int) Math.ceil(player.getHealth());
		int maxHealth = (int) Math.ceil(player.getMaxHealth());
		float healthRatio = (float) curHealth / Math.max(1.0f, maxHealth);
		cards.add(new CardInfo(
			0, // Heart icon
			curHealth + " / " + maxHealth,
			healthRatio,
			0xFFFF6B6B, 0xFFE51515, 0xFF8B0000,
			0xFF26080A, 0xFF140305
		));

		// Panel 2: Armor (Dynamic: only displayed when player has armor)
		int armor = player.getArmorValue();
		if (armor > 0) {
			float armorRatio = (float) armor / 20.0f;
			cards.add(new CardInfo(
				1, // Chestplate icon
				armor + " / 20",
				armorRatio,
				0xFFFFFFFF, 0xFFDCDCE5, 0xFF707380,
				0xFF222328, 0xFF121316
			));
		}

		// Panel 3: Hunger / Food (Always shown)
		FoodData foodData = player.getFoodData();
		int curFood = foodData.getFoodLevel();
		int staminaRank = ClientProgressionData.getRank(ModAttributes.STAMINA_ID);
		int maxFood = Math.max(20, 20 + (staminaRank / 2));
		if (foodData instanceof StaminaFoodData sfd) {
			sfd.advancementSkillPoints$setMaxFoodLevel(maxFood);
		}
		if (curFood > maxFood) {
			maxFood = curFood;
		}
		float foodRatio = (float) curFood / Math.max(1.0f, maxFood);
		cards.add(new CardInfo(
			2, // Drumstick icon
			curFood + " / " + maxFood,
			foodRatio,
			0xFFFFD152, 0xFFFF9F1C, 0xFF994F00,
			0xFF281C08, 0xFF140D04
		));

		// Panel 4: Air Supply / Oxygen (Dynamic: only when underwater or depleted)
		int curAir = Math.max(0, player.getAirSupply());
		int maxAir = Math.max(1, player.getMaxAirSupply());
		if (player.isEyeInFluid(FluidTags.WATER) || curAir < maxAir) {
			float airRatio = (float) curAir / maxAir;
			cards.add(new CardInfo(
				3, // Lungs icon
				curAir + " / " + maxAir,
				airRatio,
				0xFF64D8FF, 0xFF00A6FB, 0xFF005599,
				0xFF081C28, 0xFF040E14
			));
		}

		// Draw all active cards centered horizontally above the hotbar
		int numCards = cards.size();
		int totalCardsWidth = (numCards * CARD_WIDTH) + ((numCards - 1) * CARD_GAP);
		int cardStartX = centerX - (totalCardsWidth / 2);

		for (int i = 0; i < numCards; i++) {
			CardInfo card = cards.get(i);
			int cardX = cardStartX + (i * (CARD_WIDTH + CARD_GAP));
			drawStatCard(
				graphics, font,
				cardX, statRowY,
				CARD_WIDTH, CARD_HEIGHT,
				card.iconType(),
				card.text(),
				card.fillRatio(),
				card.topCol(), card.bodyCol(), card.botCol(),
				card.emptyTop(), card.emptyBot()
			);
		}

		// -------------------------------------------------------------
		// 2. MIDDLE ROW: LEVEL BADGE + SEGMENTED PURPLE XP BAR + XP TEXT
		// -------------------------------------------------------------
		int levelBadgeWidth = 27;
		int levelBadgeX = startX;

		// Level Badge (Left) - "Lv" in neon green, level number in white
		drawBeveledBox(graphics, levelBadgeX, xpRowY, levelBadgeWidth, XP_ROW_HEIGHT);
		int level = player.experienceLevel;
		String fullLevelDisplay = "§aLv §f" + level;
		int levelStrWidth = font.width("Lv " + level);
		int levelTextX = levelBadgeX + (levelBadgeWidth - levelStrWidth) / 2;
		graphics.text(font, fullLevelDisplay, levelTextX, xpRowY + 2, 0xFFFFFFFF, true);

		// XP Container (Right of Level Badge)
		int xpContainerX = startX + 29;
		int xpContainerWidth = TOTAL_WIDTH - 29; // 153px
		drawBeveledBox(graphics, xpContainerX, xpRowY, xpContainerWidth, XP_ROW_HEIGHT);

		// Calculate XP Progress & Text: "<cur> / <next>" in white, "XP" in magenta
		int nextLevelXp = player.getXpNeededForNextLevel();
		int curLevelXp = Math.round(player.experienceProgress * nextLevelXp);
		String xpNumStr = curLevelXp + " / " + nextLevelXp;
		String fullXpStr = xpNumStr + " XP";
		int xpTextWidth = font.width(fullXpStr);

		// Draw XP Text at right side of container
		int textX = xpContainerX + xpContainerWidth - xpTextWidth - 4;
		int textY = xpRowY + 2;
		graphics.text(font, xpNumStr + " §dXP", textX, textY, 0xFFFFFFFF, true);

		// Draw Segmented Purple XP Bar (Left of text)
		int barX = xpContainerX + 4;
		int barY = xpRowY + 3;
		int barWidth = (textX - 4) - barX;
		int barHeight = 6;
		if (barWidth > 20) {
			drawSegmentedBar(
				graphics,
				barX, barY, barWidth, barHeight,
				10, // 10 segments matching screenshot
				player.experienceProgress,
				0xFFFFAAFF, 0xFFCC00FF, 0xFF660099,
				0xFF20052C, 0xFF12021A
			);
		}
	}

	private static void drawStatCard(
		GuiGraphicsExtractor graphics,
		Font font,
		int x, int y,
		int width, int height,
		int iconType,
		String text,
		float fillRatio,
		int topCol, int bodyCol, int botCol,
		int emptyTop, int emptyBot
	) {
		// 1. Beveled Card Outer Frame
		drawBeveledBox(graphics, x, y, width, height);

		// 2. Icon (9x8) at (x + 3, y + 2)
		drawIcon(graphics, iconType, x + 3, y + 2);

		// 3. Numbers Text in area (x + 13 .. x + width - 3)
		int availLeft = x + 13;
		int availWidth = (width - 3) - 13; // 28px available width
		int availHeight = 8; // y + 2 to y + 10

		float unscaledW = font.width(text);
		float unscaledH = font.lineHeight;

		float targetScale = 0.65f;
		if (unscaledW * targetScale > availWidth) {
			targetScale = (float) availWidth / unscaledW;
		}

		float renderedW = unscaledW * targetScale;
		float renderedH = unscaledH * targetScale;

		float textX = availLeft + (availWidth - renderedW) / 2.0f;
		float textY = (y + 2) + (availHeight - renderedH) / 2.0f;

		graphics.pose().pushMatrix();
		graphics.pose().translate(textX, textY);
		graphics.pose().scale(targetScale, targetScale);
		graphics.text(font, text, 0, 0, 0xFFFFFFFF, true);
		graphics.pose().popMatrix();

		// 4. Segmented Bar across bottom (10 segments)
		int barX = x + 3;
		int barY = y + 10;
		int barW = width - 6; // 38px
		int barH = 3;
		drawSegmentedBar(
			graphics,
			barX, barY, barW, barH,
			10,
			fillRatio,
			topCol, bodyCol, botCol,
			emptyTop, emptyBot
		);
	}

	private static void drawBeveledBox(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		// Outer 1px black outline with rounded/cut corners
		graphics.fill(x + 1, y, x + width - 1, y + 1, 0xFF000000);                      // top
		graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, 0xFF000000);      // bottom
		graphics.fill(x, y + 1, x + 1, y + height - 1, 0xFF000000);                      // left
		graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, 0xFF000000);      // right

		// Metallic dark border rim
		graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF353740);

		// Deep dark container background
		graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xFF101115);

		// Subtle top-inner specular highlight
		graphics.fill(x + 2, y + 2, x + width - 2, y + 3, 0xFF454854);
	}

	private static void drawSegmentedBar(
		GuiGraphicsExtractor graphics,
		int x, int y, int totalWidth, int height,
		int segmentCount,
		float fillRatio,
		int topCol, int bodyCol, int botCol,
		int emptyTop, int emptyBot
	) {
		float clampedRatio = Math.clamp(fillRatio, 0.0f, 1.0f);
		int totalGaps = segmentCount - 1;
		int availableWidth = totalWidth - totalGaps;

		for (int i = 0; i < segmentCount; i++) {
			int segX = x + i + (i * availableWidth / segmentCount);
			int nextSegX = x + (i + 1) + ((i + 1) * availableWidth / segmentCount) - 1;
			int segW = Math.max(1, nextSegX - segX);

			float segStart = (float) i / segmentCount;
			float segEnd = (float) (i + 1) / segmentCount;

			if (clampedRatio >= segEnd || (clampedRatio > 0.0f && i == 0 && clampedRatio < segEnd)) {
				// Fully filled segment
				drawPillSegment(graphics, segX, y, segW, height, topCol, bodyCol, botCol);
			} else if (clampedRatio > segStart) {
				// Partial transition segment (dimmer pill)
				int dimTop = dimColor(topCol, 0.6f);
				int dimBody = dimColor(bodyCol, 0.5f);
				int dimBot = dimColor(botCol, 0.5f);
				drawPillSegment(graphics, segX, y, segW, height, dimTop, dimBody, dimBot);
			} else {
				// Empty segment slot
				graphics.fill(segX, y, segX + segW, y + height, emptyBot);
				graphics.fill(segX, y, segX + segW, y + 1, emptyTop);
			}
		}
	}

	private static void drawPillSegment(
		GuiGraphicsExtractor graphics,
		int x, int y, int width, int height,
		int topCol, int bodyCol, int botCol
	) {
		// Pill top highlight
		graphics.fill(x, y, x + width, y + 1, topCol);
		// Pill middle body
		if (height > 2) {
			graphics.fill(x, y + 1, x + width, y + height - 1, bodyCol);
		}
		// Pill bottom shadow
		graphics.fill(x, y + height - 1, x + width, y + height, botCol);
	}

	private static int dimColor(int color, float factor) {
		int a = (color >> 24) & 0xFF;
		int r = Math.round(((color >> 16) & 0xFF) * factor);
		int g = Math.round(((color >> 8) & 0xFF) * factor);
		int b = Math.round((color & 0xFF) * factor);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static void drawIcon(GuiGraphicsExtractor graphics, int type, int x, int y) {
		switch (type) {
			case 0 -> drawHeartIcon(graphics, x, y);
			case 1 -> drawArmorIcon(graphics, x, y);
			case 2 -> drawFoodIcon(graphics, x, y);
			case 3 -> drawLungsIcon(graphics, x, y);
		}
	}

	private static void drawHeartIcon(GuiGraphicsExtractor graphics, int x, int y) {
		int R = 0xFFE51515; // Red
		int W = 0xFFFFFFFF; // White glint
		int D = 0xFF8B0000; // Dark red shadow
		int[][] pixels = {
			{0, R, R, 0, 0, R, R, 0},
			{R, W, W, R, R, R, R, R},
			{R, W, R, R, R, R, R, R},
			{R, R, R, R, R, R, R, R},
			{0, R, R, R, R, R, R, 0},
			{0, 0, D, R, R, D, 0, 0},
			{0, 0, 0, D, D, 0, 0, 0}
		};
		drawPixelMatrix(graphics, x, y, pixels);
	}

	private static void drawArmorIcon(GuiGraphicsExtractor graphics, int x, int y) {
		int S = 0xFFDCDCE5; // Silver
		int W = 0xFFFFFFFF; // Highlight
		int D = 0xFF707380; // Dark metal shadow
		int[][] pixels = {
			{W, S, 0, 0, 0, 0, S, D},
			{W, S, S, 0, 0, S, S, D},
			{W, S, S, S, S, S, S, D},
			{0, W, S, S, S, S, D, 0},
			{0, W, S, S, S, S, D, 0},
			{0, 0, W, S, S, D, 0, 0},
			{0, 0, 0, S, S, 0, 0, 0}
		};
		drawPixelMatrix(graphics, x, y, pixels);
	}

	private static void drawFoodIcon(GuiGraphicsExtractor graphics, int x, int y) {
		int O = 0xFFFF9F1C; // Roast body
		int H = 0xFFFFD152; // Roast highlight
		int D = 0xFF994F00; // Brown shadow
		int B = 0xFFFFFFFF; // Bone
		int[][] pixels = {
			{0, H, O, O, 0, 0, 0, 0},
			{H, O, O, O, O, 0, 0, 0},
			{O, O, O, O, O, D, 0, 0},
			{O, O, O, O, D, 0, B, 0},
			{0, O, O, D, 0, B, B, B},
			{0, 0, D, 0, 0, 0, B, 0},
			{0, 0, 0, 0, 0, 0, 0, 0}
		};
		drawPixelMatrix(graphics, x, y, pixels);
	}

	private static void drawLungsIcon(GuiGraphicsExtractor graphics, int x, int y) {
		int T = 0xFF64D8FF; // Trachea
		int L = 0xFF8AE4FF; // Light cyan highlight
		int C = 0xFF00A6FB; // Cyan body
		int D = 0xFF005599; // Deep blue shadow
		int[][] pixels = {
			{0, 0, 0, T, T, 0, 0, 0},
			{0, L, L, C, C, L, L, 0},
			{L, L, C, 0, 0, C, L, D},
			{L, C, C, 0, 0, C, C, D},
			{L, C, C, 0, 0, C, C, D},
			{0, C, D, 0, 0, D, C, 0},
			{0, 0, D, 0, 0, D, 0, 0}
		};
		drawPixelMatrix(graphics, x, y, pixels);
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
