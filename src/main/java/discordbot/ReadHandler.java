package discordbot;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioInputStream;

import net.dv8tion.jda.api.audio.AudioSendHandler;

public class ReadHandler implements AudioSendHandler {
    Queue<AudioInputStream> queue = new ConcurrentLinkedQueue<>();

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
            System.out.println(e);
            System.exit(-1);
        }
        return ByteBuffer.wrap(dst);
    }

}