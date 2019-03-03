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

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.tui.AnsiPrinter;

/**
 * Batch runner that exports its data in a format suitable to be used in
 * CodeOcean.
 * 
 * @author Chafik Meniar
 * @author Sylvain Hallé
 */
public class CodeOceanRunner extends LocalBatchRunner
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
}
