package com.raltamirano.midipedalboard.commands;

import com.raltamirano.midipedalboard.orchestration.Command;
import com.raltamirano.midipedalboard.orchestration.Orchestrator;

import java.util.Map;

public class Stop implements Command {
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void execute(final Orchestrator orchestrator,
                        final Map<String, Object> parameters) {
        orchestrator.stop();
    }

    public static final String NAME = "Stop";
}
