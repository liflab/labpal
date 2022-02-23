package ca.uqac.lif.labpal.experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uqac.lif.labpal.Group;

public class ExperimentGroup extends Group<Experiment>
{
	public ExperimentGroup(String name) 
	{
		super(name);
	}
	
	public ExperimentGroup(String name, String description) 
	{
		super(name, description);
	}
	
	/**
	 * Gets the sorted list of all input parameter names present in the
	 * experiments contained in this group.
	 * @return The list of parameter names 
	 */
	/*@ pure non_null @*/ public List<String> getInputParameters()
	{
		Set<String> params = new HashSet<String>();
		for (Experiment e : m_objects)
		{
			params.addAll(e.getInputParameters().keySet());
		}
		List<String> s_params = new ArrayList<String>(params.size());
		s_params.addAll(params);
		Collections.sort(s_params);
		return s_params;
	}
	
}