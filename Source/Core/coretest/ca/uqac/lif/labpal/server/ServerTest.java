package ca.uqac.lif.labpal.server;

import java.io.IOException;

import ca.uqac.lif.labpal.DummyExperiment;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.assistant.Assistant;
import ca.uqac.lif.labpal.assistant.QueuedThreadPoolExecutor;
import ca.uqac.lif.labpal.assistant.SingleThreadExecutor;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentGroup;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.units.si.Second;

public class ServerTest {

	public static void main(String[] args) throws IOException 
	{
		Laboratory lab = new Laboratory();
		//lab.setAssistant(new Assistant(new SingleThreadExecutor()));
		lab.setAssistant(new Assistant(new QueuedThreadPoolExecutor(3)));
		ExperimentGroup g = new ExperimentGroup("My group", "A group of experiments");
		lab.add(g);
		ExperimentTable et = new ExperimentTable("bar");
		et.setTitle("A first table");
		lab.add(et);
		for (int i = 0; i < 10; i++)
		{
			Experiment e = new DummyExperiment().setDuration(new Second(10)).setTimeout(new Second(0));
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
		}
		LabPalServer s = new LabPalServer(lab);
		s.startServer();
	}

}