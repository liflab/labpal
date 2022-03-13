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
package ca.uqac.lif.labpal.server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Calendar;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.labpal.util.FileHelper;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.Stateful.Status;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.LatexTableRenderer;

import com.sun.net.httpserver.HttpExchange;

/**
 * Callback to download all tables as a single LaTeX file.
 * 
 * @author Sylvain Hallé
 *
 */
public class AllTablesCallback extends LaboratoryCallback
{
	protected static final String s_barberPoleCode =  FileHelper.internalFileToString(AllPlotsLatexCallback.class, "resource/table-barber-pole.tex");
	
  public AllTablesCallback(LabPalServer s)
  {
    super(s, Method.GET, "/tables/tex");
  }

  @Override
  public CallbackResponse process(HttpExchange t)
  {
    CallbackResponse response = new CallbackResponse(t);
    response.setContentType(ContentType.LATEX);
    String filename = Server.urlEncode("labpal-tables.tex");
    response.setAttachment(filename);
    response.setContents(getLatexFile());
    return response;
  }

  /**
   * Generates the LaTeX file
   * 
   * @return The contents of the file
   */
  public String getLatexFile()
  {
    StringBuilder out = new StringBuilder();
    out.append("% ----------------------------------------------------------------")
        .append(FileHelper.CRLF);
    out.append("% File generated by LabPal ").append(Laboratory.s_versionString)
        .append(FileHelper.CRLF);
    out.append("% Date:     ").append(String.format("%1$te-%1$tm-%1$tY", Calendar.getInstance()))
        .append(FileHelper.CRLF);
    out.append("% Lab name: ").append(m_server.getLaboratory().getName()).append(FileHelper.CRLF);
    out.append("%").append(FileHelper.CRLF)
        .append("% To insert one of the tables into your text, do:").append(FileHelper.CRLF);
    out.append("% \\begin{table}").append(FileHelper.CRLF).append("% \\usebox{\\boxname}")
        .append(FileHelper.CRLF).append("% \\end{table}").append(FileHelper.CRLF);
    out.append("% where \\boxname is one of the boxes defined in the file below")
        .append(FileHelper.CRLF);
    out.append("% ----------------------------------------------------------------")
        .append(FileHelper.CRLF).append(FileHelper.CRLF);
    out.append(s_barberPoleCode);
    out.append(FileHelper.CRLF);
    for (Table tab : m_server.getLaboratory().getTables())
    {
      if (!tab.showsInList())
      {
        continue;
      }
      LatexTableRenderer renderer = new LatexTableRenderer(tab);
      String box_name = "t" + tab.getTitle();
      if (!tab.getNickname().isEmpty())
      {
        box_name = tab.getNickname();
      }
      if (box_name.compareTo("Untitled") == 0)
      {
        box_name += tab.getId();
      }
      box_name = LatexTableRenderer.formatName(box_name);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      renderer.render(new PrintStream(baos));
      String tab_contents = baos.toString();
      out.append("% ----------------------").append(FileHelper.CRLF).append("% Table: ")
          .append(box_name).append(FileHelper.CRLF).append("% ")
          .append(LatexTableRenderer.formatName(tab.getTitle())).append(FileHelper.CRLF);
      out.append("% ----------------------").append(FileHelper.CRLF);
      out.append("\\newsavebox{\\").append(box_name).append("}").append(FileHelper.CRLF);
      out.append("\\begin{lrbox}{\\").append(box_name).append("}").append(FileHelper.CRLF);
      Status st = tab.getStatus();
      boolean barber_pole = false;
      if (st == Status.FAILED || st == Status.INTERRUPTED)
      {
      	barber_pole = true;
      }
      if (barber_pole)
      {
      	out.append("\\myplotstripbox{").append(FileHelper.CRLF);
      }
      out.append(tab_contents);
      if (barber_pole)
      {
      	out.append("}");
      }
      out.append(FileHelper.CRLF).append("\\end{lrbox}").append(FileHelper.CRLF)
          .append(FileHelper.CRLF);
    }
    return out.toString();
  }
}
