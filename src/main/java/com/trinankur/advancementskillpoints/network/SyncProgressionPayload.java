package com.trinankur.advancementskillpoints.network;

import com.trinankur.advancementskillpoints.AdvancementSkillPoints;
import com.trinankur.advancementskillpoints.progression.PlayerProgressionData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SyncProgressionPayload(
	int availablePoints,
	int totalEarnedPoints,
	Map<String, Integer> ranks,
	List<String> claimedAdvancements
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SyncProgressionPayload> TYPE =
		new CustomPacketPayload.Type<>(AdvancementSkillPoints.id("sync_progression"));

	public static final StreamCodec<ByteBuf, SyncProgressionPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, SyncProgressionPayload::availablePoints,
		ByteBufCodecs.VAR_INT, SyncProgressionPayload::totalEarnedPoints,
		ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), SyncProgressionPayload::ranks,
		ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SyncProgressionPayload::claimedAdvancements,
		SyncProgressionPayload::new
	);

	public static SyncProgressionPayload fromData(PlayerProgressionData data) {
		List<String> claimed = data.getClaimedAdvancements().stream()
			.map(Identifier::toString)
			.toList();

		return new SyncProgressionPayload(
			data.getAvailableSkillPoints(),
			data.getTotalEarnedPoints(),
			new HashMap<>(data.getAttributeRanksMap()),
			new ArrayList<>(claimed)
		);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
