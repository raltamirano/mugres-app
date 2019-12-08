package com.raltamirano.midipedalboard.filters;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.model.Events;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Monitor extends AbstractFilter {
    private final String label;

    public Monitor(final String label) {
        this.label = label;
    }

    @Override
    protected boolean canHandle(final Pedalboard pedalboard, final Events events) {
        return true;
    }

    @Override
    protected Events handle(final Pedalboard pedalboard, final Events events) {
        events.forEach(e -> System.out.println(String.format("%s [%-10s]%s",
                TIME_FORMAT.format(new Date()), label, e)));
        return events;
    }

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm:ss.SSS");
}
