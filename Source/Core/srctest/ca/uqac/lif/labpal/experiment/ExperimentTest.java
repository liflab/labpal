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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import ca.uqac.lif.labpal.DummyExperiment;
import ca.uqac.lif.labpal.Persistent.PersistenceException;
import ca.uqac.lif.labpal.Stateful.Status;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentException;
import ca.uqac.lif.units.Time;
import ca.uqac.lif.units.si.Second;

/**
 * Unit tests for {@link Experiment}.
 */
public class ExperimentTest 
{
	public static final Time t_1s = new Second(1);

	public static final Time t_250ms = new Second(0.25);

	public static final Time t_0s = new Second(0);

	@Test
	public void testLifecycle1()
	{
		Experiment de = new DummyExperiment().setDuration(t_1s).setTimeout(t_0s);
		assertTrue(de.prerequisitesFulfilled());
		assertEquals(Status.READY, de.getStatus());
		assertEquals(0, de.getStartTime());
		assertEquals(0, de.getPrerequisitesTime());
		assertEquals(0, de.getEndTime());
		assertEquals(0, new Second(de.getTotalDuration()).get().floatValue(), 0.001);
	}

	@Test
	public void testLifecycle2() throws ExperimentException, InterruptedException
	{
		Experiment de = new DummyExperiment().hasPrerequisites(true).setDuration(t_1s).setTimeout(t_0s);
		assertFalse(de.prerequisitesFulfilled());
		assertEquals(Status.UNINITIALIZED, de.getStatus());
		assertEquals(0, de.getStartTime());
		assertEquals(0, de.getPrerequisitesTime());
		assertEquals(0, de.getEndTime());
		assertEquals(0, new Second(de.getTotalDuration()).get().floatValue(), 0.001);
		de.fulfillPrerequisites();
		assertEquals(Status.READY, de.getStatus());
	}

	@Test
	public void testLifecycle5()
	{
		Experiment de = new DummyExperiment().hasPrerequisites(true).setDuration(t_1s).setTimeout(t_0s);
		assertFalse(de.prerequisitesFulfilled());
		assertEquals(Status.UNINITIALIZED, de.getStatus());
		de.run();
		assertTrue(((DummyExperiment) de).fulfillCalled());
		assertEquals(Status.DONE, de.getStatus());
	}

	@Test
	public void testLifecycle6()
	{
		Experiment de = new DummyExperiment() {
			public void execute() throws ExperimentException {
				throw new ExperimentException("foo");
			}
		}.setDuration(t_1s).setTimeout(t_0s);
		de.run();
		assertEquals(Status.FAILED, de.getStatus());
	}

	@Test
	public void testLifecycle7()
	{
		Experiment de = new DummyExperiment() {
			public void fulfillPrerequisites() throws ExperimentException, InterruptedException {
				throw new ExperimentException("foo");
			}
		}.hasPrerequisites(true).setDuration(t_1s).setTimeout(t_0s);
		de.run();
		assertEquals(Status.FAILED, de.getStatus());
	}

	@Test
	public void testLifecycle8() throws InterruptedException
	{
		Experiment de = new DummyExperiment() {
			public void fulfillPrerequisites() throws InterruptedException {
				Thread.sleep(1000);
			}
		}.hasPrerequisites(true).setDuration(new Second(2)).setTimeout(t_0s);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(de);
		Thread.sleep(500);
		executor.shutdownNow();
		executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		assertEquals(Status.INTERRUPTED, de.getStatus());
	}

	@Test
	public void testLifecycle9() throws InterruptedException
	{
		Experiment de = new DummyExperiment() {
			public void execute() throws InterruptedException {
				Thread.sleep(1000);
			}
		}.setDuration(t_1s).setTimeout(t_0s);;
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
		Experiment de = new DummyExperiment() {
			public void execute() throws InterruptedException, ExperimentException {
				writeOutput("foo", 42);
				super.execute();
			}
		}.setDuration(t_250ms).setTimeout(t_0s);
		de.run();
		assertEquals(Status.DONE, de.getStatus());
		assertTrue(de.getStartTime() > 0);
		assertEquals(de.getStartTime(), de.getPrerequisitesTime());
		assertTrue(de.getEndTime() > de.getStartTime());
		de.reset();
		assertEquals(Status.READY, de.getStatus());
		assertEquals(0, de.getStartTime());
		assertEquals(0, de.getPrerequisitesTime());
		assertEquals(0, de.getEndTime());
	}

	@Test
	public void testLifecycle4()
	{
		Experiment de = new DummyExperiment() {
			public void fulfillPrerequisites() throws ExperimentException, InterruptedException
			{
				super.fulfillPrerequisites();
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			public void execute() throws InterruptedException, ExperimentException {
				writeOutput("foo", 42);
				super.execute();
			}
		}.hasPrerequisites(true).setDuration(t_250ms).setTimeout(t_0s);
		de.run();
		assertEquals(Status.DONE, de.getStatus());
		assertTrue(de.getStartTime() > 0);
		assertTrue(de.getPrerequisitesTime() > de.getStartTime());
		assertTrue(de.getEndTime() > de.getPrerequisitesTime());
		de.reset();
		assertEquals(Status.READY, de.getStatus());
		assertEquals(0, de.getStartTime());
		assertEquals(0, de.getPrerequisitesTime());
		assertEquals(0, de.getEndTime());
	}

	@Test
	public void testReset1()
	{
		Experiment de = new DummyExperiment() {
			public void execute() {
				writeOutput("foo", 42);
			}
		}.setDuration(t_1s).setTimeout(t_0s);
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
	public void testPersist1() throws PersistenceException
	{
		// Test serialization before the experiment has run
		Experiment de = new SerializableExperiment().setDuration(t_1s).setTimeout(t_250ms);
		Object printed = de.saveState();
		Experiment copy = new SerializableExperiment();
		copy.loadState(printed);
		assertEquals(de.getId(), copy.getId());
		assertEquals(de.read("bar"), copy.read("bar"));
		assertEquals(de.getStatus(), copy.getStatus());
		assertEquals(de.getStartTime(), copy.getStartTime());
		assertEquals(de.getPrerequisitesTime(), copy.getPrerequisitesTime(), 0.00001);
		assertEquals(de.getEndTime(), copy.getEndTime());
		assertEquals(de.getProgression(), copy.getProgression(), 0.00001);
		assertEquals(de.getTimeRatio(), copy.getTimeRatio(), 0.00001);
		assertEquals(de.getTimeout().get().floatValue(), copy.getTimeout().get().floatValue(), 0.001);
	}

	@Test
	public void testPersist2() throws PersistenceException
	{
		// Test serialization after the experiment has run
		SerializableExperiment de = new SerializableExperiment();
		de.setDuration(t_1s).setTimeout(t_250ms);
		de.run();
		Object printed = de.saveState();
		SerializableExperiment copy = new SerializableExperiment();
		copy.loadState(printed);
		assertEquals(de.getId(), copy.getId());
		assertEquals(de.read("bar"), copy.read("bar"));
		assertEquals(de.getStatus(), copy.getStatus());
		assertEquals(de.getStartTime(), copy.getStartTime());
		assertEquals(de.getPrerequisitesTime(), copy.getPrerequisitesTime(), 0.00001);
		assertEquals(de.getEndTime(), copy.getEndTime());
		assertEquals(de.getProgression(), copy.getProgression(), 0.00001);
		assertEquals(de.getTimeRatio(), copy.getTimeRatio(), 0.00001);
		assertEquals(0, de.getTimeout().compareTo(copy.getTimeout()));
		List<?> orig_list = (List<?>) de.read("somelist");
		List<?> copy_list = (List<?>) copy.read("somelist");
		assertNotNull(copy_list);
		assertEquals(orig_list.size(), copy_list.size());
		// Also check inner fields of the descendant
		assertEquals(de.hasPrerequisites(), copy.hasPrerequisites());
	}

	public static class SerializableExperiment extends DummyExperiment
	{
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

		@Override
		public Map<String,Object> saveState() throws PersistenceException
		{
			Map<String,Object> map = super.saveState();
			List<Boolean> list = new ArrayList<Boolean>(2);
			list.add(m_fulfillCalled);
			list.add(m_hasPrerequisites);
			map.put("other", list);
			return map;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void loadState(Object o) throws PersistenceException
		{
			super.loadState(o);
			Map<String,Object> map = (Map<String,Object>) o;
			if (!map.containsKey("other"))
			{
				throw new PersistenceException("");
			}
			Object o_list = map.get("other");
			if (!(o_list instanceof List))
			{
				throw new PersistenceException("");
			}
			List<?> list = (List<?>) o_list;
			m_fulfillCalled = Boolean.TRUE.equals(list.get(0));
			m_hasPrerequisites = Boolean.TRUE.equals(list.get(1));
		}
	}
}
