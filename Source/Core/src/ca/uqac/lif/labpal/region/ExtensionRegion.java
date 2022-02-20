/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

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
package ca.uqac.lif.labpal.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A region composed of an explicit collection of points (i.e. a collection of
 * points stored in extension).
 * 
 * @author Sylvain Hallé
 */
class ExtensionRegion implements Region
{
	/**
	 * The names of each dimension of this region.
	 */
	/*@ non_null @*/ protected final String[] m_dimensions;
	
	/**
	 * The set of points contained in the region. In line with the class' name,
	 * these points are stored explicitly in a set.
	 */
	/*@ non_null @*/ protected final Set<Point> m_points;

	/**
	 * A map that references points in the region according to the values they
	 * have for each dimension. The main map has one key for each dimension.
	 * The value for each key is yet another map, which this time has one key
	 * for each possible value this dimension can have. This value is
	 * associated to the set of points in the region that have this value
	 * for this dimension. This field is not essential since it duplicates
	 * information that could be calculated from the set of points alone, but
	 * it avoids performing repetitive enumerations of the set of points in
	 * methods such as {@link #all(String...)}. 
	 */
	/*@ non_null @*/ protected final Map<String,Map<Object,Set<Point>>> m_contents;

	/**
	 * Creates a new empty region by specifying its dimensions.
	 * @param dimensions The names of each dimension of this region
	 */
	public ExtensionRegion(String ... dimensions)
	{
		super();
		m_points = new HashSet<Point>();
		m_dimensions = dimensions;
		m_contents = new HashMap<String,Map<Object,Set<Point>>>();
		for (String d : dimensions)
		{
			m_contents.put(d, new HashMap<Object,Set<Point>>());
		}
	}

	/**
	 * Creates a new region containing a given set of points.
	 * @param points The points to be contained in the region
	 */
	public ExtensionRegion(Point ... points)
	{
		this(fetchDimensions(points));
		for (Point p : points)
		{
			add(p);
		}
	}

	/**
	 * Creates a new region based on the contents of a map computed from
	 * another region. This constructor does not accept the map as is, and
	 * performs an extra cleanup step by keeping only points in the map that
	 * appear in each dimension. Other points are discarded, both from the
	 * map and from the internal set of points of the new region. This way, any
	 * map can be given to the constructor, but only a subset corresponding to
	 * a valid internal state is always kept.
	 * @param dimensions The dimensions of the region
	 * @param contents A map to be used as the internal contents of the new
	 * region
	 */
	protected ExtensionRegion(String[] dimensions, Map<String,Map<Object,Set<Point>>> contents)
	{
		super();
		m_dimensions = dimensions;
		m_points = new HashSet<Point>();
		m_points.addAll(allPoints(contents.get(m_dimensions[0])));
		for (int i = 1; i < m_dimensions.length; i++)
		{
			m_points.retainAll(allPoints(contents.get(m_dimensions[i])));
		}
		m_contents = new HashMap<String,Map<Object,Set<Point>>>(m_dimensions.length);
		for (String d : m_dimensions)
		{
			m_contents.put(d, retainAll(m_points, contents.get(d)));
		}
	}

	@Override
	public Set<Point> allPoints()
	{
		return m_points;
	}

	@Override
	public int size()
	{
		return m_points.size();
	}


	@Override
	/*@ pure non_null @*/ public String[] getDimensions()
	{
		return m_dimensions;
	}

	@Override
	/*@ pure non_null @*/ public Region set(String name, Object value)
	{
		if (!m_contents.keySet().contains(name))
		{
			return EmptyRegion.instance;
		}
		Map<Object,Set<Point>> domain = m_contents.get(name);
		if (!domain.containsKey(value))
		{
			return EmptyRegion.instance;
		}
		Map<String,Map<Object,Set<Point>>> new_map = new HashMap<String,Map<Object,Set<Point>>>();
		for (String d : m_dimensions)
		{
			if (d.compareTo(name) == 0)
			{
				Set<Point> set = new HashSet<Point>();
				set.addAll(m_contents.get(d).get(value));
				Map<Object,Set<Point>> d_map = new HashMap<Object,Set<Point>>();
				d_map.put(value, set);
				new_map.put(d, d_map);
			}
			else
			{
				Map<Object,Set<Point>> d_map = new HashMap<Object,Set<Point>>();
				d_map.putAll(m_contents.get(d));
				new_map.put(d, d_map);
			}
		}
		return new ExtensionRegion(m_dimensions, new_map);
	}

	/**
	 * In a map associating values to set of points, keeps only points that are
	 * present in a given set.
	 * @param points The set of points to keep
	 * @param map The map to filter 
	 * @return A new map containing only the points in <tt>point</tt>
	 */
	protected static Map<Object,Set<Point>> retainAll(Set<Point> points, Map<Object,Set<Point>> map)
	{
		Map<Object,Set<Point>> new_map = new HashMap<Object,Set<Point>>();
		for (Map.Entry<Object,Set<Point>> e : map.entrySet())
		{
			Set<Point> s = new HashSet<Point>();
			s.addAll(e.getValue());
			s.retainAll(points);
			if (!s.isEmpty())
			{
				new_map.put(e.getKey(), s);
			}
		}
		return new_map;
	}

	/**
	 * Gets the set of all the points mentioned in the domain of a region's
	 * dimension.
	 * @param map The map associating values of that dimension to sets of
	 * points
	 * @return The set of all the points occurring in this map
	 */
	protected static Set<Point> allPoints(Map<Object,Set<Point>> map)
	{
		Set<Point> points = new HashSet<Point>();
		for (Set<Point> list : map.values())
		{
			if (list != null)
			{
				points.addAll(list);
			}
		}
		return points;
	}

	/**
	 * Adds one or more points to this region.
	 * @param points The point(s) to add
	 */
	protected void add(Point ... points)
	{
		for (Point p : points)
		{
			addPoint(p);
		}
	}

	/**
	 * Adds one or more points to this region.
	 * @param points The point(s) to add
	 */
	protected void addAll(Collection<Point> points)
	{
		for (Point p : points)
		{
			addPoint(p);
		}
	}

	/**
	 * Adds a point to this region.
	 * @param point The point to add
	 */
	protected void addPoint(Point p)
	{
		if (m_points.contains(p))
		{
			// Adding p again would create duplicate entries in the hash maps
			return;
		}
		m_points.add(p);
		for (String d : m_dimensions)
		{
			Object v = p.get(d);
			Map<Object,Set<Point>> map = m_contents.get(d);
			if (map.containsKey(v))
			{
				Set<Point> list = map.get(v);
				list.add(p);
			}
			else
			{
				Set<Point> list = new HashSet<Point>();
				list.add(p);
				map.put(v, list);
			}
		}	
	}

	/**
	 * Retrieves the dimension names present in a set of points. The method
	 * assumes that all points have the same dimension names.
	 * @param points The set of points
	 * @return The dimension names
	 */
	protected static String[] fetchDimensions(Point ... points)
	{
		if (points.length == 0)
		{
			return new String[0];
		}
		return points[0].getDimensions();
	}

	@SuppressWarnings("rawtypes")
	public Iterable<Region> all(String ... dimensions)
	{
		List<Region> regions = new ArrayList<Region>();
		// If no dimension is mentioned, iterate over all dimensions
		if (dimensions.length == 0)
		{
			dimensions = m_dimensions;
		}
		ResettableIterator[] enums = new ResettableIterator[dimensions.length];
		Object[] values = new Object[dimensions.length];
		for (int i = 0; i < dimensions.length; i++)
		{
			enums[i] = new ResettableIterator<Object>(m_contents.get(dimensions[i]).keySet());
			values[i] = enums[i].next();
		}
		regions.add(createNewRegion(dimensions, values));
		boolean go = true;
		while (go)
		{
			for (int i = 0; i < dimensions.length; i++)
			{
				if (enums[i].hasNext())
				{
					values[i] = enums[i].next();
					break;
				}
				else
				{
					if (i == dimensions.length - 1)
					{
						go = false;
						break;
					}
					enums[i].reset();
					values[i] = enums[i].next();
				}
			}
			if (go)
			{
				regions.add(createNewRegion(dimensions, values));
			}
		}
		return new ResettableIterator<Region>(regions);
	}

	/**
	 * Creates a new region instance from the contents of the current region,
	 * by "flattening" some of the dimensions to a single given value. This
	 * method is used internally by {@link #all(String...)}. 
	 * @param dimensions The names of the dimensions to flatten
	 * @param values The values of the corresponding dimensions
	 * @return A new region identical to the current one, except for the
	 * dimensions that have been flattened.
	 */
	protected Region createNewRegion(String[] dimensions, Object[] values)
	{
		Map<String,Map<Object,Set<Point>>> new_contents = new HashMap<String,Map<Object,Set<Point>>>(m_dimensions.length);
		Set<String> iterated_dims = new HashSet<String>(dimensions.length);
		for (int i = 0; i < dimensions.length; i++)
		{
			iterated_dims.add(dimensions[i]);
			Map<Object,Set<Point>> mp = new HashMap<Object,Set<Point>>(1);
			mp.put(values[i], m_contents.get(dimensions[i]).get(values[i]));
			new_contents.put(dimensions[i], mp);
		}
		for (String d : m_dimensions)
		{
			if (!iterated_dims.contains(d))
			{
				new_contents.put(d, m_contents.get(d));
			}
		}
		return new ExtensionRegion(m_dimensions, new_contents);
	}

	@Override
	public boolean isSet(String name)
	{
		if (!m_contents.containsKey(name))
		{
			return false;
		}
		return m_contents.get(name).values().size() == 1;
	}

	@Override
	/*@ pure non_null @*/ public Set<Object> getDomain(String name)
	{
		if (!m_contents.containsKey(name))
		{
			return new HashSet<Object>(0);
		}
		return m_contents.get(name).keySet();
	}
}
