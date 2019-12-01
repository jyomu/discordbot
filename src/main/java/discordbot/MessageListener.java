package discordbot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {
        JDA jda = new JDABuilder(args[0]).build();
        jda.addEventListener(new MessageListener());
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        event.getGuild().getTextChannelsByName("通知", false).get(0).sendMessageFormat("%sさんが[🔊%s]に入りました",
                event.getMember().getEffectiveName(), event.getChannelJoined().getName()).queue();
    }

}