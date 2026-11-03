package com.trinankur.advancementskillpoints;

import com.trinankur.advancementskillpoints.command.SkillPointsCommand;
import com.trinankur.advancementskillpoints.config.ModConfig;
import com.trinankur.advancementskillpoints.network.RequestResetPayload;
import com.trinankur.advancementskillpoints.network.RequestUpgradePayload;
import com.trinankur.advancementskillpoints.network.SyncProgressionPayload;
import com.trinankur.advancementskillpoints.progression.PlayerProgressionData;
import com.trinankur.advancementskillpoints.progression.ProgressionAttachments;
import com.trinankur.advancementskillpoints.progression.ProgressionService;
import com.trinankur.advancementskillpoints.progression.attribute.ModAttributes;
import com.trinankur.advancementskillpoints.progression.reward.AdvancementRewardRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancementSkillPoints implements ModInitializer {
	public static final String MOD_ID = "advancement-skill-points";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Advancement Skill Points (Phase 3)...");

		// 1. Load configuration
		ModConfig.load();

		// 2. Initialize attribute registry
		ModAttributes.init();

		// 3. Register attachments
		ProgressionAttachments.init();

		// 4. Register Network Payloads
		PayloadTypeRegistry.clientboundPlay().register(SyncProgressionPayload.TYPE, SyncProgressionPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RequestUpgradePayload.TYPE, RequestUpgradePayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RequestResetPayload.TYPE, RequestResetPayload.STREAM_CODEC);

		// 5. Handle C2S Upgrade Request & Reset Request
		ServerPlayNetworking.registerGlobalReceiver(RequestUpgradePayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ProgressionService.handleUpgradeRequest(context.player(), payload.attributeId());
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(RequestResetPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ProgressionService.handleResetRequest(context.player());
			});
		});

		// 6. Advancement reward snapshot on server start and data pack reload
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			AdvancementRewardRegistry.recalculate(server.getAdvancements());
		});

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (success) {
				AdvancementRewardRegistry.recalculate(server.getAdvancements());
			}
		});

		// 7. Player lifecycle events: sync & apply on join & respawn
		ServerPlayerEvents.JOIN.register(player -> {
			ProgressionService.syncAllCompletedAdvancements(player);
			ProgressionService.applyAllModifiers(player);
			ProgressionService.syncToClient(player);
		});

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			PlayerProgressionData oldData = ProgressionAttachments.get(oldPlayer);
			PlayerProgressionData newData = ProgressionAttachments.get(newPlayer);
			newData.copyFrom(oldData);
			if (oldPlayer.getAdvancements() != null) {
				oldPlayer.getAdvancements().save();
			}
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			ProgressionService.applyAllModifiers(newPlayer);
			ProgressionService.syncToClient(newPlayer);
			if (newPlayer.getAdvancements() != null && newPlayer.level().getServer() != null) {
				newPlayer.getAdvancements().reload(newPlayer.level().getServer().getAdvancements());
				newPlayer.getAdvancements().flushDirty(newPlayer, true);
			}
		});

		// 8. Register testing commands (/skillpoints)
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			SkillPointsCommand.register(dispatcher);
		});

		LOGGER.info("Advancement Skill Points initialized successfully (Phase 3 with Testing Commands).");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
