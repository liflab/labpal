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
package ca.uqac.lif.labpal.claim;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.labpal.DummyExperiment;
import ca.uqac.lif.labpal.claim.Troolean.Value;
import ca.uqac.lif.labpal.claim.TrooleanQuantifier.AllObjects;
import ca.uqac.lif.labpal.experiment.ExperimentException;
import ca.uqac.lif.labpal.experiment.ExperimentValue;
import ca.uqac.lif.labpal.provenance.StatusOf;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.OrNode;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;

import static ca.uqac.lif.labpal.claim.Troolean.Value.FALSE;
import static ca.uqac.lif.labpal.claim.Troolean.Value.INCONCLUSIVE;
import static ca.uqac.lif.labpal.claim.Troolean.Value.TRUE;
import static org.junit.Assert.*;

import ca.uqac.lif.units.si.Second;

/**
 * Unit tests for {@link TrooleanQuantifier}.
 */
public class TrooleanQuantifierTest
{
	protected static Second t_0s = new Second(0);
	
	protected static DummyExperiment de1 = new DummyExperiment().setDuration(t_0s);
	protected static DummyExperiment de2 = new DummyExperiment().setDuration(t_0s);
	protected static DummyExperiment de3 = new DummyExperiment().setDuration(t_0s);
	protected static DummyExperiment de4 = new DummyExperiment().setDuration(t_0s);

	@Before
	public void reset()
	{
		de1.reset();
		de2.reset();
		de3.reset();
		de4.reset();
	}
	
	@Test
	public void testAllObjects1()
	{
		AllObjects q = new AllObjects(new TrooleanCondition(new ValueOf("z")));
		Value v = (Value) q.evaluate(getList(de1, de2, de3))[0];
		assertEquals(INCONCLUSIVE, v);
		PartNode root = q.getExplanation(NthOutput.FIRST);
		OrNode or = (OrNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(3, or.getOutputLinks(0).size());
		isStatusBranch(or, 0, 0);
		isStatusBranch(or, 1, 1);
		isStatusBranch(or, 2, 2);
	}
	
	@Test
	public void testAllObjects2() throws ExperimentException, InterruptedException
	{
		AllObjects q = new AllObjects(new TrooleanCondition(new ValueOf("z")));
		de1.run();
		Value v = (Value) q.evaluate(getList(de1, de2, de3))[0];
		assertEquals(FALSE, v);
		PartNode root = q.getExplanation(NthOutput.FIRST);
		isValueBranch(root, 0, 0);
	}
	
	@Test
	public void testAllObjects3() throws ExperimentException, InterruptedException
	{
		AllObjects q = new AllObjects(new TrooleanCondition(new ValueOf("z")));
		de1.run();
		de2.run();
		Value v = (Value) q.evaluate(getList(de1, de2, de3))[0];
		assertEquals(FALSE, v);
		PartNode root = q.getExplanation(NthOutput.FIRST);
		isValueBranch(root, 0, 0);
	}
	
	@Test
	public void testAllObjects4() throws ExperimentException, InterruptedException
	{
		AllObjects q = new AllObjects(new TrooleanCondition(new ValueOf("z")));
		de1.run();
		de2.run();
		de3.run();
		Value v = (Value) q.evaluate(getList(de1, de2, de3))[0];
		assertEquals(FALSE, v);
		PartNode root = q.getExplanation(NthOutput.FIRST);
		OrNode or = (OrNode) root.getOutputLinks(0).get(0).getNode();
		isValueBranch(or, 0, 0);
		isValueBranch(or, 1, 2);
	}
	
	@Test
	public void testAllObjects5() throws ExperimentException, InterruptedException
	{
		AllObjects q = new AllObjects(new TrooleanCondition(new ValueOf("z")));
		de2.run();
		Value v = (Value) q.evaluate(getList(de1, de2, de3))[0];
		assertEquals(INCONCLUSIVE, v);
		PartNode root = q.getExplanation(NthOutput.FIRST);
		AndNode and = (AndNode) root.getOutputLinks(0).get(0).getNode();
		OrNode or = (OrNode) and.getOutputLinks(0).get(0).getNode();
		isStatusBranch(or, 0, 0);
		isStatusBranch(or, 1, 2);
		isValueBranch(and, 1, 1);
	}
	
	@Test
	public void testAllObjects6() throws ExperimentException, InterruptedException
	{
		AllObjects q = new AllObjects(new TrooleanCondition(new ValueOf("z")));
		de2.run();
		de4.run();
		Value v = (Value) q.evaluate(getList(de2, de4))[0];
		assertEquals(TRUE, v);
		PartNode root = q.getExplanation(NthOutput.FIRST);
		AndNode and = (AndNode) root.getOutputLinks(0).get(0).getNode();
		isValueBranch(and, 0, 0);
		isValueBranch(and, 1, 1);
	}
	
	protected static void isStatusBranch(LabelledNode root, int child_index, int elem_index)
	{
		PartNode child1 = (PartNode) root.getOutputLinks(0).get(child_index).getNode();
		assertEquals(NthOutput.FIRST, child1.getPart()); // output of TrooleanCondition
		PartNode child2 = (PartNode) child1.getOutputLinks(0).get(0).getNode();
		assertEquals(NthOutput.FIRST, child2.getPart()); // output of ValueOf
		PartNode child3 = (PartNode) child2.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(StatusOf.instance, NthInput.FIRST), child3.getPart()); // status of input of ValueOf
		PartNode child4 = (PartNode) child3.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(StatusOf.instance, NthInput.FIRST), child4.getPart()); // status of input of TrooleanCondition
		PartNode child5 = (PartNode) child4.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(StatusOf.instance, new NthElement(elem_index), NthInput.FIRST), child5.getPart());
	}
	
	protected static void isValueBranch(LabelledNode root, int child_index, int elem_index)
	{
		PartNode child1 = (PartNode) root.getOutputLinks(0).get(child_index).getNode();
		assertEquals(NthOutput.FIRST, child1.getPart()); // output of TrooleanCondition
		PartNode child2 = (PartNode) child1.getOutputLinks(0).get(0).getNode();
		assertEquals(NthOutput.FIRST, child2.getPart()); // output of ValueOf
		PartNode child3 = (PartNode) child2.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(new ExperimentValue("z"), NthInput.FIRST), child3.getPart()); // input of ValueOf
		PartNode child4 = (PartNode) child3.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(new ExperimentValue("z"), NthInput.FIRST), child4.getPart()); // input of TrooleanCondition
		PartNode child5 = (PartNode) child4.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(new ExperimentValue("z"), new NthElement(elem_index), NthInput.FIRST), child5.getPart());
	}
	
	protected static List<Object> getList(Object ... objects)
	{
		return Arrays.asList(objects);
	}
}
