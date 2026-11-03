package com.trinankur.advancementskillpoints.network;

import com.trinankur.advancementskillpoints.AdvancementSkillPoints;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestResetPayload() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<RequestResetPayload> TYPE =
		new CustomPacketPayload.Type<>(AdvancementSkillPoints.id("request_reset"));

	public static final StreamCodec<ByteBuf, RequestResetPayload> STREAM_CODEC =
		StreamCodec.unit(new RequestResetPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

