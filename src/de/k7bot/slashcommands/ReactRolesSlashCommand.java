package de.k7bot.slashcommands;

import de.k7bot.SQL.LiteSQL;
import de.k7bot.commands.types.SlashCommand;
import de.k7bot.util.PermissionError;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class ReactRolesSlashCommand implements SlashCommand {

	@Override
	public void performSlashCommand(SlashCommandInteraction event) {

		InteractionHook hook = event.deferReply(true).complete();
		Member m = event.getMember();

		if (m.hasPermission(Permission.MANAGE_ROLES)) {

			CustomEmoji emote;
			OptionMapping channel = event.getOption("channel");
			OptionMapping messageid = event.getOption("messageid");
			OptionMapping emoteop = event.getOption("emoteid-oder-utfemote");
			OptionMapping roleop = event.getOption("role");

			TextChannel tc = channel.getAsChannel().asTextChannel();
			Role role = roleop.getAsRole();
			long MessageId = messageid.getAsLong();

			try {

				emote = tc.getGuild().getEmojiById(emoteop.getAsLong());

				tc.addReactionById(MessageId, emote).queue();

				LiteSQL.onUpdate("INSERT INTO reactroles(guildid, channelid, messageid, emote, roleid) VALUES("
						+ tc.getGuild().getIdLong() + ", " + tc.getIdLong() + ", " + MessageId + ", '" + emote.getId()
						+ "', " + role.getIdLong() + ")");

			} catch (NumberFormatException e) {

				tc.addReactionById(MessageId, Emoji.fromUnicode(emoteop.getAsString())).queue();

				LiteSQL.onUpdate("INSERT INTO reactroles(guildid, channelid, messageid, emote, roleid) VALUES("
						+ tc.getGuild().getIdLong() + ", " + tc.getIdLong() + ", " + MessageId + ", '"
						+ emoteop.getAsString() + "', " + role.getIdLong() + ")");

			}

			hook.sendMessage("Reactrole was successfull set for Message: " + MessageId).queue();

		} else {
			PermissionError.onPermissionError(m, event.getChannel().asTextChannel());
		}

	}

}
