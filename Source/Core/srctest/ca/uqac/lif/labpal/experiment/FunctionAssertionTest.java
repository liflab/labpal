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

import org.junit.Test;

import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.Constant;
import ca.uqac.lif.petitpoucet.function.Identity;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;

/**
 * Unit tests for {@link FunctionAssertion}.
 */
public class FunctionAssertionTest
{
	@Test
	public void test1() throws FunctionAssertion
	{
		FunctionAssertion fa = new FunctionAssertion("foo", new Constant(true));
		fa.evaluate();
	}
	
	@Test(expected = FunctionAssertion.class)
	public void test2() throws FunctionAssertion
	{
		FunctionAssertion fa = new FunctionAssertion("foo", new Constant(false));
		fa.evaluate();
	}
	
	@Test(expected = FunctionAssertion.class)
	public void test3() throws FunctionAssertion
	{
		FunctionAssertion fa = new FunctionAssertion("foo", new Constant("bar"));
		fa.evaluate();
	}
	
	@Test
	public void test4()
	{
		Identity f = new Identity(1);
		FunctionAssertion fa = new FunctionAssertion("foo", f);
		try
		{
			fa.evaluate(false);
		}
		catch (FunctionAssertion e)
		{
			assertEquals(fa, e);
			PartNode root = e.getExplanation(NthOutput.FIRST);
			assertEquals(1, root.getOutputLinks(0).size());
			PartNode child1 = (PartNode) root.getOutputLinks(0).get(0).getNode();
			assertEquals(1, child1.getOutputLinks(0).size());
			assertEquals(NthOutput.FIRST, child1.getPart());
			assertEquals(f, child1.getSubject());
			PartNode child2 = (PartNode) child1.getOutputLinks(0).get(0).getNode();
			assertEquals(NthInput.FIRST, child2.getPart());
			assertEquals(f, child2.getSubject());
		}
	}
	
	@Test
	public void test5() throws FunctionAssertion
	{
		Identity f = new Identity(1);
		FunctionAssertion fa = new FunctionAssertion("foo", f);
		fa.evaluate(true);
	}
}
