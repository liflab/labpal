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

/**
 * Exception thrown by a server page callback when the request to a page is
 * considered invalid. The exception carries an error message and the HTTP
 * response code to be returned.
 * 
 * @author Sylvain Hallé
 */
public class PageRenderingException extends Exception
{
	/**
	 * Dummy UID.
	 */
	private static final long serialVersionUID = 1L;
	
	protected int m_code;
	
	protected String m_title;
	
	public PageRenderingException(int code, String title, String message)
	{
		super(message);
		m_title = title;
		m_code = code;
	}
	
	/*@ pure non_null @*/ public int getCode()
	{
		return m_code;
	}
	
	/*@ pure non_null @*/ public String getTitle()
	{
		return m_title;
	}
}
