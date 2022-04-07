package de.k7bot.commands;

import de.k7bot.Klassenserver7bbot;

import de.k7bot.commands.types.ServerCommand;
import de.k7bot.util.PermissionError;
import de.k7bot.util.SyntaxError;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.HierarchyException;

public class BanCommand implements ServerCommand {
	public void performCommand(Member m, TextChannel channel, Message message) {
		List<Member> ment = message.getMentionedMembers();
		try {
			String grund = message.getContentDisplay()
					.substring(ment.get(0).getEffectiveName().length() + 8);
			message.delete().queue();

			channel.sendTyping().queue();

			if (m.hasPermission(Permission.BAN_MEMBERS)) {
				if (ment.size() > 0) {
					for (Member u : ment) {
						onBan(m, u, channel, grund);
					}
				}
			} else {
				PermissionError.onPermissionError(m, channel);
			}
		} catch (StringIndexOutOfBoundsException e) {
			SyntaxError.oncmdSyntaxError(channel, "ban [@user] [reason]", m);
		}
	}

	public void onBan(Member requester, Member u, TextChannel channel, String grund) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setFooter("Requested by @" + requester.getEffectiveName());
		builder.setTimestamp(OffsetDateTime.now());
		builder.setThumbnail(u.getUser().getEffectiveAvatarUrl());
		builder.setColor(16711680);
		builder.setTitle("@" + u.getEffectiveName() + " was banned");

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("**User: **" + u.getAsMention() + "\n");
		strBuilder.append("**Case: **" + grund + "\n");
		strBuilder.append("**Requester: **" + requester.getAsMention() + "\n");

		builder.setDescription(strBuilder);

		Guild guild = channel.getGuild();
		TextChannel system = guild.getSystemChannel();

		try {
			u.ban(7).reason(grund).queue();

			if (system.getIdLong() != channel.getIdLong()) {
				(channel.sendMessageEmbeds(builder.build()).complete()).delete().queueAfter(20L, TimeUnit.SECONDS);
				system.sendMessageEmbeds(builder.build()).queue();
			} else {
				system.sendMessageEmbeds(builder.build()).queue();
			}

			String action = "ban";
			Klassenserver7bbot.INSTANCE.getDB().onUpdate(
					"INSERT INTO modlogs(guildId, memberId, requesterId, memberName, requesterName, action, reason, date) VALUES("
							+ channel.getGuild().getIdLong() + ", " + u.getIdLong() + ", " + requester.getIdLong()
							+ ", '" + u.getEffectiveName() + "', '" + requester.getEffectiveName() + "', '" + action
							+ "', '" + grund + "', '" + OffsetDateTime.now() + "')");
		} catch (HierarchyException e) {
			PermissionError.onPermissionError(requester, channel);
		}
	}
}