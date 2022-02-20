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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Extension of Java's {@link ExecutorService} providing an alternate means of
 * shutting down the execution of tasks. Three methods for shutting down are
 * implemented:
 * <ol>
 * <li>{@link #shutdown()}: runs to completion all the tasks submitted up to
 * this point, and refuses the submission of any further task;</li>
 * <li>{@link #shutdownNow()}: immediately interrupts any running task,
 * and refuses the submission of any further task;</li>
 * <li>{@link #shutdownAtEnd()}: runs to completion all the tasks that are
 * currently running, cancels all pending tasks and refuses the submission of
 * any further task.</li>
 * </ol>
 * <p>
 * Among these three methods, {@link #shutdown()} and {@link #shutdownNow()}
 * work in the same way as for the parent {@link ExecutorService} class, and
 * {@link #shutdownAtEnd()} is new.
 * 
 * @since 3.0
 * 
 * @author Sylvain Hallé
 *
 */
public abstract class LabPalExecutorService implements ExecutorService 
{
	/**
	 * The underlying executor service used to run tasks.
	 */
	/*@ non_null @*/ protected ExecutorService m_executor;

	/**
	 * Creates a new LabPal executor service.
	 */
	public LabPalExecutorService()
	{
		super();
	}

	/**
	 * Shuts down the executor service and lets currently running tasks to
	 * finish. This method by runs to completion all the tasks that are
	 * currently running, cancels all pending tasks and refuses the submission
	 * of any further task.
	 */
	public void shutdownAtEnd()
	{
		m_executor.shutdown();
	}

	@Override
	public boolean awaitTermination(long arg0, TimeUnit arg1) throws InterruptedException {
		return m_executor.awaitTermination(arg0, arg1);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> arg0) throws InterruptedException {
		return m_executor.invokeAll(arg0);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> arg0, long arg1, TimeUnit arg2)
			throws InterruptedException {
		return m_executor.invokeAll(arg0, arg1, arg2);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> arg0) throws InterruptedException, ExecutionException {
		return m_executor.invokeAny(arg0);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> arg0, long arg1, TimeUnit arg2)
			throws InterruptedException, ExecutionException, TimeoutException {
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
	public void shutdown()
	{
		m_executor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow()
	{
		return m_executor.shutdownNow();
	}

	@Override
	public <T> Future<T> submit(Callable<T> c)
	{
		return m_executor.submit(c);
	}

	@Override
	public Future<?> submit(Runnable r)
	{
		return m_executor.submit(r);
	}

	@Override
	public <T> Future<T> submit(Runnable r, T t)
	{
		return m_executor.submit(r, t);
	}

	@Override
	public void execute(Runnable r) 
	{
		m_executor.execute(r);

	}
	
	/**
	 * Creates a new instance of this executor with the same configuration
	 * as the current object. Instances should not be
	 * reused and this method is expected to return a new instance on every
	 * call.
	 * @return A new instance of this executor
	 */
	/*@ non_null @*/ public abstract LabPalExecutorService newInstance();
}