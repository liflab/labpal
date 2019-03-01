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
import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.config.Config;
import ca.uqac.lif.labpal.server.AllPlotsLatexCallback;
import ca.uqac.lif.mtnp.plot.Plot;
import ca.uqac.lif.mtnp.plot.Plot.ImageType;
import ca.uqac.lif.mtnp.plot.gnuplot.GnuPlot;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TempTable;
import ca.uqac.lif.tui.AnsiPrinter;

/**
 * Batch runner that exports its data in a format suitable to be used in
 * CodeOcean.
 * 
 * @author Chafik Meniar
 * @author Sylvain Hallé
 */
public class CodeOceanRunner extends BatchRunner
{
  public CodeOceanRunner(Laboratory lab, LabAssistant assistant, AnsiPrinter printer)
  {
    super(lab, assistant, printer, "../results");
  }

  @Override
  public void showStartMessage()
  {
    m_stdout.println("Starting " + m_lab.getTitle() + " in Code Ocean");
  }

  void exportExperiments() throws IOException
  {
    StringBuilder rowshtmlTable = new StringBuilder();
    StringBuilder tagTable = new StringBuilder(
        "<table> <tr>  <th>#</th>  <th>Experiments</th>  <th>Status</th>  </tr>");
    rowshtmlTable.append(tagTable);
    for (Experiment e : m_lab.getExperiments())
    {
      StringBuilder row = new StringBuilder();
      row.append("  <tr>");
      row.append("    <td>" + e.getId() + "</td>");
      row.append("    <td>" + e.getInputParameters().toString() + "</td>");
      row.append("    <td>" + e.getStatus() + "</td>");
      row.append("  </tr>");
      rowshtmlTable.append(row);

    }
    rowshtmlTable.append("</table>");

    StringBuilder templateContent = FileManager.readFile(Config.getProperty("namefileTempExp"), ".html",
        Config.getProperty("pathInput"));

    int index = templateContent.indexOf("<!-- include -->");
    templateContent.insert(index, rowshtmlTable);

    FileManager.writeFile(Config.getProperty("pathOutput"), Config.getProperty("namefileTempExp"), ".html",
        templateContent.toString());
  }

  void exportTables() throws IOException
  {
    StringBuilder allTablesContent = new StringBuilder();
    StringBuilder allTablesContentHtml = new StringBuilder();
    for (int id : m_lab.getTableIds())
    {

      Table tab = m_lab.getTable(id);
      if (!tab.showsInList())
      {
        continue;
      }
      TempTable d_tab = tab.getDataTable();
      String content = tab.getTitle() + "    \n" + d_tab.toCsv();
      allTablesContent.append(content);
      allTablesContent.append("\n");
      FileManager.writeFile(Config.getProperty("pathOutput"), "table" + id, ".csv", content);
      content = "<H1> " + tab.getTitle() + "</H1> " + d_tab.toHtml();
      allTablesContentHtml.append(content);
      allTablesContentHtml.append("\n");
      FileManager.writeFile(Config.getProperty("pathOutput"), "table" + id, ".html", content);
    }
    FileManager.writeFile(Config.getProperty("pathOutput"), "tables", ".csv",
        allTablesContent.toString());
    FileManager.writeFile(Config.getProperty("pathOutput"), "tables", ".html",
        allTablesContentHtml.toString());
  }

  void exportPlots() throws IOException
  {
    for (int id : m_lab.getPlotIds())
    {
      byte[] byte_array = exportTo(id, "png");
      FileManager.writeFile(Config.getProperty("pathOutput"), Config.getProperty("imageName") + id, ".png",
          byte_array);
    }
    List<String> lstPath = new ArrayList<String>();
    for (int id : m_lab.getPlotIds())
    {
      byte[] bytes = exportTo(id, "pdf");
      lstPath.add(Config.getProperty("pathOutput") + Config.getProperty("pdfName") + id + ".pdf");
      FileManager.writeFile(Config.getProperty("pathOutput"), Config.getProperty("pdfName") + id, ".pdf",
          bytes);
    }
    if (!lstPath.isEmpty())
    {
      String[] tab = lstPath.toArray(new String[lstPath.size()]);
      FileManager.mergePdf(Config.getProperty("pathOutput") + Config.getProperty("pdfName") + ".pdf", tab);
    }
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


  void exportLatex() throws IOException
  {
    FileManager.writeFile(Config.getProperty("pathOutput"), "AllPlotsLatex", ".tex",
        AllPlotsLatexCallback.generateBodyLatex(m_lab).toString());
  }

  @Override
  public void export() throws IOException
  {
    m_stdout.println("Exporting files to " + m_path);
    exportExperiments();
    exportTables();
    exportPlots();
    exportLatex();
  }
}
