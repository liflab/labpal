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
package ca.uqac.lif.labpal.assistant;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An executor service that dispatches tasks over a fixed number of predefined
 * threads. 
 * @author Sylvain Hallé
 */
public class QueuedThreadPoolExecutor extends LabPalExecutorService
{
	/**
	 * The number of threads to use to run tasks
	 */
	protected final int m_numThreads;
	
	/**
	 * A queue to contain experiment instances that have been sent to the
	 * underlying executor. This queue is only used in {@link #shutdown()} to
	 * force the cancellation of tasks that have not yet been started by the
	 * executor.
	 */
	/*@ non_null @*/ protected final BlockingQueue<Runnable> m_queue;
	
	/**
	 * Creates a new instance of the executor.
	 * @param num_threads The number of threads to use to run tasks
	 */
	public QueuedThreadPoolExecutor(int num_threads)
	{
		super();
		m_numThreads = num_threads;
		m_queue = new LinkedBlockingQueue<>();
		m_executor = new ThreadPoolExecutor(num_threads, num_threads, 0L, TimeUnit.MILLISECONDS, m_queue);
	}
	
	@Override
	public void shutdown()
	{
		// To force queued jobs not to start: https://stackoverflow.com/a/52387017
		m_queue.clear();
		m_executor.shutdown();
	}

	@Override
	public QueuedThreadPoolExecutor newInstance()
	{
		return new QueuedThreadPoolExecutor(m_numThreads);
	}

}
