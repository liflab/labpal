package examples.sorting;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.experiment.MultiClassExperimentFactory;

public class SortExperimentFactory extends MultiClassExperimentFactory<SortExperiment>
{
	public SortExperimentFactory(Laboratory lab)
	{
		super(lab, SortExperiment.ALGORITHM);
	}
	
	@Override
	protected Class<? extends SortExperiment> getClassFor(String dimension)
	{
		switch (dimension)
		{
		case BubbleSort.NAME:
			return BubbleSort.class;
		case GnomeSort.NAME:
			return GnomeSort.class;
		case QuickSort.NAME:
			return QuickSort.class;
		case ShellSort.NAME:
			return ShellSort.class;
		case BadSort.NAME:
			return BadSort.class;
		}
		return null;
	}
}
