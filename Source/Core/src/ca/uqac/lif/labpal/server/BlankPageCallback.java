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

import java.util.Map;

/**
 * A page with only the header and footer of the lab's GUI, allowing users
 * to insert custom content in the middle. This callback can be overridden to
 * produce additional pages in the lab that conform to the global user
 * interface.
 * 
 * @author Sylvain Hallé
 */
public abstract class BlankPageCallback extends TemplatePageCallback
{

	public BlankPageCallback(LabPalServer server, Method m, String path, String menu_highlight)
	{
		super(server, m, path, "blank.ftlh", menu_highlight);
	}
	
	public BlankPageCallback(LabPalServer server, Method m, String path)
	{
		super(server, m, path, "blank.ftlh", "");
	}
	
	@Override
	protected void fillInputModel(String uri, Map<String,String> request_params, Map<String,Object> input, Map<String,byte[]> parts) throws PageRenderingException
	{
		super.fillInputModel(uri, request_params, input, parts);
		input.put("customcontent", getCustomContent(request_params));
	}
	
	/**
	 * Gets the "custom" content that is to be inserted in the page.
	 * @param request_params The request parameters for this page
	 * @return The content, which must be valid HTML markup
	 */
	/*@ non_null @*/ public abstract String getCustomContent(Map<String,String> request_params);
}
