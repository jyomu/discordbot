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
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (event.getEntity().getUser().isBot()) {
            return;
        }
        if (event.getChannelLeft() != null) {
            event.getEntity().getGuild().getTextChannelsByName("通知", false).get(0)
                    .sendMessageFormat("%sさんが[🔊%s]から出ました", event.getEntity().getEffectiveName(),
                            event.getChannelLeft().getName())
                    .queue();
        }
        if (event.getChannelJoined() != null) {
            event.getEntity().getGuild().getTextChannelsByName("通知", false).get(0)
                    .sendMessageFormat("%sさんが[🔊%s]に入りました", event.getEntity().getEffectiveName(),
                            event.getChannelJoined().getName())
                    .queue();
        }
    }

}