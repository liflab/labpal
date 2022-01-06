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

import java.awt.FlowLayout;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.uqac.lif.dag.Node;
import ca.uqac.lif.petitpoucet.GraphUtilities;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.Part.All;
import ca.uqac.lif.petitpoucet.function.LineageDotRenderer;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.plot.Plot;
import ca.uqac.lif.spreadsheet.plot.PlotFormat;
import ca.uqac.lif.spreadsheet.plot.UnsupportedPlotFormatException;
import ca.uqac.lif.spreadsheet.plots.gnuplot.CommandRunner;

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
	 * Displays an explanation graph into a window. This method acts as a
	 * primitive image viewer, used to display the result of the examples.
	 * @param roots The roots of the graph to display
	 * @param no_captions Set to {@code true} to hide non-leaf captions
	 */
	public void display(List<Node> roots, boolean no_captions)
	{
		BitmapJFrame window = new BitmapJFrame(toImage(roots, no_captions));
		window.setVisible(true);
	}
	
	/**
	 * Displays an explanation graph into a window. This method acts as a
	 * primitive image viewer, used to display the result of the examples.
	 * @param roots The roots of the graph to display
	 */
	public void display(List<Node> roots)
	{
		display(roots, false);
	}
	
	/**
	 * Displays an explanation graph into a window. This method acts as a
	 * primitive image viewer, used to display the result of the examples.
	 * @param root The root of the graph to display
	 */
	public void display(Node root)
	{
		display((List<Node>) Arrays.asList(root), false);
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
		return toImage(root, PlotFormat.PNG, no_captions);
	}
	
	/**
	 * Renders a graph, calls DOT in the background and retrieves the binary
	 * image it produces.
	 * @param root The root of the graph to render
	 * @param format The format in which to display the graph
	 * @param no_captions Set to {@code true} to hide non-leaf captions
	 * @return An array of bytes containing the image to display
	 */
	public byte[] toImage(Node root, PlotFormat format, boolean no_captions)
	{
		String graph = toDot(root, no_captions);
		CommandRunner runner = new CommandRunner(new String[] {"dot", "-T" + format.getExtension()}, graph);
		runner.run();
		return runner.getBytes();
	}
		
	/**
	 * Receives a byte array as an input, and shows it in a Swing
	 * window as a picture.
	 */
	public static class BitmapJFrame extends JFrame
	{
		/**
		 * Dummy UID
		 */
		private static final long serialVersionUID = 1L;

		protected transient JFrame m_frame;

		protected transient JLabel m_label;

		public BitmapJFrame(byte[] image_bytes)
		{
			super("Graph");
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			add(panel);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			m_label = new JLabel();
			panel.add(m_label);
			ImageIcon icon = new ImageIcon(image_bytes); 
			m_label.setIcon(icon);
			pack();
		}
		
		public BitmapJFrame(Plot p, Spreadsheet s)
		{
			this(getBytes(p, s));
		}
		
		public BitmapJFrame display()
		{
			super.setVisible(true);
			return this;
		}
		
		/**
		 * Gets the frame associated to the object
		 * @return The frame
		 */
		public JFrame getFrame()
		{
		  return m_frame;
		}
		
		protected static byte[] getBytes(Plot p, Spreadsheet s)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try
			{
				p.render(baos, s);
				return baos.toByteArray();
			}
			catch (IllegalArgumentException | UnsupportedPlotFormatException | IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new byte[0];
		}
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
		protected void renderPartNode(PrintStream ps, PartNode current, String n_id)
		{
			Object o = current.getSubject();
			if (!(o instanceof Spreadsheet))
			{
				super.renderPartNode(ps, current, n_id);
				return;
			}
			Spreadsheet sheet = (Spreadsheet) o;
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
