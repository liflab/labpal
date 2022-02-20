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

import org.junit.Test;

import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentException;

/**
 * A "dummy" experiment that simulates processing by waiting for some
 * predefined amount of time before terminating.
 */
public class DummyExperiment extends Experiment
{
	@Test
	public void dummyExperimentTest1()
	{
		// A dummy test to avoid JUnit complaining about the class having no
		// test
	}

	/**
	 * The simulated duration of the experiment.
	 */
	protected long m_duration;
	
	protected boolean m_hasPrerequisites;
	
	protected boolean m_fulfillCalled;
	
	protected DummyExperiment()
	{
		super();
	}

	public DummyExperiment(long duration, long timeout)
	{
		super();
		m_duration = duration;
		m_hasPrerequisites = false;
		m_fulfillCalled = false;
		setTimeout(timeout);
	}
	
	public DummyExperiment hasPrerequisites(boolean b)
	{
		m_hasPrerequisites = b;
		return this;
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
			try 
			{
				Thread.sleep(m_duration);
			}
			catch (InterruptedException e) 
			{
				throw e;
			}
		}
	}
}
