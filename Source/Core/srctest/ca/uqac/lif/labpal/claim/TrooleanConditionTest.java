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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ca.uqac.lif.labpal.claim.Troolean.Value;
import static ca.uqac.lif.labpal.claim.Troolean.Value.FALSE;
import static ca.uqac.lif.labpal.claim.Troolean.Value.INCONCLUSIVE;
import static ca.uqac.lif.labpal.claim.Troolean.Value.TRUE;

import ca.uqac.lif.petitpoucet.OrNode;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.booleans.And;
import ca.uqac.lif.petitpoucet.function.number.IsOdd;

/**
 * Unit tests for {@link TrooleanCondition}.
 */
public class TrooleanConditionTest
{
	@Test
	public void test1()
	{
		TrooleanCondition tc = new TrooleanCondition(new IsOdd());
		Value v = (Value) tc.evaluate(3)[0];
		assertEquals(TRUE, v);
	}
	
	@Test
	public void test2()
	{
		TrooleanCondition tc = new TrooleanCondition(new IsOdd());
		Value v = (Value) tc.evaluate(new Object[] {null})[0];
		assertEquals(INCONCLUSIVE, v);
	}
	
	@Test
	public void test3()
	{
		TrooleanCondition tc = new TrooleanCondition(new IsOdd());
		Value v = (Value) tc.evaluate(2)[0];
		assertEquals(FALSE, v);
	}
	
	@Test
	public void test4()
	{
		TrooleanCondition tc = new TrooleanCondition(new And(3));
		Value v = (Value) tc.evaluate(true, false, true)[0];
		assertEquals(FALSE, v);
	}
	
	@Test
	public void test5()
	{
		TrooleanCondition tc = new TrooleanCondition(new And(3));
		Value v = (Value) tc.evaluate(null, true, null)[0];
		assertEquals(INCONCLUSIVE, v);
		PartNode root = tc.getExplanation(NthOutput.FIRST);
		OrNode or = (OrNode) root.getOutputLinks(0).get(0).getNode();
		{
			PartNode child = (PartNode) or.getOutputLinks(0).get(0).getNode();
			assertEquals(0, NthInput.mentionedInput(child.getPart()));
		}
		{
			PartNode child = (PartNode) or.getOutputLinks(0).get(1).getNode();
			assertEquals(2, NthInput.mentionedInput(child.getPart()));
		}
	}
}
