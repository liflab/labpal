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
package ca.uqac.lif.labpal.provenance;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uqac.lif.dag.Node;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentValue;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.server.ExplainCallback;
import ca.uqac.lif.labpal.server.TemplatePageCallback;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.GraphUtilities;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.Part.All;
import ca.uqac.lif.petitpoucet.function.LineageDotRenderer;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.chart.ChartFormat;
import ca.uqac.lif.spreadsheet.chart.gnuplot.CommandRunner;

/**
 * Utility methods to render and display explanation graphs.
 */
public class GraphViewer
{
	public static final boolean s_dotPresent = isDotPresent();

	/**
	 * Checks if DOT is present on the command line
	 * @return {@code true} if present
	 */
	public static boolean isDotPresent()
	{
		// Check if DOT is present
		String[] args = {"dot", "--version"};
		CommandRunner runner = new CommandRunner(args);
		runner.run();
		// Exception: dot returns 1 when called
		return runner.getErrorCode() == 1;
	}

	/**
	 * Saves a graph to a file.
	 * @param roots The roots of the graph to display
	 * @param filename The file where this graph will be saved
	 * @param no_captions Set to {@code true} to hide non-leaf captions
	 */
	public void save(List<Node> roots, String filename, boolean no_captions)
	{
		File outputFile = new File(filename);
		try (FileOutputStream outputStream = new FileOutputStream(outputFile))
		{
			outputStream.write(toImage(roots, no_captions));
			outputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Saves a graph to a file.
	 * @param root The root of the graph to display
	 * @param filename The file where this graph will be saved
	 * @param no_captions Set to {@code true} to hide non-leaf captions
	 */
	public void save(Node root, String filename, boolean no_captions)
	{
		save(Arrays.asList(root), filename, no_captions);
	}

	/**
	 * Saves a graph to a file.
	 * @param roots The roots of the graph to display
	 * @param filename The file where this graph will be saved
	 */
	public void save(List<Node> roots, String filename)
	{
		save(roots, filename, false);
	}

	/**
	 * Renders a graph as a DOT file.
	 * @param roots The roots of the graph to render
	 * @param no_captions Set to {@code true} to hide non-leaf captions
	 * @return A string with the contents of the DOT file
	 */
	public String toDot(List<Node> roots, boolean no_captions)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		SpreadsheetLineageDotRenderer renderer = new SpreadsheetLineageDotRenderer(roots);
		renderer.setNoCaptions(no_captions);
		renderer.render(ps);
		return baos.toString();
	}

	/**
	 * Renders a graph as a DOT file.
	 * @param root The root of the graph to render
	 * @param no_captions Set to {@code true} to hide non-leaf captions
	 * @return A string with the contents of the DOT file
	 */
	public String toDot(Node root, boolean no_captions)
	{
		List<Node> roots = new ArrayList<Node>(1);
		roots.add(root);
		return toDot(roots, no_captions);
	}

	/**
	 * Renders a graph, calls DOT in the background and retrieves the binary
	 * image it produces.
	 * @param roots The roots of the graph to render
	 * @param no_captions Set to {@code true} to hide non-leaf captions
	 * @return An array of bytes containing the image to display
	 */
	public byte[] toImage(List<Node> roots, boolean no_captions)
	{
		String graph = toDot(roots, no_captions);
		CommandRunner runner = new CommandRunner(new String[] {"dot", "-Tpng"}, graph);
		runner.run();
		return runner.getBytes();
	}

	/**
	 * Renders a graph, calls DOT in the background and retrieves the binary
	 * image it produces.
	 * @param root The root of the graph to render
	 * @param no_captions Set to {@code true} to hide non-leaf captions
	 * @return An array of bytes containing the image to display
	 */
	public byte[] toImage(Node root, boolean no_captions)
	{
		return toImage(root, ChartFormat.PNG, no_captions);
	}

	/**
	 * Renders a graph, calls DOT in the background and retrieves the binary
	 * image it produces.
	 * @param root The root of the graph to render
	 * @param format The format in which to display the graph
	 * @param no_captions Set to {@code true} to hide non-leaf captions
	 * @return An array of bytes containing the image to display
	 */
	public byte[] toImage(Node root, ChartFormat format, boolean no_captions)
	{
		String graph = toDot(root, no_captions);
		CommandRunner runner = new CommandRunner(new String[] {"dot", "-T" + format.getExtension()}, graph);
		runner.run();
		return runner.getBytes();
	}

	/**
	 * A specialized {@link LineageDotRenderer} with special handling of
	 * spreadsheets.
	 */
	protected static class SpreadsheetLineageDotRenderer extends LineageDotRenderer
	{
		public SpreadsheetLineageDotRenderer(List<Node> roots)
		{
			super(roots);
		}

		public SpreadsheetLineageDotRenderer(Node inner_start, String new_prefix, int nesting_level, boolean captions)
		{
			super(inner_start, new_prefix, nesting_level, captions);
		}
		
		@Override
		protected void renderAndNode(PrintStream ps, AndNode current, String n_id)
		{
			String url = TemplatePageCallback.htmlEscape(ExplainCallback.getDataPointUrl(current));
			ps.println(m_indent + n_id + " [shape=\"circle\",label=<<font color='white'><b>∧</b></font>>,width=.25,fixedsize=\"true\",fillcolor=\"blue\",textcolor=\"white\",href=\"" + url + "\"];");
		}

		@Override
		protected void renderPartNode(PrintStream ps, PartNode current, String n_id)
		{
			Object o = current.getSubject();
			if (o instanceof Spreadsheet)
			{
				renderSpreadsheetNode((Spreadsheet) o, ps, current, n_id);
				return;
			}
			else if (o instanceof Experiment)
			{
				renderExperimentNode((Experiment) o, ps, current, n_id);
				return;
			}
			else if (o instanceof Table)
			{
				renderTableNode((Table) o, ps, current, n_id);
				return;
			}
			else if (o instanceof Laboratory)
			{
				renderLabNode(ps, current, n_id);
				return;
			}
			else
			{
				super.renderPartNode(ps, current, n_id);
			}
		}

		protected void renderLabNode(PrintStream ps, PartNode current, String n_id)
		{
			Part d = current.getPart();
			String color = getPartNodeColor(d);
			if (m_noCaptions && ((!GraphUtilities.isLeaf(current) && !m_roots.contains(current)) || m_nestingLevel > 0))
			{
				ps.println(m_indent + n_id + " [height=0.25,shape=\"circle\",label=\"\",fillcolor=\"" + color + "\"];");
			}
			else
			{
				String message;
				if (d instanceof All)
				{
					message = "this lab";
				}
				else
				{
					message = d.toString() + " of this lab";
				}
				ps.println(m_indent + n_id + " [height=0.25,label=<" + message + ">,fillcolor=\"" + color + "\"];");
			}
		}

		protected void renderExperimentNode(Experiment e, PrintStream ps, PartNode current, String n_id)
		{
			Part d = current.getPart();
			String color = getPartNodeColor(d);
			if (m_noCaptions && ((!GraphUtilities.isLeaf(current) && !m_roots.contains(current)) || m_nestingLevel > 0))
			{
				ps.println(m_indent + n_id + " [height=0.25,shape=\"circle\",label=\"\",fillcolor=\"" + color + "\"];");
			}
			else
			{
				String message = "";
				String url = "experiment?id=" + e.getId();
				String highlight_string = "";
				if (d instanceof All)
				{
					message = "Experiment " + e.getId();
				}
				else
				{
					message = d.toString() + " of " + "Experiment " + e.getId();
					ExperimentValue mentioned = ExperimentValue.mentionedValue(d);
					if (mentioned != null)
					{
						highlight_string = "&amp;highlight=" + mentioned.getParameter();
						int elem_index = NthElement.mentionedElement(d);
						if (elem_index >= 0)
						{
							highlight_string += ":" + elem_index;
						}
					}
				}
				ps.println(m_indent + n_id + " [height=0.25,label=<" + message + ">,href=\"" + url + highlight_string + "\",fillcolor=\"" + color + "\"];");
			}			
		}

		protected void renderTableNode(Table t, PrintStream ps, PartNode current, String n_id)
		{
			Part d = current.getPart();
			String color = getPartNodeColor(d);
			if (m_noCaptions && ((!GraphUtilities.isLeaf(current) && !m_roots.contains(current)) || m_nestingLevel > 0))
			{
				ps.println(m_indent + n_id + " [height=0.25,shape=\"circle\",label=\"\",fillcolor=\"" + color + "\"];");
			}
			else
			{
				String message;
				String url = "table?id=" + t.getId();
				String highlight_string = "";
				if (d instanceof All)
				{
					message = "Table " + t.getId();
				}
				else
				{
					Cell mentioned = Cell.mentionedCell(d);
					if (mentioned != null)
					{
						highlight_string = "&amp;highlight=" + mentioned.getRow() + "." + mentioned.getColumn();
					}
					message = d.toString() + " of " + "Table " + t.getId();
				}
				ps.println(m_indent + n_id + " [height=0.25,label=<" + message + ">,href=\"" + url + highlight_string + "\",fillcolor=\"" + color + "\"];");
			}			

		}

		protected void renderSpreadsheetNode(Spreadsheet sheet, PrintStream ps, PartNode current, String n_id)
		{
			Part d = current.getPart();
			String color = getPartNodeColor(d);
			if (m_noCaptions && ((!GraphUtilities.isLeaf(current) && !m_roots.contains(current)) || m_nestingLevel > 0))
			{
				ps.println(m_indent + n_id + " [height=0.25,shape=\"circle\",label=\"\",fillcolor=\"" + color + "\"];");
			}
			else
			{
				String message;
				if (d instanceof All)
				{
					message = renderSpreadsheet(sheet);
				}
				else
				{
					message = d.toString() + " of " + renderSpreadsheet(sheet);
				}
				ps.println(m_indent + n_id + " [height=0.25,label=<" + message + ">,fillcolor=\"" + color + "\"];");
			}			
		}

		@Override
		protected LineageDotRenderer getSubRenderer(Node inner_start, String new_prefix, int nesting_level, boolean captions)
		{
			return new SpreadsheetLineageDotRenderer(inner_start, new_prefix, nesting_level, captions);
		}

		protected static String renderSpreadsheet(Spreadsheet s)
		{
			String out = s.toString();
			if (out.length() > 10)
			{
				out = out.substring(0, 10) + "&hellip;";
			}
			return out;
		}
	}
}
