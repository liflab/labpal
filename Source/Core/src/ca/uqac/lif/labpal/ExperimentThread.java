/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

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

/**
 * Thread that notifies its running experiment with it is being
 * interrupted.
 * @author Sylvain Hallé
 */
public class ExperimentThread extends Thread 
{
	/**
	 * The experiment this thread is running
	 */
	protected final Experiment m_experiment;
	
	/**
	 * Creates a new thread to run an experiment
	 * @param e The experiment to run
	 */
	public ExperimentThread(Experiment e)
	{
		super(e);
		m_experiment = e;
	}
	
	@Override
	public void interrupt()
	{
		m_experiment.setRunning(false);
		try 
		{
			join();
		}
		catch (InterruptedException e) 
		{
			// Do nothing
		}
		m_experiment.interrupt();
	}
	
	public void kill()
	{
		m_experiment.setRunning(false);
		m_experiment.prepareToInterrupt();
		try 
		{
			sleep(10);
		}
		catch (InterruptedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try 
		{
			join();
		}
		catch (InterruptedException e) 
		{
			// Do nothing
		}
		m_experiment.kill();
	}
}