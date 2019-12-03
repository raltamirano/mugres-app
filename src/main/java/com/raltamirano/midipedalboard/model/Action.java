package com.raltamirano.midipedalboard.model;

import com.raltamirano.midipedalboard.Pedalboard;
import com.raltamirano.midipedalboard.orchestration.Command;
import lombok.Data;
import lombok.NonNull;

import java.util.*;

/** Commands to be executed when a pedal is pressed. */
@Data
public class Action {
    private final List<Step> steps = new ArrayList<>();

    public Action addStep(final Command command) {
        return addStep(command, Collections.EMPTY_MAP);
    }

    public Action addStep(final Command command, final Map<String, Object> parameters) {
        steps.add(new Step(command, parameters));
        return this;
    }

    public static Action of(final Command command) {
        return new Action().addStep(command);
    }

    public static Action of(final Command command,
                            final Map<String, Object> parameters) {
        return new Action().addStep(command, parameters);
    }

    public static Action of(final Command command,
                            final String parameter1, final Object value1) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put(parameter1, value1);
        return of(command, parameters);
    }

    public static Action of(final Command command,
                            final String parameter1, final Object value1,
                            final String parameter2, final Object value2) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put(parameter1, value1);
        parameters.put(parameter2, value2);
        return of(command, parameters);
    }

    public static Action of(final Command command,
                            final String parameter1, final Object value1,
                            final String parameter2, final Object value2,
                            final String parameter3, final Object value3) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put(parameter1, value1);
        parameters.put(parameter2, value2);
        parameters.put(parameter3, value3);
        return of(command, parameters);
    }

    public static Action of(final Command command,
                            final String parameter1, final Object value1,
                            final String parameter2, final Object value2,
                            final String parameter3, final Object value3,
                            final String parameter4, final Object value4) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put(parameter1, value1);
        parameters.put(parameter2, value2);
        parameters.put(parameter3, value3);
        parameters.put(parameter4, value4);
        return of(command, parameters);
    }

    public void execute(final Pedalboard pedalboard) {
        for(int index = 0; index < steps.size(); index++) {
            final Step step = steps.get(index);
            step.getCommand().execute(pedalboard, step.getParameters());
        }
    }

    public Action then(final Action next) {
        final Action newAction = new Action();

        for(int index = 0; index < steps.size(); index++)
            newAction.addStep(steps.get(index).command, steps.get(index).parameters);

        for(int index = 0; index < next.steps.size(); index++)
            newAction.addStep(next.steps.get(index).command, next.steps.get(index).parameters);

        return newAction;
    }

    @Data
    class Step {
        @NonNull
        private Command command;
        @NonNull
        private Map<String, Object> parameters;

        public Step(final Command command, final Map<String, Object> parameters) {
            this.command = command;
            this.parameters = parameters;
        }
    }
}
