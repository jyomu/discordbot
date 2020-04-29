package discordbot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
    public static void main(final String[] args) throws LoginException {
        final JDA jda = new JDABuilder(args[0]).build();
        jda.addEventListener(new MessageListener());
        jda.addEventListener(new ChatReader());
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (event.getEntity().getGuild().getTextChannelsByName("通知", false).isEmpty()) {
            event.getEntity().getGuild().getTextChannels().get(0)
                    .sendMessage("#通知というテキストチャンネルを作成してください。ボイスチャンネルの通知はそこで行われます。").queue();
            return;
        }
        if (event.getEntity().getUser().isBot()) {
            return;
        }

        if (event.getChannelLeft() != null) {
            event.getEntity().getGuild().getTextChannelsByName("通知", false).get(0)
                    .sendMessageFormat("%sさんが[🔊%s]から出ました(計%d人)", event.getEntity().getEffectiveName(),
                            event.getChannelLeft().getName(), event.getChannelLeft().getMembers().size() - event
                                    .getChannelLeft().getMembers().stream().filter(m -> m.getUser().isBot()).count())
                    .queue();
        }
        if (event.getChannelJoined() != null) {
            event.getEntity().getGuild().getTextChannelsByName("通知", false).get(0)
                    .sendMessageFormat("%sさんが[🔊%s]に入りました(計%d人)", event.getEntity().getEffectiveName(),
                            event.getChannelJoined().getName(), event.getChannelJoined().getMembers().size() - event
                                    .getChannelJoined().getMembers().stream().filter(m -> m.getUser().isBot()).count())
                    .queue();
        }
    }

}