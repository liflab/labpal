package ca.uqac.lif.labpal.assistant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import ca.uqac.lif.labpal.experiment.Experiment;

public class Subsample implements ExperimentScheduler
{

	@Override
	public List<Experiment> schedule(Collection<Experiment> experiments)
	{
		List<Experiment> list = new ArrayList<Experiment>(experiments.size());
		list.addAll(experiments);
		return scheduleFromList(list);
	}

	@Override
	public List<Experiment> schedule(Queue<Experiment> experiments)
	{
		List<Experiment> list = new ArrayList<Experiment>(experiments.size());
		list.addAll(experiments);
		return scheduleFromList(list);
	}
	
	protected static List<Experiment> scheduleFromList(List<Experiment> experiments)
	{
		List<Experiment> out_list = new ArrayList<Experiment>();
		for (int i = 0; i < experiments.size(); i += 2)
		{
			out_list.add(experiments.get(i));
		}
		return out_list;
	}

}
