/*
  LabPal, a versatile benchmark environment
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
package ca.uqac.lif.labpal.plot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import ca.uqac.lif.labpal.Progressive;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.chart.Chart;
import ca.uqac.lif.spreadsheet.chart.ChartFormat;
import ca.uqac.lif.spreadsheet.chart.UnsupportedPlotFormatException;
import ca.uqac.lif.spreadsheet.chart.gnuplot.Gnuplot;

/**
 * An association between a {@link Table} and a {@link Chart} that is used to
 * generate a picture from experimental data.
 * @author Sylvain Hallé
 */
public class Plot extends AtomicFunction implements ExplanationQueryable, Progressive, Stateful
{
	/**
	 * A counter for plot IDs.
	 */
	protected static int s_idCounter = 1;

	/**
	 * A unique ID given to the plot in the lab.
	 */
	protected int m_id;

	/**
	 * The nickname given to this plot.
	 */
	protected String m_nickname;

	/**
	 * The description associated to this plot.
	 */
	protected String m_description;

	/**
	 * The title of this plot.
	 */
	protected String m_title;

	/**
	 * The table from which this plot is created.
	 */
	protected Table m_table;

	/**
	 * The {@link Chart} object used to create an output.
	 */
	protected Chart m_plot;

	/**
	 * Resets the global plot counter.
	 */
	public static void resetCounter()
	{
		s_idCounter = 1;
	}

	public Plot(Table t, Chart p)
	{
		this(s_idCounter++, t, p);
	}

	protected Plot(int id, Table t, Chart p)
	{
		super(0, 1);
		m_id = id;
		m_table = t;
		m_plot = p;
		m_nickname = "";
	}

	/**
	 * Gets the description associated to this plot.
	 * @return The description
	 */
	public String getDescription()
	{
		return m_description;
	}

	/**
	 * Sets the description associated to this plot.
	 * @param description The description
	 */
	public Plot setDescription(String description)
	{
		m_description = description;
		return this;
	}

	/**
	 * Gets the description associated to this plot.
	 * @return The description
	 */
	public String getTitle()
	{
		if (m_title == null)
		{
			return "Plot " + m_id;
		}
		return m_title;
	}

	/**
	 * Sets the description associated to this plot.
	 * @param title The description
	 */
	public Plot setTitle(String title)
	{
		m_title = title;
		return this;
	}

	/**
	 * Gets the nickname given to this plot.
	 * @return The name
	 */
	public String getNickname()
	{
		return m_nickname;
	}

	/**
	 * Gets the nickname given to this plot.
	 * @param nickname The name
	 * @return This plot
	 */
	public Plot setNickname(String nickname)
	{
		m_nickname = nickname;
		return this;
	}

	/**
	 * Gets the unique ID of this plot.
	 * @return The id
	 */
	public int getId()
	{
		return m_id;
	}

	/**
	 * Gets the {@link Table} to which this plot is associated.
	 * @return The table
	 */
	public Table getTable()
	{
		return m_table;
	}

	/**
	 * Determines if the chart produced by this plot supports the Gnuplot
	 * export format.
	 */
	/*@ pure @*/ public boolean supportsGnuplot()
	{
		return m_plot instanceof Gnuplot;
	}

	/**
	 * Gets the image produced by this plot.
	 * @param format The format of the image
	 * @return The byte array with the contents of the image
	 */
	public byte[] getImage(ChartFormat format) throws UnsupportedPlotFormatException
	{
		return getImage(format, true);
	}

	@Override
	public float getProgression()
	{
		// The progression of a plot is the progression of the table it is drawn from
		return m_table.getProgression();
	}

	@Override
	public Status getStatus() 
	{
		// The status of a plot is the status of the table it is drawn from
		return m_table.getStatus();
	}

	/**
	 * Gets the image produced by this plot.
	 * @param format The format of the image
	 * @param with_title Set to <tt>true</tt> to generate the plot with its
	 * title, <tt>false</tt> to get only the plot
	 * @return The byte array with the contents of the image
	 */
	public byte[] getImage(ChartFormat format, boolean with_title) throws UnsupportedPlotFormatException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Spreadsheet s = m_table.getSpreadsheet();
		try
		{
			m_plot.render(baos, s, format, with_title);
			return baos.toByteArray();
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Renders the plot as a GnuPlot output text file. This method only returns
	 * a non-null value if the underlying chart is a descendant of
	 * {@link Gnuplot}.
	 * @param format The image format to which the GnuPlot file will export
	 * @param plot_title The plot's title
	 * @param with_title Set to <tt>true</tt> to generate the plot with its
	 * title, <tt>false</tt> to get only the plot
	 * @return The GnuPlot string, or <tt>null</tt>
	 */
	public String toGnuplot(ChartFormat format, String plot_title, boolean with_title)
	{
		if (!(m_plot instanceof Gnuplot))
		{
			return null;
		}
		Spreadsheet s = m_table.getSpreadsheet();
		m_plot.setTitle(plot_title);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		((Gnuplot) m_plot).toGnuplot(new PrintStream(baos), s, format, with_title);
		return baos.toString();
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		return new Object[] {getImage(null)};
	}

	@Override
	public AtomicFunction duplicate(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Gets the collection of experiments on which this table depends on.
	 * @param sorted Set to <tt>true</tt> to sort the collection
	 * @return The set of experiments
	 */
	/*@ non_null @*/ public Collection<Experiment> getExperimentDependencies(boolean sorted)
	{
		return m_table.getExperimentDependencies(sorted);
	}
}
