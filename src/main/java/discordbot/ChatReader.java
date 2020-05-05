package discordbot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

@Slf4j
public class ChatReader extends ListenerAdapter {
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getGuild().getAudioManager().setSendingHandler(new ReadHandler());
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        if (Pattern.matches("!jyomubot\\s++read\\s++start", event.getMessage().getContentRaw())) {
            final AudioManager audioManager = event.getGuild().getAudioManager();
            audioManager.openAudioConnection(event.getMember().getVoiceState().getChannel());
            getReadChannels(event).add(event.getChannel());
            return;
        }
        if (Pattern.matches("!jyomubot\\s++read\\s++stop", event.getMessage().getContentRaw())) {
            getReadChannels(event).remove(event.getChannel());
            if (getReadChannels(event).size() == 0) {
                event.getGuild().getAudioManager().closeAudioConnection();
            }
            return;
        }
        // ここまでreturn

        if (getReadChannels(event).contains(event.getChannel())) {// read
            AudioSendHandler audioSendHandler = event.getGuild().getAudioManager().getSendingHandler();
            if (Pattern.matches("[;；!].*", event.getMessage().getContentRaw())) {
                return;
            }
            if (Pattern.matches("(?i)http.*", event.getMessage().getContentRaw())) {
                addReaded(audioSendHandler, event.getMember().getEffectiveName() + " url省略");
                return;
            }
            addReaded(audioSendHandler,
                    event.getMember().getEffectiveName() + " " + event.getMessage().getContentDisplay());
            return;
        }
    }

    @Nullable
    private HashSet<TextChannel> getReadChannels(GenericGuildEvent event) {
        AudioSendHandler audioSendHandler = event.getGuild().getAudioManager().getSendingHandler();
        ReadHandler readHandler = null;
        if (audioSendHandler instanceof ReadHandler) {
            readHandler = (ReadHandler) audioSendHandler;
        } else {
            log.error("getReadChannels:audioSendHandler instanceof ReadHandler is false");
        }
        return readHandler.readChannels;
    }

    public void addReaded(@NonNull AudioSendHandler dst, @NonNull String text) {
        if (dst instanceof ReadHandler) {
            ((ReadHandler) dst).queue.add(synthesize(text));
        } else {
            log.warn("addReadedにReadHandler以外が投げられた");
        }
    }

    public AudioInputStream synthesize(@NonNull String text) {
        System.out.println(text);
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the text input to be synthesized
            final SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Build the voice request, select the language code ("en-US") and the ssml
            // voice gender
            // ("neutral")
            final VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("ja-JP")
                    .setName("ja-JP-Standard-A").build();

            // Select the type of audio file you want returned
            final AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16)
                    .setSpeakingRate(1.0f).setPitch(1.0f).setVolumeGainDb(-3.0f).setSampleRateHertz(48000).build();
            // Perform the text-to-speech request on the text input with the selected voice
            // parameters and
            // audio file type
            final SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            final AudioFormat target = new AudioFormat(48000f, 16, 2, true, true);
            final byte[] res = response.getAudioContent().toByteArray();
            final AudioInputStream sourceStream = new AudioInputStream(new ByteArrayInputStream(res),
                    new AudioFormat(48000f, 16, 1, true, false), res.length);
            return AudioSystem.getAudioInputStream(target, sourceStream);
        } catch (IOException e) {
            log.warn("faild to create TextToSpeechClient:" + e, e);
            System.exit(-1);
            return null;
        }
    }

    @Override
    public void onGuildVoiceLeave(final GuildVoiceLeaveEvent event) {
        final AudioManager audioManager = event.getGuild().getAudioManager();
        if (event.getJDA().getSelfUser().equals(event.getMember().getUser())
                || (audioManager.getConnectedChannel() != null && audioManager.getConnectedChannel().getMembers()
                        .stream().filter(m -> !m.getUser().isBot()).count() == 0)) {
            audioManager.closeAudioConnection();
            getReadChannels(event).clear();
        }
    }
}