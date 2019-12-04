package com.raltamirano.midipedalboard.commands;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.orchestration.Command;

import java.util.Map;

public class Note implements Command {
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void execute(final Pedalboard pedalboard,
                        final Map<String, Object> parameters) {
        final int note = (Integer)parameters.get("note");
        final int velocity = (Integer)parameters.get("velocity");
        final int channel = (Integer)parameters.get("channel");
        final int duration = (Integer)parameters.get("duration");

        pedalboard.noteOn(note, velocity, channel);
        pedalboard.noteOff(note, velocity, channel, duration);
    }

    public static final String NAME = "Note";
}
