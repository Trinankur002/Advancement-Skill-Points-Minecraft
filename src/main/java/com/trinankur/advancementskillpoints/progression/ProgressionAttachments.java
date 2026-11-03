package com.trinankur.advancementskillpoints.progression;

import com.trinankur.advancementskillpoints.AdvancementSkillPoints;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;

public final class ProgressionAttachments {
	public static final AttachmentType<PlayerProgressionData> PROGRESSION = AttachmentRegistry.<PlayerProgressionData>builder()
		.persistent(PlayerProgressionData.CODEC)
		.initializer(PlayerProgressionData::new)
		.copyOnDeath()
		.buildAndRegister(AdvancementSkillPoints.id("progression"));

	private ProgressionAttachments() {}

	public static void init() {
		// Class loading triggers static registration
	}

	public static PlayerProgressionData get(Player player) {
		return player.getAttachedOrCreate(PROGRESSION);
	}
}

