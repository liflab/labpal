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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ca.uqac.lif.labpal.Stateful.Status;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.units.si.Second;

/**
 * A runnable class that lets another thread running an experiment last for up
 * to a maximum duration, after which it is interrupted.
 * The {@link FutureWatcher} does so by being given a reference to that
 * thread's {@link Future} object. In case the experiment's thread ends because
 * of a timeout or an external interruption, it takes care of setting the
 * experiment's status to the appropriate value before terminating.
 * An assistant can run this object in a
 * thread to automatically let experiments timeout without actively monitoring
 * them.
 * @author Sylvain Hallé
 */
class FutureWatcher implements Runnable
{
	private final Experiment m_experiment;

	private final Future<?> m_future;

	public FutureWatcher(Experiment e, Future<?> future)
	{
		super();
		m_experiment = e;
		m_future = future;
	}

	public Experiment getExperiment()
	{
		return m_experiment;
	}

	public void cancel()
	{
		m_future.cancel(true);
	}

	@Override
	public void run()
	{
		long timeout = (int) (new Second(m_experiment.getTimeout()).get().floatValue() * 1000f);
		try
		{
			if (timeout <= 0)
			{
				m_future.get();
			}
			else
			{
				m_future.get(timeout, TimeUnit.MILLISECONDS);
			}
		}
		catch (InterruptedException | ExecutionException e)
		{
			m_experiment.setStatus(Status.INTERRUPTED);
		}
		catch (TimeoutException e)
		{
			m_experiment.setStatus(Status.INTERRUPTED);
			m_experiment.declareTimeout();
		}
	}
}
