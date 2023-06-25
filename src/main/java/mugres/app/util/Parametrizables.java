package mugres.app.util;

import javafx.beans.property.adapter.JavaBeanIntegerProperty;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanStringProperty;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import mugres.parametrizable.Parametrizable;
import mugres.tracker.Track;

public class Parametrizables {
    private Parametrizables() {}

    public static JavaBeanStringProperty createStringProperty(final Parametrizable parametrizable,
                                                              final String parameter) {
        final JavaBeanStringPropertyBuilder builder = JavaBeanStringPropertyBuilder.create()
                .beanClass(Track.class)
                .setter(parameter)
                .getter(parameter)
                .name(parameter);
        try {
            return builder.bean(parametrizable).build();
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static JavaBeanIntegerProperty createIntegerProperty(final Parametrizable parametrizable,
                                                                final String parameter) {
        final JavaBeanIntegerPropertyBuilder builder = JavaBeanIntegerPropertyBuilder.create()
                .beanClass(Track.class)
                .setter(parameter)
                .getter(parameter)
                .name(parameter);
        try {
            return builder.bean(parametrizable).build();
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static JavaBeanObjectProperty createObjectProperty(final Parametrizable parametrizable,
                                                              final String parameter) {
        final JavaBeanObjectPropertyBuilder builder = JavaBeanObjectPropertyBuilder.create()
                .beanClass(Track.class)
                .setter(parameter)
                .getter(parameter)
                .name(parameter);
        try {
            return builder.bean(parametrizable).build();
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
