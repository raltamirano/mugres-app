package com.raltamirano.midipedalboard;

import com.raltamirano.midipedalboard.commands.*;
import com.raltamirano.midipedalboard.filters.AbstractFilter;
import com.raltamirano.midipedalboard.filters.Input;
import com.raltamirano.midipedalboard.filters.Output;
import com.raltamirano.midipedalboard.model.Action;
import com.raltamirano.midipedalboard.model.Song;
import com.raltamirano.midipedalboard.orchestration.Command;
import com.raltamirano.midipedalboard.orchestration.Orchestrator;
import lombok.Getter;
import lombok.NonNull;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static javax.sound.midi.ShortMessage.NOTE_ON;

public class Pedalboard {
    @NonNull
    @Getter
    private Song song;

    @NonNull
    @Getter
    private Orchestrator orchestrator;

    @NonNull
    @Getter
    private final Map<String, Command> commands = new HashMap<>();

    @NonNull
    @Getter
    private final Processor processor;

    public Pedalboard(final Receiver outputPort) {
        this(outputPort, new Song("Untitled", 120));
    }

    public Pedalboard(final Receiver outputPort, final Song song) {
        this.song = song;
        this.orchestrator = new Orchestrator(song, outputPort);
        this.processor = new Processor(this, outputPort);

        initCommands();
    }

    private void initCommands() {
        commands.clear();
        commands.put(NoOp.NAME, new NoOp());
        commands.put(Stop.NAME, new Stop());
        commands.put(Finish.NAME, new Finish());
        commands.put(Play.NAME, new Play());
        commands.put(Wait.NAME, new Wait());
        commands.put(Note.NAME, new Note());
    }

    /** Act upon a pedal being activated. */
    public void pedal(final int pedal) {
        final Action action = song.getAction(pedal);
        if (action != null)
            action.execute(this);
    }

    public void noteOn(final int note, final int velocity,
                        final int channel) {
        noteOn(note, velocity, channel, 0L);
    }

    public void noteOn(final int note, final int velocity,
                       final int channel, final long delayInMillis) {
        try {
            final ShortMessage message = new ShortMessage(NOTE_ON, channel, note, velocity);
            sendMidiMessage(message, delayInMillis);
        } catch (final InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }

    public void noteOff(final int note, final int velocity,
                        final int channel) {
        noteOff(note, velocity, channel, 0L);
    }

    public void noteOff(final int note, final int velocity,
                        final int channel, final long delayInMillis) {
        try {
            final ShortMessage message = new ShortMessage(NOTE_OFF, channel, note, velocity);
            sendMidiMessage(message, delayInMillis);
        } catch (final InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMidiMessage(final MidiMessage message, final long delayInMillis) {
        processor.process(message, delayInMillis);
    }

    public static class Processor {
        private final Pedalboard pedalboard;
        private final AbstractFilter filterChain;
        private final PriorityQueue<Pair> queue = new PriorityQueue<>(Comparator.comparingLong(Pair::getTimestamp));
        private final Thread worker = new Thread(() -> {
            while(true)
                try {
                    if (!queue.isEmpty()) {
                        if (queue.peek().timestamp <= System.currentTimeMillis()) {
                            final Pair pair = queue.poll();
                            getFilterChain().accept(getPedalboard(), pair.message);
                        }
                    }
                } catch (final Throwable ignore) {
                    // Do nothing!
                } finally {
                    try { Thread.sleep(5); } catch (final Throwable ignore) {}
                }
        });

        public Processor(final Pedalboard pedalboard, final Receiver outputPort) {
            this.pedalboard = pedalboard;
            filterChain = getDefaultOutputChain(outputPort);
            worker.start();
        }

        private Pedalboard getPedalboard() {
            return pedalboard;
        }

        private AbstractFilter getFilterChain() {
            return filterChain;
        }

        private AbstractFilter getDefaultOutputChain(final Receiver receiver) {
            return new Input(new Output(receiver));
        }

        public void process(final MidiMessage message, final long delayInMillis) {
            if (delayInMillis == 0L)
                filterChain.accept(pedalboard, message);
            else
                //throw new UnsupportedOperationException("Event scheduling not supported yet!");
                queue.add(new Pair(System.currentTimeMillis() + delayInMillis, message));
        }

        public Processor addBeforeOutput(final AbstractFilter filter) {
            AbstractFilter next = filterChain;
            while(!(next.getNext() instanceof Output))
                next = next.getNext();

            final Output output = (Output)next.getNext();
            next.setNext(filter);
            filter.setNext(output);

            return this;
        }

        private class Pair {
            private long timestamp;
            private MidiMessage message;

            public Pair(long timestamp, MidiMessage message) {
                this.timestamp = timestamp;
                this.message = message;
            }

            public long getTimestamp() {
                return timestamp;
            }

            public MidiMessage getMessage() {
                return message;
            }
        }
    }
}
