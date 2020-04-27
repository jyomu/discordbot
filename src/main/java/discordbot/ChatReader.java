package discordbot;

import java.util.HashSet;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class ChatReader extends ListenerAdapter {
    private HashSet<TextChannel> readChannels = new HashSet<TextChannel>();
    private ReadHandler readHandler = new ReadHandler();
    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (Pattern.matches("!jyomubot\\s++read\\s++start", event.getMessage().getContentRaw())) {
            readChannels.add(event.getTextChannel());
            System.out.println(readChannels.size());
            AudioManager audioManager = event.getGuild().getAudioManager();
            audioManager.openAudioConnection(event.getMember().getVoiceState().getChannel());
            audioManager.setSendingHandler(readHandler);
        }
        if (Pattern.matches("!jyomubot\\s++read\\s++stop", event.getMessage().getContentRaw())) {
            readChannels.remove(event.getTextChannel());
            System.out.println(readChannels.size());
        }
    }

}