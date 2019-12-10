package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.common.Length;
import com.raltamirano.midipedalboard.common.Value;
import com.raltamirano.midipedalboard.model.Events;
import com.raltamirano.midipedalboard.model.Events.NoteEvent;

public class FixNoteLength extends AbstractFilter {
    private Length length;

    public FixNoteLength(final Value value) {
        this.length = value.length();
    }

    public FixNoteLength(final Length length) {
        this.length = length;
    }

    @Override
    protected boolean canHandle(final Pedalboard pedalboard, final Events events) {
        return events.noteEventsOnly();
    }

    @Override
    protected Events handle(final Pedalboard pedalboard, final Events events) {
        final Events result = Events.empty();

        final long valueInMillis = length.toMillis(pedalboard.getSong().getTempo());

        for (final NoteEvent e : events.noteEvents()) {
            if (e.isNoteOn()) {
                result.append(e);
                result.append(e.clone().toNoteOff().deltaTimestamp(valueInMillis).get());
            }
        }

        return result;
    }
}
