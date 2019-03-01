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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    m_stdout.println("Exporting files to " + m_path);
    byte[] zip_bytes = m_server.exportToStaticHtml();
    byte[] buffer = new byte[2048];
    Path out_path = Paths.get(m_path);
    // Unzip the contents
    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip_bytes));
    ZipEntry ze = zis.getNextEntry();
    while (ze != null)
    {
      String filename = ze.getName();
      System.out.println(filename);
      if (ze.isDirectory())
      {
        File f = new File(filename);
        f.mkdirs();
      }
      else
      {
        int len;
        Path file_path = out_path.resolve(filename);
        FileOutputStream  output = new FileOutputStream(file_path.toFile());
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
  public void showStartMessage()
  {
    m_stdout.println("Starting " + m_lab.getTitle() + " in batch mode");
  }
}
