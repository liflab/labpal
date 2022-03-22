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
package examples.sorting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentException;
import ca.uqac.lif.labpal.util.FileHelper;
import ca.uqac.lif.labpal.util.Stopwatch;
import ca.uqac.lif.units.si.Millisecond;
import ca.uqac.lif.units.si.Second;

public abstract class SortExperiment extends Experiment
{
	/**
	 * The name of parameter "Size".
	 */
	public static final String SIZE = "Size";
	
	/**
	 * The name of parameter "Algorithm".
	 */
	public static final String ALGORITHM = "Algorithm";
	
	/**
	 * The name of parameter "Duration".
	 */
	public static final String DURATION = "Duration";
	
	/**
	 * The name of parameter "Comparisons".
	 */
	public static final String COMPARISONS = "Comparisons";
	
	/**
	 * The folder where the generated data will be put.
	 */
	protected static final transient String s_dataDir = "data/";
	
	/**
	 * A random generator to produce the lists to sort.
	 */
	protected static final transient Random s_random = new Random();
	
	/**
	 * The list of elements, as it was sorted by the algorithm.
	 */
	protected List<Integer> m_sortedList;

	public SortExperiment(String name, int size)
	{
		super();
		describe(ALGORITHM, "Name of the sorting algorithm");
		describe(SIZE, "Size of the array to sort");
		describe(DURATION, "Sorting time", Second.DIMENSION);
		writeInput(ALGORITHM, name);
		writeInput(SIZE, size);
		m_sortedList = new ArrayList<Integer>(size);
	}

	@Override
	public final boolean prerequisitesFulfilled()
	{
		return FileHelper.fileExists(getDataFilename());
	}

	@Override
	public final void cleanPrerequisites()
	{
		String filename = getDataFilename();
		if (FileHelper.fileExists(filename))
		{
			FileHelper.deleteFile(filename);
		}
	}

	@Override
	public final void fulfillPrerequisites()
	{
		// Generates a random list of integers of given size, and saves it
		// to a file
		String filename = getDataFilename();
		StringBuilder out = new StringBuilder();
		int size = readInt(SIZE);
		int range = 2 * size;
		for (int i = 0; i < size; i++)
		{
			int number = s_random.nextInt(range);
			if (i > 0)
			{
				out.append(",");
			}
			out.append(number);
		}
		FileHelper.writeFromString(new File(filename), out.toString());
	}

	/**
	 * Generates the data filename based on the experiment's parameters.
	 * @param input The experiment's input parameters
	 * @return The filename
	 */
	protected final String getDataFilename()
	{
		int size = readInt(SIZE);
		return s_dataDir + "list-" + size + ".txt";
	}
	
	/**
	 * Returns the sorted list produced by this experiment.
	 * @return The sorted list
	 */
	public List<Integer> getSortedList()
	{
		return m_sortedList;
	}

	@Override
	public void execute() throws ExperimentException
	{
		int[] array = getArray();
		if (array == null)
		{
			throw new ExperimentException("A null array was passed to this experiment");
		}
		Stopwatch.start(this);
		sort(array);
		writeOutput(DURATION, new Millisecond(Stopwatch.stop(this)));
		for (int i : array)
		{
			m_sortedList.add(i);
		}
	}

	protected final int[] getArray()
	{
		int size = readInt(SIZE);
		int[] array = new int[size];
		String filename = getDataFilename();
		String contents = FileHelper.readToString(new File(filename));
		String[] str = contents.split(",");
		for (int i = 0; i < size; i++)
		{
			array[i] = Integer.parseInt(str[i].trim());
		}
		return array;
	}

	protected abstract void sort(int[] array) throws ExperimentException;

	/*@Override
	public float getDurationEstimate(float factor)
	{
		float size = readFloat("size");
		return (size / 20000) / factor;
	}*/

	@Override
	public String toString()
	{
		String out = "";
		out += readString(ALGORITHM);
		out += " " + (readInt(SIZE) / 1000) + "k";
		return out;
	}
	
	@Override
	public String getDescription()
	{
		return "Sorts an array of size " + readInt(SIZE) + " using " + readString(ALGORITHM);
	}
}
