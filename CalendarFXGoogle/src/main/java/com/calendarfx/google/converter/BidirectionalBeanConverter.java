package com.calendarfx.google.converter;

/**
 * Converter in both directions.
 *
 * Created by gdiaz on 28/04/2017.
 */
public interface BidirectionalBeanConverter<L, R> extends BeanConverter<L, R> {

    public R leftToRight (L left);

    public L rightToLeft (R right);

    @Override
    public default R convert(L source) {
        return leftToRight(source);
    }
}
