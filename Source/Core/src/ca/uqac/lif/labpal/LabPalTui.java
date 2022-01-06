/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2019 Sylvain Hallé

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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.Experiment.Status;
import ca.uqac.lif.labpal.plot.LabPalGnuplot;
import ca.uqac.lif.labpal.plot.LabPalPlot;
import ca.uqac.lif.spreadsheet.plot.PlotFormat;
import ca.uqac.lif.spreadsheet.plots.gnuplot.Gnuplot;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.tui.AnsiPrinter;
import ca.uqac.lif.tui.AnsiPrinter.Color;
import ca.uqac.lif.tui.Checkbox;
import ca.uqac.lif.tui.Menu;
import ca.uqac.lif.tui.MenuItem;
import ca.uqac.lif.tui.NestedMenu;
import ca.uqac.lif.tui.TuiElement;
import ca.uqac.lif.tui.TuiList;

/**
 * Takes care of all the user interaction at the command line.
 * 
 * @author Sylvain Hallé
 *
 */
public class LabPalTui
{
  /**
   * The lab to be controlled by the command line
   */
	protected Laboratory m_lab;
	
	/**
	 * The assistant that runs the experiments
	 */
	protected LabAssistant m_assistant;
	
	/**
	 * The printer where the messages are output
	 */
	protected AnsiPrinter m_printer;
		
	/**
	 * A map to remember which experiments are currently checked in the TUI
	 */
	protected Map<Integer,ExperimentElement> m_selectedExperiments;

	/**
	 * A map to remember which plots are currently checked in the TUI
	 */
	protected Map<Integer,Checkbox> m_selectedPlots;
	
	/**
	 * A map to remember which tables are currently checked in the TUI
	 */
	protected Map<Integer,Checkbox> m_selectedTables;
	
	/**
	 * Initializes the TUI for a lab
	 * @param lab The lab
	 * @param assistant The assistant
	 * @param printer The printer used to display the TUI
	 * @param args The arguments passed from the command line
	 */
	public LabPalTui(Laboratory lab, LabAssistant assistant, AnsiPrinter printer, ArgumentMap args)
	{
		super();
		List<String> others = args.getOthers();
		if (!others.isEmpty())
		{
			String filename = others.get(0);
			if (!FileHelper.fileExists(filename))
			{
				printer.print("ERROR: file " + filename + " not found\n");
				System.exit(-1);
			}
			String contents = FileHelper.readToString(new File(filename));
			lab.addClassToSerialize(this.getClass());
			try 
			{
				m_lab = lab.loadFromString(contents);
			} 
			catch (ReadException e) 
			{
			} 
			catch (JsonParseException e) 
			{
			}
			m_lab.setAssistant(assistant);
			if (m_lab == null)
			{
				printer.print("ERROR: cannot load lab data from " + filename + "\n");
				System.exit(-1);
			}
		}
		else
		{
			m_lab = lab;
		}
		m_assistant = assistant;
		m_printer = printer;
		if (args.hasOption("color"))
		{
			m_printer.enableColors();
		}
		else
		{
			m_printer.disableColors();
		}
		m_selectedExperiments = new HashMap<Integer,ExperimentElement>();
		for (int id : m_lab.getExperimentIds())
		{
			Experiment e = m_lab.getExperiment(id);
			m_selectedExperiments.put(id, new ExperimentElement(e));
		}
		m_selectedPlots = new HashMap<Integer,Checkbox>();
		for (int id : m_lab.getPlotIds())
		{
			m_selectedPlots.put(id, new Checkbox());
		}
		m_selectedTables = new HashMap<Integer,Checkbox>();
		for (int id : m_lab.getTableIds())
		{
			m_selectedTables.put(id, new Checkbox());
		}
	}
	
	/**
	 * Runs the interactive loop of the TUI
	 * @return An error code for the command line
	 */
	public int run()
	{
		m_printer.resetColors();
		Menu main_menu = new Menu();
		{
			ExperimentMenu exp_menu = new ExperimentMenu(m_selectedExperiments);
			exp_menu.addItem(new SelectExperimentMenuItem());
			exp_menu.addItem(new SelectAllExperimentMenuItem());
			exp_menu.addItem(new UnselectAllExperimentMenuItem());
			exp_menu.addItem(new QueueExperimentMenuItem());
			exp_menu.addItem(new UnqueueExperimentMenuItem());
			exp_menu.addItem(new CleanExperimentMenuItem());
			exp_menu.addItem(new ResetExperimentMenuItem());
			exp_menu.addItem(new ExperimentDetailsMenuItem());
			{
				ViewMenu view_menu = new ViewMenu();
				view_menu.addItem(new ColumnsExperimentMenuItem(exp_menu.m_list));
				view_menu.addItem(new BackMenuItem());
				ViewMenuItem vmi = new ViewMenuItem(view_menu);
				exp_menu.addItem(vmi);
			}
			exp_menu.addItem(new BackMenuItem());
			NestedMenu exp_item = new NestedMenu("E", "Experiments", exp_menu);
			main_menu.addItem(exp_item);
		}
		{
			PlotMenu plot_menu = new PlotMenu();
			//plot_menu.addItem(new SelectPlotMenuItem());
			if (Gnuplot.isGnuplotPresent())
				plot_menu.addItem(new ViewPlotMenuItem());
			plot_menu.addItem(new SavePlotMenuItem());
			plot_menu.addItem(new BackMenuItem());
			NestedMenu plot_item = new NestedMenu("P", "Plots", plot_menu);
			main_menu.addItem(plot_item);
		}
		{
			TableMenu table_menu = new TableMenu();
			//plot_menu.addItem(new SelectPlotMenuItem());
			table_menu.addItem(new ViewTableMenuItem());
			table_menu.addItem(new SaveTableMenuItem());
			table_menu.addItem(new BackMenuItem());
			NestedMenu table_item = new NestedMenu("L", "Tables", table_menu);
			main_menu.addItem(table_item);
		}

		main_menu.addItem(new RunMenuItem());
		main_menu.addItem(new StopMenuItem());
		main_menu.addItem(new StatusMenuItem());
		main_menu.addItem(new SaveMenuItem());
		main_menu.addItem(new MainHelpMenuItem());
		main_menu.addItem(new ExitMenuItem());
		m_printer.print("\nWelcome to the lab ");
		m_printer.setForegroundColor(AnsiPrinter.Color.LIGHT_PURPLE);
		m_printer.print("'" + m_lab.getTitle() + "'");
		m_printer.resetColors();
		m_printer.print("\nYour lab assistant is ");
		m_printer.setForegroundColor(AnsiPrinter.Color.LIGHT_PURPLE);
		m_printer.print(m_assistant.getName());
		m_printer.resetColors();
		m_printer.print("\nRandom seed: ");
		m_printer.setForegroundColor(AnsiPrinter.Color.LIGHT_PURPLE);
		m_printer.print(m_lab.getRandomSeed());
		m_printer.resetColors();
		if (!Gnuplot.isGnuplotPresent())
		{
			m_printer.print("\nWarning: Gnuplot was not found on your system");
		}
		if (m_lab.isEnvironmentOk() != null)
		{
			m_printer.print("\nError: some of the environment requirements for this lab are not met");
			m_printer.print("\nThis means you are missing something to run the experiments.");
		}
		m_printer.print("\n\n");
		main_menu.render(m_printer);
		m_printer.print("\nGood bye!\n");
		return Laboratory.ERR_OK;
	}
		
	protected class RunMenuItem extends MenuItem
	{
		public RunMenuItem()
		{
			super("T", "Start", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			m_lab.start();
			showStatus(printer, m_lab, m_assistant);
		}
	}
	
	protected class StopMenuItem extends MenuItem
	{
		public StopMenuItem()
		{
			super("S", "Stop", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			m_assistant.stop();
			printer.print("Stopped\n");
		}
	}
	
	protected class MainHelpMenuItem extends MenuItem
	{
		public MainHelpMenuItem()
		{
			super("?", "Help", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			printer.print("Help is on teh way\n");
		}
	}
	
	protected class StatusMenuItem extends MenuItem
	{
		public StatusMenuItem()
		{
			super("U", "Status", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			showStatus(printer, m_lab, m_assistant);
		}
		
	}
	
	protected class SaveMenuItem extends MenuItem
	{
		public SaveMenuItem()
		{
			super("V", "Save", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			String filename = m_lab.getTitle() + "." + Laboratory.s_fileExtension;
			printer.print("Save to [" + filename + "] ");
			String line = printer.readLine();
			if (line == null)
				return;
			line = line.trim();
			if (!line.isEmpty())
			{
				filename = line;
			}
			try
			{
			  String json_string = m_lab.saveToString();
			  FileHelper.writeFromString(new File(filename), json_string);
	      printer.print("Wrote " + json_string.length() + " bytes\n");
			}
			catch (PrintException e)
			{
			  printer.print("Could not save the lab's contents");
			}
		}
	}
	
	protected class ExitMenuItem extends MenuItem
	{
		public ExitMenuItem()
		{
			super("X", "Exit", true);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
		}
	}

	protected class BackMenuItem extends MenuItem
	{
		public BackMenuItem()
		{
			super("B", "Back", true);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
		}
	}
	
	protected class TableMenu extends Menu
	{
		public void renderBefore(AnsiPrinter printer)
		{
			printer.print("\n");
			for (int id : m_selectedTables.keySet())
			{
				//Checkbox cb = m_selectedPlots.get(id);
				Table t = m_lab.getTable(id);
				printer.setForegroundColor(Color.LIGHT_GRAY);
				printer.print(AnsiPrinter.padToLength(Integer.toString(t.getId()), 3));
				printer.resetColors();
				//cb.render(printer);
				printer.print(" " + t.getTitle());
				printer.print("\n");
			}
			printer.print("\n");
		}
	}
	
	protected abstract class TableMenuItem extends MenuItem
	{
		public TableMenuItem(String shortcut, String label)
		{
			super(shortcut, label, false);
		}

		@Override
		public final void doSomething(AnsiPrinter printer)
		{
			printer.print("Table number: ");
			String s_id = printer.readLine();
			int id = Integer.parseInt(s_id);
			Table t = m_lab.getTable(id);
			if (t == null)
			{
				printer.print("This ID does not exist\n");
				return;
			}
			doWithTable(printer, t);
		}
		
		protected abstract void doWithTable(AnsiPrinter printer, Table t);
	}
	
	protected class ViewTableMenuItem extends TableMenuItem
	{
		public ViewTableMenuItem()
		{
			super("V", "View");
		}

		@Override
		protected void doWithTable(AnsiPrinter printer, Table t)
		{
			String contents = t.toString();
			if (contents == null || contents.isEmpty())
			{
				printer.print("Cannot display table\n");
				return;
			}
			printer.print(contents + "\n");			
		}
	}
	
	protected class SaveTableMenuItem extends TableMenuItem
	{
		public SaveTableMenuItem()
		{
			super("S", "Save");
		}

		@Override
		protected void doWithTable(AnsiPrinter printer, Table t)
		{
			String contents = t.toString();
			String filename = t.getTitle() + ".csv";
			printer.print("Save to [" + filename + "] ");
			String line = printer.readLine();
			if (line == null)
				return;
			line = line.trim();
			if (!line.isEmpty())
			{
				filename = line;
			}
			FileHelper.writeFromString(new File(filename), contents);
			printer.print("Wrote " + contents.length() + " bytes\n");
		}
	}
	
	protected class PlotMenu extends Menu
	{
		public void renderBefore(AnsiPrinter printer)
		{
			printer.print("\n");
			for (int id : m_selectedPlots.keySet())
			{
				//Checkbox cb = m_selectedPlots.get(id);
				LabPalPlot ex = m_lab.getPlot(id);
				printer.setForegroundColor(Color.LIGHT_GRAY);
				printer.print(AnsiPrinter.padToLength(Integer.toString(ex.getId()), 3));
				printer.resetColors();
				//cb.render(printer);
				printer.print(" " + ex.getTitle());
				printer.print("\n");
			}
			printer.print("\n");
		}
	}
	
	protected class ViewMenuItem extends NestedMenu
	{
		public ViewMenuItem(Menu menu)
		{
			super("V", "View", menu);
		}
	}
	
	protected class ViewMenu extends Menu
	{
	}
	
	protected class SelectPlotMenuItem extends MenuItem
	{
		public SelectPlotMenuItem()
		{
			super("e", "Select", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			printer.print("Plot numbers: ");
			printer.readLine();
		}
	}
	
	protected abstract class PlotMenuItem extends MenuItem
	{
		public PlotMenuItem(String shortcut, String label)
		{
			super(shortcut, label, false);
		}

		@Override
		public final void doSomething(AnsiPrinter printer)
		{
			printer.print("Plot number: ");
			String s_id = printer.readLine();
			int id = Integer.parseInt(s_id);
			LabPalPlot p = m_lab.getPlot(id);
			if (p == null)
			{
				printer.print("This ID does not exist\n");
				return;
			}
			doWithPlot(printer, p);
		}
		
		protected abstract void doWithPlot(AnsiPrinter printer, LabPalPlot p);
	}
	
	protected class ViewPlotMenuItem extends PlotMenuItem
	{
		public ViewPlotMenuItem()
		{
			super("V", "View");
		}

		@Override
		protected void doWithPlot(AnsiPrinter printer, LabPalPlot p)
		{
			byte[] image = null;
			image = p.getImage(Gnuplot.DUMB);
			if (image == null)
			{
				printer.print("Cannot display plot\n");
				return;
			}
			String graph = new String(image);
			printer.print(graph + "\n");			
		}
	}
	
	protected class SavePlotMenuItem extends PlotMenuItem
	{
		public SavePlotMenuItem()
		{
			super("S", "Save");
		}

		@Override
		protected void doWithPlot(AnsiPrinter printer, LabPalPlot plot)
		{
			if (!(plot instanceof LabPalGnuplot))
			{
				printer.print("This plot is not a GnuPlot graph");
				return;
			}
			LabPalGnuplot p = (LabPalGnuplot) plot;
			String gnuplot = p.toGnuplot(PlotFormat.PDF, m_lab.getTitle(), true);
			String filename = p.getTitle() + ".gp";
			printer.print("Save to [" + filename + "] ");
			String line = printer.readLine();
			if (line == null)
				return;
			line = line.trim();
			if (!line.isEmpty())
			{
				filename = line;
			}
			FileHelper.writeFromString(new File(filename), gnuplot);
			printer.print("Wrote " + gnuplot.length() + " bytes\n");
		}
	}

	protected class ExperimentMenu extends Menu
	{
		TuiList m_list;
		
		public ExperimentMenu(Map<Integer,ExperimentElement> map)
		{
			super();
			m_list = new TuiList();
			for (ExperimentElement e : map.values())
			{
				m_list.add(e);				
			}
		}
		
		@Override
		public void renderBefore(AnsiPrinter printer)
		{
			printer.print("\n");
			m_list.render(printer);
		}
	}
	
	protected class SelectExperimentMenuItem extends MenuItem
	{
		public SelectExperimentMenuItem()
		{
			super("e", "Select", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			printer.print("Experiment numbers: ");
			String choice = printer.readLine();
			String[] ids = choice.split(",");
			for (String s_id : ids)
			{
				int id = Integer.parseInt(s_id);
				m_selectedExperiments.get(id).m_checkbox.toggle();
			}
		}
	}
	
	protected class ColumnsExperimentMenuItem extends MenuItem
	{
		protected TuiList m_list;
		
		public ColumnsExperimentMenuItem(TuiList list)
		{
			super("l", "Columns", false);
			m_list = list;
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			printer.print("Number of columns: ");
			String choice = printer.readLine();
			int cols = Integer.parseInt(choice);
			m_list.setColumns(cols);
		}
	}

	
	protected class SelectAllExperimentMenuItem extends MenuItem
	{
		public SelectAllExperimentMenuItem()
		{
			super("a", "All", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			for (int id : m_selectedExperiments.keySet())
			{
				m_selectedExperiments.get(id).m_checkbox.setChecked(true);
			}
		}
	}

	protected class UnselectAllExperimentMenuItem extends MenuItem
	{
		public UnselectAllExperimentMenuItem()
		{
			super("n", "None", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			for (int id : m_selectedExperiments.keySet())
			{
				m_selectedExperiments.get(id).m_checkbox.setChecked(false);
			}
		}
	}
	
	protected class QueueExperimentMenuItem extends MenuItem
	{
		public QueueExperimentMenuItem()
		{
			super("Q", "Queue", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			int num_queued = 0;
			for (int id : m_selectedExperiments.keySet())
			{
				Checkbox cb = m_selectedExperiments.get(id).m_checkbox;
				if (cb.isChecked())
				{
					Experiment e = m_selectedExperiments.get(id).m_experiment;
					m_assistant.queue(e);
					num_queued++;
				}
			}
			printer.print("Queued " + num_queued + " experiment(s)\n");
		}
	}
	
	protected class UnqueueExperimentMenuItem extends MenuItem
	{
		public UnqueueExperimentMenuItem()
		{
			super("U", "Unqueue", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			int num_queued = 0;
			for (int id : m_selectedExperiments.keySet())
			{
				Checkbox cb = m_selectedExperiments.get(id).m_checkbox;
				if (cb.isChecked())
				{
					m_assistant.unqueue(id);
					num_queued++;
				}
			}
			printer.print("Removed " + num_queued + " experiment(s) from queue\n");
		}
	}

	
	protected class CleanExperimentMenuItem extends MenuItem
	{
		public CleanExperimentMenuItem()
		{
			super("C", "Clean", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			int num_queued = 0;
			for (int id : m_selectedExperiments.keySet())
			{
				Checkbox cb = m_selectedExperiments.get(id).m_checkbox;
				if (cb.isChecked())
				{
					m_selectedExperiments.get(id).m_experiment.clean();
					num_queued++;
				}
			}
			printer.print("Cleaned " + num_queued + " experiment(s)\n");
		}
	}
	
	protected class ResetExperimentMenuItem extends MenuItem
	{
		public ResetExperimentMenuItem()
		{
			super("R", "Reset", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			int num_queued = 0;
			for (int id : m_selectedExperiments.keySet())
			{
				Checkbox cb = m_selectedExperiments.get(id).m_checkbox;
				if (cb.isChecked())
				{
					m_selectedExperiments.get(id).m_experiment.reset();
					num_queued++;
				}
			}
			printer.print("Reset " + num_queued + " experiment(s)\n");
		}
	}
	
	protected class ExperimentDetailsMenuItem extends MenuItem
	{
		public ExperimentDetailsMenuItem()
		{
			super("D", "Details", false);
		}

		@Override
		public void doSomething(AnsiPrinter printer)
		{
			for (int id : m_selectedExperiments.keySet())
			{
				Checkbox cb = m_selectedExperiments.get(id).m_checkbox;
				if (cb.isChecked())
				{
					Experiment ex = m_selectedExperiments.get(id).m_experiment;
					printer.print("Experiment #" + id + ", status: " + ex.getStatus() + "\n");
					if (ex.getStatus() == Status.FAILED)
					{
						printer.print(ex.getErrorMessage() + "\n");
					}
					if (ex.hasWarnings())
					{
						StringBuilder out = new StringBuilder();
						for (ExperimentException eex : ex.getWarnings())
						{
							out.append(eex.getMessage()).append("\n");
						}
						printer.print(out);
					}
					printer.print(ex.getAllParameters() + "\n");
				}
			}
		}
	}
	
	protected class ExperimentElement extends TuiElement
	{
		Checkbox m_checkbox;
		
		Experiment m_experiment;
		
		public ExperimentElement(Experiment e)
		{
			super();
			m_experiment = e;
			m_checkbox = new Checkbox();
		}

		@Override
		public void render(AnsiPrinter printer)
		{
			m_checkbox.render(printer);
			printer.print(AnsiPrinter.padToLength(Integer.toString(m_experiment.getId()), 3));
			printStatus(printer, m_experiment);
			printer.print(AnsiPrinter.padToLength(m_experiment.toShortString(20), 20));
		}
		
		protected void printStatus(AnsiPrinter printer, Experiment e)
		{
			Experiment.Status status = e.getStatus();
			switch (status)
			{
			case PREREQ_NOK:
				printer.print("p");
				break;
			case PREREQ_OK:
				if (m_assistant.isQueued(e))
				{
					printer.setBackgroundColor(AnsiPrinter.Color.WHITE);
					printer.setForegroundColor(AnsiPrinter.Color.BLACK);
					printer.print("Q");
					printer.resetColors();
				}
				else
				{
					printer.print(" ");
				}
				break;
			case DONE:
				printer.setBackgroundColor(AnsiPrinter.Color.GREEN);
				printer.setForegroundColor(AnsiPrinter.Color.BLACK);
				printer.print("D");
				printer.resetColors();
				break;
			case DONE_WARNING:
				printer.setBackgroundColor(AnsiPrinter.Color.YELLOW);
				printer.setForegroundColor(AnsiPrinter.Color.BLACK);
				printer.print("W");
				printer.resetColors();
				break;
			case RUNNING:
				printer.print("R");
				break;
			case FAILED:
				printer.setBackgroundColor(AnsiPrinter.Color.RED);
				printer.setForegroundColor(AnsiPrinter.Color.WHITE);
				printer.print("F");
				printer.resetColors();
				break;
			default:
				printer.setForegroundColor(AnsiPrinter.Color.DARK_GRAY);
				printer.print("?");
				printer.resetColors();
				break;
			}
			printer.print(" ");
		}
	}
	
	protected static void showStatus(AnsiPrinter printer, Laboratory lab, LabAssistant assistant)
	{
		int num_ex = 0, num_q = 0, num_failed = 0, num_done = 0, num_warn = 0;
		for (int id : lab.getExperimentIds())
		{
			num_ex++;
			Experiment ex = lab.getExperiment(id);
			switch (ex.getStatus())
			{
			case DONE:
				num_done++;
				break;
			case DONE_WARNING:
				num_warn++;
				break;
			case FAILED:
				num_failed++;
				break;
			default:
				break;
			}
			if (assistant.isQueued(id))
			{
				num_q++;
			}
		}
		printer.print("\n");
		printer.setForegroundColor(Color.YELLOW).print(num_ex);
		printer.resetColors();
		printer.print(" experiments: ");
		printer.setForegroundColor(Color.YELLOW).print(num_q);
		printer.resetColors();
		printer.print(" queued, ");
		printer.setForegroundColor(Color.YELLOW).print(num_done);
		printer.resetColors();
		printer.print(" done, ");
		printer.setForegroundColor(Color.YELLOW).print(num_warn);
		printer.resetColors();
		printer.print(" done with warnings, ");
		printer.setForegroundColor(Color.YELLOW).print(num_failed);
		printer.resetColors();
		printer.print(" failed\n");
		printer.print("ETA: " + formatEta(assistant.getTimeEstimate()));
		if (assistant.isRunning())
		{
			printer.print(" (running)\n");
		}
		else
		{
			printer.print(" (not running)\n");
		}
		printer.print(String.format("Scaling factor: %.2f\n", Laboratory.s_parkMips));
		printer.print("\n");
	}
	
	/**
	 * Formats a duration into a scale-dependent expression. For example,
	 * a duration of 50 seconds will be shown as "50 seconds", while a duration
	 * of 2000 seconds will be shown as "18 minutes" (exact seconds not shown).
	 * @param n The duration (in seconds)
	 * @return The formatted expression
	 */
	public static String formatEta(float n)
	{
		if (n < 90)
		{
			return String.format("%ds", (int) n);
		}
		int seconds = (int) n % 60;
		n = (n - seconds) / 60;
		if (n < 60)
		{
			return String.format("%dm", (int) n);
		}
		int minutes = (int) n % 60;
		n = (n - minutes) / 60;
		return String.format("%dh%dm", (int) n, minutes);
	}
}
