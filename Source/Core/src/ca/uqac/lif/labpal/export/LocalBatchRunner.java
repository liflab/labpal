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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.tui.AnsiPrinter;

/**
 * Batch runner that exports its data in a zip file.
 * 
 * @author Chafik Meniar
 * @author Sylvain Hallé
 */
public class LocalBatchRunner extends BatchRunner
{

  public LocalBatchRunner(Laboratory lab, LabAssistant assistant, AnsiPrinter printer, String path)
  {
    super(lab, assistant, printer, path);
  }

  @Override
  public void export() throws IOException
  {
    new File(m_path).mkdirs();
    saveLab();
    m_stdout.println("Exporting files to " + m_path);
    byte[] zip_bytes = m_server.exportToStaticHtml();
    byte[] buffer = new byte[2048];
    // Unzip the contents
    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip_bytes));
    ZipEntry ze = zis.getNextEntry();
    while (ze != null)
    {
      String filename = ze.getName();
      if (filename.isEmpty())
      {
        ze = zis.getNextEntry();
        continue;
      }
      String out_filename = m_path + FileHelper.SLASH + filename;
      if (ze.isDirectory())
      {
        File f = new File(out_filename);
        f.mkdirs();
      }
      else
      {
        int len;
        File f = new File(out_filename);
        File p_f = f.getParentFile();
        if (p_f != null)
        {
          p_f.mkdirs();
        }
        FileOutputStream output = new FileOutputStream(f);
        while ((len = zis.read(buffer)) > 0)
        {
          output.write(buffer, 0, len);
        }
        output.close();
      }
      ze = zis.getNextEntry();
    }
  }
  
  @Override
  public void saveLab()
  {
    try
    {
      JsonMap jm = (JsonMap) m_lab.saveToJson();
      String lab = jm.toString("", false);
      String filename = m_path + FileHelper.SLASH + "Lab.json";
      File f = new File(filename);
      FileHelper.writeFromString(f, lab);
      m_stdout.println("Lab status saved to " + filename);
    }
    catch (PrintException e)
    {
      m_stdout.println("Lab data could not be saved");
    }
  }
  
  @Override
  public void showStartMessage()
  {
    m_stdout.println("Starting " + m_lab.getTitle() + " in batch mode");
  }
}
