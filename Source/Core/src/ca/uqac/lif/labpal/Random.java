/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

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

/**
 * Random number generator that remembers the seed it was
 * instantiated with.
 * @author Sylvain Hallé
 *
 */
public class Random extends java.util.Random
{
	/**
	 * Dummy UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The initial seed
	 */
	private long m_seed;
	
	/**
	 * Create a new random number generator with seed 0
	 */
	public Random()
	{
		super(0);
		m_seed = 0;
	}
	
	/**
	 * Creates a new random number generator using a single long seed. 
	 * @param seed The initial seed
	 */
	public Random(long seed)
	{
		super(seed);
		m_seed = seed;
	}
	
	/**
	 * Resets the state of the random number generator
	 */
	public void reseed()
	{
		super.setSeed(m_seed);
	}
	
	@Override
	public void setSeed(long seed)
	{
		m_seed = seed;
		super.setSeed(seed);
	}
	
	/**
	 * Retrieves the seed used to instantiate this random number
	 * generator
	 * @return The seed
	 */
	public long getSeed()
	{
		return m_seed;
	}

}
