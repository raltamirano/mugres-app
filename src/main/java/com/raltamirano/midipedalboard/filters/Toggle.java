package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.model.Events;
import com.raltamirano.midipedalboard.model.Events.NoteEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Toggle extends AbstractFilter {
    private final Map<Integer, Set<Integer>> toggled;

    public Toggle() {
        toggled = new HashMap<>();
        for(int channel = 0; channel < 16; channel++)
            toggled.put(channel, new HashSet<>());
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
                final Set<Integer> channelToggled = toggled.get(e.getChannel());
                if (channelToggled.remove(e.getNote())) {
                    // note were in toggled state, send note off
                    result.append(NoteEvent.noteOff(e.getChannel(), e.getNote(), e.getTimestamp()));
                } else {
                    channelToggled.add(e.getNote());
                    result.append(e);
                }
            }
        }

        return result;
    }
}
