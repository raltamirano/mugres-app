package com.raltamirano.midipedalboard.commands;

import com.raltamirano.midipedalboard.orchestration.Command;
import com.raltamirano.midipedalboard.orchestration.Orchestrator;

import java.util.Map;

public class Wait implements Command {
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void execute(final Orchestrator orchestrator,
                        final Map<String, Object> parameters) {
        final Long millis = (Long)parameters.get("millis");
        try {
            Thread.sleep(millis);
        } catch (final Throwable ignore) {}
    }

    public static final String NAME = "Wait";
    public static final long HALF_SECOND       =   500L;
    public static final long ONE_SECOND       =  1_000L;
    public static final long FIVE_SECONDS     =  5_000L;
    public static final long ONE_MINUTE       = 60_000L;
}
