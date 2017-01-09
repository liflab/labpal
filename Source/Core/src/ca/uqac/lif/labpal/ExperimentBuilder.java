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
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.labpal.EmptyException;

/**
 * Populates the content of an experiment based on results fetched from
 * an external source. This is useful to integrate in a lab results
 * from other experiments (such as results from someone else's paper
 * on the same input data).
 * 
 * @author Sylvain Hallé
 */
public class ExperimentBuilder<T extends Experiment>
{
	protected static final Pattern s_paramPattern = Pattern.compile("(.*?):(.*)");

	/**
	 * Symbol used in the input file to designate an input parameter
	 */
	public static final String s_inputSymbol = "*";

	/**
	 * Symbol used in the input file to designate a comment line
	 */
	public static final String s_commentSymbol = "#";

	/**
	 * The regular expression used to split columns in the input file
	 */
	public static final String s_separatorRegex = "\\t+";
	
	/**
	 * The experiment instance that will be used to create
	 * experiment clones
	 */
	protected final CloneableExperiment<T> m_referenceClone;

	/**
	 * Creates a new experiment builder
	 * @param experiment An experiment instance that will be used to create
	 * experiment clones
	 */
	public ExperimentBuilder(CloneableExperiment<T> experiment)
	{
		super();
		m_referenceClone = experiment;
	}

	/**
	 * Populates the content of an experiment based on results fetched from
	 * an external source.
	 * @param exp The experiment to fill in
	 * @param scanner A scanner to the source to read from
	 * @return The experiment
	 * @throws ParseException If the format of the input does not follow
	 *   the rules mentioned above
	 */
	public Set<T> buildExperiments(Scanner scanner) throws ParseException
	{
		Matcher mat;
		String[] headers = null;
		Set<T> experiments = new HashSet<T>();
		JsonMap input_parameters = new JsonMap();
		boolean first_line = true;
		int line_count = 0;
		while (scanner.hasNextLine())
		{
			line_count++;
			String line = scanner.nextLine().trim();
			if (line.isEmpty() || line.startsWith(s_commentSymbol) || line.startsWith("----"))
			{
				continue;
			}
			mat = s_paramPattern.matcher(line);
			if (mat.find())
			{
				String param = mat.group(1).trim();
				String value = mat.group(2).trim();
				try
				{
					int i = Integer.parseInt(value);
					input_parameters.put(param, i);
					continue;
				}
				catch (NumberFormatException e)
				{
					// Do nothing
				}
				try
				{
					float f = Float.parseFloat(value);
					input_parameters.put(param, f);
				}
				catch (NumberFormatException e)
				{
					// Do nothing
				}
				input_parameters.put(param, value);
				continue;
			}
			if (first_line)
			{
				first_line = false;
				headers = line.split(s_separatorRegex);
				for (int i = 0; i < headers.length; i++)
				{
					headers[i] = headers[i].trim();
				}
				continue;
			}
			T new_experiment = m_referenceClone.newExperiment();
			new_experiment.setInput(input_parameters);
			String[] parts = line.split(s_separatorRegex);
			if (parts.length > headers.length)
			{
				throw new ParseException("Line " + line_count + ": number of data columns greater than number of headers");
			}
			for (int i = 0; i < parts.length; i++)
			{
				if (headers[i].endsWith(s_inputSymbol))
				{
					new_experiment.setInputPrimitive(headers[i].substring(0, headers[i].length() - 1), parts[i].trim());
				}
				else
				{
					new_experiment.writePrimitive(headers[i], parts[i].trim());
				}
			}
			experiments.add(new_experiment);
		}
		return experiments;
	}

	/**
	 * Exception occurring when attempting to parse a write-in experiment
	 */
	public static class ParseException extends EmptyException
	{
		/**
		 * Dummy UID
		 */
		private static final long serialVersionUID = 1L;

		public ParseException(String message) 
		{
			super(message);
		}
		
		public ParseException(Throwable t) 
		{
			super(t);
		}

	}
}
