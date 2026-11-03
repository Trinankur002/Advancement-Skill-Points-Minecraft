package com.trinankur.advancementskillpoints.progression;

import com.trinankur.advancementskillpoints.AdvancementSkillPoints;
import com.trinankur.advancementskillpoints.network.SyncProgressionPayload;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeDefinition;
import com.trinankur.advancementskillpoints.progression.attribute.AttributeRegistry;
import com.trinankur.advancementskillpoints.progression.reward.AdvancementRewardRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class ProgressionService {
	private ProgressionService() {}

	public static int getRank(ServerPlayer player, Identifier attributeId) {
		if (player == null || attributeId == null) return 0;
		PlayerProgressionData data = ProgressionAttachments.get(player);
		return data.getRank(attributeId);
	}

	public static void syncToClient(ServerPlayer player) {
		if (player == null) return;
		PlayerProgressionData data = ProgressionAttachments.get(player);
		ServerPlayNetworking.send(player, SyncProgressionPayload.fromData(data));
	}

	public static void applyAllModifiers(ServerPlayer player) {
		if (player == null) return;
		PlayerProgressionData data = ProgressionAttachments.get(player);
		for (AttributeDefinition def : AttributeRegistry.getAll()) {
			int rank = data.getRank(def.getId());
			def.apply(player, rank);
		}
	}

	public static void handleUpgradeRequest(ServerPlayer player, String attributeId) {
		if (player == null || attributeId == null) return;
		AttributeDefinition def = AttributeRegistry.getByPath(attributeId);
		if (def == null) {
			AdvancementSkillPoints.LOGGER.warn("Invalid attribute upgrade request received: {}", attributeId);
			return;
		}

		PlayerProgressionData data = ProgressionAttachments.get(player);
		if (!data.canUpgrade(def)) {
			syncToClient(player);
			return;
		}

		if (data.upgrade(def)) {
			applyAllModifiers(player);
			syncToClient(player);
			player.sendSystemMessage(
				Component.literal("Upgraded " + def.getDisplayName() + " to Lv. " + data.getRank(def.getId()) + "!"),
				true
			);
		}
	}

	public static void handleResetRequest(ServerPlayer player) {
		if (player == null) return;
		if (!player.isCreative()) {
			AdvancementSkillPoints.LOGGER.warn("Non-creative player {} attempted to reset skill points", player.getName().getString());
			return;
		}

		PlayerProgressionData data = ProgressionAttachments.get(player);
		data.resetAttributes();
		applyAllModifiers(player);
		syncToClient(player);
		player.sendSystemMessage(
			Component.literal("Skill points and attributes have been reset!"),
			true
		);
	}

	public static void handleAdvancementAward(ServerPlayer player, AdvancementHolder holder) {
		if (player == null || holder == null) return;

		// Ensure reward registry has been evaluated
		if (!AdvancementRewardRegistry.isInitialized()) {
			AdvancementRewardRegistry.recalculate(player.level().getServer().getAdvancements());
		}

		Identifier id = holder.id();
		PlayerProgressionData data = ProgressionAttachments.get(player);
		if (data.hasClaimed(id)) {
			return;
		}

		int points = AdvancementRewardRegistry.getReward(id);
		// Claim advancement regardless of whether points > 0 (Section 6.2)
		if (data.claimAdvancement(id, points)) {
			syncToClient(player);
			if (points > 0) {
				String title = holder.value().name()
					.or(() -> holder.value().display().map(d -> d.getTitle()))
					.map(Component::getString)
					.orElse(id.getPath());
				player.sendSystemMessage(
					Component.literal("+" + points + " Skill Point" + (points > 1 ? "s" : "") + " from '" + title + "'!"),
					true
				);
			}
		}
	}

	public static int syncAllCompletedAdvancements(ServerPlayer player) {
		if (player == null || player.level().getServer() == null) return 0;
		net.minecraft.server.ServerAdvancementManager manager = player.level().getServer().getAdvancements();
		if (manager == null) return 0;

		if (!AdvancementRewardRegistry.isInitialized()) {
			AdvancementRewardRegistry.recalculate(manager);
		}

		net.minecraft.server.PlayerAdvancements playerAdvancements = player.getAdvancements();
		PlayerProgressionData data = ProgressionAttachments.get(player);
		int newlyClaimed = 0;
		int pointsAwarded = 0;

		for (AdvancementHolder holder : manager.getAllAdvancements()) {
			Identifier id = holder.id();
			if (!data.hasClaimed(id)) {
				net.minecraft.advancements.AdvancementProgress progress = playerAdvancements.getOrStartProgress(holder);
				if (progress != null && progress.isDone()) {
					int points = AdvancementRewardRegistry.getReward(id);
					if (data.claimAdvancement(id, points)) {
						newlyClaimed++;
						pointsAwarded += points;
					}
				}
			}
		}

		if (newlyClaimed > 0) {
			if (pointsAwarded > 0) {
				player.sendSystemMessage(
					Component.literal("§6[✦] Retroactively awarded §e" + pointsAwarded + " Skill Points §6for " + newlyClaimed + " completed advancement" + (newlyClaimed > 1 ? "s" : "") + "!"),
					false
				);
			}
			syncToClient(player);
		}
		return pointsAwarded;
	}
}
