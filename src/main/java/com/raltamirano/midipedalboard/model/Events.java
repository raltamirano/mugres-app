package com.raltamirano.midipedalboard.model;

import javax.sound.midi.MidiMessage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Events implements Iterable<Events.Event> {
    private final List<Event> eventList = new ArrayList<>();

    public Events append(final MidiMessage message, final long timestamp) {
        eventList.add(new Event(message, timestamp));
        return this;
    }

    public Events append(final Event event) {
        eventList.add(event);
        return this;
    }

    public static Events empty() {
        return new Events();
    }

    public static Events of(final MidiMessage message) {
        return new Events().append(message, System.currentTimeMillis());
    }

    public static Events of(final MidiMessage message,
                            final long timestamp) {
        return new Events().append(message, timestamp);
    }

    public static Events of(final Event event) {
        return new Events().append(event);
    }

    @Override
    public Iterator<Event> iterator() {
        return eventList.iterator();
    }

    @Override
    public void forEach(Consumer<? super Event> action) {
        eventList.forEach(action);
    }

    @Override
    public Spliterator<Event> spliterator() {
        return eventList.spliterator();
    }

    public static class Event {
        private final long timestamp;
        private final MidiMessage message;

        private Event(final MidiMessage message, final long timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }

        public static Event of(final MidiMessage message, final long timestamp) {
            return new Event(message, timestamp);
        }

        public long getTimestamp() {
            return timestamp;
        }

        public MidiMessage getMessage() {
            return message;
        }
    }
}
