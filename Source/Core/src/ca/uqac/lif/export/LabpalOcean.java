package ca.uqac.lif.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Experiment.QueueStatus;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.config.Config;
import ca.uqac.lif.mtnp.plot.Plot;
import ca.uqac.lif.mtnp.plot.Plot.ImageType;
import ca.uqac.lif.mtnp.plot.gnuplot.GnuPlot;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TempTable;
import ca.uqac.lif.tui.AnsiPrinter;

//import org.apache.pdfbox.multipdf.PDFMergerUtility;
public class LabpalOcean {

	protected AnsiPrinter m_printer;

	protected Laboratory lab;

	protected LabAssistant assistant;

	public Set<Integer> getExperimentIds() {
		return lab.getExperimentIds();
	}

	public Experiment getExperiment(int id) {
		return lab.getExperiment(id);
	}

	public List<Experiment> getListExperiment() {
		List<Experiment> lst = new ArrayList<>();
		for (int id : getExperimentIds()) {
			lst.add(getExperiment(id));
		}
		return lst;
	}

	public LabpalOcean(Laboratory m_lab, LabAssistant m_assistant, AnsiPrinter m_printer) {
		super();
		this.m_printer = m_printer;
		this.lab = m_lab;
		this.assistant = m_assistant;
	}

	boolean isExpStillRunning(List<Experiment> lstE) {
		for (Experiment e : lstE) {
			if (!e.finished) {
				return true;

			}
		}
		return false;
	}

	/**
	 * Gets the image file corresponding to a plot in the given format
	 * 
	 * @param plot_id
	 *            The ID of the plot to display
	 * @param format
	 *            The image file format (png, pdf, dumb or gp)
	 * @return An array of bytes containing the image
	 */
	public byte[] exportTo(int plot_id, String format) {
		Plot p = lab.getPlot(plot_id);
		byte[] image = null;
		if (format.compareToIgnoreCase("png") == 0) {
			image = ((GnuPlot) p).getImage(ImageType.PNG);
		} else if (format.compareToIgnoreCase("pdf") == 0) {
			image = p.getImage(ImageType.PDF);
		} else if (format.compareToIgnoreCase("dumb") == 0) {
			image = p.getImage(ImageType.DUMB);
		} else if (format.compareToIgnoreCase("gp") == 0 && p instanceof GnuPlot) {
			image = ((GnuPlot) p).toGnuplot(ImageType.PDF, lab.getTitle(), true).getBytes();
		}
		return image;
	}

	public void run() {

		if (!GnuPlot.isGnuplotPresent()) {
			m_printer.print("\nWarning: Gnuplot was not found on your system");
		}
		if (lab.isEnvironmentOk() != null) {
			m_printer.print("\nError: some of the environment requirements for this lab are not met");
			m_printer.print("\nThis means you are missing something to run the experiments.");
		}

		try {
			FileManager rp = new FileManager();
			StringBuilder listExp = new StringBuilder();
			List<Experiment> lstE = fillTemplateExperiment(rp, listExp);
			exportTableHtmlForExp(rp, listExp);
			// ajouter tous experience aux queue
			addExperimentToQeue(lstE);

			lab.start();

			Thread.sleep(1000);

			while (!assistant.isRunning() && !assistant.getCurrentQueue().isEmpty()
					&& (assistant.getRunningTime() / 1000) != 0) {
				System.out.println("This lab assistant has no experiment left to do");

			}

			while (isExpStillRunning(lstE)) {
				System.out.println("---------Some experiments are still running-------------");
				fillTemplateExperiment(rp, listExp);
				Thread.sleep(50000);
			}
			// Afficher les tables
			StringBuilder allTablesContent = new StringBuilder();
			StringBuilder allTablesContentHtml = new StringBuilder();
			for (int id : lab.getTableIds()) {

				Table tab = lab.getTable(id);
				TempTable d_tab = tab.getDataTable();

				// System.out.println(" " + id + " " + tab.getTitle());
				// export table:
				// System.out.println("csv");
				String content = tab.getTitle() + "    \n" + d_tab.toCsv();
				allTablesContent.append(content);
				allTablesContent.append("\n");
				// System.out.println(st);
				//rp.writeFile(Config.getProp("pathTablesAbsolute"), "table" + id + ".csv", content);
				/*try {
					content = d_tab.toHtml();
					allTablesContentHtml.append(content);
					allTablesContentHtml.append("\n");
					rp.writeFile(Config.getProp("pathTablesAbsolute"), "table" + id + ".html", content);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e);
				}*/

			}
			//rp.writeFile(Config.getProp("pathTablesAbsolute"), "tables" + ".csv", allTablesContent.toString());
		//	rp.writeFile(Config.getProp("pathTablesAbsolute"), "tables.html", allTablesContentHtml.toString());

			for (int id : lab.getPlotIds()) {

				byte[] byte_array = exportTo(id, "png");
				rp.saveImage(Config.getProp("pathImageAbsolute"), Config.getProp("imageName") + id, byte_array);

			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			List<String> lstPath = new ArrayList<>();
			for (int id : lab.getPlotIds()) {

				byte[] bytes = exportTo(id, "pdf");
				lstPath.add(Config.getProp("pathPdfAbsolute") + Config.getProp("pdfName") + id + ".pdf");
				rp.savePdf(Config.getProp("pathPdfAbsolute"), Config.getProp("pdfName") + id, bytes);
			}
			if (!lstPath.isEmpty()) {

				String[] tab = lstPath.toArray(new String[lstPath.size()]);
				rp.mergePdF(Config.getProp("pathPdfAbsolute") + Config.getProp("pdfName") + ".pdf", tab);
			}
			fillTemplateExperiment(rp, listExp);
			exportTableHtmlForExp(rp, listExp);
			System.out.println("Done");

			// Afficher les plots
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void addExperimentToQeue(List<Experiment> lstE) {

		for (Experiment e : lstE) {
			assistant.queue(e);
			boolean b = true;
			while (b) {
				if (e.getQueueStatus().equals(QueueStatus.QUEUED))
					b = false;
			}
		}

	}

	public List<Experiment> fillTemplateExperiment(FileManager rp, StringBuilder rowshtmlTable) throws IOException {
		List<Experiment> lst = new ArrayList<>();
		rowshtmlTable.append(" <br/><br/>    ");
		rowshtmlTable.append("<p> TimeStamp : " + new Timestamp(System.currentTimeMillis()) + " </p> ");
		// StringBuilder rowshtmlTable = new StringBuilder();
		/**
		 * <tr>
		 * <th>#</th>
		 * <th>a</th>
		 * <th>name</th>
		 * <th>status</th>
		 * </tr>
		 */

		StringBuilder tagTable = new StringBuilder(
				"<table> <tr>  <th>#</th>  <th>Experiments</th>  <th>Status</th>	</tr>");
		rowshtmlTable.append(tagTable);
		for (int id : lab.getExperimentIds()) {
			Experiment e = lab.getExperiment(id);

			StringBuilder row = new StringBuilder();

			row.append("  <tr>");
			row.append("    <td>" + e.getId() + "</td>");
			row.append("    <td>" + e.getInputParameters().toString() + "</td>");
			// row.append(" <td>gg</td>");
			row.append("    <td>" + e.getStatus() + "</td>");
			row.append("  </tr>");

			rowshtmlTable.append(row);
			lst.add(e);
			// assistant.queue(e);

		}
		rowshtmlTable.append("</table>");
		return lst;

	}

	public void exportTableHtmlForExp(FileManager rp, StringBuilder rowshtmlTable) {
		StringBuilder templateContent;
	//	try {
			//templateContent = rp.readFile(Config.getProp("namefileTempExp"), Config.getProp("pathTempExp"));

		//	int index = templateContent.indexOf("<!-- include -->");
		//	templateContent.insert(index, rowshtmlTable);

			// System.out.println("New Contents of StringBuilder object:" +
			// templateContent);
			//rp.writeFile(Config.getProp("pathOutputExp"), Config.getProp("namefileTempExp"),
			//		templateContent.toString());
		//} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}

	}

}
