package com.trinankur.advancementskillpoints.client.screen;

import com.trinankur.advancementskillpoints.client.ClientProgressionData;
import com.trinankur.advancementskillpoints.network.RequestUpgradePayload;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeDefinition;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PlayerStatsScreen extends Screen {
	private static final int PANEL_WIDTH = 340;
	private static final int PANEL_HEIGHT = 190;
	private static final int CARD_HEIGHT = 30;

	private AttributeDefinition selectedAttribute;
	private Button upgradeButton;
	private int panelLeft;
	private int panelTop;

	public PlayerStatsScreen() {
		super(Component.literal("Player Stats"));
	}

	@Override
	protected void init() {
		this.panelLeft = (this.width - PANEL_WIDTH) / 2;
		this.panelTop = (this.height - PANEL_HEIGHT) / 2;

		List<AttributeDefinition> list = AttributeRegistry.getGuiAttributes();
		if (!list.isEmpty() && selectedAttribute == null) {
			selectedAttribute = list.get(0);
		}

		int detailX = panelLeft + 175;
		int detailWidth = 150;
		int upgradeBtnY = panelTop + PANEL_HEIGHT - 32;

		this.upgradeButton = Button.builder(Component.literal("Upgrade"), btn -> {
			if (selectedAttribute != null && ClientProgressionData.canUpgrade(selectedAttribute)) {
				ClientPlayNetworking.send(new RequestUpgradePayload(selectedAttribute.getId().toString()));
			}
		}).bounds(detailX + 15, upgradeBtnY, detailWidth - 30, 20).build();

		this.addRenderableWidget(this.upgradeButton);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		// Dark background overlay
		this.extractTransparentBackground(graphics);

		// Main container panel
		graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xEE121316);
		graphics.outline(panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT, 0xFF3D414D);

		// Header bar
		graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + 24, 0x55000000);
		graphics.horizontalLine(panelLeft, panelLeft + PANEL_WIDTH - 1, panelTop + 24, 0xFF2A2D35);

		// Header: Title & Star Points
		graphics.text(this.font, "§6[|||] §fPlayer Stats", panelLeft + 10, panelTop + 8, 0xFFFFFFFF, true);
		int points = ClientProgressionData.getAvailablePoints();
		graphics.text(this.font, "§e★ §fSkill Points: §e" + points, panelLeft + PANEL_WIDTH - 125, panelTop + 8, 0xFFFFAA00, true);

		// Left list of attributes
		int listX = panelLeft + 10;
		int listY = panelTop + 32;
		int cardWidth = 155;

		List<AttributeDefinition> attributes = AttributeRegistry.getGuiAttributes();
		if (selectedAttribute == null && !attributes.isEmpty()) {
			selectedAttribute = attributes.get(0);
		}

		for (int i = 0; i < attributes.size(); i++) {
			AttributeDefinition def = attributes.get(i);
			int cardY = listY + (i * (CARD_HEIGHT + 6));
			boolean isSelected = (def == selectedAttribute);
			boolean isHovered = mouseX >= listX && mouseX <= listX + cardWidth && mouseY >= cardY && mouseY <= cardY + CARD_HEIGHT;

			int cardBg = isSelected ? 0xFF20232B : (isHovered ? 0xFF1C1E24 : 0xFF16181D);
			graphics.fill(listX, cardY, listX + cardWidth, cardY + CARD_HEIGHT, cardBg);

			int borderColor = isSelected ? 0xFFB359FF : (isHovered ? 0xFF5A606E : 0xFF2D313A);
			graphics.outline(listX, cardY, cardWidth, CARD_HEIGHT, borderColor);

			// Attribute Icon + Name
			String icon = getAttributeIcon(def.getId().getPath());
			graphics.text(this.font, icon + " " + def.getDisplayName(), listX + 8, cardY + 6, isSelected ? 0xFFFFFFFF : 0xFFDDDDDD, true);

			// Current Rank / Max Rank
			int rank = ClientProgressionData.getRank(def.getId());
			boolean isMax = rank >= def.getMaxRank();
			String rankText = isMax ? "§6MAX" : "§e" + rank + "/" + def.getMaxRank();
			graphics.text(this.font, rankText, listX + 8, cardY + 17, isMax ? 0xFFFFAA00 : 0xFFAAAAAA, true);

			// [+] Affordance Button
			boolean canUp = ClientProgressionData.canUpgrade(def);
			int plusBg = canUp ? 0xFF2ECC71 : 0xFF4A4E57;
			graphics.fill(listX + cardWidth - 20, cardY + 6, listX + cardWidth - 6, cardY + 20, plusBg);
			graphics.text(this.font, "+", listX + cardWidth - 16, cardY + 9, 0xFFFFFFFF, false);
		}

		// Vertical divider
		graphics.verticalLine(panelLeft + 170, panelTop + 24, panelTop + PANEL_HEIGHT - 1, 0xFF2A2D35);

		// Right Detail Panel
		if (selectedAttribute != null) {
			int detailX = panelLeft + 175;
			int detailY = panelTop + 32;
			int currentRank = ClientProgressionData.getRank(selectedAttribute.getId());
			int nextRank = currentRank + 1;
			int cost = selectedAttribute.getUpgradeCost(currentRank);
			boolean isMax = currentRank >= selectedAttribute.getMaxRank();

			// Detail Header
			String headerIcon = getAttributeIcon(selectedAttribute.getId().getPath());
			graphics.text(this.font, "§l" + headerIcon + " " + selectedAttribute.getDisplayName(), detailX + 10, detailY, 0xFFFFFFFF, true);

			// Description (wrapped)
			graphics.textWithWordWrap(this.font, Component.literal(selectedAttribute.getDescription()), detailX + 10, detailY + 14, 140, 0xFFAAAAAA);

			// Current Bonus
			graphics.text(this.font, "Current (Rank " + currentRank + "):", detailX + 10, detailY + 50, 0xFFCCCCCC, true);
			String currentEffect = (currentRank > 0) ? selectedAttribute.formatEffect(currentRank) : "None (Base Rank 0)";
			graphics.text(this.font, "§a" + currentEffect, detailX + 10, detailY + 62, 0xFF55FF55, true);

			// Next Level
			graphics.text(this.font, "Next (Rank " + nextRank + "):", detailX + 10, detailY + 78, 0xFFCCCCCC, true);
			String nextEffect = isMax ? "§cMAX RANK (" + selectedAttribute.getMaxRank() + ")" : "§a" + selectedAttribute.formatEffect(nextRank);
			graphics.text(this.font, nextEffect, detailX + 10, detailY + 90, 0xFF55FF55, true);

			// Upgrade Cost
			graphics.text(this.font, "Upgrade Cost:", detailX + 10, detailY + 108, 0xFFCCCCCC, true);
			String costText = isMax ? "§7—" : "§e★ " + cost + " Skill Points";
			graphics.text(this.font, costText, detailX + 10, detailY + 120, 0xFFFFAA00, true);

			// Update upgrade button active state and label
			if (this.upgradeButton != null) {
				this.upgradeButton.active = ClientProgressionData.canUpgrade(selectedAttribute);
				if (isMax) {
					this.upgradeButton.setMessage(Component.literal("Maxed (Rank " + selectedAttribute.getMaxRank() + ")"));
				} else {
					this.upgradeButton.setMessage(Component.literal("Upgrade (★ " + cost + ")"));
				}
			}
		}

		// Render child widgets (buttons)
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();

		int listX = panelLeft + 10;
		int listY = panelTop + 32;
		int cardWidth = 155;

		List<AttributeDefinition> attributes = AttributeRegistry.getGuiAttributes();
		for (int i = 0; i < attributes.size(); i++) {
			AttributeDefinition def = attributes.get(i);
			int cardY = listY + (i * (CARD_HEIGHT + 6));
			if (mx >= listX && mx <= listX + cardWidth && my >= cardY && my <= cardY + CARD_HEIGHT) {
				this.selectedAttribute = def;

				// If clicking directly on the [+] square, execute upgrade immediately
				if (mx >= listX + cardWidth - 22 && mx <= listX + cardWidth - 4 && my >= cardY + 4 && my <= cardY + 22) {
					if (ClientProgressionData.canUpgrade(this.selectedAttribute)) {
						ClientPlayNetworking.send(new RequestUpgradePayload(this.selectedAttribute.getId().toString()));
					}
				}
				return true;
			}
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static String getAttributeIcon(String path) {
		if (path == null) return "§b✦";
		return switch (path.toLowerCase()) {
			case "strength" -> "§c🗡";
			case "vitality" -> "§4❤";
			case "endurance" -> "§6👟";
			case "stamina" -> "§e🍗";
			default -> "§b✦";
		};
	}
}
