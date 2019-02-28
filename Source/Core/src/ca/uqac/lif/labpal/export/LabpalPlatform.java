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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Experiment.QueueStatus;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.config.Config;
import ca.uqac.lif.mtnp.plot.Plot;
import ca.uqac.lif.mtnp.plot.Plot.ImageType;
import ca.uqac.lif.mtnp.plot.gnuplot.GnuPlot;
import ca.uqac.lif.tui.AnsiPrinter;

public class LabpalPlatform
{

  protected AnsiPrinter m_printer;

  protected Laboratory m_lab;

  protected LabAssistant m_assistant;

  private List<Experiment> lstExp = new ArrayList<Experiment>();

  public LabpalPlatform(Laboratory lab, LabAssistant m_assistant, AnsiPrinter m_printer)
  {
    super();
    this.m_printer = m_printer;
    this.m_lab = lab;
    this.m_assistant = m_assistant;
  }

  boolean isExpStillRunning()
  {
    for (Experiment e : lstExp)
    {
      Experiment.Status st = e.getStatus();
      if (st == Experiment.Status.RUNNING || st == Experiment.Status.RUNNING_REMOTELY)
      {
        return true;
      }
    }
    return false;
  }

  public List<Experiment> getLstExp()
  {
    if (lstExp.isEmpty())
      setLstExp(getListExperiment());
    return lstExp;
  }

  public void setLstExp(List<Experiment> lstExp)
  {
    this.lstExp = lstExp;
  }

  protected Set<Integer> getExperimentIds()
  {
    return m_lab.getExperimentIds();
  }

  protected Experiment getExperiment(int id)
  {
    return m_lab.getExperiment(id);
  }

  private List<Experiment> getListExperiment()
  {
    List<Experiment> lst = new ArrayList<Experiment>();
    for (int id : getExperimentIds())
    {
      lst.add(getExperiment(id));
    }
    return lst;
  }

  protected void addExperimentsToQeue()
  {
    getLstExp();
    for (Experiment e : lstExp)
    {
      m_assistant.queue(e);
      boolean b = true;
      while (b)
      {
        if (e.getQueueStatus().equals(QueueStatus.QUEUED))
          b = false;
      }
    }

  }

  protected void displayRuningExperiments()
  {

    int num_done = 0;
    for (int id : getExperimentIds())
    {

      Experiment ex = getExperiment(id);
      switch (ex.getStatus())
      {
      case RUNNING:
        long seconds = System.currentTimeMillis() - ex.getStartTime();
        float progression = ex.getProgression();
        m_printer.print("\n Running experiment #" + ex.getId() + " since " + (seconds / 1000)
            + " seconds " + printProgression(progression)  + num_done + "/" + getLstExp().size());

        break;
      default:

        break;
      }
    }
  }
  
  protected String printProgression(float x)
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
   * Gets the image file corresponding to a plot in the given format
   * 
   * @param plot_id
   *          The ID of the plot to display
   * @param format
   *          The image file format (png, pdf, dumb or gp)
   * @return An array of bytes containing the image
   */
  protected byte[] exportTo(int plot_id, String format)
  {
    Plot p = m_lab.getPlot(plot_id);
    byte[] image = null;
    if (format.compareToIgnoreCase("png") == 0)
    {
      image = ((GnuPlot) p).getImage(ImageType.PNG);
    }
    else if (format.compareToIgnoreCase("pdf") == 0)
    {
      image = p.getImage(ImageType.PDF);
    }
    else if (format.compareToIgnoreCase("dumb") == 0)
    {
      image = p.getImage(ImageType.DUMB);
    }
    else if (format.compareToIgnoreCase("gp") == 0 && p instanceof GnuPlot)
    {
      image = ((GnuPlot) p).toGnuplot(ImageType.PDF, m_lab.getTitle(), true).getBytes();
    }
    return image;
  }

  protected void checkLibDependency()
  {
    if (!GnuPlot.isGnuplotPresent())
    {
      m_printer.print("\nWarning: Gnuplot was not found on your system");
    }
    if (m_lab.isEnvironmentOk() != null)
    {
      m_printer.print("\nError: some of the environment requirements for this lab are not met");
      m_printer.print("\nThis means you are missing something to run the experiments.");
    }

  }

  protected void runStarting()
  {
    m_printer.print("\nStart");

  }

  protected void runFinishing()
  {
    m_printer.print("\nPath output: " + Config.getProperty("pathOutput"));
    m_printer.print("\nDone");

  }

  protected void checkExprimentsRunning() throws Exception
  {

    do
    {
      displayRuningExperiments();
      Thread.sleep(5000);
    } while (isExpStillRunning());

    displayRuningExperiments();
  }

  protected void startLab()
  {
    m_lab.start();
  }

  public void run()
  {

    try
    {
      runStarting();

      checkLibDependency();

      addExperimentsToQeue();

      startLab();

      checkExprimentsRunning();
      export();
      runFinishing();
    }
    catch (Exception e)
    {
      Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage());
    }
  }

  protected void export()
  {

  }

  protected void config()
  {

  }

}
