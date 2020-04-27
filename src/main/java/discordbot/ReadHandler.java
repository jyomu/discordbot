package discordbot;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.dv8tion.jda.api.audio.AudioSendHandler;

public class ReadHandler implements AudioSendHandler {
    Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

    @Override
    public boolean canProvide() {
        return !queue.isEmpty();
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        byte[] data = queue.poll();
        return data == null ? null : ByteBuffer.wrap(data); // Wrap this in a java.nio.ByteBuffer
    }

}