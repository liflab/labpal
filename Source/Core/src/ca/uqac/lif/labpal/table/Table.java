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
package ca.uqac.lif.labpal.table;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

import ca.uqac.lif.dag.LeafCrawler.LeafFetcher;
import ca.uqac.lif.dag.Node;
import ca.uqac.lif.labpal.Dependent;
import ca.uqac.lif.labpal.Identifiable;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.latex.LatexExportable;
import ca.uqac.lif.labpal.region.Region;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * An explainable 0:1 function that outputs a {@link Spreadsheet}.
 * @author Sylvain Hallé
 */
public abstract class Table extends AtomicFunction implements ExplanationQueryable, Stateful, Identifiable, Dependent<Experiment>, LatexExportable
{
	/**
	 * A counter for table IDs.
	 */
	protected static int s_idCounter = 1;

	/**
	 * A flag indicating whether this table should be displayed in the list of
	 * tables in the web interface.
	 */
	protected boolean m_showsInList = true;

	/**
	 * The nickname given to this table.
	 */
	protected String m_nickname = "";

	/**
	 * The description associated to this table.
	 */
	protected String m_description = "";

	/**
	 * The title of this table.
	 */
	protected String m_title;

	/**
	 * A unique ID given to the table in the lab.
	 */
	protected int m_id;

	/**
	 * Whether the output spreadsheet for the table has already been computed
	 * at least once.
	 */
	protected boolean m_calculated = false;

	/**
	 * Resets the global table counter.
	 */
	public static void resetCounter()
	{
		s_idCounter = 1;
	}

	public Table()
	{
		this(s_idCounter++);
	}

	protected Table(int id)
	{
		super(0, 1);
		m_id = id;
	}

	/**
	 * Gets the description associated to this table.
	 * @return The description
	 */
	public String getDescription()
	{
		return m_description;
	}

	/**
	 * Sets the description associated to this table.
	 * @param description The description
	 */
	public Table setDescription(String description)
	{
		m_description = description;
		return this;
	}

	/**
	 * Gets the description associated to this table.
	 * @return The description
	 */
	public String getTitle()
	{
		if (m_title == null)
		{
			return "Table " + m_id;
		}
		return m_title;
	}

	/**
	 * Sets the description associated to this table.
	 * @param title The description
	 */
	public Table setTitle(String title)
	{
		m_title = title;
		return this;
	}

	/**
	 * Sets the title associated to this plot, based on the values
	 * of a region.
	 * @param r The region
	 * @return This table
	 */
	/*@ non_null @*/ public Table setTitle(/*@ non_null @*/ Region r)
	{
		return setTitle("", r);
	}

	/**
	 * Sets the title and nickname associated to this plot, based on the values
	 * of a region.
	 * @param prefix A string prefix to put before the auto-generated
	 * title
	 * @param r The region
	 * @return This table
	 */
	/*@ non_null @*/ public Table setTitle(String prefix, Region r)
	{
		setTitle(getTitle(prefix, r));
		return this;
	}

	/**
	 * Sets the title <em>and</em> nickname of a table based on the values of a
	 * region.
	 * @param prefix A string prefix to put before the auto-generated
	 * title
	 * @param r The region
	 * @return This table
	 */
	public Table name(String prefix, Region r)
	{
		setTitle(getTitle(prefix, r));
		setNickname(LatexExportable.latexify(m_title));
		return this;
	}

	@Override
	/*@ pure @*/ public String getNickname()
	{
		return m_nickname;
	}

	/**
	 * Sets the nickname given to this table.
	 * @param nickname The name
	 * @return This table
	 */
	/*@ non_null @*/ public Table setNickname(String nickname)
	{
		m_nickname = nickname;
		return this;
	}

	/**
	 * Gets the unique ID of this table.
	 * @return The id
	 */
	@Override
	public int getId()
	{
		return m_id;
	}

	/**
	 * Determines if the table is displayed in the list of tables from the lab.
	 * @return <tt>true</tt> if the table shows in the list, <tt>false</tt>
	 * otherwise
	 */
	public boolean showsInList()
	{
		return m_showsInList;
	}
	
	/**
	 * Sets whether the table is displayed in the list of tables from the lab.
	 * @param b Set to <tt>true</tt> to show the table in the list,
	 * <tt>false</tt> otherwise
	 * @return This table
	 */
	public Table setShowInList(boolean b)
	{
		m_showsInList = b;
		return this;
	}

	/**
	 * Gets the spreadsheet produced by this table.
	 * @return The spreadsheet
	 */
	/*@ null @*/ public final Spreadsheet getSpreadsheet()
	{
		Spreadsheet s = calculateSpreadsheet();
		m_calculated = true;
		return s;
	}

	/**
	 * Gets the spreadsheet produced by this table.
	 * @return The spreadsheet
	 */
	/*@ null @*/ protected abstract Spreadsheet calculateSpreadsheet();

	/**
	 * Gets the table on which a cell of the spreadsheet immediately depends
	 * on.
	 * @param col The cell's column
	 * @param row The cell's row
	 * @return Another table, or <tt>null</tt> if the cell does not depend on another
	 * table
	 */
	/*@ null @*/ public abstract Table dependsOn(int col, int row);

	@Override
	public float getProgression()
	{
		PartNode root = getExplanation(NthOutput.FIRST);
		LeafFetcher lf = new LeafFetcher(root);
		lf.crawl();
		Set<Node> leaves = lf.getLeaves();
		float total = 0, prog = 0;
		for (Node n : leaves)
		{
			if (!(n instanceof PartNode))
			{
				continue;
			}
			PartNode pn = (PartNode) n;
			Object o = pn.getSubject();
			if (o instanceof Stateful)
			{
				total++;
				prog += ((Stateful) o).getProgression();
			}
		}
		if (total == 0)
		{
			return 0;
		}
		return prog / total;
	}

	protected abstract PartNode explain(Part d, NodeFactory f);

	@Override
	public final PartNode getExplanation(Part d, NodeFactory f)
	{
		if (!m_calculated)
		{
			getSpreadsheet();
		}
		return explain(d, f);
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		return new Object[] {getSpreadsheet()};
	}

	@Override
	public int hashCode()
	{
		return 11 * m_id;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Table))
		{
			return false;
		}
		return ((Table) o).getId() == m_id;
	}

	@Override
	public String toString()
	{
		return "T" + m_id;
	}

	@Override
	public String toLatex()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new LatexTableRenderer(this).render(new PrintStream(baos));
		return baos.toString();
	}
	
	protected static Object pick(Set<?> set)
	{
		for (Object o : set)
		{
			return o;
		}
		return null;
	}

	/**
	 * Auto-generates a table title from a region. The method does so by
	 * concatenating all key-value pairs of the region that have a dimension of
	 * 1. Optionally a string can be prepended at the beginning of the title.
	 * @param prefix A string prefix to put before the auto-generated
	 * title
	 * @param r The region
	 * @return The title
	 */
	protected static String getTitle(String prefix, Region r)
	{
		StringBuilder out = new StringBuilder();
		out.append(prefix);
		boolean first = true;
		for (String d : r.getDimensions())
		{
			Set<Object> domain = r.getDomain(d);
			if (domain.size() == 1)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					out.append(", ");
				}
				out.append(d + "=" + pick(domain));
			}
		}
		return out.toString();
	}
}
