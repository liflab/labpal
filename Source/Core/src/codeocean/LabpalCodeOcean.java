package ca.uqac.lif.codeocean;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.config.Config;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TempTable;
import ca.uqac.lif.tui.AnsiPrinter;

public class LabpalCodeOcean extends LabpalPlatform implements IPlatform {

	enum EXPORTTYPE {
		EXPERIMENT, TABLE, GRAPHIC
	}

	public LabpalCodeOcean(Laboratory m_lab, LabAssistant m_assistant, AnsiPrinter m_printer) {
		super(m_lab, m_assistant, m_printer);
		run();
	}

	void exportExperiments() throws Exception {

		StringBuilder rowshtmlTable = new StringBuilder();
		StringBuilder tagTable = new StringBuilder(
				"<table> <tr>  <th>#</th>  <th>Experiments</th>  <th>Status</th>	</tr>");
		rowshtmlTable.append(tagTable);
		for (Experiment e : getLstExp()) {
			StringBuilder row = new StringBuilder();
			row.append("  <tr>");
			row.append("    <td>" + e.getId() + "</td>");
			row.append("    <td>" + e.getInputParameters().toString() + "</td>");
			row.append("    <td>" + e.getStatus() + "</td>");
			row.append("  </tr>");
			rowshtmlTable.append(row);

		}
		rowshtmlTable.append("</table>");

		StringBuilder templateContent = FileManager.readFile(Config.getProp("namefileTempExp"), ".html",
				Config.getProp("pathTempExp"));

		int index = templateContent.indexOf("<!-- include -->");
		templateContent.insert(index, rowshtmlTable);

		FileManager.writeFile(Config.getProp("pathOutputExp"), Config.getProp("namefileTempExp"), ".html",
				templateContent.toString());

	}

	void exportTables() throws Exception {

		StringBuilder allTablesContent = new StringBuilder();
		StringBuilder allTablesContentHtml = new StringBuilder();

		for (int id : lab.getTableIds()) {

			Table tab = lab.getTable(id);
			TempTable d_tab = tab.getDataTable();
			String content = tab.getTitle() + "    \n" + d_tab.toCsv();
			allTablesContent.append(content);
			allTablesContent.append("\n");
			FileManager.writeFile(Config.getProp("pathTablesAbsolute"), "table" + id, ".csv", content);

			content = "<H1> " + tab.getTitle() + "</H1> " + d_tab.toHtml();
			allTablesContentHtml.append(content);
			allTablesContentHtml.append("\n");
			FileManager.writeFile(Config.getProp("pathTablesAbsolute"), "table" + id, ".html", content);

		}

		FileManager.writeFile(Config.getProp("pathTablesAbsolute"), "tables", ".csv", allTablesContent.toString());
		FileManager.writeFile(Config.getProp("pathTablesAbsolute"), "tables", ".html", allTablesContentHtml.toString());

	}

	void exportPlots() throws Exception {

		for (int id : lab.getPlotIds()) {

			byte[] byte_array = exportTo(id, "png");

			FileManager.writeFile(Config.getProp("pathImageAbsolute"), Config.getProp("imageName") + id, ".png",
					byte_array);

		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		List<String> lstPath = new ArrayList<>();
		for (int id : lab.getPlotIds()) {

			byte[] bytes = exportTo(id, "pdf");
			lstPath.add(Config.getProp("pathPdfAbsolute") + Config.getProp("pdfName") + id + ".pdf");
			FileManager.writeFile(Config.getProp("pathPdfAbsolute"), Config.getProp("pdfName") + id, ".pdf", bytes);
		}
		if (!lstPath.isEmpty()) {

			String[] tab = lstPath.toArray(new String[lstPath.size()]);
			FileManager.mergePdF(Config.getProp("pathPdfAbsolute") + Config.getProp("pdfName") + ".pdf", tab);
		}

	}


	@Override
	public void export() {

		try {
			exportExperiments();
			exportTables();
			exportPlots();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
