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

import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.plot.Plot;
import ca.uqac.lif.spreadsheet.plot.PlotFormat;
import ca.uqac.lif.spreadsheet.plot.UnsupportedPlotFormatException;

/**
 * A 0:1 function associated to a table, and which generates a picture from the
 * spreadsheet produced by that table.
 * @author Sylvain Hallé
 */
public abstract class LabPalPlot extends AtomicFunction implements ExplanationQueryable
{
	/**
	 * A counter for LabPalPlot IDs.
	 */
	protected static int s_idCounter = 0;
	
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
	 * The {@link Plot} object used to create an output.
	 */
	protected Plot m_plot;
	
	/**
	 * Resets the global LabPalPlot counter.
	 */
	public static void resetCounter()
	{
		s_idCounter = 0;
	}

	public LabPalPlot(Table t, Plot p)
	{
		this(s_idCounter++, t, p);
	}

	protected LabPalPlot(int id, Table t, Plot p)
	{
		super(0, 1);
		m_id = id;
		m_table = t;
		m_plot = p;
	}
	
	/**
	 * Gets the description associated to this LabPalPlot.
	 * @return The description
	 */
	public String getDescription()
	{
		return m_description;
	}

	/**
	 * Sets the description associated to this LabPalPlot.
	 * @param description The description
	 */
	public LabPalPlot setDescription(String description)
	{
		m_description = description;
		return this;
	}
	
	/**
	 * Gets the description associated to this LabPalPlot.
	 * @return The description
	 */
	public String getTitle()
	{
		return m_title;
	}

	/**
	 * Sets the description associated to this LabPalPlot.
	 * @param title The description
	 */
	public LabPalPlot setTitle(String title)
	{
		m_title = title;
		return this;
	}

	/**
	 * Gets the nickname given to this LabPalPlot.
	 * @return The name
	 */
	public String getNickname()
	{
		return m_nickname;
	}

	/**
	 * Gets the nickname given to this LabPalPlot.
	 * @param nickname The name
	 * @return This LabPalPlot
	 */
	public LabPalPlot setNickname(String nickname)
	{
		m_nickname = nickname;
		return this;
	}

	/**
	 * Gets the unique ID of this LabPalPlot.
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
	
	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		return new Object[] {getImage(null)};
	}
	
	public byte[] getImage(PlotFormat format)
	{
		return getImage(format, true);
	}
	
	public byte[] getImage(PlotFormat format, boolean with_title)
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
		catch (UnsupportedPlotFormatException e)
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
}
