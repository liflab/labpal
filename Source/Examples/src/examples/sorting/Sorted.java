package examples.sorting;

import ca.uqac.lif.dag.NodeConnector;
import ca.uqac.lif.labpal.claim.RegionClaim;
//import ca.uqac.lif.labpal.claim.TrooleanQuantifier.AllObjects;
import ca.uqac.lif.labpal.experiment.ExperimentFactory;
import ca.uqac.lif.labpal.region.Region;
import ca.uqac.lif.petitpoucet.function.Circuit;
import ca.uqac.lif.petitpoucet.function.Fork;
import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.petitpoucet.function.Identity;
import ca.uqac.lif.petitpoucet.function.booleans.AllObjects;
import ca.uqac.lif.petitpoucet.function.number.IsLessOrEqual;
import ca.uqac.lif.petitpoucet.function.reflect.Call;
import ca.uqac.lif.petitpoucet.function.vector.ElementAt;
import ca.uqac.lif.petitpoucet.function.vector.Window;

public class Sorted extends RegionClaim
{
	public Sorted(ExperimentFactory<?> factory, Region r)
	{
		super(getFunction(), factory, r);
		setStatement("All lists are correctly sorted.");
	}
	
	protected static Function getFunction()
	{
		Circuit c = new Circuit(1, 1, "Allsort");
		{
			Call gl = new Call("getSortedList");
			c.associateInput(0, gl.getInputPin(0));
			Window w = new Window(new Identity(1), 2);
			NodeConnector.connect(gl, 0, w, 0);
			Circuit gt = new Circuit(1, 1, "Pairsort");
			{
				Fork f = new Fork(2);
				gt.associateInput(0, f.getInputPin(0));
				ElementAt first = new ElementAt(0);
				NodeConnector.connect(f, 0, first, 0);
				ElementAt second = new ElementAt(1);
				NodeConnector.connect(f, 1, second, 0);
				IsLessOrEqual ile = new IsLessOrEqual();
				NodeConnector.connect(first, 0, ile, 0);
				NodeConnector.connect(second, 0, ile, 1);
				gt.associateOutput(0, ile.getOutputPin(0));
				gt.addNodes(f, first, second, ile);
			}
			AllObjects a = new AllObjects(gt);
			NodeConnector.connect(w, 0, a, 0);
			c.associateOutput(0, a.getOutputPin(0));
			c.addNodes(gl, w, a);
		}
		return new AllObjects(c);
	}
}
