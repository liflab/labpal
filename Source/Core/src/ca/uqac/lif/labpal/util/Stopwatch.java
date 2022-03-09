package ca.uqac.lif.labpal.util;

import java.util.HashMap;
import java.util.Map;

public class Stopwatch
{
	protected long m_start;
	
	protected long m_stop;
	
	protected static Map<Object,Stopwatch> s_stopwatches = new HashMap<Object,Stopwatch>();
	
	public static void start(Object key)
	{
		Stopwatch s = new Stopwatch();
		s_stopwatches.put(key, s);
		s.start();
	}
	
	public static long stop(Object key)
	{
		if (s_stopwatches.containsKey(key))
		{
			Stopwatch s = s_stopwatches.get(key);
			s.stop();
			return s.getDuration();
		}
		return 0;
	}
	
	public Stopwatch()
	{
		super();
		m_start = 0;
		m_stop = 0;
	}
	
	/*@ non_null @*/ public Stopwatch start()
	{
		m_start = System.currentTimeMillis();
		return this;
	}
	
	/*@ non_null @*/ public Stopwatch stop()
	{
		m_stop = System.currentTimeMillis();
		return this;
	}
	
	public long getDuration()
	{
		if (m_start == 0 || m_stop == 0)
		{
			return 0;
		}
		return m_stop - m_start;
	}
	
	public static void sleep(long duration)
	{
		try
		{
			Thread.sleep(duration);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
