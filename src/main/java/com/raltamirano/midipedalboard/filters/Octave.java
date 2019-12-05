package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.model.Events;

import javax.sound.midi.ShortMessage;

public class Octave extends AbstractFilter {
    private final int octaveOffset;
    private final int octaveOffsetSemitones;

    public Octave(final int octaveOffset) {
        this.octaveOffset = octaveOffset;
        this.octaveOffsetSemitones = octaveOffset * 12;
    }

    @Override
    protected boolean canHandle(final Pedalboard pedalboard, final Events events) {
        return true;
    }

    @Override
    protected Events handle(final Pedalboard pedalboard, final Events events) {
        final Events result = Events.empty();

        for (final Events.Event e : events) {
            result.append(e);
            if (e.getMessage() instanceof ShortMessage) {
                final ShortMessage sm = (ShortMessage)e.getMessage();
                if (sm.getCommand() == ShortMessage.NOTE_ON || sm.getCommand() == ShortMessage.NOTE_OFF) {
                    final int targetNote = sm.getData1() + octaveOffsetSemitones;
                    if (targetNote >= 0 && targetNote <= 127) {
                        try {
                            final ShortMessage message = shortMessage(sm.getCommand(), sm.getChannel(),
                                    targetNote, sm.getData2());
                            result.append(message, e.getTimestamp() + 250);
                        } catch (final Throwable ignore) {}
                    }
                }
            }
        }

        return result;
    }
}
