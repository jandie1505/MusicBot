package net.jandie1505.musicbot.console;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import net.jandie1505.musicbot.MusicBot;

public class CustomAppender extends AppenderBase<ILoggingEvent> {
    private Encoder<ILoggingEvent> encoder;

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        if (this.encoder != null) {
            MusicBot.LINE_READER.printAbove(new String(this.encoder.encode(iLoggingEvent)));
        } else {
            MusicBot.LINE_READER.printAbove(iLoggingEvent.getFormattedMessage());
        }
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }
}
