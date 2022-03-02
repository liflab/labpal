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
package ca.uqac.lif.labpal.experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uqac.lif.labpal.Group;
import ca.uqac.lif.labpal.Stateful;

public class ExperimentGroup extends Group<Experiment> implements Stateful
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
	
	@Override
	/*@ pure non_null @*/ public Status getStatus()
	{
		return Stateful.getLowestStatus(m_objects);
	}
	
	@Override
	/*@ pure @*/ public float getProgression()
	{
		float p = 0;
		float t = 0;
		for (Experiment e : m_objects)
		{
			p += e.getProgression();
			t++;
		}
		if (t == 0)
		{
			return 0;
		}
		return p / t;
	}

	@Override
	public void reset()
	{
		for (Experiment e : m_objects)
		{
			e.reset();
		}
	}
	
}