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
