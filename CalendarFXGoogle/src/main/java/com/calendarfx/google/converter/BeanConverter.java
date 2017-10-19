package com.calendarfx.google.converter;

/**
 * Interface representing a converter from/to a single bean.
 *
 * Created by gdiaz on 20/02/2017.
 */
public interface BeanConverter<S, T> {

	/**
	 * Converts the given source into an object of type defined by the target.
	 * @param source The object source to be converted.
	 * @return The target result of the convertion.
	 */
	public T convert (S source);

}
