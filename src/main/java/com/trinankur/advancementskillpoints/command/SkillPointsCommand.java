package com.trinankur.advancementskillpoints.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.trinankur.advancementskillpoints.progression.PlayerProgressionData;
import com.trinankur.advancementskillpoints.progression.ProgressionAttachments;
import com.trinankur.advancementskillpoints.progression.ProgressionService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public final class SkillPointsCommand {
	private SkillPointsCommand() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands.literal("skillpoints")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				// 1. /skillpoints add <targets> <amount>
				.then(Commands.literal("add")
					.then(Commands.argument("targets", EntityArgument.players())
						.then(Commands.argument("amount", IntegerArgumentType.integer(1))
							.executes(ctx -> {
								Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
								int amount = IntegerArgumentType.getInteger(ctx, "amount");
								return executeAdd(ctx.getSource(), players, amount);
							})
						)
					)
				)
				// 2. /skillpoints remove <targets> <amount>
				.then(Commands.literal("remove")
					.then(Commands.argument("targets", EntityArgument.players())
						.then(Commands.argument("amount", IntegerArgumentType.integer(1))
							.executes(ctx -> {
								Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
								int amount = IntegerArgumentType.getInteger(ctx, "amount");
								return executeRemove(ctx.getSource(), players, amount);
							})
						)
					)
				)
				// 3. /skillpoints set <targets> <amount>
				.then(Commands.literal("set")
					.then(Commands.argument("targets", EntityArgument.players())
						.then(Commands.argument("amount", IntegerArgumentType.integer(0))
							.executes(ctx -> {
								Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
								int amount = IntegerArgumentType.getInteger(ctx, "amount");
								return executeSet(ctx.getSource(), players, amount);
							})
						)
					)
				)
				// 4. /skillpoints get <targets>
				.then(Commands.literal("get")
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(ctx -> {
							Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
							return executeGet(ctx.getSource(), players);
						})
					)
				)
				// 5. /skillpoints sync [targets]
				.then(Commands.literal("sync")
					.executes(ctx -> {
						ServerPlayer self = ctx.getSource().getPlayerOrException();
						int points = ProgressionService.syncAllCompletedAdvancements(self);
						ctx.getSource().sendSuccess(() -> Component.literal(
							String.format("Synchronized advancements for %s. Awarded %d Skill Points.", self.getName().getString(), points)
						), true);
						return 1;
					})
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(ctx -> {
							Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
							int totalPoints = 0;
							for (ServerPlayer p : players) {
								totalPoints += ProgressionService.syncAllCompletedAdvancements(p);
							}
							int finalTotal = totalPoints;
							ctx.getSource().sendSuccess(() -> Component.literal(
								String.format("Synchronized advancements for %d player(s). Awarded %d total Skill Points.", players.size(), finalTotal)
							), true);
							return players.size();
						})
					)
				)
		);
	}

	private static int executeAdd(CommandSourceStack source, Collection<ServerPlayer> targets, int amount) {
		for (ServerPlayer player : targets) {
			PlayerProgressionData data = ProgressionAttachments.get(player);
			int prev = data.getAvailableSkillPoints();
			long sum = (long) prev + (long) amount;
			int next = (int) Math.min((long) Integer.MAX_VALUE, sum);
			data.setAvailableSkillPoints(next);

			ProgressionService.syncToClient(player);
			source.sendSuccess(() -> Component.literal(
				String.format("Added %d Skill Points to %s (Previous: %d, New: %d)", amount, player.getName().getString(), prev, next)
			), true);
		}
		return targets.size();
	}

	private static int executeRemove(CommandSourceStack source, Collection<ServerPlayer> targets, int amount) {
		for (ServerPlayer player : targets) {
			PlayerProgressionData data = ProgressionAttachments.get(player);
			int prev = data.getAvailableSkillPoints();
			int next = Math.max(0, prev - amount);
			data.setAvailableSkillPoints(next);

			ProgressionService.syncToClient(player);
			source.sendSuccess(() -> Component.literal(
				String.format("Removed %d Skill Points from %s (Previous: %d, New: %d)", amount, player.getName().getString(), prev, next)
			), true);
		}
		return targets.size();
	}

	private static int executeSet(CommandSourceStack source, Collection<ServerPlayer> targets, int amount) {
		for (ServerPlayer player : targets) {
			PlayerProgressionData data = ProgressionAttachments.get(player);
			int prev = data.getAvailableSkillPoints();
			data.setAvailableSkillPoints(amount);

			ProgressionService.syncToClient(player);
			source.sendSuccess(() -> Component.literal(
				String.format("Set Skill Points for %s to %d (Previous: %d)", player.getName().getString(), amount, prev)
			), true);
		}
		return targets.size();
	}

	private static int executeGet(CommandSourceStack source, Collection<ServerPlayer> targets) {
		for (ServerPlayer player : targets) {
			PlayerProgressionData data = ProgressionAttachments.get(player);
			int available = data.getAvailableSkillPoints();
			int earned = data.getTotalEarnedPoints();
			source.sendSuccess(() -> Component.literal(
				String.format("%s has %d available Skill Points (Total Earned: %d)", player.getName().getString(), available, earned)
			), false);
		}
		return targets.size();
	}
}
