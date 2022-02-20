package ca.uqac.lif.labpal.assistant;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import ca.uqac.lif.labpal.DummyExperiment;
import ca.uqac.lif.labpal.assistant.FutureWatcher;
import ca.uqac.lif.labpal.experiment.Experiment.Status;

public class FutureWatcherTest 
{
	@Test
	public void testTimeout1() throws InterruptedException
	{
		// Experiment runs to completion
		ExecutorService executor = Executors.newSingleThreadExecutor();
		DummyExperiment e = new DummyExperiment(100, 500);
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
		DummyExperiment e = new DummyExperiment(100, 50);
		Future<?> f = executor.submit(e);
		FutureWatcher w = new FutureWatcher(e, f);
		Thread t = new Thread(w);
		t.start();
		t.join();
		assertEquals(Status.TIMEOUT, e.getStatus());
	}
	
	@Test
	public void testInterrupt1() throws InterruptedException
	{
		// Experiment is externally interrupted before it ends
		ExecutorService executor = Executors.newSingleThreadExecutor();
		DummyExperiment e = new DummyExperiment(500, 0);
		Future<?> f = executor.submit(e);
		FutureWatcher w = new FutureWatcher(e, f);
		Thread t = new Thread(w);
		t.start();
		Thread.sleep(50);
		t.interrupt();
		Thread.sleep(50);
		assertEquals(Status.CANCELLED, e.getStatus());
	}
}
