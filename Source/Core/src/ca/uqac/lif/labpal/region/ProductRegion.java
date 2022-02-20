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

/**
 * A region defined as the Cartesian product of a number of domains.
 * @author Sylvain Hallé
 *
 */
public class ProductRegion extends ExtensionRegion
{
	/**
	 * Creates a new product region.
	 * @param domains The domains for each dimension of the region.
	 */
	public ProductRegion(Domain<?> ... domains)
	{
		super(getDimensions(domains));
		addPoints(domains);
	}
	
	/**
	 * Adds points to the region by computing the Cartesian product of domains
	 * @param domains The domains
	 */
	@SuppressWarnings("rawtypes")
	protected void addPoints(Domain<?> ... domains)
	{
		PointFactory factory = new PointFactory(m_dimensions);
		ResettableIterator[] enums = new ResettableIterator[domains.length];
		Object[] values = new Object[domains.length];
		for (int i = 0; i < domains.length; i++)
		{
			enums[i] = domains[i].getValues();
			values[i] = enums[i].next();
		}
		add(factory.get(values));
		boolean go = true;
		while (go)
		{
			for (int i = 0; i < domains.length; i++)
			{
				if (enums[i].hasNext())
				{
					values[i] = enums[i].next();
					break;
				}
				else
				{
					if (i == domains.length - 1)
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
				add(factory.get(values));
			}
		}
	}
	
	/**
	 * Gets the dimension names for a set of domains.
	 * @param domains The set of domains
	 * @return The names of each domain
	 */
	protected static String[] getDimensions(Domain<?> ... domains)
	{
		String[] dims = new String[domains.length];
		for (int i = 0; i < domains.length; i++)
		{
			dims[i] = domains[i].getName();
		}	
		return dims;
	}
}
