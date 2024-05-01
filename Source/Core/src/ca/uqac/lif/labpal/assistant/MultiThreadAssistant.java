/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2024 Sylvain Hallé

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
package ca.uqac.lif.labpal.assistant;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Assistant that runs experiments in a configurable number of parallel
 * threads.
 * @author Sylvain Hallé
 * @since 3.0
 */
public class MultiThreadAssistant extends Assistant
{
	/**
	 * The number of threads to use
	 */
	protected final int m_threadCount;
	
	/**
	 * Creates a new assistant that will run experiments in a fixed number of
	 * threads.
	 * 
	 * @param thread_count
	 *          The number of threads to use
	 */
	public MultiThreadAssistant(int thread_count)
	{
		super();
		m_threadCount = thread_count;
	}
	
	@Override
	protected ClearableExecutorService getExecutor()
	{
		//Executors.newScheduledThreadPool(m_threadCount);
		//return Executors.newFixedThreadPool(m_threadCount);
		return new QueuedThreadPoolExecutor(m_threadCount);
	}
	
	@Override
	public String getName()
	{
		return "MT" + m_threadCount;
	}
}
