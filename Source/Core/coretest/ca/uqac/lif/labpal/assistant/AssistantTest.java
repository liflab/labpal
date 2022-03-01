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

import org.junit.Test;

import ca.uqac.lif.labpal.DummyExperiment;
import ca.uqac.lif.labpal.Stateful.Status;
import ca.uqac.lif.labpal.assistant.Assistant;
import ca.uqac.lif.labpal.assistant.AssistantRun;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.units.Time;
import ca.uqac.lif.units.si.Second;

/**
 * Unit tests for {@link Assistant} and {@link AssistantRun}.
 */
public class AssistantTest 
{
	public static final Time t_20s = new Second(20);
	
	public static final Time t_750ms = new Second(0.75);
	
	public static final Time t_500ms = new Second(0.5);
	                                                                       
	public static final Time t_200ms = new Second(0.2);
	
	public static final Time t_0ms = new Second(0);
	
	@Test(timeout = 5000)
	public void testQueueSingleThread1()
	{
		Assistant assistant = new Assistant();
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		AssistantRun run = assistant.enqueue(de1);
		run.join();
		assertEquals(Status.DONE, de1.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testQueueSingleThread2()
	{
		Assistant assistant = new Assistant();
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		Experiment de2 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		AssistantRun run = assistant.enqueue(de1, de2);
		run.join();
		// Check that both experiments are done when join() returns
		assertEquals(Status.DONE, de1.getStatus());
		assertEquals(Status.DONE, de2.getStatus());
		// de2 started after de1 started
		assertTrue(de2.getStartTime() >= de1.getStartTime());
		// de2 started after de1 ended (since a single thread is being used)
		assertTrue(de2.getStartTime() >= de1.getEndTime());
	}
	
	@Test(timeout = 5000)
	public void testTimeoutSingleThread1()
	{
		Assistant assistant = new Assistant();
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_200ms);
		AssistantRun run = assistant.enqueue(de1);
		run.join();
		assertEquals(Status.INTERRUPTED, de1.getStatus());
		assertTrue(de1.hasTimedOut());
	}
	
	@Test(timeout = 5000)
	public void testTimeoutSingleThread3()
	{
		Assistant assistant = new Assistant();
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_20s);
		Experiment de2 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_20s);
		AssistantRun run = assistant.enqueue(de1, de2);
		run.join();
		assertEquals(Status.DONE, de1.getStatus());
		assertEquals(Status.DONE, de2.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testTimeoutSingleThread2()
	{
		Assistant assistant = new Assistant();
		Experiment de2 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_200ms);
		Experiment de3 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_20s);
		AssistantRun run = assistant.enqueue(de2, de3);
		run.join();
		assertEquals(Status.INTERRUPTED, de2.getStatus());
		assertTrue(de2.hasTimedOut());
		assertEquals(Status.DONE, de3.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testQueueMultipleThread1()
	{
		Assistant assistant = new Assistant().setExecutor(new QueuedThreadPoolExecutor(2));
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		Experiment de2 = new DummyExperiment().setDuration(t_750ms).setTimeout(t_0ms);
		Experiment de3 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		AssistantRun run = assistant.enqueue(de1, de2, de3);
		run.join();
		// Check that both experiments are done when join() returns
		assertEquals(Status.DONE, de1.getStatus());
		assertEquals(Status.DONE, de2.getStatus());
		assertEquals(Status.DONE, de3.getStatus());
		// de2 started after de1 started
		assertTrue(de2.getStartTime() >= de1.getStartTime());
		// de2 started before de1 ended (since two threads is being used)
		assertTrue(de2.getStartTime() <= de1.getEndTime());
		// de3 started after de1 ended (since two threads is being used)
		assertTrue(de3.getStartTime() >= de1.getEndTime());
		// de3 started before de2 ended (since two threads is being used)
		assertTrue(de3.getStartTime() <= de2.getEndTime());
	}
	
	@Test(timeout = 5000)
	public void testSoftStopSingleThread1() throws InterruptedException
	{
		Assistant assistant = new Assistant();
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		AssistantRun run = assistant.enqueue(de1);
		run.join();
		Thread.sleep(100); // Give the assistant some time to start the first experiment
		run.stop(false);
		run.join();
		assertEquals(Status.DONE, de1.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testHardStopSingleThread1() throws InterruptedException
	{
		Assistant assistant = new Assistant();
		Experiment de1 = new DummyExperiment().setDuration(new Second(1.5)).setTimeout(t_0ms);
		AssistantRun run = assistant.enqueue(de1);
		Thread.sleep(100); // Give the assistant some time to start the first experiment 
		run.stop(true);
		run.join();
		assertEquals(Status.INTERRUPTED, de1.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testSoftStopMultipleThread1() throws InterruptedException
	{
		Assistant assistant = new Assistant().setExecutor(new QueuedThreadPoolExecutor(2));
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		Experiment de2 = new DummyExperiment().setDuration(t_750ms).setTimeout(t_0ms);
		Experiment de3 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		AssistantRun run = assistant.enqueue(de1, de2, de3);
		Thread.sleep(200); // Give enough time only for de1 and de2 to start, and hence to complete
		run.stop(false);
		run.join();
		assertEquals(Status.DONE, de1.getStatus());
		assertEquals(Status.DONE, de2.getStatus());
		assertEquals(Status.READY, de3.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testHardStopMultipleThread1() throws InterruptedException
	{
		Assistant assistant = new Assistant().setExecutor(new QueuedThreadPoolExecutor(2));
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		Experiment de2 = new DummyExperiment().setDuration(t_750ms).setTimeout(t_0ms);
		Experiment de3 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		AssistantRun run = assistant.enqueue(de1, de2, de3);
		Thread.sleep(200); // Give enough time only for de1 and de2 to start, and hence to be cancelled
		run.stop(true);
		run.join();
		assertEquals(Status.INTERRUPTED, de1.getStatus());
		assertEquals(Status.INTERRUPTED, de2.getStatus());
		assertEquals(Status.READY, de3.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testTimeoutMultipleThread1()
	{
		Assistant assistant = new Assistant().setExecutor(new QueuedThreadPoolExecutor(2));
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_200ms);
		AssistantRun run = assistant.enqueue(de1);
		run.join();
		assertEquals(Status.INTERRUPTED, de1.getStatus());
		assertTrue(de1.hasTimedOut());
	}
	
	@Test(timeout = 5000)
	public void testTimeoutMultipleThread2()
	{
		Assistant assistant = new Assistant().setExecutor(new QueuedThreadPoolExecutor(2));
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		Experiment de2 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_200ms);
		Experiment de3 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		AssistantRun run = assistant.enqueue(de1, de2, de3);
		run.join();
		assertEquals(Status.DONE, de1.getStatus());
		assertEquals(Status.INTERRUPTED, de2.getStatus());
		assertTrue(de2.hasTimedOut());
		assertEquals(Status.DONE, de3.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testMultipleRunsSingleThread1()
	{
		Assistant assistant = new Assistant();
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		Experiment de2 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		AssistantRun run1 = assistant.enqueue(de1);
		AssistantRun run2 = assistant.enqueue(de2);
		run1.join();
		run2.join();
		assertEquals(Status.DONE, de1.getStatus());
		assertEquals(Status.DONE, de2.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testMultipleRunsCancelSingleThread1() throws InterruptedException
	{
		Assistant assistant = new Assistant();
		Experiment de1 = new DummyExperiment().setDuration(new Second(1)).setTimeout(t_0ms);
		Experiment de2 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		AssistantRun run1 = assistant.enqueue(de1);
		Thread.sleep(100);
		AssistantRun run2 = assistant.enqueue(de2);
		run1.stop(true);
		run1.join();
		run2.join();
		assertEquals(Status.INTERRUPTED, de1.getStatus());
		assertEquals(Status.DONE, de2.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testMultipleRunsMultiThread1()
	{
		Assistant assistant = new Assistant().setExecutor(new QueuedThreadPoolExecutor(2));
		Experiment de1 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		Experiment de2 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		Experiment de3 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		Experiment de4 = new DummyExperiment().setDuration(t_500ms).setTimeout(t_0ms);
		AssistantRun run1 = assistant.enqueue(de1, de2);
		AssistantRun run2 = assistant.enqueue(de3, de4);
		run1.join();
		run2.join();
		assertEquals(Status.DONE, de1.getStatus());
		assertEquals(Status.DONE, de2.getStatus());
		assertEquals(Status.DONE, de3.getStatus());
		assertEquals(Status.DONE, de4.getStatus());
		// Check that de1 and de2 finished before de3 and de4 started
		assertTrue(de3.getStartTime() >= de1.getEndTime());
		assertTrue(de3.getStartTime() >= de2.getEndTime());
		assertTrue(de4.getStartTime() >= de1.getEndTime());
		assertTrue(de4.getStartTime() >= de2.getEndTime());
	}
}
