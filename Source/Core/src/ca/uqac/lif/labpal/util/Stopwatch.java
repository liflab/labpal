/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2023 Sylvain Hallé

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
	
	public static long lap(Object key)
	{
		if (s_stopwatches.containsKey(key))
		{
			Stopwatch s = s_stopwatches.get(key);
			return s.getDuration();
		}
		return 0;
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
		if (m_start == 0)
		{
			return 0;
		}
		if (m_stop == 0)
		{
			return System.currentTimeMillis() - m_start;
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
