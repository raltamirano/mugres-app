package com.raltamirano.midipedalboard.model;

import lombok.Getter;
import lombok.NonNull;

import javax.sound.midi.*;
import java.io.File;

@Getter
public class Part {
    @NonNull
    private String name;
    @NonNull
    private Sequence sequence;

    private Part(String name, Sequence sequence) {
        this.name = name;
        this.sequence = sequence;
    }

    public static Part fromFile(@NonNull final File file) {
        try {
            final MidiFileFormat format = MidiSystem.getMidiFileFormat(file);
            final Sequence sequence = MidiSystem.getSequence(file);
            final Track track = sequence.getTracks()[(format.getType() == 0) ? 0 : 1];
            return new Part(file.getName(), toSequence(track, sequence.getDivisionType(), sequence.getResolution()));
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static Sequence toSequence(Track track, float divisionType, int resolution) {
        try {
            final Sequence sequence = new Sequence(divisionType, resolution, 1);
            final Track newTrack = sequence.getTracks()[0];
            for (int index = 0; index < track.size(); index++)
                newTrack.add(track.get(index));
            return sequence;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
