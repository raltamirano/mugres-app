package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.model.Events;

import javax.sound.midi.MidiMessage;

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

    public void setNext(final AbstractFilter filter) {
        this.next = filter;
    }

    private Events doAccept(final Pedalboard pedalboard,
                                       final Events events) {
        return canHandle(pedalboard, events) ?
                handle(pedalboard, events) : events;
    }
}
