package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.common.Length;
import com.raltamirano.midipedalboard.common.Value;
import com.raltamirano.midipedalboard.model.Events;
import com.raltamirano.midipedalboard.model.Events.NoteEvent;
import com.raltamirano.midipedalboard.model.Song;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Arp extends AbstractFilter {
    private String pattern;

    public Arp(final String pattern) {
        this.pattern = pattern;
    }

    @Override
    protected boolean canHandle(final Pedalboard pedalboard, final Events events) {
        return events.noteEventsOnly();
    }

    @Override
    protected Events handle(final Pedalboard pedalboard, final Events events) {
        final Events result = Events.empty();

        final Song song = pedalboard.getSong();
        final List<NoteEvent> noteEvents = events.noteEvents();

        Length acummulated = Length.ZERO;
        final Matcher matcher = ARP_PATTERN.matcher(pattern);
        while(matcher.find()) {
            final String whatToPlay = matcher.group(1);
            final Value duration = matcher.group(2) != null ? Value.forId(matcher.group(2)) :
                    song.getTimeSignature().getDenominator();

            if (REST.equals(whatToPlay)) {
                throw new UnsupportedOperationException("Rests in Arp pattern not available yet!");
            } else {
                final int eventIndex = Integer.parseInt(whatToPlay);
                if (eventIndex <= noteEvents.size()) {
                    result.append(noteEvents.get(eventIndex - 1)
                            .clone()
                            .deltaTimestamp(acummulated.toMillis(song.getTempo()))
                            .get());
                    acummulated = acummulated.plus(duration.length());
                }
            }
        }

        if (result.isEmpty())
            return events;

        return result;
    }

    private static final String REST = "R";
    private static final Pattern ARP_PATTERN = Pattern.compile("([1-9]|" + REST + ")(w|h|q|e|s|t|m)?");
}
