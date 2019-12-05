package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.model.Events;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

public abstract class AbstractFilter {
    private AbstractFilter next;

    AbstractFilter() {
        this(null);
    }

    AbstractFilter(final AbstractFilter next) {
        this.next = next;
    }

    protected abstract boolean canHandle(final Pedalboard pedalboard,
                                         final Events events);

    protected abstract Events handle(final Pedalboard pedalboard,
                                   final Events events);

    public final void accept(final Pedalboard pedalboard,
                             final MidiMessage message,
                             final long timestamp) {
        accept(pedalboard, Events.of(message, timestamp));
    }

    public final void accept(final Pedalboard pedalboard,
                       final Events events) {
        Events output = doAccept(pedalboard, events);

        AbstractFilter nextFilter = next;
        while (nextFilter != null) {
            output = nextFilter.doAccept(pedalboard, output);
            nextFilter = nextFilter.next;
        }
    }

    private Events doAccept(final Pedalboard pedalboard,
                                       final Events events) {
        return canHandle(pedalboard, events) ?
                handle(pedalboard, events) : events;
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

    protected ShortMessage shortMessage(final int command,
                                        final int channel,
                                        final int data1,
                                        final int data2) {
        try {
            return new ShortMessage(command, channel, data1, data2);
        } catch (final InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }
}
