/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2019 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.labpal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;

/**
 * A set of parameters with associated ranges of values. A region can be used
 * to iterate over all combinations of values of all parameters, without the
 * need for nested for loops and their associated loop variables.
 * <p>
 * For example, suppose you have three parameters named A, B and C. Suppose
 * that A ranges from 1 to 10, with increments of 1. You can create a range
 * object, and specify this as follows:
 * <pre>
 * Region region = new Region();
 * region.setRange("A", 1, 10);
 * </pre>
 * Suppose that B is a string parameter that can have three values, "foo",
 * "bar" and "baz", and finally that "C" can be either 0, 1.5 or 4.5:
 * <pre>
 * region.set("B", "foo", "bar", "baz");
 * region.set("C", 0, 1.5, 4.5);
 * </pre>
 * It is then possible to iterate over all values of A, B and C like this:
 * <pre>
 * for (Region r : region.all()) {
 *   ...
 * }
 * </pre>
 * The object <tt>r</tt> inside the loop replaces three nested loops and their
 * associated variables. To get the value of "A" in each iteration, you write
 * <code>r.getInt("A")</code> (and similarly using other getter method for B
 * and C).
 * <p>
 * Regions can also be used to filter experiments
 * (using {@link #filterExperiments(Collection, Region)}) or to iterate over
 * combinations of values that do not form an n-dimensional rectangle.
 * 
 * @author Sylvain Hallé
 */
public class Region 
{
	/**
	 * The association between parameters and their possible values
	 */
	/*@ non_null @*/ protected Map<String,List<JsonElement>> m_ranges;
	
	/**
	 * A list of names representing parameters that have a fixed value
	 */
	/*@ non_null @*/ protected List<String> m_names;
	
	/**
	 * Creates a new empty region
	 */
	public Region()
	{
		super();
		m_ranges = new HashMap<String,List<JsonElement>>();
		m_names = new ArrayList<String>();
	}
	
	/**
	 * Creates a new region by copying an existing region
	 * @param r The region to copy from
	 */
	public Region(Region r)
	{
		this();
		m_ranges.putAll(r.m_ranges);
		m_names.addAll(r.m_names);
	}
	
	/**
	 * Replaces the values of a given dimension by a single one
	 * @param name The name of the dimension
	 * @param value The value to set for this dimension
	 */
	public void set(String name, JsonElement value)
	{
		List<JsonElement> new_list = new ArrayList<JsonElement>(1);
		new_list.add(value);
		m_ranges.put(name, new_list);
		m_names.add(name);
	}
	
	/**
	 * Creates a region from the current one by reducing the values of a given
	 * dimension to a single one
	 * @param name The name of the dimension
	 * @param value The value to set for this dimension
	 * @return The new region
	 */
	public Region set(String name, String value)
	{
		Region reg = new Region(this);
		reg.set(name, new JsonString(value));
		return reg;
	}
	
	/**
	 * Creates a region from the current one by reducing the values of a given
	 * dimension to a single one
	 * @param name The name of the dimension
	 * @param value The value to set for this dimension
	 * @return The new region
	 */
	public Region set(String name, Number value)
	{
		Region reg = getRegion(this);
		reg.set(name, new JsonNumber(value));
		return reg;
	}
	
	/**
	 * Gets the name of the i-th fixed dimension of this region
	 * @param index The index of the dimension
	 * @return The name of the dimension
	 */
	public String getName(int index)
	{
		if (index < 0 || index >= m_names.size())
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		return m_names.get(index);
	}
	
	/**
	 * Adds a new dimension to the region, and sets its value to a range
	 * @param name The name of the new dimension
	 * @param start The start of the range
	 * @param end The end of the range (inclusive)
	 * @param increment The increment step for generating values in the range
	 * @return This region
	 */
	public Region addRange(String name, Number start, Number end, Number increment)
	{
		List<JsonElement> values = new ArrayList<JsonElement>();
		for (Number n = start; n.doubleValue() <= end.doubleValue(); n = increment(n, increment))
		{
			values.add(new JsonNumber(n));
		}
		m_ranges.put(name, values);
		return this;
	}
	
	/**
	 * Adds a new dimension to the region, and sets its value to a range
	 * @param name The name of the new dimension
	 * @param start The start of the range
	 * @param end The end of the range (inclusive)
	 * @return This region
	 */
	public Region addRange(String name, Number start, Number end)
	{
		return addRange(name, start, end, 1);
	}
	
	/**
	 * Adds a new dimension to the region, and sets its possible values
	 * @param name The name of the new dimension
	 * @param values The values this dimension can take
	 * @return This region
	 */
	public Region add(String name, JsonElement ... values)
	{
		List<JsonElement> vals = new ArrayList<JsonElement>(values.length);
		for (JsonElement je : values)
		{
			vals.add(je);
		}
		m_ranges.put(name, vals);
		return this;
	}
	
	/**
	 * Adds a new dimension to the region, and sets its possible values
	 * @param name The name of the new dimension
	 * @param values The values this dimension can take
	 * @return This region
	 */
	public Region add(String name, Number ... values)
	{
		List<JsonElement> vals = new ArrayList<JsonElement>(values.length);
		for (Number n : values)
		{
			vals.add(new JsonNumber(n));
		}
		m_ranges.put(name, vals);
		return this;
	}
	
	/**
	 * Adds a new dimension to the region, and sets its possible values
	 * @param name The name of the new dimension
	 * @param values The values this dimension can take
	 * @return This region
	 */
	public Region add(String name, String ... values)
	{
		List<JsonElement> vals = new ArrayList<JsonElement>(values.length);
		for (String  n : values)
		{
			vals.add(new JsonString(n));
		}
		m_ranges.put(name, vals);
		return this;
	}
	
	/**
	 * Increments a number by another
	 * @param n The number to increment
	 * @param increment The increment to add to it
	 * @return The new number
	 */
	protected static Number increment(Number n, Number increment)
	{
		if (n instanceof Integer && increment instanceof Integer)
		{
			// If both are ints, work over ints
			int i_n = n.intValue();
			int i_inc = increment.intValue();
			return i_n + i_inc;
		}
		return n.doubleValue() + increment.doubleValue();
	}
	
	/**
	 * Determines if a given point belongs to the zone. Override this
	 * method to create regions of arbitrary shapes.
	 * @param point A map associating each dimension name to a value from
	 *   its domain
	 * @return {@code true} if the corresponding point belongs to the zone,
	 *   {@code false} otherwise
	 */
	public boolean isInRegion(Region point)
	{
		return true;
	}
	
	/**
	 * Creates a projection of the current region by keeping only specific
	 * dimensions
	 * @param names The names of the dimensions to keep
	 * @return The projected region
	 */
	public Region project(String ... names)
	{
		Region r = getEmptyRegion();
		for (String name : names)
		{
			r.m_ranges.put(name, m_ranges.get(name));
		}
		return r;
	}
	
	/**
	 * Gets the values of a given dimension
	 * @param name The name of the dimension
	 * @return A list of values, or the empty list if the dimension does not
	 * exist
	 */
	public /*@NonNull*/ List<JsonElement> getAll(String name)
	{
		if (m_ranges.containsKey(name))
		{
			return m_ranges.get(name);
		}
		return new ArrayList<JsonElement>();
	}
	
	/**
	 * Gets a single value for a given dimension
	 * @param name The name of the dimension
	 * @return A single value for this dimension
	 */
	public /*@NonNull*/ JsonElement get(String name)
	{
		if (m_ranges.containsKey(name))
		{
			List<JsonElement> list = m_ranges.get(name);
			if (list.isEmpty())
			{
				return JsonNull.instance;
			}
			return list.get(0);
		}
		return JsonNull.instance;
	}
	
	/**
	 * Retrieves an integer value for a dimension
	 * @param name The name of the dimension
	 * @return The integer value, or 0 if not found
	 */
	public int getInt(String name)
	{
		JsonElement je = get(name);
		if (!(je instanceof JsonNumber))
		{
			return 0;
		}
		return ((JsonNumber) je).numberValue().intValue();
	}
	
	/**
	 * Retrieves a String value for a dimension
	 * @param name The name of the dimension
	 * @return The string value, or the empty string if not found
	 */
	public String getString(String name)
	{
		JsonElement je = get(name);
		if (!(je instanceof JsonString))
		{
			return "";
		}
		return ((JsonString) je).stringValue();
	}
	
	/**
	 * Retrieves an integer value for a dimension
	 * @param index The index of the dimension
	 * @return The integer value, or 0 if not found
	 */
	public int getInt(int index)
	{
		return getInt(getName(index));
	}
	
	/**
	 * Creates an iterable collection of all regions
	 * @param names The names of all the dimensions to be iterated over
	 * @return An iterable collection of sub-regions
	 */
	public Iterable<Region> all(String ... names)
	{
		List<Region> regions = null;
		if (names.length == 0)
		{
			String[] new_names = new String[m_ranges.size()];
			int i = 0;
			for (String n : m_ranges.keySet())
			{
				new_names[i++] = n;
			}
			regions = all(0, new_names);
		}
		else
		{
			regions = all(0, names);
		}
		// Before outputting the list, remove those points that are outside
		// the region
		Iterator<Region> it = regions.iterator();
		while (it.hasNext())
		{
			Region r = it.next();
			if (!isInRegion(r))
			{
				it.remove();
			}
		}
		return regions;
	}
	
	/**
   * Creates an iterable collection of all regions, by specifying the index
   * of the dimension to iterate over
   * @param index The index of the dimension
   * @param names The names of all the dimensions to be iterated over
   * @return An iterable collection of sub-regions
   */
	protected List<Region> all(int index, String ... names)
	{
		List<Region> regions = new ArrayList<Region>();
		if (index == names.length)
		{
			regions.add(this);
			return regions;
		}
		String name = names[index];
		if (!m_ranges.containsKey(name))
			return regions;
		List<JsonElement> values = m_ranges.get(name);
		for (JsonElement v : values)
		{
			Region new_r = getRegion(this);
			new_r.set(name, v);
			for (Region r : new_r.all(index + 1, names))
			{
				regions.add(r);
			}
		}
		return regions;
	}
	
	/**
	 * Gets all experiments that fit into a given region
	 * @param exps A collection of experiments
	 * @param r The region to consider
	 * @return The subset of all experiments that lie within the region
	 */
	public static Collection<Experiment> filterExperiments(Collection<Experiment> exps, Region r)
	{
		Collection<Experiment> new_col = new HashSet<Experiment>();
		for (Experiment e : exps)
		{
			if (r.includes(e))
				new_col.add(e);
		}
		return new_col;
	}
	
	/**
	 * Checks if a region includes an experiment. A region includes an experiment
	 * if, for all its dimensions, there is an experiment's parameter with a value
	 * in the domain for that dimension.
	 * @param e The experiment
	 * @return {@code true} if the region includes the experiment,
	 * {@code false} otherwise
	 */
	public boolean includes(Experiment e)
	{
		if (e == null)
			return false;
		JsonMap all_params = e.getAllParameters();
		for (Map.Entry<String,List<JsonElement>> entry : m_ranges.entrySet())
		{
			String name = entry.getKey();
			List<JsonElement> values = entry.getValue();
			if (!all_params.containsKey(name))
				return false;
			if (!values.contains(all_params.get(name)))
				return false;
		}
		return true;
	}
	
	public Region applyAll(String ... names)
	{
		for (Region r : all(names))
		{
			doFor(r);
		}
		return this;
	}
	
	/**
	 * Callback that is being called by {@link #applyAll(String...)}
	 * on each iterated region. Override this method to implement
	 * a specific functionality.
	 * @param r The region
	 */
	public void doFor(Region r)
	{
		// Do nothing
	}
	
	/**
	 * Gets the names of all the dimensions included in this region
	 * @return The names of the dimensions
	 */
	/*@ non_null @*/ public Collection<String> getDimensions()
	{
	  return m_ranges.keySet();
	}
	
	/**
	 * Checks if a region has a dimension of a given name
	 * @param name The name of the dimension
	 * @return <tt>true</tt> if the dimension exists, <tt>false</tt>
	 * otherwise
	 */
	public boolean hasDimension(String name)
	{
	  return m_ranges.containsKey(name);
	}
	
	/**
	 * Gets a new instance of an empty region
	 * @return The region
	 */
	protected Region getEmptyRegion()
	{
	  return new Region();
	}
	
	 /**
   * Gets a new instance of a region by copying from another
   * @param r The region to copy from
   * @return The region
   */
  protected Region getRegion(Region r)
  {
    return new Region(r);
  }
}
