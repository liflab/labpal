package browser;

import java.util.List;

import ca.uqac.lif.labpal.Group;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.plot.TwoDimensionalPlot.Axis;
import ca.uqac.lif.labpal.plot.gnuplot.ClusteredHistogram;
import ca.uqac.lif.labpal.plot.gnuplot.GnuPlot;
import ca.uqac.lif.labpal.server.WebCallback;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.Table.Type;
import ca.uqac.lif.labpal.table.ExperimentTable;

public class BrowserLab extends Laboratory
{
	public static void main(String[] args)
	{
		initialize(args, BrowserLab.class);
	}

	@Override
	public void setupExperiments(ArgumentMap map, List<WebCallback> callbacks)
	{
		setTitle("Browser market share");
		setAuthorName("Emmett Brown");
		ExperimentTable et = new ExperimentTable("browser", "market");
		ExperimentTable pc_et = new ExperimentTable();
		
		// Add experiments
		Group group_ie = new Group("Experiments for IE");
		add(new BrowserExperiment("IE", "video", 30), group_ie, et, pc_et);
		add(new BrowserExperiment("IE", "audio", 10), group_ie, et, pc_et);
		add(new BrowserExperiment("IE", "flash", 25), group_ie, et, pc_et);
		add(new BrowserExperiment("IE", "html", 20), group_ie, et, pc_et);
		add(new BrowserExperiment("IE", "js", 15), group_ie, et, pc_et);
		add(new BrowserExperiment("Firefox", "video", 20), et, pc_et);
		add(new BrowserExperiment("Firefox", "audio", 5), et, pc_et);
		add(new BrowserExperiment("Firefox", "flash", 35), et, pc_et);
		add(new BrowserExperiment("Firefox", "html", 30), et, pc_et);
		add(new BrowserExperiment("Firefox", "js", 10), et, pc_et);
		add(group_ie);
		
		// Create a histogram
		ClusteredHistogram plot = new ClusteredHistogram(et);
		plot.setTitle("My bar plot");
		plot.setCaption(Axis.X, "Browser").setCaption(Axis.Y, "Share");
		plot.setPalette(GnuPlot.QUALITATIVE_1);
		
		// Create another histogram
		ClusteredHistogram stacked_plot = new ClusteredHistogram(et);
		stacked_plot.setTitle("My bar plot");
		add(plot, stacked_plot);
		
		// Create a pie chart
		/*PieChart pc_ie = new PieChart(pc_et);
		pc_ie.setTitle("Market share for IE").assignTo(this);*/

		
	}

}
