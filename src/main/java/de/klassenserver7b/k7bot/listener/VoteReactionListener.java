package de.klassenserver7b.k7bot.listener;

import java.util.HashMap;
import java.util.LinkedHashMap;

import de.klassenserver7b.k7bot.Klassenserver7bbot;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class VoteReactionListener extends ListenerAdapter {

	private Long messageId;
	private User gameMaster;
	private LinkedHashMap<String, User> users;
	private HashMap<User, String> votings;
	private GenericMessageSendHandler channel;

	public VoteReactionListener(Long messageId, User gameMaster, LinkedHashMap<String, User> users,
			GenericMessageSendHandler channel) {
		this.messageId = messageId;
		this.users = users;
		this.gameMaster = gameMaster;
		this.channel = channel;
		votings = new HashMap<>();
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {

		if (event.getMessageIdLong() != messageId) {
			return;
		}

		User u = event.getUser();

		if (event.getGuild().getSelfMember().getUser().getIdLong() == u.getIdLong()) {
			return;
		}

		if (!votings.containsKey(u) && users.values().contains(u)) {
			votings.put(u, event.getEmoji().getName());
		}

		event.getReaction().removeReaction(u).queue();

		if (votings.size() >= users.size()) {

			StringBuilder build = new StringBuilder();

			votings.forEach((user, emojiname) -> {

				build.append(user.getName());
				build.append(" - ");
				build.append(emojiname);
				build.append("\n");

			});

			channel.sendMessage("Voting beendet!");
			gameMaster.openPrivateChannel().complete()
					.sendMessageEmbeds(EmbedUtils.getBuilderOf(build.toString()).build()).queue();

			Klassenserver7bbot.getInstance().getShardManager().removeEventListener(this);

		}

	}

}