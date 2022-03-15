package examples;

import static ca.uqac.lif.labpal.region.ConditionalRegion.filter;
import static ca.uqac.lif.labpal.region.DimensionRange.range;
import static ca.uqac.lif.labpal.region.DiscreteRange.range;
import static ca.uqac.lif.labpal.region.ExtensionDomain.extension;
import static ca.uqac.lif.labpal.region.ProductRegion.product;
import static ca.uqac.lif.labpal.table.ExperimentTable.table;
import static ca.uqac.lif.labpal.table.TransformedTable.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.uqac.lif.dag.NodeConnector;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.claim.FunctionClaim;
import ca.uqac.lif.labpal.claim.TrooleanCondition;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentException;
import ca.uqac.lif.labpal.experiment.ExperimentFactory;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.region.Point;
import ca.uqac.lif.labpal.region.Region;

import ca.uqac.lif.labpal.util.Stopwatch;
import ca.uqac.lif.labpal.claim.TrooleanQuantifier.AllObjects;
import ca.uqac.lif.labpal.claim.ValueOf;
import ca.uqac.lif.petitpoucet.function.Circuit;
import ca.uqac.lif.petitpoucet.function.Constant;
import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.petitpoucet.function.number.IsGreaterThan;
import ca.uqac.lif.spreadsheet.chart.Chart.Axis;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotScatterplot;
import ca.uqac.lif.spreadsheet.functions.ExpandAsColumns;
import ca.uqac.lif.spreadsheet.functions.Sort;
import ca.uqac.lif.spreadsheet.units.MoveUnitsToHeader;
import ca.uqac.lif.units.DimensionValue;
import ca.uqac.lif.units.Length;
import ca.uqac.lif.units.Time;
import ca.uqac.lif.units.si.Centimeter;
import ca.uqac.lif.units.si.Millisecond;
import ca.uqac.lif.units.si.Second;

/**
 * A more elaborate example illustrating how to create experiments and
 * manipulate lab objects. Contrary to the simpler {@link FoobarExample},
 * this code excerpt is deliberately verbose to illustrate
 * 
 * @see FoobarExample
 */
public class FoobarExtended
{
	/* We declare the various parameters in the lab as string constants, instead
	 * of writing them as strings everywhere. This improves the legibility of the
	 * code and makes it less prone to typos. */
	public static final transient String ABC = "abc";
	public static final transient String DEF = "def";
	public static final transient String DURATION = "Duration";
	public static final transient String TOOL = "Tool";
	public static final transient String VALUE = "Value";

	public static class MyLab extends Laboratory
	{
		@Override
		public void setup()	
		{
			ExperimentFactory<?> f = new ExperimentFactory<>(this, RunTool.class);

			Region pr = filter(product(range(ABC, new Centimeter(0), new Centimeter(5), new Centimeter(1)), range(DEF, 0, 5),
					extension(TOOL, "Foobar", "Foobaz")), (p) -> {
						return p.getInt(ABC) % 2 == 0 || ((DimensionValue) p.get(ABC)).get().intValue() >= p.getInt(DEF);});

			/* Set abc to fixed value and vary def */
			for (Region r : pr.all(ABC))
			{
				add(new Plot(
						add(transform(table(DEF, TOOL, DURATION).add(f, r)
								.name("Impact of " + DEF + " with ", r), 
								new ExpandAsColumns(TOOL, DURATION), new Sort().by(0), new MoveUnitsToHeader())),
						new GnuplotScatterplot().setCaption(Axis.X, DEF).setCaption(Axis.Y, DURATION)));
			}
			
			/* Set def to fixed value and vary abc. Since the value of abc is not
			 * a scalar, we add an extra processing step so that the unit is moved
			 * to the header of the table, and the cells only contain scalars. */
			for (Region r : pr.all(DEF))
			{
				add(new Plot(
						add(transform(table(ABC, TOOL, DURATION).add(f, r)
								.name("Impact of " + ABC + " with ", r), 
								new ExpandAsColumns(TOOL, DURATION), new Sort().by(0), new MoveUnitsToHeader())),
						new GnuplotScatterplot().setCaption(Axis.X, ABC).setCaption(Axis.Y, DURATION)));
			}
			
			add(new DurationClaim(this));
		}

		/**
		 * An experiment that simulates the fictional execution of two entities
		 * called "Foobar" and "Foobaz", according to two parameters named "abc"
		 * (supposed to be a physical length) and "def".
		 * As Foobar and Foobaz do not really exist, the experiment
		 * fakes some processing by simply waiting for an amount of time arbitrarily
		 * based on the values of abc and def.
		 * <p>
		 * The code inside {@link #execute()} contains some logic destined to
		 * simulate various conditions that can happen when an experiment executes:
		 * <ul>
		 * <li>When abc=0 and def=0, the experiment attempts to write a duration
		 * as a scalar instead of a time duration. This throws an exception and
		 * causes the experiment to fail, illustrating how a correctly designed
		 * experiment can enforce proper manipulation of quantities with units.</li>
		 * <li>When abc=1 and def=0, the experiment deliberately throws an
		 * exception. This illustrates the fact that an experiment can interrupt
		 * and place itself into a failing state, to indicate that something went
		 * wrong.</li> 
		 * <li>The timeout value of 2 seconds will cause some experiments to be
		 * interrupted by the assistant and fail for large values of abc and
		 * def.</li>
		 * </ul>
		 */
		public static class RunTool extends Experiment 
		{			
			public RunTool(Point p)
			{
				/* Write dimensions of the point as input parameters of the experiment. */
				super(p);

				/* Add a textual description and possibly a dimension to the input and
				 * output parameters that are produced by this experiment. When a
				 * parameter has no dimension (i.e. it is a scalar), this argument can
				 * be omitted. The description of parameters is optional, but improves
				 * the legibility of the lab by giving information shown in the web
				 * interface. */
				describe(ABC, "The first parameter of the procedure", Length.DIMENSION);
				describe(DEF, "The second parameter of the procedure");
				describe(TOOL, "The name of the tool that is run in this experiment");
				describe(DURATION, "The time taken to run", Time.DIMENSION);
				describe(VALUE, "The value produced by the tool", Time.DIMENSION);

				/* Sets a maximum duration for the experiment of 0.5 seconds. An
				 * experiment instance taking longer than this duration will be
				 * forcibly interrupted by the assistant and be placed in a failure
				 * state. Calling this method is optional; the experiment is allowed
				 * to run for as long as it needs if no timeout is given. */
				setTimeout(new Second(0.5));
			}

			@Override
			public void execute() throws ExperimentException, InterruptedException 
			{
				/* Start a stopwatch instance associated to the current experiment. */
				Stopwatch.start(this);

				/* Read the name of the "tool" (either Foobar or Foobaz) this
				 * experiment is expected to run. */
				if (readString(TOOL).matches("Foobar")) 
				{
					foobar(read(ABC), readInt(DEF));
				}
				else
				{
					foobaz(read(ABC), readInt(DEF));
				}
				if (((DimensionValue) read(ABC)).get().intValue() == 0 && readInt(DEF) == 0)
				{
					/* In this case only, write the duration as a scalar; this will cause
					 * an experiment reaching this line to end in a failure, since
					 * duration was declared above to be a time value. */ 
					writeOutput(DURATION, Stopwatch.stop(this));
				}
				else
				{
					/* Write the duration as an output parameter, and advertise that its
					 * value is in milliseconds. */
					writeOutput(DURATION, new Millisecond(Stopwatch.stop(this)));
				}

			}
		}

		/**
		 * Simulates the execution of "Foobar" by waiting some arbitrary amount
		 * of time, which is made proportional to the value of abc and to which
		 * some random noise is added to make it more realistic.
		 */
		public static float foobar(Object abc, int def) throws InterruptedException
		{
			Thread.sleep((long) (Math.random() * 100 * ((DimensionValue) abc).get().intValue()));
			return 0;
		}

		/**
		 * Simulates the execution of "Foobaz" by waiting some arbitrary amount
		 * of time, which is made proportional to the value of def and to which
		 * some random noise is added to make it more realistic.
		 * @throws InterruptedException 
		 */
		public static float foobaz(Object abc, int def) throws InterruptedException
		{
			Thread.sleep((long) (Math.random() * 150 * def));
			/* This will throw a division by zero exception when def = 0. This in
			 * turn will cause the that runs this code to end in a FAILED state. */
			return 1 / def;
		}
		
		public static class DurationClaim extends FunctionClaim
		{
			protected Laboratory m_lab;
			
			public DurationClaim(Laboratory lab)
			{
				super(getCondition());
				m_lab = lab;
				setStatement("In all experiments, the value of def is always greater than 1.");
			}
			
			protected static Function getCondition()
			{
				Circuit c = new Circuit(1, 1, "def&gt;1");
				{
					TrooleanCondition gt = new TrooleanCondition(new IsGreaterThan());
					ValueOf v = new ValueOf(DEF);
					Constant one = new Constant(1);
					NodeConnector.connect(v, 0, gt, 0);
					NodeConnector.connect(one, 0, gt, 1);
					c.addNodes(gt, v, one);
					c.associateInput(0, v.getInputPin(0));
					c.associateOutput(0, gt.getOutputPin(0));
				}
				AllObjects ao = new AllObjects(c);
				return ao;
			}

			@Override
			public Collection<Stateful> dependsOn()
			{
				return null;
			}

			@Override
			protected Object[] getInput()
			{
				List<Experiment> exps = new ArrayList<Experiment>();
				exps.addAll(m_lab.getExperiments());
				return new Object[] {exps};
			}
		}

		public static void main(String[] args)
		{
			System.exit(initialize(args, MyLab.class));
		}
	}
}