package com.raltamirano.midipedalboard.commands;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.model.Action;
import com.raltamirano.midipedalboard.orchestration.Command;

import java.util.Map;

public class Finish implements Command {
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void execute(final Pedalboard pedalboard,
                        final Action.Context context,
                        final Map<String, Object> parameters) {
        pedalboard.getOrchestrator().finish();
    }

    public static final String NAME = "Finish";
}
