package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import java.util.Collections;
import java.util.List;

public final class Output extends AbstractFilter {
    private final Receiver outputPort;

    public Output(final Receiver outputPort) {
        this.outputPort = outputPort;
    }

    @Override
    protected boolean canHandle(Pedalboard pedalboard, List<MidiMessage> messages) {
        return true;
    }

    @Override
    protected List<MidiMessage> handle(Pedalboard pedalboard, List<MidiMessage> messages) {
        for(MidiMessage mm : messages)
            outputPort.send(mm, -1);

        return Collections.emptyList();
    }
}
