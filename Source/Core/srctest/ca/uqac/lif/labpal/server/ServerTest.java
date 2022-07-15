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
package ca.uqac.lif.labpal.server;

import java.io.IOException;
import java.util.Set;

import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.TempFolder;
import ca.uqac.lif.labpal.DummyExperiment;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.assistant.Assistant;
import ca.uqac.lif.labpal.assistant.QueuedThreadPoolExecutor;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentGroup;
import ca.uqac.lif.labpal.macro.ExperimentMacro;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotScatterplot;
import ca.uqac.lif.units.si.Second;

public class ServerTest 
{

	public static void main(String[] args) throws IOException, FileSystemException 
	{
		Laboratory lab = new Laboratory();
		//lab.setAssistant(new Assistant(new SingleThreadExecutor()));
		lab.setAssistant(new Assistant(new QueuedThreadPoolExecutor(3)));
		ExperimentGroup g = new ExperimentGroup("My group", "A group of experiments");
		lab.add(g);
		ExperimentTable et = new ExperimentTable("foo", "y");
		et.setTitle("A first table");
		lab.add(et);
		Plot p = new Plot(et, new GnuplotScatterplot());
		lab.add(p);
		ExperimentMacro m = new ExperimentMacro(lab, "Maximum of y", "maxY") {
			public Object getValue(Set<Experiment> set) {
				float max = 0;
				for (Experiment e : set) {
					Number m = (Number) e.read("y");
					if (m != null) max = Math.max(max, m.floatValue());
				}
				return max;
			}
		};
		for (int i = 0; i < 10; i++)
		{
			Experiment e = new DummyExperiment() {
				public void fulfillPrerequisites() throws InterruptedException {
					Thread.sleep(5000);
				}
				
			}.setDuration(new Second(10 * Math.random())).setTimeout(new Second(0));
			e.writeInput("foo", i);
			e.writeInput("bar", "baz" + i);
			if (i % 3 == 0)
			{
				((DummyExperiment) e).hasPrerequisites(true);
			}
			if (i % 2 == 0)
			{
				g.add(e);
				et.add(e);
			}
			lab.add(e);
			m.add(e);
		}
		LabPalServer s = new LabPalServer(lab);
		TempFolder hd = new TempFolder("labpal");
		hd.open();
		System.out.println(hd.getRoot());
		hd.deleteOnClose(false);
		s.startServer();
		System.out.println("Saving done");
	}

}