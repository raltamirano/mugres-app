package com.raltamirano.midipedalboard.commands;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.orchestration.Command;

import java.util.Map;

public class NoOp implements Command {
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void execute(final Pedalboard pedalboard,
                        final Map<String, Object> parameters) {
        // Do nothing!
    }

    public static final String NAME = "NoOp";
}
