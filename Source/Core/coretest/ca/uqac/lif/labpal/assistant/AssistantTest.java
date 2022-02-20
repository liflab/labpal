package ca.uqac.lif.labpal.assistant;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.uqac.lif.labpal.DummyExperiment;
import ca.uqac.lif.labpal.assistant.Assistant;
import ca.uqac.lif.labpal.assistant.AssistantRun;
import ca.uqac.lif.labpal.experiment.Experiment.Status;

/**
 * Unit tests for {@link Assistant} and {@link AssistantRun}.
 */
public class AssistantTest 
{
	@Test(timeout = 5000)
	public void testQueueSingleThread1()
	{
		Assistant assistant = new Assistant();
		DummyExperiment de1 = new DummyExperiment(500, 0);
		AssistantRun run = assistant.enqueue(de1);
		run.join();
		assertEquals(Status.DONE, de1.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testQueueSingleThread2()
	{
		Assistant assistant = new Assistant();
		DummyExperiment de1 = new DummyExperiment(500, 0);
		DummyExperiment de2 = new DummyExperiment(500, 0);
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
		DummyExperiment de1 = new DummyExperiment(500, 200);
		AssistantRun run = assistant.enqueue(de1);
		run.join();
		assertEquals(Status.TIMEOUT, de1.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testTimeoutSingleThread3()
	{
		Assistant assistant = new Assistant();
		DummyExperiment de1 = new DummyExperiment(500, 20000);
		DummyExperiment de2 = new DummyExperiment(500, 20000);
		AssistantRun run = assistant.enqueue(de1, de2);
		run.join();
		assertEquals(Status.DONE, de1.getStatus());
		assertEquals(Status.DONE, de2.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testTimeoutSingleThread2()
	{
		Assistant assistant = new Assistant();
		DummyExperiment de2 = new DummyExperiment(500, 200);
		DummyExperiment de3 = new DummyExperiment(500, 20000);
		AssistantRun run = assistant.enqueue(de2, de3);
		run.join();
		assertEquals(Status.TIMEOUT, de2.getStatus());
		assertEquals(Status.DONE, de3.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testQueueMultipleThread1()
	{
		Assistant assistant = new Assistant().setExecutor(new QueuedThreadPoolExecutor(2));
		DummyExperiment de1 = new DummyExperiment(500, 0);
		DummyExperiment de2 = new DummyExperiment(750, 0);
		DummyExperiment de3 = new DummyExperiment(500, 0);
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
		DummyExperiment de1 = new DummyExperiment(500, 0);
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
		DummyExperiment de1 = new DummyExperiment(1500, 0);
		AssistantRun run = assistant.enqueue(de1);
		Thread.sleep(100); // Give the assistant some time to start the first experiment 
		run.stop(true);
		run.join();
		assertEquals(Status.CANCELLED, de1.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testSoftStopMultipleThread1() throws InterruptedException
	{
		Assistant assistant = new Assistant().setExecutor(new QueuedThreadPoolExecutor(2));
		DummyExperiment de1 = new DummyExperiment(500, 0);
		DummyExperiment de2 = new DummyExperiment(750, 0);
		DummyExperiment de3 = new DummyExperiment(500, 0);
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
		DummyExperiment de1 = new DummyExperiment(500, 0);
		DummyExperiment de2 = new DummyExperiment(750, 0);
		DummyExperiment de3 = new DummyExperiment(500, 0);
		AssistantRun run = assistant.enqueue(de1, de2, de3);
		Thread.sleep(200); // Give enough time only for de1 and de2 to start, and hence to be cancelled
		run.stop(true);
		run.join();
		assertEquals(Status.CANCELLED, de1.getStatus());
		assertEquals(Status.CANCELLED, de2.getStatus());
		assertEquals(Status.READY, de3.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testTimeoutMultipleThread1()
	{
		Assistant assistant = new Assistant().setExecutor(new QueuedThreadPoolExecutor(2));
		DummyExperiment de1 = new DummyExperiment(500, 200);
		AssistantRun run = assistant.enqueue(de1);
		run.join();
		assertEquals(Status.TIMEOUT, de1.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testTimeoutMultipleThread2()
	{
		Assistant assistant = new Assistant().setExecutor(new QueuedThreadPoolExecutor(2));
		DummyExperiment de1 = new DummyExperiment(500, 0);
		DummyExperiment de2 = new DummyExperiment(500, 200);
		DummyExperiment de3 = new DummyExperiment(500, 0);
		AssistantRun run = assistant.enqueue(de1, de2, de3);
		run.join();
		assertEquals(Status.DONE, de1.getStatus());
		assertEquals(Status.TIMEOUT, de2.getStatus());
		assertEquals(Status.DONE, de3.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testMultipleRunsSingleThread1()
	{
		Assistant assistant = new Assistant();
		DummyExperiment de1 = new DummyExperiment(500, 0);
		DummyExperiment de2 = new DummyExperiment(500, 0);
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
		DummyExperiment de1 = new DummyExperiment(1000, 0);
		DummyExperiment de2 = new DummyExperiment(500, 0);
		AssistantRun run1 = assistant.enqueue(de1);
		Thread.sleep(100);
		AssistantRun run2 = assistant.enqueue(de2);
		run1.stop(true);
		run1.join();
		run2.join();
		assertEquals(Status.CANCELLED, de1.getStatus());
		assertEquals(Status.DONE, de2.getStatus());
	}
	
	@Test(timeout = 5000)
	public void testMultipleRunsMultiThread1()
	{
		Assistant assistant = new Assistant().setExecutor(new QueuedThreadPoolExecutor(2));
		DummyExperiment de1 = new DummyExperiment(500, 0);
		DummyExperiment de2 = new DummyExperiment(500, 0);
		DummyExperiment de3 = new DummyExperiment(500, 0);
		DummyExperiment de4 = new DummyExperiment(500, 0);
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
