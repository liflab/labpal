package browser;

import java.util.List;

import ca.uqac.lif.labpal.Group;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.plot.BarPlot;
import ca.uqac.lif.labpal.plot.PieChart;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.server.ParkBenchCallback;
import ca.uqac.lif.labpal.table.ExperimentTable;

public class BrowserLab extends Laboratory
{
	public static void main(String[] args)
	{
		initialize(args, BrowserLab.class);
	}

	@Override
	public void setupExperiments(ArgumentMap map, List<ParkBenchCallback> callbacks)
	{
		setTitle("Browser market share");
		ExperimentTable et = new ExperimentTable();
		et.useForX("browser").useForY("share").groupBy("market");
		ExperimentTable pc_et = new ExperimentTable();
		pc_et.useForX("market").useForY("share");
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
		BarPlot plot = new BarPlot(et);
		plot.labelX("Browser").labelY("Share").setTitle("My bar plot");
		plot.setPalette(Plot.QUALITATIVE_1);
		plot.assignTo(this);
		BarPlot stacked_plot = new BarPlot(et);
		stacked_plot.setTitle("My bar plot").assignTo(this);
		PieChart pc_ie = new PieChart(pc_et);
		pc_ie.setTitle("Market share for IE").assignTo(this);

		
	}

}
