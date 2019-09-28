package com.raltamirano.midipedalboard.model;

import lombok.Data;
import lombok.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
public class Pattern {
    @NonNull
    private String name;
    @NonNull
    private int tempo;
    @NonNull
    private Pattern.Mode groovesMode;
    @NonNull
    private Pattern.Mode fillsMode;
    @NonNull
    private List<Part> grooves = new ArrayList<>();
    @NonNull
    private List<Part> fills = new ArrayList<>();

    public Pattern appendGroove(@NonNull final File file) {
        return appendGroove(Part.fromFile(file));
    }

    public Pattern appendGroove(@NonNull final Part part) {
        grooves.add(part);
        return this;
    }

    public Pattern appendFill(@NonNull final File file) {
        return appendFill(Part.fromFile(file));
    }

    public Pattern appendFill(@NonNull final Part part) {
        fills.add(part);
        return this;
    }

    public enum Mode {
        SEQUENCE,
        RANDOM
    }
}
