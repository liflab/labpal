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
package examples.sorting;

import ca.uqac.lif.dag.NodeConnector;
import ca.uqac.lif.labpal.claim.TrooleanQuantifier.AllObjects;
import ca.uqac.lif.labpal.claim.ExperimentPair.First;
import ca.uqac.lif.labpal.claim.ExperimentPair.Second;
import ca.uqac.lif.labpal.claim.PairClaim;
import ca.uqac.lif.labpal.claim.TrooleanCondition;
import ca.uqac.lif.labpal.claim.ValueOf;
import ca.uqac.lif.labpal.experiment.ExperimentFactory;
import ca.uqac.lif.labpal.region.Region;
import ca.uqac.lif.petitpoucet.function.Circuit;
import ca.uqac.lif.petitpoucet.function.Fork;
import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.units.functions.CompareQuantities.QuantityIsLessThan;

import static examples.sorting.SortExperiment.DURATION;

public abstract class DurationComparison extends PairClaim
{
	protected final ExperimentFactory<?> m_factory;

	protected final Region m_region;

	public DurationComparison(ExperimentFactory<?> factory, Region r)
	{
		super(getFunction());
		m_factory = factory;
		m_region = r;
	}

	/**
	 * Gets the function to be evaluated on the set of experiment pairs.
	 * This function asserts that the DURATION value of the first experiment in
	 * the pair is greater than the DURATION value of the second experiment in
	 * the pair. 
	 * @return The function
	 */
	protected static Function getFunction()
	{
		Circuit c = new Circuit(1, 1);
		Fork f = new Fork(2);
		c.associateInput(0, f.getInputPin(0));
		First e1 = new First();
		NodeConnector.connect(f, 0, e1, 0);
		Second e2 = new Second();
		NodeConnector.connect(f, 1, e2, 0);
		ValueOf d1 = new ValueOf(DURATION);
		NodeConnector.connect(e1, 0, d1, 0);
		ValueOf d2 = new ValueOf(DURATION);
		NodeConnector.connect(e2, 0, d2, 0);
		TrooleanCondition gt = new TrooleanCondition(new QuantityIsLessThan());
		NodeConnector.connect(d1, 0, gt, 0);
		NodeConnector.connect(d2, 0, gt, 1);
		c.associateOutput(0, gt.getOutputPin(0));
		c.addNodes(f, e1, e2, d1, d2, gt);
		return new AllObjects(c);
	}
}
