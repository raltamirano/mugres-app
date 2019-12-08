package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.common.Key;
import com.raltamirano.midipedalboard.common.Pitch;
import com.raltamirano.midipedalboard.model.Events;
import com.raltamirano.midipedalboard.model.Events.NoteEvent;

import java.util.List;

public class Chord extends AbstractFilter {
    @Override
    protected boolean canHandle(final Pedalboard pedalboard, final Events events) {
        return events.noteEventsOnly();
    }

    @Override
    protected Events handle(final Pedalboard pedalboard, final Events events) {
        final Events result = Events.empty();
        final Key key = pedalboard.getSong().getKey();

        for (final NoteEvent e : events.noteEvents()) {
            final List<Pitch> chordPitches = key.chord(Pitch.of(e.getNote()));
            if (chordPitches.isEmpty())
                result.append(e);
            else
                if (e.isNoteOn())
                    for (final Pitch p : chordPitches)
                        result.append(NoteEvent.noteOn(e.getChannel(), p.getMidi(), e.getVelocity(), e.getTimestamp()));
                else
                    for (final Pitch p : chordPitches)
                        result.append(NoteEvent.noteOff(e.getChannel(), p.getMidi(), e.getTimestamp()));
        }

        return result;
    }
}
