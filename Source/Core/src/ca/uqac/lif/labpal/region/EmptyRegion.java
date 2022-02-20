package ca.uqac.lif.labpal.region;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A region with no points.
 * @author Sylvain Hallé
 */
public class EmptyRegion implements Region
{
	public static final EmptyRegion instance = new EmptyRegion();
	
	private EmptyRegion()
	{
		super();
	}

	@Override
	public Iterable<Region> all(String... names)
	{
		return new EmptyIterable();
	}

	@Override
	public Region set(String name, Object value) 
	{
		return this;
	}
	
	@Override
	public boolean isSet(String name)
	{
		return false;
	}
	
	@Override
	public Set<Object> getDomain(String name)
	{
		return new HashSet<Object>(0);
	}
	
	@Override
	public String[] getDimensions() 
	{
		return new String[0];
	}
	
	@Override
	public Set<Point> allPoints() 
	{
		return new HashSet<Point>(0);
	}
	
	@Override
	public int size()
	{
		return 0;
	}
	
	/**
	 * An {@link Iterable} that enumerates nothing.
	 */
	protected static class EmptyIterable implements Iterable<Region>, Iterator<Region>
	{
		public EmptyIterable()
		{
			super();
		}
		
		@Override
		public Iterator<Region> iterator()
		{
			return new EmptyIterable();
		}

		@Override
		public boolean hasNext() 
		{
			return false;
		}

		@Override
		public Region next() 
		{
			throw new NoSuchElementException();
		}
	}
}
