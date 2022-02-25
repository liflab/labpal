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
package ca.uqac.lif.labpal;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentException;
import ca.uqac.lif.units.Time;
import ca.uqac.lif.units.si.Second;

/**
 * A "dummy" experiment that simulates processing by waiting for some
 * predefined amount of time before terminating.
 */
public class DummyExperiment extends Experiment
{
	@Test
	public void dummyExperimentTest1()
	{
		DummyExperiment de = new DummyExperiment().setDuration(new Second(1));
		long start = System.currentTimeMillis();
		de.run();
		long end = System.currentTimeMillis();
		assertTrue(end - start >= 1000);
	}

	/**
	 * The simulated duration of the experiment.
	 */
	protected long m_duration;
	
	protected boolean m_hasPrerequisites;
	
	protected boolean m_fulfillCalled;
	
	public DummyExperiment()
	{
		super();
		m_hasPrerequisites = false;
		m_fulfillCalled = false;
	}

	public DummyExperiment setDuration(Time duration)
	{
		m_duration = (long) (new Second(duration).get().floatValue() * 1000f);
		return this;
	}
	
	public DummyExperiment hasPrerequisites(boolean b)
	{
		m_hasPrerequisites = b;
		return this;
	}
	
	@Override
	public String getDescription()
	{
		return "A dummy experiment for testing purposes.";	
	}
	
	public boolean hasPrerequisites()
	{
		return m_hasPrerequisites;
	}
	
	public boolean fulfillCalled()
	{
		return m_fulfillCalled;
	}
	
	@Override
	public boolean prerequisitesFulfilled()
	{
		return !m_hasPrerequisites || m_fulfillCalled;
	}
	
	@Override
	public void fulfillPrerequisites() throws ExperimentException, InterruptedException
	{
		m_fulfillCalled = true;
	}

	@Override
	public void execute() throws ExperimentException, InterruptedException
	{
		if (m_duration > 0)
		{
			for (int i = 0; i < m_duration / 50; i++)
			try 
			{
				Thread.sleep(50);
				setProgression(((float) i) * 50f / (float) m_duration);
			}
			catch (InterruptedException e) 
			{
				throw e;
			}
			writeOutput("x", getId());
			writeOutput("y", getId() * Math.random());
		}
	}
}
