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
package ca.uqac.lif.labpal.export;

import java.io.IOException;
import java.util.Set;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.server.LabPalServer;
import ca.uqac.lif.mtnp.plot.gnuplot.GnuPlot;
import ca.uqac.lif.tui.AnsiPrinter;

/**
 * Coordinates the execution of a lab in batch mode and the exporting of its
 * tables and plots to static files.
 * 
 * @author Chafik Meniar
 * @author Sylvain Hallé
 */
public abstract class BatchRunner
{
  /**
   * The local path where the static files will be written
   */
  protected String m_path;

  /**
   * The lab that will be run in batch
   */
  protected Laboratory m_lab;

  /**
   * The printer where status messages will be written during the process
   */
  protected AnsiPrinter m_stdout;

  /**
   * The assistant that will run the experiments
   */
  protected LabAssistant m_assistant;

  /**
   * A headless server, which will be used to export data to static pages
   */
  protected LabPalServer m_server;

  /**
   * The interval (in ms) at which updates will be printed to the console
   */
  protected static final transient long s_updateInterval = 5000;

  /**
   * Creates a new batch runner
   * @param lab The lab that will be run in batch
   * @param assistant The assistant that will run the experiments
   * @param printer The assistant that will run the experiments
   * @param path The local path where the static files will be written
   */
  public BatchRunner(Laboratory lab, LabAssistant assistant, AnsiPrinter printer, String path)
  {
    super();
    m_stdout = printer;
    m_lab = lab;
    m_assistant = assistant;
    m_server = new LabPalServer(m_lab.getCliArguments(), m_lab, m_assistant);
    m_path = path;
  }

  /**
   * Runs the lab in batch mode
   */
  public void run()
  {
    showStartMessage();
    checkDependencies();
    // Start lab and display regular updates
    m_lab.startAll();
    // Give some time for the assistant to start
    Experiment.wait(500);
    long last_update = 0, now = 0;
    while (m_assistant.isRunning())
    {
      Experiment.wait(500);
      now = System.currentTimeMillis();
      if (last_update == 0 || now - last_update > s_updateInterval)
      {
        showStatus();
        last_update = now;
      }
    }
    showStatus(); // One last time
    m_stdout.println();
    // Export data
    try
    {
      export();
    }
    catch (IOException e)
    {
      m_stdout.println("Error exporting lab data.");
      e.printStackTrace();
      //m_stdout.println(e.getMessage());
    }
    m_stdout.println("Done.");
  }

  /**
   * Checks the dependencies
   */
  protected void checkDependencies()
  {
    if (!GnuPlot.isGnuplotPresent())
    {
      m_stdout.print("\nWarning: Gnuplot was not found on your system");
    }
    if (m_lab.isEnvironmentOk() != null)
    {
      m_stdout.print("\nError: some of the environment requirements for this lab are not met");
      m_stdout.print("\nThis means you are missing something to run the experiments.");
    }
  }

  /**
   * Prints a textual progression bar
   * @param x A value between 0 and 1 indicating the progression
   * @return The progress bar
   */
  protected static String printProgression(float x)
  {
    int total = 10;
    int to_draw = (int) (x * (float) total);
    StringBuilder out = new StringBuilder();
    out.append("[");
    for (int i = 0; i < to_draw; i++)
    {
      out.append("#");
    }
    for (int i = to_draw; i < total; i++)
    {
      out.append(" ");
    }
    out.append("]");
    return out.toString();
  }

  /**
   * Shows a status message indicating the state of the running experiments
   * @return <tt>true</tt> if the assistant is still running, <tt>false</tt>
   * otherwise
   */
  protected boolean showStatus()
  {
    Set<Experiment> running = m_assistant.getRunningExperiments();
    int all = m_lab.getExperiments().size();
    int queued = m_assistant.getCurrentQueue().size();
    int done = all - queued - running.size();
    for (Experiment e : running)
    {
      long seconds = System.currentTimeMillis() - e.getStartTime();
      float progression = e.getProgression();
      m_stdout.printf("Running experiment #%3d since %3d seconds %s %d/%d\n", e.getId(), seconds / 1000,
          printProgression(progression), done, all);
    }
    return m_assistant.isRunning();
  }

  /**
   * Exports the lab's data
   * @throws IOException If writing data to an output stream cannot be done
   * for some reason
   */
  protected abstract void export() throws IOException;
  
  /**
   * Exports the lab's state
   * @throws IOException If writing data to an output stream cannot be done
   * for some reason
   */
  protected abstract void saveLab() throws IOException;

  /**
   * Shows a start message
   */
  protected abstract void showStartMessage();
}
