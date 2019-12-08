package com.raltamirano.midipedalboard.orchestration;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.model.Action;

import java.util.Map;

/** Command. */
public interface Command {
    /** Returns this command's name. */
    String getName();

    /** Executes this command. */
    void execute(final Pedalboard pedalboard,
                 final Action.Context context,
                 final Map<String, Object> parameters);
}
