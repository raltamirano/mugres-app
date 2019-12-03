package com.raltamirano.midipedalboard.commands;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.orchestration.Command;

import java.util.Map;

public class Play implements Command {
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void execute(final Pedalboard pedalboard,
                        final Map<String, Object> parameters) {
        pedalboard.getOrchestrator().play((String)parameters.get("pattern"));
    }

    public static final String NAME = "Play";
}
