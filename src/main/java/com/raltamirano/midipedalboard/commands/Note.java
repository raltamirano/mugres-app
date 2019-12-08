package com.raltamirano.midipedalboard.commands;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.model.Action;
import com.raltamirano.midipedalboard.orchestration.Command;

import java.util.Map;

public class Note implements Command {
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void execute(final Pedalboard pedalboard,
                        final Action.Context context,
                        final Map<String, Object> parameters) {
        final int note = (Integer)parameters.get("note");
        final int velocity = (Integer)parameters.get("velocity");
        final int channel = (Integer)parameters.get("channel");

        if (context.isPedalDown())
            pedalboard.noteOn(note, velocity, channel, context.getTimestamp());
        else
            pedalboard.noteOff(note, channel, context.getTimestamp());
    }

    public static final String NAME = "Note";
}
