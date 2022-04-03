package io.icker.factions.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.icker.factions.config.Config;
import io.icker.factions.database.Member;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import static net.minecraft.command.CommandSource.suggestMatching;
import net.minecraft.server.network.ServerPlayerEntity;
import io.icker.factions.util.FactionSuggestions;
import me.lucko.fabric.api.permissions.v0.Permissions;

public class CommandRegistryWithPermissions {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> factions = CommandManager
			.literal("factions")
			.requires(Permissions.require("factions.all"))
			.build();

		LiteralCommandNode<ServerCommandSource> alias = CommandManager
			.literal("f")
			.requires(Permissions.require("factions.all"))
			.redirect(factions)
			.build();

		LiteralCommandNode<ServerCommandSource> create = CommandManager
			.literal("create")
			.requires(CommandRegistry::isFactionless)
			.requires(Permissions.require("factions.create"))
			.then(
				CommandManager.argument("name", StringArgumentType.greedyString())
				.executes(new CreateCommand())
			)
			.build();

		LiteralCommandNode<ServerCommandSource> disband = CommandManager
			.literal("disband")
			.requires(Permissions.require("factions.disband"))
			.requires(CommandRegistry::isOwner)
			.executes(new DisbandCommand())
			.build();

		LiteralCommandNode<ServerCommandSource> join = CommandManager
			.literal("join")
			.requires(Permissions.require("factions.join"))
			.requires(CommandRegistry::isFactionless)
			.then(
				CommandManager.argument("name", StringArgumentType.greedyString())
				.executes(new JoinCommand())
			)
			.build();

		LiteralCommandNode<ServerCommandSource> leave = CommandManager
			.literal("leave")
			.requires(Permissions.require("factions.leave"))
			.requires(s -> CommandRegistry.isFactionMember(s) && !CommandRegistry.isOwner(s))
			.executes(new LeaveCommand())
			.build();

		LiteralCommandNode<ServerCommandSource> info = CommandManager
			.literal("info")
			.requires(Permissions.require("factions.info"))
			.executes(InfoCommand::self)
			.then(
				CommandManager.argument("faction", StringArgumentType.greedyString())
				.executes(InfoCommand::any)
			)
			.build();

		LiteralCommandNode<ServerCommandSource> list = CommandManager
			.literal("list")
			.requires(Permissions.require("factions.list"))
			.executes(new ListCommand())
			.build();

			LiteralCommandNode<ServerCommandSource> chat = CommandManager
			.literal("chat")
			.requires(Permissions.require("factions.chat"))
			.then(
				CommandManager.literal("global")
				.executes(ChatCommand::global)
			)
			.then(
				CommandManager.literal("faction")
				.executes(ChatCommand::faction)
			)
			.then(
				CommandManager.literal("focus")
				.executes(ChatCommand::focus)
			)
			.build();

		LiteralCommandNode<ServerCommandSource> modify = CommandManager
			.literal("modify")
			.requires(Permissions.require("factions.modify"))
			.requires(CommandRegistry::isRankAboveOfficer)
			.build();

		LiteralCommandNode<ServerCommandSource> description = CommandManager
			.literal("description")
			.requires(Permissions.require("factions.modify.description"))
			.then(
				CommandManager.argument("description", StringArgumentType.greedyString())
				.executes(ModifyCommand::description)
			)
			.build();

		LiteralCommandNode<ServerCommandSource> color = CommandManager
			.literal("color")
			.requires(Permissions.require("factions.modify.color"))
			.then(
				CommandManager.argument("color", ColorArgumentType.color())
				.executes(ModifyCommand::color)
			)
			.build();

		LiteralCommandNode<ServerCommandSource> open = CommandManager
			.literal("open")
			.requires(Permissions.require("factions.modify.open"))
			.then(
				CommandManager.argument("open", BoolArgumentType.bool())
				.executes(ModifyCommand::open)
			)
			.build();

		LiteralCommandNode<ServerCommandSource> invite = CommandManager
			.literal("invite")
			.requires(Permissions.require("factions.invite"))
			.requires(CommandRegistry::isRankAboveCivilian)
			.build();

		LiteralCommandNode<ServerCommandSource> listInvites = CommandManager
			.literal("list")
			.requires(Permissions.require("factions.invite.list"))
			.executes(InviteCommand::list)
			.build();

		LiteralCommandNode<ServerCommandSource> addInvite = CommandManager
			.literal("add")
			.requires(Permissions.require("factions.invite.add"))
			.then(
				CommandManager.argument("player", EntityArgumentType.player())
				.executes(InviteCommand::add)
			)
			.build();

		LiteralCommandNode<ServerCommandSource> removeInvite = CommandManager
			.literal("remove")
			.requires(Permissions.require("factions.invite.remove"))
			.then(
				CommandManager.argument("player", EntityArgumentType.player())
				.executes(InviteCommand::remove)
			)
			.build();

		LiteralCommandNode<ServerCommandSource> ally = CommandManager
				.literal("ally")
				.requires(Permissions.require("factions.ally"))
				.requires(CommandRegistry::isRankAboveCivilian)
				.build();

		LiteralCommandNode<ServerCommandSource> addAlly = CommandManager
				.literal("add")
				.requires(Permissions.require("factions.ally.add"))
				.then(
						CommandManager.argument("faction", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> suggestMatching(FactionSuggestions.general(ctx), builder))
								.executes(AllyCommand::add)
				)
				.build();

		LiteralCommandNode<ServerCommandSource> acceptAlly = CommandManager
				.literal("accept")
				.requires(Permissions.require("factions.ally.accept"))
				.then(
						CommandManager.argument("faction", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> suggestMatching(FactionSuggestions.allyInvites(ctx), builder))
								.executes(AllyCommand::accept)
				)
				.build();
		
		LiteralCommandNode<ServerCommandSource> listAlly = CommandManager
				.literal("listinvites")
				.requires(Permissions.require("factions.ally.list"))
				.executes(AllyCommand::list)
				.build();

		LiteralCommandNode<ServerCommandSource> removeAlly = CommandManager
				.literal("remove")
				.requires(Permissions.require("factions.ally.remove"))
				.then(
						CommandManager.argument("faction", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> suggestMatching(FactionSuggestions.allies(ctx), builder))
								.executes(AllyCommand::remove)
				)
				.build();

		LiteralCommandNode<ServerCommandSource> claim = CommandManager
			.literal("claim")
			.requires(Permissions.require("factions.claim"))
			.requires(CommandRegistry::isRankAboveCivilian)
			.executes(ClaimCommand::add)
			.build();

		LiteralCommandNode<ServerCommandSource> listClaim = CommandManager
			.literal("list")
			.requires(Permissions.require("factions.claim.list"))
			.executes(ClaimCommand::list)
			.build();
		
		LiteralCommandNode<ServerCommandSource> removeClaim = CommandManager
			.literal("remove")
			.requires(Permissions.require("factions.claim.remove"))
			.executes(ClaimCommand::remove)
			.build();

		LiteralCommandNode<ServerCommandSource> removeAllClaims = CommandManager
			.literal("all")
			.requires(Permissions.require("factions.claim.remove.all"))
			.executes(ClaimCommand::removeAll)
			.build();

		LiteralCommandNode<ServerCommandSource> home = CommandManager
			.literal("home")
			.requires(Permissions.require("factions.home"))
			.requires(s -> isFactionMember(s) && Config.HOME != Config.HomeOptions.DISABLED)
			.executes(HomeCommand::go)
			.build();
		
		LiteralCommandNode<ServerCommandSource> setHome = CommandManager
			.literal("set")
			.requires(Permissions.require("factions.home.set"))
			.requires(CommandRegistry::isRankAboveOfficer)
			.executes(HomeCommand::set)
			.build();

		LiteralCommandNode<ServerCommandSource> map = CommandManager
			.literal("map")
			.requires(Permissions.require("factions.map"))
			.executes(MapCommand::show)
			.build();
		
		LiteralCommandNode<ServerCommandSource> adminBypass = CommandManager
			.literal("bypass")
			.requires(Permissions.require("factions.admin.bypass"))
			.requires(s -> s.hasPermissionLevel(Config.REQUIRED_BYPASS_LEVEL))
			.executes(new BypassCommand())
			.build();

		LiteralCommandNode<ServerCommandSource> admin = CommandManager
			.literal("admin")
			.requires(Permissions.require("factions.admin"))
			.requires(s -> s.hasPermissionLevel(Config.REQUIRED_BYPASS_LEVEL))
			.build();

		LiteralCommandNode<ServerCommandSource> rank = CommandManager
			.literal("rank")
			.requires(Permissions.require("factions.rank"))
			.requires(CommandRegistry::isRankAboveOfficer)
			.build();

		LiteralCommandNode<ServerCommandSource> promote = CommandManager
				.literal("promote")
				.requires(Permissions.require("factions.rank.promote"))
				.then(CommandManager.argument("player", EntityArgumentType.player())
						.executes(RankCommand::promote))
				.build();

		LiteralCommandNode<ServerCommandSource> demote = CommandManager
				.literal("demote")
				.requires(Permissions.require("factions.rank.demote"))
				.then(CommandManager.argument("player", EntityArgumentType.player())
						.executes(RankCommand::demote))
				.build();

		LiteralCommandNode<ServerCommandSource> transferOwner = CommandManager
			.literal("transferOwner")
			.requires(Permissions.require("factions.transfer"))
			.requires(CommandRegistry::isOwner)
			.then(CommandManager.argument("player", EntityArgumentType.player())
					.executes(c -> new TransferOwnerCommand().run(c)))
			.build();

		LiteralCommandNode<ServerCommandSource> kickMember = CommandManager
			.literal("kickMember")
			.requires(Permissions.require("factions.kick"))
			.requires(CommandRegistry::isRankAboveOfficer)
			.then(CommandManager.argument("player", EntityArgumentType.player())
					.executes(c -> new KickMemberCommand().run(c)))
			.build();

		dispatcher.getRoot().addChild(factions);
		dispatcher.getRoot().addChild(alias);

		factions.addChild(create);
		factions.addChild(disband);
		factions.addChild(join);
		factions.addChild(leave);
		factions.addChild(info);
		factions.addChild(list);
		factions.addChild(chat);

		factions.addChild(modify);
		modify.addChild(description);
		modify.addChild(color);
		modify.addChild(open);

		factions.addChild(invite);
		invite.addChild(listInvites);
		invite.addChild(addInvite);
		invite.addChild(removeInvite);

		factions.addChild(ally);
		ally.addChild(addAlly);
		ally.addChild(acceptAlly);
		ally.addChild(listAlly);
		ally.addChild(removeAlly);

		factions.addChild(admin);
		admin.addChild(adminBypass);

		factions.addChild(claim);
		claim.addChild(listClaim);
		claim.addChild(removeClaim);
		removeClaim.addChild(removeAllClaims);

		factions.addChild(map);

		factions.addChild(home);

		factions.addChild(rank);
		rank.addChild(promote);
		rank.addChild(demote);
		factions.addChild(transferOwner);
		factions.addChild(kickMember);
		
		home.addChild(setHome);
	}

	public static boolean isFactionMember(ServerCommandSource source) {
		try {
			ServerPlayerEntity player = source.getPlayer();
			return Member.get(player.getUuid()) != null;
		} catch (CommandSyntaxException e) { return false; }
	}

	public static boolean isFactionless(ServerCommandSource source) {
		return !isFactionMember(source);
	}

	public static boolean isCivilian(ServerCommandSource source) {
		try {
			ServerPlayerEntity player = source.getPlayer();
			return isFactionMember(source) && Member.get(player.getUuid()).getRank() == Member.Rank.CIVILIAN;
		} catch (CommandSyntaxException e) { return false; }
	}

	public static boolean isOfficer(ServerCommandSource source) {
		try {
			ServerPlayerEntity player = source.getPlayer();
			return isFactionMember(source) && Member.get(player.getUuid()).getRank() == Member.Rank.OFFICER;
		} catch (CommandSyntaxException e) { return false; }
	}

	public static boolean isCoOwner(ServerCommandSource source) {
		try {
			ServerPlayerEntity player = source.getPlayer();
			return isFactionMember(source) && Member.get(player.getUuid()).getRank() == Member.Rank.CO_OWNER;
		} catch (CommandSyntaxException e) { return false; }
	}

	public static boolean isOwner(ServerCommandSource source) {
		try {
			ServerPlayerEntity player = source.getPlayer();
			return isFactionMember(source) && Member.get(player.getUuid()).getRank() == Member.Rank.OWNER;
		} catch (CommandSyntaxException e) { return false; }
	}

	public static boolean isRankAboveOfficer(ServerCommandSource source) {
		return isOwner(source) || isCoOwner(source);
	}

	public static boolean isRankAboveCivilian(ServerCommandSource source) {
		return isOwner(source) || isCoOwner(source) || isOfficer(source);
	}
}