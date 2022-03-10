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

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import ca.uqac.lif.labpal.DummyExperiment;
import ca.uqac.lif.labpal.Stateful.Status;
import ca.uqac.lif.labpal.assistant.FutureWatcher;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.units.Time;
import ca.uqac.lif.units.si.Second;

public class FutureWatcherTest 
{
	public static final Time t_500ms = new Second(0.5);
	
	public static final Time t_100ms = new Second(0.1);
	
	public static final Time t_50ms = new Second(0.05);
	
	public static final Time t_0ms = new Second(0);
	
	@Test
	public void testTimeout1() throws InterruptedException
	{
		// Experiment runs to completion
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Experiment e = new DummyExperiment().setDuration(t_100ms).setTimeout(t_500ms);
		Future<?> f = executor.submit(e);
		FutureWatcher w = new FutureWatcher(e, f);
		Thread t = new Thread(w);
		t.start();
		t.join();
		assertEquals(Status.DONE, e.getStatus());
	}
	
	@Test
	public void testTimeout2() throws InterruptedException
	{
		// Experiment times out and is stopped by FutureWatcher
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Experiment e = new DummyExperiment().setDuration(t_500ms).setTimeout(t_50ms);
		Future<?> f = executor.submit(e);
		FutureWatcher w = new FutureWatcher(e, f);
		Thread t = new Thread(w);
		t.start();
		t.join();
		assertEquals(Status.INTERRUPTED, e.getStatus());
		assertTrue(e.hasTimedOut());
	}
	
	@Test
	public void testInterrupt1() throws InterruptedException
	{
		// Experiment is externally interrupted before it ends
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Experiment e = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		Future<?> f = executor.submit(e);
		FutureWatcher w = new FutureWatcher(e, f);
		Thread t = new Thread(w);
		t.start();
		Thread.sleep(50);
		t.interrupt();
		Thread.sleep(50);
		assertEquals(Status.INTERRUPTED, e.getStatus());
	}
}
