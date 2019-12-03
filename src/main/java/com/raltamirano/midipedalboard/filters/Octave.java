package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;
import java.util.List;

public class Octave extends AbstractFilter {
    private final int octaveOffset;
    private final int octaveOffsetSemitones;

    public Octave() {
        this(-1);
    }

    public Octave(final int octaveOffset) {
        this.octaveOffset = octaveOffset;
        this.octaveOffsetSemitones = octaveOffset * 12;
    }

    @Override
    protected boolean canHandle(Pedalboard pedalboard, List<MidiMessage> messages) {
        return true;
    }

    @Override
    protected List<MidiMessage> handle(Pedalboard pedalboard, List<MidiMessage> messages) {
        final List<MidiMessage> result = new ArrayList<>();

        for (final MidiMessage mm : messages) {
            result.add(mm);
            if (mm instanceof ShortMessage) {
                final ShortMessage sm = (ShortMessage)mm;
                if (sm.getCommand() == ShortMessage.NOTE_ON || sm.getCommand() == ShortMessage.NOTE_OFF) {
                    final int targetNote = sm.getData1() + octaveOffsetSemitones;
                    if (targetNote >= 0 && targetNote <= 127) {
                        try {
                            result.add(new ShortMessage(sm.getCommand(), sm.getChannel(), targetNote, sm.getData2()));
                        } catch (final Throwable ignore) {}
                    }
                }
            }
        }

        return result;
    }
}
