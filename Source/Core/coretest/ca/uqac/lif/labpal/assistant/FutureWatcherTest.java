package ca.uqac.lif.labpal.assistant;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import ca.uqac.lif.labpal.DummyExperiment;
import ca.uqac.lif.labpal.assistant.FutureWatcher;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.Experiment.Status;
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
		assertEquals(Status.TIMEOUT, e.getStatus());
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
		assertEquals(Status.CANCELLED, e.getStatus());
	}
}
