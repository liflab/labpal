/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

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
package browser;

import ca.uqac.lif.labpal.Group;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.LabPalGnuplot;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.labpal.table.TransformedTable;
import ca.uqac.lif.spreadsheet.functions.ExpandAsColumns;
import ca.uqac.lif.spreadsheet.plot.Plot.Axis;
import ca.uqac.lif.spreadsheet.plots.gnuplot.GnuplotHistogram;

/**
 * Perform a transformation on a table before plotting it.
 */
public class TransformLab extends Laboratory
{
	public static void main(String[] args)
	{
		// Nothing more to do here
		System.exit(initialize(args, TransformLab.class));
	}

	@Override
	public void setup()
	{
		setTitle("Browser market share");
		setAuthor("Emmett Brown");
		ExperimentTable et = new ExperimentTable("browser", "market", "share");
		et.setTitle("Browser market share");
		ExperimentTable pc_et = new ExperimentTable("share");
		pc_et.setTitle("Market share for IE");
		add(et, pc_et); // Add tables to lab
		
		// Add experiments
		Group group_ie = new Group("Experiments for IE");
		Group group_ff = new Group("Experiments for Firefox");
		add(group_ie, group_ff);
		add(new BrowserExperiment("IE", "video", 30), group_ie, et, pc_et);
		add(new BrowserExperiment("IE", "audio", 10), group_ie, et, pc_et);
		add(new BrowserExperiment("IE", "flash", 25), group_ie, et, pc_et);
		add(new BrowserExperiment("IE", "html", 20), group_ie, et, pc_et);
		add(new BrowserExperiment("IE", "js", 15), group_ie, et, pc_et);
		add(new BrowserExperiment("Firefox", "video", 20), group_ff, et);
		add(new BrowserExperiment("Firefox", "audio", 5), group_ff, et);
		add(new BrowserExperiment("Firefox", "flash", 35), group_ff, et);
		add(new BrowserExperiment("Firefox", "html", 30), group_ff, et);
		add(new BrowserExperiment("Firefox", "js", 10), group_ff, et);
		
		// Create a histogram
		TransformedTable tt = new TransformedTable(new ExpandAsColumns("market", "share"), et);
		add(tt);
		LabPalGnuplot plot = new LabPalGnuplot(tt, new GnuplotHistogram()
				.setTitle("My bar plot")
				.setCaption(Axis.X, "Browser").setCaption(Axis.Y, "Share"));
		add(plot);
		
		// Create a stacked bar plot
		LabPalGnuplot stacked_plot = new LabPalGnuplot(tt, new GnuplotHistogram()
				.rowStacked().setTitle("My stacked plot"));
		add(stacked_plot);
		
		// Create a pie chart
		PieChart pie = new PieChart(pc_et);
		add(pie);
		
	}

}
