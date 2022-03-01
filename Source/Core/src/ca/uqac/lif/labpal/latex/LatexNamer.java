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
package ca.uqac.lif.labpal.latex;

import static ca.uqac.lif.labpal.latex.LatexExportable.latexify;

import ca.uqac.lif.labpal.util.Namer;

/**
 * Namer that builds a LaTeX string.
 * @author Sylvain Hallé
 * @since 2.11
 */
public class LatexNamer extends Namer
{
  @Override
  protected String createFragment(String dim_name, Number n)
  {
    return latexify(dim_name) + latexify(n.toString());
  }
  
  @Override
  protected String createFragment(String dim_name, String s)
  {
    return latexify(dim_name) + latexify(s);
  }
  
  @Override
  protected String handlePrefix(String prefix)
  {
    return latexify(prefix);
  }
  
  @Override
  protected String handleSuffix(String suffix)
  {
    return latexify(suffix);
  }
}
