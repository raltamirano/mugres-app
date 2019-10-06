package com.raltamirano.midipedalboard.orchestration;

import java.util.Map;

/** Command. */
public interface Command {
    /** Returns this command's name. */
    String getName();

    /** Executes this command. */
    void execute(final Orchestrator orchestrator,
                 final Map<String, Object> parameters);
}
