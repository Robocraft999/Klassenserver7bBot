package de.klassenserver7b.k7bot.util.commands.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.HelpCategories;
import de.klassenserver7b.k7bot.Klassenserver7bbot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.StatsCategorieUtil;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class StatsCategoryCommand implements ServerCommand {

	private boolean isEnabled;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public String gethelp() {
		String help = "Legt eine Kategorie mit dem Bot-Status (Online/Offline) an.\n - kann nur von Mitgliedern mit der Berechtigung 'Administrator' ausgeführt werden!";
		return help;
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "statscategory" };
	}

	@Override
	public HelpCategories getcategory() {
		return HelpCategories.TOOLS;
	}

	@Override
	public void performCommand(Member m, GuildMessageChannel channel, Message message) {

		if (m.hasPermission(Permission.ADMINISTRATOR)) {

			Guild guild = channel.getGuild();
			try (ResultSet set = LiteSQL.onQuery("SELECT * FROM statschannels WHERE guildId = ?;",
					channel.getGuild().getIdLong())) {

				if (!set.next()) {

					Category cat = guild.createCategory("botstatus").complete();
					cat.getManager().setPosition(0);
					long catid = cat.getIdLong();
					LiteSQL.onUpdate("INSERT INTO statschannels(guildId, categoryId) VALUES(?, ?);", guild.getIdLong(),
							catid);

					StatsCategorieUtil.fillCategory(cat, Klassenserver7bbot.getInstance().isDevMode());

				} else {

					long catid = set.getLong("categoryId");
					channel.sendMessage("Category updated!").complete().delete().queueAfter(10, TimeUnit.SECONDS);
					Category cat = guild.getCategoryById(catid);
					cat.getChannels().forEach(chan -> {
						chan.delete().complete();
					});
					StatsCategorieUtil.fillCategory(guild.getCategoryById(catid),
							Klassenserver7bbot.getInstance().isDevMode());

				}
			}
			catch (SQLException e) {
				log.error(e.getMessage(), e);
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