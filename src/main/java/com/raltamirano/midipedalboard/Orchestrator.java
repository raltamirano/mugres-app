package com.raltamirano.midipedalboard;

import com.raltamirano.midipedalboard.model.Pattern;
import com.raltamirano.midipedalboard.model.Song;
import lombok.NonNull;

import javax.sound.midi.*;

import java.util.HashMap;
import java.util.Map;

public class Orchestrator {
    private final Song song;
    private final Receiver outputPort;
    private final Sequencer sequencer;
    private final Map<String, Integer> playCounter = new HashMap<>();

    private Pattern playingPattern;
    private Pattern nextPattern;

    public Orchestrator(@NonNull final Song song,
                        @NonNull final Receiver outputPort) {
        this.song = song;
        this.outputPort = outputPort;
        this.sequencer = createSequencer();
    }

    private Sequencer createSequencer() {
        try {
            final Sequencer aSequencer = MidiSystem.getSequencer(false);
            aSequencer.getTransmitter().setReceiver(outputPort);
            aSequencer.addMetaEventListener(meta -> {
                if (meta.getType() == 47)
                    playPattern();
            });

            aSequencer.open();
            return aSequencer;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private void playPattern() {
        final boolean repeating = this.playingPattern != null && this.nextPattern == null;
        final Pattern patternToPlay = this.nextPattern != null ?
                this.nextPattern : this.playingPattern;

        this.playingPattern = patternToPlay;
        this.nextPattern = null;

        if (patternToPlay == null)
            return;

        try {
            if (!repeating)
                sequencer.setSequence(patternToPlay.getGrooves().get(0).getSequence());
            sequencer.setTickPosition(0);
            sequencer.start();
        } catch (final InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }

    public Orchestrator play(final String pattern) {
        if (playingPattern != null && pattern.equals(playingPattern.getName())) {
            this.nextPattern = null;
            return this;
        }

        this.nextPattern = song.getPattern(pattern);

        if (!sequencer.isRunning())
            playPattern();

        return this;
    }

    public Orchestrator stop() {
        if (sequencer.isRunning())
            sequencer.stop();

        this.playingPattern = null;
        this.nextPattern = null;

        return this;
    }
}
