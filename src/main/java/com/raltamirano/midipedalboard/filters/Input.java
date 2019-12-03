package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;

import javax.sound.midi.MidiMessage;
import java.util.List;

public final class Input extends AbstractFilter {
    public Input(final AbstractFilter next) {
        super(next);
    }

    @Override
    protected boolean canHandle(Pedalboard pedalboard, List<MidiMessage> messages) {
        return true;
    }

    @Override
    protected List<MidiMessage> handle(Pedalboard pedalboard, List<MidiMessage> messages) {
        return messages;
    }
}
