package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;

import javax.sound.midi.MidiMessage;
import java.util.Collections;
import java.util.List;

public abstract class AbstractFilter {
    private AbstractFilter next;

    public AbstractFilter() {
        this(null);
    }

    public AbstractFilter(final AbstractFilter next) {
        this.next = next;
    }

    protected abstract boolean canHandle(final Pedalboard pedalboard,
                                         final List<MidiMessage> messages);

    protected abstract List<MidiMessage> handle(final Pedalboard pedalboard,
                                   final List<MidiMessage> messages);

    public final void accept(final Pedalboard pedalboard,
                       final MidiMessage message) {
        accept(pedalboard, Collections.singletonList(message));
    }

    public final void accept(final Pedalboard pedalboard,
                       final List<MidiMessage> messages) {
        List<MidiMessage> output =
                canHandle(pedalboard, messages) ?
                handle(pedalboard, messages) : messages;

        AbstractFilter nextFilter = next;
        while (nextFilter != null) {
            output = nextFilter.handle(pedalboard, output);
            nextFilter = nextFilter.getNext();
        }
    }

    public AbstractFilter getNext() {
        return next;
    }

    public void setNext(final AbstractFilter next) {
        this.next = next;
    }
}
