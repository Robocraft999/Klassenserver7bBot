package de.klassenserver7b.k7bot.util.commands.common;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.HelpCategories;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

public class AddReactionCommand implements ServerCommand {

	private boolean isEnabled;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public String gethelp() {
		return "Reagiert als Bot auf die ausgewählte Nachricht.\n - z.B. [prefix]react #GuildMessageChannel [messageid] [:emote:] <:emote:> <:emote:> usw.";
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "react" };
	}

	@Override
	public HelpCategories getcategory() {
		return HelpCategories.TOOLS;
	}

	@Override
	public void performCommand(Member m, GuildMessageChannel channel, Message message) {

		String[] args = message.getContentDisplay().split(" ");
		List<GuildMessageChannel> channels = message.getMentions().getChannels(GuildMessageChannel.class);
		List<CustomEmoji> emotes = message.getMentions().getCustomEmojis();

		if (!channels.isEmpty()) {
			GuildMessageChannel tc = message.getMentions().getChannels(GuildMessageChannel.class).get(0);
			String MessageIdString = args[2];

			try {
				long MessageId = Long.parseLong(MessageIdString);

				for (CustomEmoji emote : emotes) {
					tc.addReactionById(MessageId, emote).queue();
				}

			}
			catch (NumberFormatException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void disableCommand() {
		isEnabled = false;
	}

	@Override
	public void enableCommand() {
		isEnabled = true;
	}

}