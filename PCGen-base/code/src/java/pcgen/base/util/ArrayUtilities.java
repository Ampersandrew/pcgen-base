/*
 * Copyright 2017 (C) Tom Parker <thpr@users.sourceforge.net>
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */
package pcgen.base.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

/**
 * ArrayUtilities is a utility class designed to provide utility methods when working with
 * Arrays.
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class ArrayUtilities
{

	private ArrayUtilities()
	{
		//Do not construct utility class
	}

	/**
	 * Returns an IntFunction that will generate an array of a specific class, based on
	 * the length provided to the IntFunction. Intended to be used in a Stream.
	 * 
	 * @param arrayClass
	 *            The class of object for the array to be generated by the IntFunction
	 * @return An IntFunction that will generate an array of a specific class, based on
	 *         the length provided to the IntFunction
	 * @param <T>
	 *            The component type of the Array to be generated
	 */
	@SuppressWarnings("unchecked")
	public static <T> IntFunction<T[]> buildOfClass(Class<T> arrayClass)
	{
		return size -> (T[]) Array.newInstance(arrayClass, size);
	}

	/**
	 * Returns an empty (and properly-cast) Array of a given Class
	 * 
	 * @param componentClass
	 *            The Class for which an empty Array will be built
	 * @param <T>
	 *            The format of the class for which the empty array will be built
	 * @return An empty (and properly-cast) Array of a given Class
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] buildEmpty(Class<T> componentClass)
	{
		return (T[]) Array.newInstance(componentClass, 0);
	}

	/**
	 * Returns an Array matching the class of the given array. If the given array is less
	 * than the size provided, then a new Array of the same type will be constructed.
	 * Otherwise, the given array will be returned.
	 * 
	 * @param minSize
	 *            The minimum size of the array to be returned
	 * @param candidateArray
	 *            The candidate array to be used as a template or returned
	 * @return An Array matching the class of the given array. If the size is appropriate,
	 *         the candidate array will be returned
	 * @param <T>
	 *            The component type of the Array to be returned
	 */
	public static <T> T[] ensureSizedArray(int minSize, T[] candidateArray)
	{
		//Protect against small array
		if (candidateArray.length < minSize)
		{
			Class<?> retClass = candidateArray.getClass().getComponentType();
			@SuppressWarnings("unchecked")
			T[] array = (T[]) Array.newInstance(retClass, minSize);
			return array;
		}
		return candidateArray;
	}

	/**
	 * Returns an IntFunction that will generate an array of a specific class, based on
	 * the candidate array provided and the length provided to the IntFunction. Intended
	 * to be used in a Stream.
	 * 
	 * @param candidateArray
	 *            The candidate array to be used as a template for a new array, or
	 *            returned if possible
	 * @return An IntFunction that will generate an array of a specific class, based on
	 *         the candidate array provided and the length provided to the IntFunction
	 * @param <T>
	 *            The component type of the Array to be returned by the IntFunction
	 */
	public static <T> IntFunction<T[]> usingArray(T[] candidateArray)
	{
		return x -> ensureSizedArray(x, candidateArray);
	}

	/**
	 * Merges two arrays of a given Class.
	 * 
	 * If either array is null, the other array is directly returned. If either array is
	 * empty, the other array is directly returned. If one is null and the other is empty,
	 * the empty array is returned. Note in NONE of those cases is the returned array
	 * different than the original - so this method is NOT insulating against modification
	 * of the original arrays.
	 *
	 * @param arrayClass
	 *            The class of the resulting array to be built
	 * @param first
	 *            The first array to be merged
	 * @param second
	 *            The second array to be merged
	 * @return A merged array containing the contents of the two given arrays
	 * @param <T>
	 *            The component type of the Arrays to be merged
	 */
	public static <T> T[] mergeArray(Class<T> arrayClass, T[] first, T[] second)
	{
		if (first == null)
		{
			return second;
		}
		if ((second == null) || (second.length == 0))
		{
			return first;
		}
		if (first.length == 0)
		{
			return second;
		}
		T[] returnArray = buildOfClass(arrayClass).apply(first.length + second.length);
		System.arraycopy(first, 0, returnArray, 0, first.length);
		System.arraycopy(second, 0, returnArray, first.length, second.length);
		return returnArray;
	}

	/**
	 * Calculates the difference between two arrays, using identity comparison.
	 * 
	 * @param oldArray
	 *            The "old" array for comparison
	 * @param newArray
	 *            The "new" array for comparison
	 * @return A Tuple indicating the items removed (present in the "old" array but not
	 *         the "new" array) and the items added (present in the "new" array, but not
	 *         the "old" array)
	 * @param <T>
	 *            The component type of the Arrays to be compared
	 */
	public static <T> Tuple<List<T>, List<T>> calculateIdentityDifference(T[] oldArray, T[] newArray)
	{
		List<T> oldList = Arrays.asList(oldArray);
		List<T> newList = new IdentityList<>(Arrays.asList(newArray));
		return processComparison(oldList, newList);
	}

	/**
	 * Calculates the difference between two arrays, using object .equals comparison.
	 * 
	 * @param oldArray
	 *            The "old" array for comparison
	 * @param newArray
	 *            The "new" array for comparison
	 * @return A Tuple indicating the items removed (present in the "old" array but not
	 *         the "new" array) and the items added (present in the "new" array, but not
	 *         the "old" array)
	 * @param <T>
	 *            The component type of the Arrays to be compared
	 */
	public static <T> Tuple<List<T>, List<T>> calculateDifference(T[] oldArray, T[] newArray)
	{
		List<T> oldList = Arrays.asList(oldArray);
		List<T> newList = new ArrayList<>(Arrays.asList(newArray));
		return processComparison(oldList, newList);
	}

	/**
	 * Calculates the difference between two arrays, using the comparison method on the
	 * given "new" List.
	 * 
	 * @param oldList
	 *            The "old" array for comparison
	 * @param newList
	 *            The "new" array for comparison
	 * @return A Tuple indicating the items removed (present in the "old" array but not
	 *         the "new" array) and the items added (present in the "new" array, but not
	 *         the "old" array)
	 * @param <T>
	 *            The component type of the Arrays to be compared
	 */
	private static <T> Tuple<List<T>, List<T>> processComparison(List<T> oldList,
		List<T> newList)
	{
		List<T> removedList = new ArrayList<>(oldList.size());
		for (T oldObject : oldList)
		{
			if (!newList.remove(oldObject))
			{
				removedList.add(oldObject);
			}
		}
		return new Tuple<>(removedList, newList);
	}

	/**
	 * Clones an array, adding 1 to the length, and placing the given object at the
	 * beginning of the new array. The length of the new array will be the length of the
	 * given array + 1. null is a legal value for the given array.
	 * 
	 * @param object
	 *            The object to be placed at the beginning of the returned array
	 * @param array
	 *            The original array, to be placed in the latter portion of the new array
	 * @return A new array with the given object as the first object and the current
	 *         contents of the array as the rest of the contents.
	 * @param <T>
	 *            The type of the array and the object to be added to the array
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] prependOnCopy(T object, T[] array)
	{
		return addOnCopy(array, 0, object);
	}

	/**
	 * Clones an array, adding 1 to the length, and placing the given object at the end of
	 * the new array. The length of the new array will be the length of the given array +
	 * 1. null is a legal value for the given array.
	 * 
	 * @param object
	 *            The object to be placed at the end of the returned array
	 * @param array
	 *            The original array, to be placed in the latter portion of the new array
	 * @return A new array with the contents of the given array as the first set of the
	 *         contents and the given object as the last element
	 * @param <T>
	 *            The type of the array and the object to be added to the array
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] addOnCopy(T[] array, T object)
	{
		return addOnCopy(array, (array == null) ? 0 : array.length, object);
	}

	/**
	 * Clones an array, adding 1 to the length, and placing the given object at the given
	 * index. Otherwise, the order of the objects in the given array is maintained. The
	 * length of the new array will be the length of the given array + 1. null is a legal
	 * value for the given array.
	 * 
	 * @param array
	 *            The original array, to be placed in the latter portion of the new array
	 * @param index
	 *            The index at which the given object will be placed
	 * @param object
	 *            The object to be inserted into the array at the given index
	 * @return A new array with the contents of the given array as the first set of the
	 *         contents and the given object as the last object
	 * @param <T>
	 *            The type of the array and the object to be added to the array
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] addOnCopy(T[] array, int index, T object)
	{
		T[] newArray;
		if (array == null)
		{
			newArray = (T[]) Array.newInstance(object.getClass(), 1);
		}
		else
		{
			int origSize = array.length;
			int newSize = (origSize + 1);
			newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), newSize);
			if (index != 0)
			{
				System.arraycopy(array, 0, newArray, 0, index);
			}
			if (index != origSize)
			{
				System.arraycopy(array, index, newArray, index + 1, origSize - index);
			}
		}
		newArray[index] = object;
		return newArray;
	}

	/**
	 * Clones an array, removing 1 from the length, by removing the object at the given
	 * index in the given array. Other than the removal, the order of the objects in the
	 * given array is maintained. The length of the new array will be the length of the
	 * given array - 1.
	 * 
	 * @param array
	 *            The original array
	 * @param index
	 *            The index location of the object to be removed from the array
	 * @return A new array with the object at the given index in the original array
	 *         removed.
	 * @param <T>
	 *            The type of object in the array
	 */
	public static <T> T[] removeOnCopy(T[] array, int index)
	{
		int newSize = array.length - 1;
		@SuppressWarnings("unchecked")
		T[] newArray =
				(T[]) Array.newInstance(array.getClass().getComponentType(), newSize);
		if (index != 0)
		{
			System.arraycopy(array, 0, newArray, 0, index);
		}
		if (index != newSize)
		{
			System.arraycopy(array, index + 1, newArray, index, newSize - index);
		}
		return newArray;
	}

}
