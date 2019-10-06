package com.raltamirano.midipedalboard.orchestration;

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
    private boolean finishing = false;

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

        Sequence sequenceToPlay;
        if (playingEndOfPattern) {
            if (finishing) {
                stop();
                return;
            }

            if (switchPattern)
                switchToNextPattern();
            sequenceToPlay = grooveSectionA.getSequence();
        } else {
            sequenceToPlay = switchPattern || finishing ? fill.getSequence() : grooveSectionB.getSequence();
        }

        final boolean splitGroove = this.grooveSectionB != null;
        if (splitGroove)
            playingEndOfPattern = !playingEndOfPattern;
        else
            playingEndOfPattern = true;

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
        this.playingPattern = this.nextPattern != null ?
                this.nextPattern : this.playingPattern;
        this.nextPattern = null;

        if (playingPattern == null)
            return;

        fill = chooseFill();

        // Split groove at fill's length
        final Part groove = chooseGroove();

        if (fill != null) {
            final Part[] grooveSections = groove.split(fill);
            grooveSectionA = grooveSections[0];
            grooveSectionB = grooveSections[1];

            // Make groove's section B and the fill the same length for a more accurate loop
            setSequenceLength(fill.getSequence(), grooveSectionB.getSequence().getTickLength());
        } else {
            groove.fixLength();
            grooveSectionA = groove;
            grooveSectionB = null;
        }
    }

    private Part chooseGroove() {
        if (playingPattern.getGrooves().isEmpty())
            throw new RuntimeException("No grooves defined for pattern: " + playingPattern.getName());

        // TODO: Honor playingPattern.getGroovesMode()!
        final Part part = playingPattern.getGrooves().get(0);
        return part; //.asClone();
    }

    private Part chooseFill() {
        if (playingPattern.getFills().isEmpty())
            return null;

        // TODO: Honor playingPattern.getFillsMode()!
        final Part part = playingPattern.getFills().get(0);
        return part; //.asClone();
    }

    public void play(final String pattern) {
        // Cancel request to finish playing
        finishing = false;

        if (playingPattern != null && pattern.equals(playingPattern.getName())) {
            this.nextPattern = null;
            return;
        }

        this.nextPattern = song.getPattern(pattern);

        if (!sequencer.isRunning()) {
            playingEndOfPattern = true;
            playNextPart();
        }
    }

    public void finish() {
        // Request to finish when it has been already
        // requested means: stop playing now!
        if (finishing)
            stop();
        else
            finishing = true;
    }

    public boolean isFinishing() {
        return finishing;
    }

    public void stop() {
        if (sequencer.isRunning())
            sequencer.stop();

        playingPattern = null;
        nextPattern = null;
        finishing = false;
    }

    private static final int END_OF_TRACK = 0x2F;
}
