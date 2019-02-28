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

import java.util.List;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.config.Config;
import ca.uqac.lif.labpal.server.LabPalServer;
import ca.uqac.lif.labpal.server.WebCallback;
import ca.uqac.lif.tui.AnsiPrinter;

public class LabpalStandalone extends LabpalPlatform implements ExportPlatform
{
  LabPalServer m_server = null;

  public LabpalStandalone(Laboratory m_lab, LabAssistant m_assistant, AnsiPrinter m_printer,
      List<WebCallback> callbacks)
  {
    super(m_lab, m_assistant, m_printer);
    config();
    this.m_server = new LabPalServer(null, m_lab, m_assistant);
    if (callbacks != null)
    {
      for (WebCallback cb : callbacks)
      {
        this.m_server.registerCallback(0, cb);
      }
    }
  }

  @Override
  protected void config()
  {
    FileManager.mkdir(Config.getProperty("pathOutput"));
  }

  @Override
  public void export()
  {
    String path = Config.getProperty("pathOutput") + Config.getProperty("zipName") + ".zip";
    FileManager.mkdir(Config.getProperty("pathOutput"));
    m_server.exportToStaticHtml(path);
  }
}
