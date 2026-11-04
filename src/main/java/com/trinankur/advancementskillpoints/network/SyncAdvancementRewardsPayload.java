package com.trinankur.advancementskillpoints.network;

import com.trinankur.advancementskillpoints.AdvancementSkillPoints;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.Map;

public record SyncAdvancementRewardsPayload(
	Map<String, Integer> rewards
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SyncAdvancementRewardsPayload> TYPE =
		new CustomPacketPayload.Type<>(AdvancementSkillPoints.id("sync_advancement_rewards"));

	public static final StreamCodec<ByteBuf, SyncAdvancementRewardsPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), SyncAdvancementRewardsPayload::rewards,
		SyncAdvancementRewardsPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

