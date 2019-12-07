package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.model.Events;
import com.raltamirano.midipedalboard.model.Events.NoteEvent;

import java.util.HashMap;
import java.util.Map;

public class Legato extends AbstractFilter {
    private final Map<Integer, Integer> toggled;

    public Legato() {
        toggled = new HashMap<>();
        for(int channel = 0; channel < 16; channel++)
            toggled.put(channel, Integer.MIN_VALUE);
    }

    @Override
    protected boolean canHandle(final Pedalboard pedalboard, final Events events) {
        return events.noteEventsOnly();
    }

    @Override
    protected Events handle(final Pedalboard pedalboard, final Events events) {
        final Events result = Events.empty();

        for (final NoteEvent e : events.noteEvents()) {
            if (e.isNoteOn()) {
                int replaced = toggled.put(e.getChannel(), Integer.MIN_VALUE);

                if (replaced != Integer.MIN_VALUE)
                    result.append(NoteEvent.noteOff(e.getChannel(), replaced, e.getTimestamp()));

                if (replaced != e.getNote()) {
                    toggled.put(e.getChannel(), e.getNote());
                    result.append(e);
                }
            }
        }

        return result;
    }
}
