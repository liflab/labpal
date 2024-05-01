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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An executor service that dispatches tasks over a fixed number of predefined
 * threads. 
 * @author Sylvain Hallé
 */
public class QueuedThreadPoolExecutor implements ExecutorService
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
	
	protected final ThreadPoolExecutor m_executor;
	
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
	public List<Runnable> shutdownNow()
	{
		// To force queued jobs not to start: https://stackoverflow.com/a/52387017
		m_queue.clear();
		return m_executor.shutdownNow();
	}
	
	@Override
	public String toString()
	{
		return m_numThreads + " thread(s)";
	}

	@Override
	public void execute(Runnable r)
	{
		m_executor.execute(r);
	}

	@Override
	public boolean awaitTermination(long arg0, TimeUnit arg1) throws InterruptedException
	{
		return m_executor.awaitTermination(arg0, arg1);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> arg0)
			throws InterruptedException
	{
		return m_executor.invokeAll(arg0);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> arg0, long arg1,
			TimeUnit arg2) throws InterruptedException
	{
		return m_executor.invokeAll(arg0, arg1, arg2);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> arg0)
			throws InterruptedException, ExecutionException
	{
		return m_executor.invokeAny(arg0);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> arg0, long arg1, TimeUnit arg2)
			throws InterruptedException, ExecutionException, TimeoutException
	{
		return m_executor.invokeAny(arg0, arg1, arg2);
	}

	@Override
	public boolean isShutdown()
	{
		return m_executor.isShutdown();
	}

	@Override
	public boolean isTerminated()
	{
		return m_executor.isTerminated();
	}

	@Override
	public <T> Future<T> submit(Callable<T> arg0)
	{
		return m_executor.submit(arg0);
	}

	@Override
	public Future<?> submit(Runnable arg0)
	{
		return m_executor.submit(arg0);
	}

	@Override
	public <T> Future<T> submit(Runnable arg0, T arg1)
	{
		return m_executor.submit(arg0, arg1);
	}
}
