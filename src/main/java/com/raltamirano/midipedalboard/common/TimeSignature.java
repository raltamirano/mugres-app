package com.raltamirano.midipedalboard.common;

import lombok.Data;

@Data
public class TimeSignature {
    private int numerator;
    private Value denominator;

    private TimeSignature(final int numerator, final Value denominator) {
        if (numerator <= 0)
            throw new IllegalArgumentException("Time signature numerator must be: 1 <= x <= 128");
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public static TimeSignature of(final int numerator, final Value denominator) {
        return new TimeSignature(numerator, denominator);
    }

    /** 3/4 */
    public static final TimeSignature TS34 = of(3, Value.QUARTER);
    /** 4/4 */
    public static final TimeSignature TS44 = of(4, Value.QUARTER);
    /** 6/8 */
    public static final TimeSignature TS68 = of(6, Value.EIGHTH);
    /** 8/8 */
    public static final TimeSignature TS88 = of(8, Value.EIGHTH);
    /** 12/8 */
    public static final TimeSignature TS128 = of(12, Value.EIGHTH);
}
