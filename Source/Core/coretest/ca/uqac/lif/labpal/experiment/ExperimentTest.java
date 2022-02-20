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
package ca.uqac.lif.labpal.experiment;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.azrael.json.JsonPrinter;
import ca.uqac.lif.azrael.json.JsonReader;
import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.labpal.DummyExperiment;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentException;
import ca.uqac.lif.labpal.experiment.Experiment.Status;

/**
 * Unit tests for {@link Experiment}.
 */
public class ExperimentTest 
{
	@Test
	public void testLifecycle1()
	{
		DummyExperiment de = new DummyExperiment(1000, 0);
		assertTrue(de.prerequisitesFulfilled());
		assertEquals(Status.READY, de.getStatus());
		assertEquals(0, de.getStartTime());
		assertEquals(0, de.getPrerequisitesTime());
		assertEquals(0, de.getEndTime());
		assertEquals(0, de.getTotalDuration());
	}
	
	@Test
	public void testLifecycle2() throws ExperimentException, InterruptedException
	{
		DummyExperiment de = new DummyExperiment(1000, 0).hasPrerequisites(true);
		assertFalse(de.prerequisitesFulfilled());
		assertEquals(Status.UNINITIALIZED, de.getStatus());
		assertEquals(0, de.getStartTime());
		assertEquals(0, de.getPrerequisitesTime());
		assertEquals(0, de.getEndTime());
		assertEquals(0, de.getTotalDuration());
		de.fulfillPrerequisites();
		assertEquals(Status.READY, de.getStatus());
	}
	
	@Test
	public void testLifecycle5()
	{
		DummyExperiment de = new DummyExperiment(1000, 0).hasPrerequisites(true);
		assertFalse(de.prerequisitesFulfilled());
		assertEquals(Status.UNINITIALIZED, de.getStatus());
		de.run();
		assertTrue(de.fulfillCalled());
		assertEquals(Status.DONE, de.getStatus());
	}
	
	@Test
	public void testLifecycle6()
	{
		DummyExperiment de = new DummyExperiment(1000, 0) {
			public void execute() throws ExperimentException {
				throw new ExperimentException("foo");
			}
		};
		de.run();
		assertEquals(Status.FAILED, de.getStatus());
	}
	
	@Test
	public void testLifecycle7()
	{
		DummyExperiment de = new DummyExperiment(1000, 0) {
			public void fulfillPrerequisites() throws ExperimentException, InterruptedException {
				throw new ExperimentException("foo");
			}
		}.hasPrerequisites(true);
		de.run();
		assertEquals(Status.FAILED, de.getStatus());
	}
	
	@Test
	public void testLifecycle8() throws InterruptedException
	{
		DummyExperiment de = new DummyExperiment(1000, 0) {
			public void fulfillPrerequisites() throws InterruptedException {
				Thread.sleep(1000);
			}
		}.hasPrerequisites(true);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(de);
		Thread.sleep(250);
		executor.shutdownNow();
		executor.awaitTermination(0, TimeUnit.MILLISECONDS);
		assertEquals(Status.CANCELLED, de.getStatus());
	}
	
	@Test
	public void testLifecycle9() throws InterruptedException
	{
		DummyExperiment de = new DummyExperiment(1000, 0) {
			public void execute() throws InterruptedException {
				Thread.sleep(1000);
			}
		};
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(de);
		Thread.sleep(250);
		executor.shutdownNow();
		executor.awaitTermination(0, TimeUnit.MILLISECONDS);
		assertEquals(Status.RUNNING, de.getStatus());
	}
	
	@Test
	public void testLifecycle3()
	{
		DummyExperiment de = new DummyExperiment(250, 0) {
			public void execute() throws InterruptedException, ExperimentException {
				writeOutput("foo", 42);
				super.execute();
			}
		};
		de.run();
		assertEquals(Status.DONE, de.getStatus());
		assertTrue(de.getStartTime() > 0);
		assertEquals(de.getStartTime(), de.getPrerequisitesTime());
		assertTrue(de.getEndTime() > de.getStartTime());
		de.reset();
		assertEquals(Status.READY, de.getStatus());
		assertEquals(-1, de.getStartTime());
		assertEquals(-1, de.getPrerequisitesTime());
		assertEquals(-1, de.getEndTime());
	}
	
	@Test
	public void testLifecycle4()
	{
		DummyExperiment de = new DummyExperiment(250, 0) {
			public void fulfillPrerequisites() throws ExperimentException, InterruptedException
			{
				super.fulfillPrerequisites();
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			public void execute() throws InterruptedException, ExperimentException {
				writeOutput("foo", 42);
				super.execute();
			}
		}.hasPrerequisites(true);
		de.run();
		assertEquals(Status.DONE, de.getStatus());
		assertTrue(de.getStartTime() > 0);
		assertTrue(de.getPrerequisitesTime() > de.getStartTime());
		assertTrue(de.getEndTime() > de.getPrerequisitesTime());
		de.reset();
		assertEquals(Status.READY, de.getStatus());
		assertEquals(-1, de.getStartTime());
		assertEquals(-1, de.getPrerequisitesTime());
		assertEquals(-1, de.getEndTime());
	}
	
	@Test
	public void testReset1()
	{
		DummyExperiment de = new DummyExperiment(1000, 0) {
			public void execute() {
				writeOutput("foo", 42);
			}
		};
		de.writeInput("bar", "baz");
		assertEquals("baz", de.read("bar"));
		assertNull(de.read("foo"));
		de.run();
		assertEquals("baz", de.read("bar"));
		assertEquals(42, de.read("foo"));
		de.reset();
		assertEquals("baz", de.read("bar"));
		assertNull(de.read("foo"));
	}
	
	@Test
	public void testSerialize1() throws PrintException, ReadException
	{
		// Test serialization before the experiment has run
		Experiment de = new SerializableExperiment(1000, 250);
		JsonPrinter jop = new JsonPrinter();
		JsonElement printed = jop.print(de);
		JsonReader jor = new JsonReader();
		Object o = jor.read(printed);
		assertNotNull(o);
		assertTrue(o instanceof SerializableExperiment);
		SerializableExperiment copy = (SerializableExperiment) o;
		assertEquals(de.getId(), copy.getId());
		assertEquals(de.read("bar"), copy.read("bar"));
		assertEquals(de.getStatus(), copy.getStatus());
		assertEquals(de.getStartTime(), copy.getStartTime());
		assertEquals(de.getPrerequisitesTime(), copy.getPrerequisitesTime(), 0.00001);
		assertEquals(de.getEndTime(), copy.getEndTime());
		assertEquals(de.getProgression(), copy.getProgression(), 0.00001);
		assertEquals(de.getTimeRatio(), copy.getTimeRatio(), 0.00001);
		assertEquals(de.getTimeout(), copy.getTimeout(), 0.00001);
	}
	
	@Test
	public void testSerialize2() throws PrintException, ReadException
	{
		// Test serialization after the experiment has run
		SerializableExperiment de = new SerializableExperiment();
		de.hasPrerequisites(true);
		de.run();
		JsonPrinter jop = new JsonPrinter();
		JsonElement printed = jop.print(de);
		JsonReader jor = new JsonReader();
		Object o = jor.read(printed);
		assertNotNull(o);
		assertTrue(o instanceof SerializableExperiment);
		SerializableExperiment copy = (SerializableExperiment) o;
		assertEquals(de.getId(), copy.getId());
		assertEquals(de.read("bar"), copy.read("bar"));
		assertEquals(de.getStatus(), copy.getStatus());
		assertEquals(de.getStartTime(), copy.getStartTime());
		assertEquals(de.getPrerequisitesTime(), copy.getPrerequisitesTime(), 0.00001);
		assertEquals(de.getEndTime(), copy.getEndTime());
		assertEquals(de.getProgression(), copy.getProgression(), 0.00001);
		assertEquals(de.getTimeRatio(), copy.getTimeRatio(), 0.00001);
		assertEquals(de.getTimeout(), copy.getTimeout(), 0.00001);
		List<?> orig_list = (List<?>) de.read("somelist");
		List<?> copy_list = (List<?>) copy.read("somelist");
		assertNotNull(copy_list);
		assertEquals(orig_list.size(), copy_list.size());
		// Also check inner fields of the descendant
		assertEquals(de.hasPrerequisites(), copy.hasPrerequisites());
	}
	
	public static class SerializableExperiment extends DummyExperiment
	{
		public SerializableExperiment(long duration, long timeout)
		{
			super(duration, timeout);
			writeInput("bar", true);
		}
		
		public SerializableExperiment()
		{
			super();
			writeInput("bar", true);
		}
		
		@Override
		public void execute()
		{
			ArrayList<Object> list = new ArrayList<>();
			list.add("foo");
			list.add(true);
			list.add(255);
			writeOutput("somelist", list);
		}
	}
}
