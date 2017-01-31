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
package sorting;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.macro.MacroScalar;

/**
 * This macro finds the name of the sorting algorithm with the slowest
 * sorting time
 */
public  class SlowestMacro extends MacroScalar
{
	public SlowestMacro(Laboratory lab)
	{
		super(lab, "slowestAlgo", "The name of the slowest sorting algorithm");
	}
	
	@Override
	public JsonElement getValue()
	{
		float longest_time = 0f;
		String algo_name = "None";
		for (Experiment e : m_lab.getExperiments())
		{
			float time = e.readFloat("time");
			if (time > longest_time)
			{
				longest_time = time;
				algo_name = e.readString("name");
			}
		}
		return new JsonString(algo_name);
	}
}