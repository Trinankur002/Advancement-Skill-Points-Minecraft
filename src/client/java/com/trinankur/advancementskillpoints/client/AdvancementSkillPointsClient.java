package com.trinankur.advancementskillpoints.client;

import com.trinankur.advancementskillpoints.config.ModConfig;
import com.trinankur.advancementskillpoints.network.SyncProgressionPayload;
import com.trinankur.advancementskillpoints.progression.attribute.ModAttributes;
import com.trinankur.advancementskillpoints.progression.attribute.StaminaFoodData;
import com.trinankur.advancementskillpoints.progression.weapon.WeaponRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;

public class AdvancementSkillPointsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Ensure config and attributes are initialized on client
		ModConfig.load();
		ModAttributes.init();

		ClientPlayNetworking.registerGlobalReceiver(SyncProgressionPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				ClientProgressionData.update(payload);
				if (context.client().player != null) {
					int staminaRank = ClientProgressionData.getRank(ModAttributes.STAMINA_ID);
					int maxFood = 20 + (staminaRank / 2);
					if (context.client().player.getFoodData() instanceof StaminaFoodData sfd) {
						sfd.advancementSkillPoints$setMaxFoodLevel(maxFood);
					}
				}
			});
		});

		// Dynamic weapon & tool tooltip damage display based on player's Strength rank
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			int strengthRank = ClientProgressionData.getRank(ModAttributes.STRENGTH_ID);
			if (strengthRank <= 0) return;

			// Maces retain completely vanilla behavior and damage display
			if (WeaponRegistry.isMace(stack)) {
				return;
			}

			boolean isRegistered = WeaponRegistry.isRegisteredWeapon(stack);
			boolean isExcluded = WeaponRegistry.isExplicitlyExcluded(stack);

			boolean modifiedAttackDamage = false;
			for (int i = 0; i < lines.size(); i++) {
				Component line = lines.get(i);
				String str = line.getString();
				if (str.toLowerCase().contains("attack damage") || str.contains("generic.attack_damage")) {
					if (isRegistered) {
						float scaled = WeaponRegistry.getScaledWeaponDamage(stack, strengthRank);
						String valStr = (scaled % 1.0f == 0) ? String.format("%.0f", scaled) : String.format("%.1f", scaled);
						lines.set(i, Component.literal(" " + valStr + " Attack Damage").withStyle(ChatFormatting.DARK_GREEN)
							.append(Component.literal(" (Strength Scaled)").withStyle(ChatFormatting.GOLD)));
						modifiedAttackDamage = true;
					} else if (isExcluded) {
						float unarmed = WeaponRegistry.getUnarmedDamage(strengthRank);
						String valStr = (unarmed % 1.0f == 0) ? String.format("%.0f", unarmed) : String.format("%.1f", unarmed);
						lines.set(i, Component.literal(" " + valStr + " Attack Damage").withStyle(ChatFormatting.DARK_GREEN)
							.append(Component.literal(" (Base Damage)").withStyle(ChatFormatting.GRAY)));
						modifiedAttackDamage = true;
					}
				}
			}

			// For ranged weapons (Bow, Crossbow) which don't have melee Attack Damage attributes
			if (!modifiedAttackDamage && isRegistered && (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem)) {
				int bonusPct = (int) Math.round(strengthRank * 15.0);
				lines.add(Component.literal("+" + bonusPct + "% Projectile Damage (Strength Rank " + strengthRank + ")").withStyle(ChatFormatting.GOLD));
			}
		});
	}
}
