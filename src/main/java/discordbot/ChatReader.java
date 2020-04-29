package discordbot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class ChatReader extends ListenerAdapter {
    private HashSet<TextChannel> readChannels = new HashSet<TextChannel>();
    private ReadHandler readHandler = new ReadHandler();

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (Pattern.matches("[;；].*", event.getMessage().getContentRaw())) {
            return;
        }
        if (Pattern.matches("(?i)http.*", event.getMessage().getContentRaw())) {
            read(event.getMember().getEffectiveName()+" url省略");
            return;
        }
        if (Pattern.matches("!jyomubot\\s++read\\s++start", event.getMessage().getContentRaw())) {
            readChannels.add(event.getTextChannel());
            System.out.println(readChannels.size());
            AudioManager audioManager = event.getGuild().getAudioManager();
            audioManager.setSendingHandler(readHandler);
            audioManager.openAudioConnection(event.getMember().getVoiceState().getChannel());
            return;
        }
        if (Pattern.matches("!jyomubot\\s++read\\s++stop", event.getMessage().getContentRaw())) {
            readChannels.remove(event.getTextChannel());
            System.out.println(readChannels.size());
            if (readChannels.size() == 0) {
                event.getGuild().getAudioManager().closeAudioConnection();
            }
            return;
        }
        // ここまでreturn

        if (readChannels.contains(event.getChannel())) {
            StringBuilder text = new StringBuilder();
            text.append(event.getMember().getEffectiveName());
            text.append(" ");
            text.append(event.getMessage().getContentDisplay());
            read(text.toString());
        }
    }

    public void read(String text) {
        try {
            readHandler.queue.add(synthesize(text));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public AudioInputStream synthesize(String text) throws IOException {
        System.out.println(text);
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Build the voice request, select the language code ("en-US") and the ssml
            // voice gender
            // ("neutral")
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("ja-JP")
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL).build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16)
                    .setSpeakingRate(1.0f).setPitch(1.0f).setVolumeGainDb(0.0f).setSampleRateHertz(48000).build();
            System.out.println("hz" + audioConfig.getSampleRateHertz());
            // Perform the text-to-speech request on the text input with the selected voice
            // parameters and
            // audio file type
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            // ByteString audioContents = response.getAudioContent();
            byte[] data = response.getAudioContent().toByteArray();
            AudioFormat target = new AudioFormat(48000f, 16, 2, true, true);
            AudioInputStream is = AudioSystem.getAudioInputStream(target, new AudioInputStream(
                    new ByteArrayInputStream(data), new AudioFormat(48000f, 16, 1, true, false), data.length));
            return is;
        }
    }
}