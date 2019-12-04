package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;

import javax.sound.midi.MidiMessage;
import java.util.Collections;
import java.util.List;

public abstract class AbstractFilter {
    private AbstractFilter next;

    AbstractFilter() {
        this(null);
    }

    AbstractFilter(final AbstractFilter next) {
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
        List<MidiMessage> output = doAccept(pedalboard, messages);

        AbstractFilter nextFilter = next;
        while (nextFilter != null) {
            output = nextFilter.doAccept(pedalboard, output);
            nextFilter = nextFilter.next;
        }
    }

    private List<MidiMessage> doAccept(final Pedalboard pedalboard,
                                       final List<MidiMessage> messages) {
        return canHandle(pedalboard, messages) ?
                handle(pedalboard, messages) : messages;
    }

    public boolean addBeforeOutput(final AbstractFilter filter) {
        if (this instanceof Output) {
            filter.next = this;
            return true;
        } else {
            AbstractFilter nextInChain = this;
            while(nextInChain != null && !(nextInChain.next instanceof Output))
                nextInChain = nextInChain.next;

            if (nextInChain != null) {
                final Output output = (Output) nextInChain.next;
                nextInChain.next = filter;
                filter.next = output;
                return true;
            } else {
                return false;
            }
        }
    }
}
