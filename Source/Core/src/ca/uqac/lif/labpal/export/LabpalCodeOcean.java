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
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.config.Config;
import ca.uqac.lif.labpal.server.AllPlotsLatexCallback;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TempTable;
import ca.uqac.lif.tui.AnsiPrinter;

public class LabpalCodeOcean extends LabpalPlatform implements ExportPlatform
{
  @Override
  protected void config()
  {

    if (Config.env.equals(Config.ENV.CODEOCEAN))
    {
      Config.setProperty("namefileTempExp", "Experiments");
      Config.setProperty("pathInput", "../data/");
      Config.setProperty("pathOutput", "../results/");
      Config.setProperty("pdfName", "labpal-plots");
      Config.setProperty("imageName", "img");
      Config.setProperty("zipName", "LabpalStatic");
    }
  }

  public LabpalCodeOcean(Laboratory m_lab, LabAssistant m_assistant, AnsiPrinter m_printer)
  {
    super(m_lab, m_assistant, m_printer);
    config();
  }

  void exportExperiments() throws Exception
  {
    StringBuilder rowshtmlTable = new StringBuilder();
    StringBuilder tagTable = new StringBuilder(
        "<table> <tr>  <th>#</th>  <th>Experiments</th>  <th>Status</th>	</tr>");
    rowshtmlTable.append(tagTable);
    for (Experiment e : getLstExp())
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

  void exportTables() throws Exception
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

  void exportPlots() throws Exception
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
      FileManager.mergePdF(Config.getProperty("pathOutput") + Config.getProperty("pdfName") + ".pdf", tab);
    }
  }

  void exportLatex() throws Exception
  {
    FileManager.writeFile(Config.getProperty("pathOutput"), "AllPlotsLatex", ".tex",
        AllPlotsLatexCallback.generateBodyLatex(m_lab).toString());
  }

  @Override
  public void export()
  {
    try
    {
      exportExperiments();
      exportTables();
      exportPlots();
      exportLatex();
    }
    catch (Exception e)
    {
      Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage());
    }
  }
}