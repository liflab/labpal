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
package ca.uqac.lif.labpal.test;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.ExperimentException;
import ca.uqac.lif.labpal.ExperimentValue;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.UnknownNode;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Unit tests for {@link ExperimentTable}.
 */
public class ExperimentTableTest
{
	@Test
	public void test1()
	{
		ExperimentTable f = new ExperimentTable("A", "B");
		f.add(new DummyExperiment().writeTo("A", 0).writeTo("B", 5));
		f.add(new DummyExperiment().writeTo("A", 1).writeTo("B", 9));
		Spreadsheet s = f.getSpreadsheet();
		assertEquals(Spreadsheet.read(2, 3, 
				"A", "B",
				0,   5,
				1,   9), s);
	}
	
	@Test
	public void test2()
	{
		ExperimentTable f = new ExperimentTable("A", "B");
		f.add(new DummyExperiment().writeTo("A", 0).writeTo("B", 5));
		f.add(new DummyExperiment().writeTo("A", 1));
		Spreadsheet s = f.getSpreadsheet();
		assertEquals(Spreadsheet.read(2, 3, 
				"A", "B",
				0,   5,
				1,   null), s);
	}
	
	@Test
	public void test3()
	{
		ExperimentTable f = new ExperimentTable("A", "B");
		f.add(new DummyExperiment().writeTo("A", getList(1, 2, 3)).writeTo("B", getList(4, 5, 6)));
		f.add(new DummyExperiment().writeTo("A", 7).writeTo("B", 8));
		Spreadsheet s = f.getSpreadsheet();
		assertEquals(Spreadsheet.read(2, 5, 
				"A", "B",
				1,   4,
				2,   5,
				3,   6,
				7,   8), s);
	}
	
	@Test
	public void test4()
	{
		ExperimentTable f = new ExperimentTable("A", "B");
		f.add(new DummyExperiment().writeTo("A", getList(1, 2, 3)).writeTo("B", getList(4, 5, 6, 7)));
		f.add(new DummyExperiment().writeTo("A", 7).writeTo("B", 8));
		Spreadsheet s = f.getSpreadsheet();
		assertEquals(Spreadsheet.read(2, 6, 
				"A", "B",
				1,   4,
				2,   5,
				3,   6,
				null,7,
				7,   8), s);
	}
	
	@Test
	public void testExplanation1()
	{
		Experiment e1 = new DummyExperiment().writeTo("A", 0).writeTo("B", 5);
		Experiment e2 = new DummyExperiment().writeTo("A", 1).writeTo("B", 9);
		ExperimentTable f = new ExperimentTable("A", "B");
		f.add(e1);
		f.add(e2);
		f.getSpreadsheet();
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(0, 1), NthOutput.FIRST));
		assertNotNull(root);
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(new ExperimentValue("A"), child.getPart());
		assertEquals(e1, child.getSubject());
	}
	
	@Test
	public void testExplanation2()
	{
		Experiment e1 = new DummyExperiment().writeTo("A", 0).writeTo("B", 5);
		Experiment e2 = new DummyExperiment().writeTo("A", 1);
		ExperimentTable f = new ExperimentTable("A", "B");
		f.add(e1);
		f.add(e2);
		f.getSpreadsheet();
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(2, 1), NthOutput.FIRST));
		assertNotNull(root);
		assertEquals(1, root.getOutputLinks(0).size());
		assertTrue(root.getOutputLinks(0).get(0).getNode() instanceof UnknownNode);
	}
	
	@Test
	public void testExplanation3()
	{
		Experiment e1 = new DummyExperiment().writeTo("A", getList(1, 2, 3)).writeTo("B", getList(4, 5, 6, 7));
		Experiment e2 = new DummyExperiment().writeTo("A", 7).writeTo("B", 8);
		ExperimentTable f = new ExperimentTable("A", "B");
		f.add(e1);
		f.add(e2);
		f.getSpreadsheet();
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(0, 2), NthOutput.FIRST));
		assertNotNull(root);
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(new NthElement(1), new ExperimentValue("A")), child.getPart());
		assertEquals(e1, child.getSubject());
	}
	
	protected static class DummyExperiment extends Experiment
	{

		@Override
		public void execute() throws ExperimentException, InterruptedException
		{
			// Do nothing
		}
		
		public DummyExperiment writeTo(String parameter, Number value)
		{
			super.write(parameter, value);
			return this;
		}
		
		public DummyExperiment writeTo(String parameter, String value)
		{
			super.write(parameter, value);
			return this;
		}
		
		public DummyExperiment writeTo(String parameter, JsonElement value)
		{
			super.write(parameter, value);
			return this;
		}
	}
	
	protected static JsonList getList(Object ... values)
	{
		JsonList list = new JsonList();
		for (Object o : values)
		{
			if (o instanceof JsonElement)
			{
				list.add((JsonElement) o);
			}
			if (o instanceof String)
			{
				list.add((String) o);
			}
			if (o instanceof Number)
			{
				list.add((Number) o);
			}
		}
		return list;
	}
}
