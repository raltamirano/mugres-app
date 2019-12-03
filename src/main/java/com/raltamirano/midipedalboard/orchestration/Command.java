package com.raltamirano.midipedalboard.orchestration;

import com.raltamirano.midipedalboard.Pedalboard;

import java.util.Map;

/** Command. */
public interface Command {
    /** Returns this command's name. */
    String getName();

    /** Executes this command. */
    void execute(final Pedalboard pedalboard,
                 final Map<String, Object> parameters);
}
