/**
 *
 */
package de.klassenserver7b.k7bot.util.customapis;

import java.awt.Color;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.Klassenserver7bbot;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.subscriptions.types.SubscriptionTarget;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.InternalStatusCodes;
import de.klassenserver7b.k7bot.util.customapis.types.LoopedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/**
 * @author Klassenserver7b
 *
 */
public class GitHubAPI implements LoopedEvent {

	private final GitHub gh;
	private final Logger log;

	public GitHubAPI() {

		gh = Klassenserver7bbot.getInstance().getGitapi();
		log = LoggerFactory.getLogger(this.getClass().getCanonicalName());

	}

	@Override
	public int checkforUpdates() {

		List<String> newcommits = getNewCommits();

		if (newcommits == null) {
			return InternalStatusCodes.FAILURE;
		}

		if (newcommits.isEmpty()) {
			return InternalStatusCodes.SUCCESS;
		}

		for (String s : newcommits) {

			String[] split = s.split("\n");

			StringBuilder body = new StringBuilder();

			for (int i = 2; i < split.length; i++) {
				body.append(split[i]);
				body.append("\n");
			}

			EmbedBuilder b = EmbedUtils.getBuilderOf(Color.decode("#7289da"), body);
			b.setTitle(split[0]);

			MessageCreateBuilder messb = new MessageCreateBuilder();
			messb.setEmbeds(b.build());

			try (MessageCreateData messdata = messb.build()) {
				Klassenserver7bbot.getInstance().getSubscriptionManager()
						.provideSubscriptionNotification(SubscriptionTarget.BOT_NEWS, messdata);
			}

		}

		return InternalStatusCodes.SUCCESS;

	}

	private List<String> getNewCommits() {

		GHRepository repo;
		List<GHCommit> commitl;
		String dbid;

		try (ResultSet set = LiteSQL.onQuery("SELECT * FROM githubinteractions;")) {

			repo = gh.getRepository("klassenserver7b/Klassenserver7bBot");
			commitl = repo.listCommits().toList();

			if (!set.next() && !commitl.isEmpty()) {

				LiteSQL.onUpdate("INSERT INTO githubinteractions(lastcommit) VALUES(?);", commitl.get(0).getSHA1());
				dbid = "";

			} else {

				dbid = set.getString("lastcommit");
			}

		} catch (HttpException e1) {
			log.warn("Github Connection failed!");
			return null;
		} catch (IOException | SQLException e) {
			log.error(e.getMessage(), e);
			return null;
		}

		if (commitl.isEmpty()) {
			return List.of();
		}

		String commitid = commitl.get(0).getSHA1();

		if (commitid.equalsIgnoreCase(dbid)) {
			return List.of();
		}

		try {
			LiteSQL.onUpdate("UPDATE githubinteractions SET lastcommit = ?;", commitid);
			return getCommitsSince(commitl, dbid);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		}

	}

	private List<String> getCommitsSince(List<GHCommit> comms, String sha) throws IOException {

		List<String> messages = new ArrayList<>();

		for (GHCommit c : comms) {

			if (c.getSHA1().equalsIgnoreCase(sha)) {
				break;
			}

			messages.add(c.getCommitShortInfo().getMessage());

		}

		return messages;

	}

	@Override
	public void shutdown() {
		log.debug("GitHubAPI disabled");
	}

	@Override
	public boolean restart() {
		log.debug("restart requested");
		gh.refreshCache();

		return true;
	}

	@Override
	public boolean isAvailable() {
		try {
			gh.checkApiUrlValidity();
		} catch (IOException e) {
			return false;
		}

		return true;

	}

	@Override
	public String getIdentifier() {
		return "github";
	}

}