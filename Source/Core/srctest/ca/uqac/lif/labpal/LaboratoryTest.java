/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2022 Sylvain Hallé

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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.azrael.json.JsonPrinter;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.labpal.Persistent.PersistenceException;
import ca.uqac.lif.labpal.Stateful.Status;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.macro.Macro;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.labpal.table.Table;

/**
 * Unit tests for {@link Laboratory}.
 */
public class LaboratoryTest
{
	@Before
	public void resetAll()
	{
		Experiment.resetCounter();
		Table.resetCounter();
		Plot.resetCounter();
		Macro.resetCounter();
	}
	
	@Test
	public void testLoad1() throws PrintException, ReadException, PersistenceException
	{
		MyLab lab = new MyLab();
		lab.setup();
		JsonElement je = new JsonPrinter().print(lab);		
		MyLab s_lab = new MyLab();
		s_lab.loadFromJsonString(je.toString());
		DummyExperiment de1 = (DummyExperiment) s_lab.getExperiment(1);
		assertNotNull(de1);
		DummyExperiment de2 = (DummyExperiment) s_lab.getExperiment(2);
		assertNotNull(de2);
		List<Experiment> exps = de2.dependsOn();
		assertEquals(1, exps.size());
		Experiment e = exps.get(0);
		assertEquals(de1, e);
	}

	@Test
	public void testLoad2() throws PrintException, ReadException, PersistenceException
	{
		MyLab lab = new MyLab();
		lab.setup();
		JsonElement je = new JsonPrinter().print(lab);		
		MyLab s_lab = new MyLab();
		s_lab.loadFromJsonString(je.toString());
		DummyExperiment de1 = (DummyExperiment) s_lab.getExperiment(1);
		assertNotNull(de1);
		DummyExperiment de2 = (DummyExperiment) s_lab.getExperiment(2);
		assertNotNull(de2);
		List<Experiment> exps = de2.dependsOn();
		assertEquals(1, exps.size());
		Experiment e = exps.get(0);
		assertEquals(de1, e);
		assertEquals(Status.DONE, de1.getStatus());
	}
	
	@Test
	public void testZip1() throws IOException, PrintException, ReadException, JsonParseException, PersistenceException, FileSystemException
	{
		MyLab lab = new MyLab();
		lab.setup();
		lab.getExperiment(1).run();
		byte[] zip_contents = lab.saveToZip();
		Laboratory s_lab =  new MyLab();
		s_lab.loadFromZipFile(new ByteArrayInputStream(zip_contents));
		DummyExperiment de1 = (DummyExperiment) s_lab.getExperiment(1);
		assertNotNull(de1);
		DummyExperiment de2 = (DummyExperiment) s_lab.getExperiment(2);
		assertNotNull(de2);
		List<Experiment> exps = de2.dependsOn();
		assertEquals(1, exps.size());
		Experiment e = exps.get(0);
		assertEquals(de1, e);
		assertEquals(Status.DONE, de1.getStatus());
	}
	
	@Test
	public void testUniqueNicknames1()
	{
		MyLab lab = new MyLab();
		lab.add(new ExperimentTable("a").setNickname("foo"), new ExperimentTable("a").setNickname("bar"));
		assertTrue(lab.getNonUniqueNicknames().isEmpty());
	}
	
	@Test
	public void testUniqueNicknames2()
	{
		MyLab lab = new MyLab();
		lab.add(new ExperimentTable("a").setNickname("foo"), new ExperimentTable("a").setNickname("bar"), new ExperimentTable("a").setNickname("foo"));
		List<String> dups = lab.getNonUniqueNicknames();
		assertEquals(1, dups.size());
		assertEquals("foo", dups.get(0));
	}

	protected static class MyLab extends Laboratory
	{
		@Override
		public void setup()
		{
			setAuthor("Barney Rubble");
			setDoi("1234");
			setName("My lab");

			DummyExperiment de1 = new DummyExperiment();
			DummyExperiment de2 = new DummyExperiment();
			de2.dependsOn(de1);
			add(de1, de2);
		}
	}
}
