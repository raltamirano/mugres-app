package com.raltamirano.midipedalboard;

import com.raltamirano.midipedalboard.model.Part;
import com.raltamirano.midipedalboard.model.Pattern;
import com.raltamirano.midipedalboard.model.Song;
import lombok.NonNull;

import javax.sound.midi.*;
import java.util.HashMap;
import java.util.Map;

import static com.raltamirano.midipedalboard.model.Part.setSequenceLength;

public class Orchestrator {
    private final Song song;
    private final Receiver outputPort;
    private final Sequencer sequencer;
    private final Map<String, Integer> playCounter = new HashMap<>();

    private Pattern playingPattern;
    private Pattern nextPattern;
    private Part grooveSectionA;
    private Part grooveSectionB;
    private Part fill;
    private boolean playingEndOfPattern = false;

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
                if (meta.getType() == END_OF_TRACK)
                    playNextPart();
            });

            aSequencer.open();
            return aSequencer;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private void playNextPart() {
        final boolean switchPattern = this.playingPattern == null || this.nextPattern != null;

        Sequence sequenceToPlay = null;
        if (playingEndOfPattern) {
            if (switchPattern)
                switchToNextPattern();
            sequenceToPlay = grooveSectionA.getSequence();
        } else {
            sequenceToPlay = switchPattern ? fill.getSequence() : grooveSectionB.getSequence();
        }

        playingEndOfPattern = !playingEndOfPattern;

        try {
            sequencer.setSequence(sequenceToPlay);
            sequencer.setTempoInBPM(playingPattern.getTempo() != 0 ? playingPattern.getTempo() : song.getTempo());
            sequencer.setTickPosition(0);
            sequencer.start();
        } catch (final InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }

    private void switchToNextPattern() {
        final Pattern patternToPlay = this.nextPattern != null ?
                this.nextPattern : this.playingPattern;

        this.playingPattern = patternToPlay;
        this.nextPattern = null;

        if (playingPattern == null)
            return;

        fill = chooseFill();

        // Split groove at fill's length
        final Part groove = chooseGroove();
        final Part[] grooveSections = groove.split(fill);

        grooveSectionA = grooveSections[0];
        grooveSectionB = grooveSections[1];

        // Make groove's section B and the fill the same length for a more accurate loop
        setSequenceLength(fill.getSequence(), grooveSectionB.getSequence().getTickLength());
    }

    private Part chooseGroove() {
        // TODO: Honor playingPattern.getGroovesMode()!
        return playingPattern.getGrooves().get(0);
    }

    private Part chooseFill() {
        // TODO: Honor playingPattern.getFillsMode()!
        return playingPattern.getFills().get(0);
    }

    public Orchestrator play(final String pattern) {
        if (playingPattern != null && pattern.equals(playingPattern.getName())) {
            this.nextPattern = null;
            return this;
        }

        this.nextPattern = song.getPattern(pattern);

        if (!sequencer.isRunning()) {
            playingEndOfPattern = true;
            playNextPart();
        }

        return this;
    }

    public Orchestrator stop() {
        if (sequencer.isRunning())
            sequencer.stop();

        this.playingPattern = null;
        this.nextPattern = null;

        return this;
    }

    private static final int END_OF_TRACK = 0x2F;
}
