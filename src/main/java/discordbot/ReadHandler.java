package discordbot;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioInputStream;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.TextChannel;

@Slf4j
public class ReadHandler implements AudioSendHandler {
    Queue<AudioInputStream> queue = new ConcurrentLinkedQueue<>();
    HashSet<TextChannel> readChannels = new HashSet<TextChannel>();

    @Override
    public boolean isOpus() {
        return false;
    }

    @Override
    public boolean canProvide() {
        return !queue.isEmpty();
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        byte[] dst = new byte[3840];
        try {
            if (queue.peek().available() >= dst.length) {
                queue.peek().read(dst);
            } else {
                queue.poll().read(dst);
            }
        } catch (Exception e) {
            log.error(""+e, e);
        }
        return ByteBuffer.wrap(dst);
    }

}