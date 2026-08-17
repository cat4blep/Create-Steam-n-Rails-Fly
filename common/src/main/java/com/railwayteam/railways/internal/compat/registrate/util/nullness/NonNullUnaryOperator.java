package com.railwayteam.railways.internal.compat.registrate.util.nullness;

@FunctionalInterface
public interface NonNullUnaryOperator<T> extends java.util.function.UnaryOperator<T> {
    static <T> NonNullUnaryOperator<T> identity() {
        return t -> t;
    }
}
