package com.raltamirano.midipedalboard;

import com.raltamirano.midipedalboard.commands.*;
import com.raltamirano.midipedalboard.model.Action;
import com.raltamirano.midipedalboard.model.Song;
import com.raltamirano.midipedalboard.orchestration.Command;
import com.raltamirano.midipedalboard.orchestration.Orchestrator;
import lombok.Getter;
import lombok.NonNull;
import mugres.core.common.Context;
import mugres.core.filters.AbstractFilter;
import mugres.core.filters.Input;
import mugres.core.filters.Output;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static javax.sound.midi.ShortMessage.NOTE_ON;

public class Pedalboard implements Context {
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

    @NonNull
    @Getter
    private long lastEventTimestamp = Long.MIN_VALUE;

    public Pedalboard(final Receiver outputPort) {
        this(outputPort, new Song("Untitled"));
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
    public void pedal(final int pedal, boolean down) {
        lastEventTimestamp = System.currentTimeMillis();

        final Action action = song.getAction(pedal);
        final Action.Context context = Action.Context.of(pedal, down, lastEventTimestamp);
        if (action != null)
            action.execute(this, context);
    }

    public void noteOn(final int note, final int velocity,
                       final int channel, final long timestamp) {
        try {
            final ShortMessage message = new ShortMessage(NOTE_ON, channel, note, velocity);
            processMidiMessage(message, timestamp);
        } catch (final InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }

    public void noteOff(final int note, final int channel, final long timestamp) {
        try {
            final ShortMessage message = new ShortMessage(NOTE_OFF, channel, note, 0);
            processMidiMessage(message, timestamp);
        } catch (final InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }

    private void processMidiMessage(final MidiMessage message, final long timestamp) {
        processor.process(message, timestamp);
    }

    public static class Processor {
        private final Pedalboard pedalboard;
        private Input input;
        private Output output;
        private AbstractFilter filterChain;
        private final List<AbstractFilter> filters = new ArrayList<>();

        public Processor(final Pedalboard pedalboard, final Receiver outputPort) {
            this.pedalboard = pedalboard;

            this.input = new Input();
            this.output = new Output(outputPort);

            updateFilterChain();
        }

        private void updateFilterChain() {
            filterChain = input;
            AbstractFilter last = filterChain;
            for(int index = 0; index < filters.size(); index++) {
                last.setNext(filters.get(index));
                last = filters.get(index);
            }
            last.setNext(output);
        }

        public void process(final MidiMessage message, final long timestamp) {
            filterChain.accept(pedalboard, message, timestamp);
        }

        public void appendFilter(final AbstractFilter filter) {
            filters.add(filter);
            updateFilterChain();
        }
    }
}
