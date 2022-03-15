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

import static org.junit.Assert.*;

import org.junit.Test;

import static ca.uqac.lif.labpal.claim.Troolean.Value.FALSE;
import static ca.uqac.lif.labpal.claim.Troolean.Value.INCONCLUSIVE;
import static ca.uqac.lif.labpal.claim.Troolean.Value.TRUE;
import ca.uqac.lif.labpal.claim.Troolean.Value;
import ca.uqac.lif.labpal.claim.TrooleanConnective.And;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.OrNode;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;

/**
 * Unit tests for {@link TrooleanConnective}.
 */
public class TrooleanConnectiveTest
{
	@Test
	public void testAnd1()
	{
		And c = new And(3);
		Value v = (Value) c.evaluate(TRUE, TRUE, TRUE)[0];
		assertEquals(TRUE, v);
		PartNode root = c.getExplanation(NthOutput.FIRST);
		AndNode conn = (AndNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(3, conn.getOutputLinks(0).size());
	}
	
	@Test
	public void testAnd2()
	{
		And c = new And(3);
		Value v = (Value) c.evaluate(TRUE, INCONCLUSIVE, TRUE)[0];
		assertEquals(INCONCLUSIVE, v);
		PartNode root = c.getExplanation(NthOutput.FIRST);
		AndNode conn = (AndNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(2, conn.getOutputLinks(0).size());
		PartNode pn = (PartNode) conn.getOutputLinks(0).get(0).getNode();
		assertEquals(1, NthInput.mentionedInput(pn.getPart()));
		AndNode trues = (AndNode) conn.getOutputLinks(0).get(1).getNode();
		assertEquals(2, trues.getOutputLinks(0).size());
		{
			PartNode child = (PartNode) trues.getOutputLinks(0).get(0).getNode();
			assertEquals(0, NthInput.mentionedInput(child.getPart()));
		}
		{
			PartNode child = (PartNode) trues.getOutputLinks(0).get(1).getNode();
			assertEquals(2, NthInput.mentionedInput(child.getPart()));
		}
	}
	
	@Test
	public void testAnd3()
	{
		And c = new And(3);
		Value v = (Value) c.evaluate(TRUE, INCONCLUSIVE, FALSE)[0];
		assertEquals(FALSE, v);
		PartNode root = c.getExplanation(NthOutput.FIRST);
		PartNode pn = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(2, NthInput.mentionedInput(pn.getPart()));
	}
	
	@Test
	public void testAnd4()
	{
		And c = new And(3);
		Value v = (Value) c.evaluate(FALSE, INCONCLUSIVE, FALSE)[0];
		assertEquals(FALSE, v);
		PartNode root = c.getExplanation(NthOutput.FIRST);
		OrNode conn = (OrNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(2, conn.getOutputLinks(0).size());
		{
			PartNode child = (PartNode) conn.getOutputLinks(0).get(0).getNode();
			assertEquals(0, NthInput.mentionedInput(child.getPart()));
		}
		{
			PartNode child = (PartNode) conn.getOutputLinks(0).get(1).getNode();
			assertEquals(2, NthInput.mentionedInput(child.getPart()));
		}
	}
}
