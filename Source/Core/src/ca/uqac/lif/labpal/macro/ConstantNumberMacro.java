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
package ca.uqac.lif.labpal.macro;

import ca.uqac.lif.labpal.Laboratory;

public class ConstantNumberMacro extends NumberMacro
{
	protected Number m_value;

	public ConstantNumberMacro(Laboratory lab, String name, String description, Number value) 
	{
		super(lab, name, description);
		m_value = value;
	}
	
	@Override
	public final Number getNumber()
	{
		return m_value;
	}

}
