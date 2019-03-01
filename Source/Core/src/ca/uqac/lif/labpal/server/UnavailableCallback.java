/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2017 Sylvain Hallé

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

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Callback for a special page, used only in the static export of a lab, saying
 * that some feature is not available.
 * 
 * @author Sylvain Hallé
 *
 */
public class UnavailableCallback extends TemplatePageCallback
{

  public UnavailableCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/unavailable", lab, assistant);
  }

  @Override
  public void addToZipBundle(ZipOutputStream zos) throws IOException
  {
    ZipEntry ze = new ZipEntry("unavailable.html");
    zos.putNextEntry(ze);
    zos.write(exportToStaticHtml("").getBytes());
    zos.closeEntry();
  }
}
