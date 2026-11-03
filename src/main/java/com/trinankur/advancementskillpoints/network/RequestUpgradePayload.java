package com.trinankur.advancementskillpoints.network;

import com.trinankur.advancementskillpoints.AdvancementSkillPoints;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestUpgradePayload(String attributeId) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<RequestUpgradePayload> TYPE =
		new CustomPacketPayload.Type<>(AdvancementSkillPoints.id("request_upgrade"));

	public static final StreamCodec<ByteBuf, RequestUpgradePayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, RequestUpgradePayload::attributeId,
		RequestUpgradePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

