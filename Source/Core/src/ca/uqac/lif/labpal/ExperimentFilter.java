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
package ca.uqac.lif.labpal;

import java.util.HashSet;
import java.util.Set;

/**
 * Determines if an experiment should be shown in the lab's interface
 * @author Sylvain Hallé
 */
public class ExperimentFilter
{
	/**
	 * Create a new experiment filter
	 * @param parameters A string containing parameters to instantiate the
	 * filter
	 */
	public ExperimentFilter(String parameters)
	{
		super();
	}

	/**
	 * Determines if an experiment should be shown in the lab's interface
	 * @param e The experiment
	 * @return {@code true} if the experiment should appear, {@code false}
	 * otherwise
	 */
	public boolean include(Experiment e)
	{
		return true;
	}

	/**
	 * Experiment filter based on experiment IDs
	 */
	public static class IdFilter extends ExperimentFilter
	{
		/**
		 * The IDs of experiments to include
		 */
		protected Set<Integer> m_ids = new HashSet<Integer>();


		/**
		 * Creates a new ID filter
		 * @param parameters A string with comma-separated numbers
		 * or ranges of numbers
		 */
		public IdFilter(String parameters)
		{
			super("");
			parameters = parameters.replaceAll("[^0-9,\\-]", "");
			if (parameters.isEmpty())
			{
				return;
			}
			String[] parts = parameters.split(",");
			for (String part : parts)
			{
				part = part.trim();
				if (part.contains("-"))
				{
					String[] lr_parts = part.split("-");
					int left = Integer.parseInt(lr_parts[0]);
					int right = Integer.parseInt(lr_parts[1]);
					for (int i = left; i <= right; i++)
					{
						m_ids.add(i);
					}
				}
				else
				{
					int id = Integer.parseInt(part);
					m_ids.add(id);
				}
			}
		}
		
		@Override
		public boolean include(Experiment e)
		{
			if (e == null)
			{
				return false;
			}
			if (m_ids.isEmpty())
			{
				return true;
			}
			return m_ids.contains(e.getId());
		}

	}
}
