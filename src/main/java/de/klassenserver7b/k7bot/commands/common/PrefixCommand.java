package de.klassenserver7b.k7bot.commands.common;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import de.klassenserver7b.k7bot.HelpCategories;
import de.klassenserver7b.k7bot.Klassenserver7bbot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class PrefixCommand implements ServerCommand {

	private boolean isEnabled;

	@Override
	public String gethelp() {
		return "Ändert das Prefix des Bots auf diesem Server.\n - z.B. [prefix][new prefix]";
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "prefix" };
	}

	@Override
	public HelpCategories getcategory() {
		return HelpCategories.ALLGEMEIN;
	}

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {

		if (m.hasPermission(Permission.ADMINISTRATOR)) {
			String[] args = message.getContentDisplay().split(" ");

			if (args.length > 1) {

				Klassenserver7bbot.getInstance().getPrefixMgr().setPrefix(channel.getGuild().getIdLong(), args[1]);
				EmbedBuilder builder = new EmbedBuilder();
				builder.setFooter("Requested by @" + m.getEffectiveName());
				builder.setTimestamp(OffsetDateTime.now());
				builder.setTitle("Prefix was set to \"" + args[1] + "\"");
				channel.sendMessageEmbeds(builder.build()).complete().delete().queueAfter(10L, TimeUnit.SECONDS);

			} else {

				SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "prefix [String]", m);
			}
		} else {
			PermissionError.onPermissionError(m, channel);
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