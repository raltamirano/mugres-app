package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.model.Events;

public final class Input extends AbstractFilter {
    public Input(final AbstractFilter next) {
        super(next);
    }

    @Override
    protected boolean canHandle(final Pedalboard pedalboard, final Events events) {
        return true;
    }

    @Override
    protected Events handle(final Pedalboard pedalboard, final Events events) {
        return events;
    }
}
