/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.labpal.GraphvizRenderer;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.provenance.DotProvenanceTreeRenderer;
import ca.uqac.lif.mtnp.table.HardTable;
import ca.uqac.lif.mtnp.table.PrimitiveValue;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TableNode;
import ca.uqac.lif.mtnp.table.Table.CellCoordinate;
import ca.uqac.lif.petitpoucet.NodeFunction;
import ca.uqac.lif.petitpoucet.ProvenanceNode;

import com.sun.net.httpserver.HttpExchange;

/**
 * Callback producing an image explaining the provenance of a data point,
 * as an image in various formats.
 * <p>
 * The HTTP request accepts the following parameters:
 * <ul>
 * <li><tt>dl=1</tt>: to download the image instead of displaying it. This
 *   will prompt the user to save the file in its browser</li>
 * <li><tt>id=x</tt>: mandatory; the ID of the data point to create the graph
 * from</li>
 * <li><tt>format=x</tt>: the requested image format. Currenly supports
 *   pdf and png
 * </ul>
 * 
 * @author Sylvain Hallé
 *
 */
public class ExplainImageCallback extends WebCallback
{
	public ExplainImageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/provenance-graph", lab, assistant);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		Map<String,String> params = getParameters(t);
		String datapoint_id = params.get("id");
		ProvenanceNode node = m_lab.getDataTracker().explain(datapoint_id);
		if (node == null)
		{
			response.setContents("<html><body><h1>Not Found</h1><p>This data point does not seem to exist.</p></body></html>");
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
			return response;
		}
		DotProvenanceTreeRenderer renderer = new DotProvenanceTreeRenderer();
		if (!GraphvizRenderer.s_dotPresent)
		{
			// Asking for an image, but DOT not available: stop right here
			response.setContents("<html><body><h1>Not Found</h1><p>DOT is not present on this system, so the picture cannot be shown.</p></body></html>");
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
			return response;
		}
		String extension = "svg";
		response.setContentType("image/svg+xml");
		if (params.containsKey("format") && params.get("format").compareToIgnoreCase("pdf") == 0)
		{
			response.setContentType(ContentType.PDF);
			extension = "pdf";
		}
		byte[] image = renderer.toImage(node, extension);
		if (image == null)
		{
			response.setContents("<html><body><h1>Internal Server Error</h1><p>The image cannot be displayed.</p></body></html>");
			response.setCode(CallbackResponse.HTTP_INTERNAL_SERVER_ERROR);
			return response;			
		}
		response.setContents(image);
		response.setCode(CallbackResponse.HTTP_OK);
		if (params.containsKey("dl"))
		{
			response.setAttachment(Server.urlEncode(node.getNodeFunction().getDataPointId() + "." + extension));
		}
		return response;
	}

	@Override
	public void addToZipBundle(ZipOutputStream zos) throws IOException {
	
	/*	Set<Integer> ids = m_lab.getTableIds();
		for (int id : ids) {
			Table tab = m_lab.getTable(id);
			HardTable tbl = tab.getDataTable();
			render1(zos,tab, tbl.getTree(), tbl.getColumnNames());
			zos.closeEntry();
		}*/
		
	}

	String render1(ZipOutputStream zos, Table tab, TableNode node, String[] sort_order) {

		int width = sort_order.length;
		StringBuilder out = new StringBuilder();
		if (node == null || (node.m_children.isEmpty())) {
			return "";
		}
		List<PrimitiveValue> values = new ArrayList<PrimitiveValue>();
		renderRecursive(zos, tab, node, values, out, width);

		return out.toString();

	}

	protected void renderRecursive(ZipOutputStream zos, Table tab, TableNode cur_node, List<PrimitiveValue> values,
			StringBuilder out, int max_depth) {
		if (values != null && values.size() > 0) {
			printCell(zos, tab, out, values, cur_node.countLeaves(), max_depth, cur_node);
		}
		boolean first_child = true;
		for (TableNode child : cur_node.m_children) {
			values.add(child.getValue());
			if (first_child) {
				first_child = false;
			}
			renderRecursive(zos, tab, child, values, out, max_depth);
			values.remove(values.size() - 1);
		}
	}

	public void printCell(ZipOutputStream zos, Table tab, StringBuilder out, List<PrimitiveValue> values,
			int nb_children, int max_depth, TableNode node) {
		List<CellCoordinate> coordinates = node.getCoordinates();
		if (coordinates.size() > 0) {
			CellCoordinate cc = coordinates.get(0);
			String dp_id = "";
			NodeFunction nf = tab.dependsOn(cc.row, cc.col);
			if (nf != null) {
				dp_id = nf.getDataPointId();
			}
			//System.out.println("hhh " + dp_id);
			HashMap<String, String> params = new HashMap<String,String>();
			params.put("id", dp_id);
			ZipEntry ze = new ZipEntry("table/"+dp_id+".png");
			try {
				zos.putNextEntry(ze);

				zos.write(exportToStaticHtml(dp_id));
				zos.closeEntry();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}

	}
	
	public byte[] exportToStaticHtml(String datapoint_id)
	{
		DotProvenanceTreeRenderer renderer = new DotProvenanceTreeRenderer();
	
		ProvenanceNode node = m_lab.getDataTracker().explain(datapoint_id);
		if (!GraphvizRenderer.s_dotPresent)
		{
		return renderer.toImage(node, "png");
		}
		return null;
		
	}

}
