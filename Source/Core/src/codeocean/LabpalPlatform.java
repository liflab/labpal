package ca.uqac.lif.codeocean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Experiment.QueueStatus;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.mtnp.plot.Plot;
import ca.uqac.lif.mtnp.plot.Plot.ImageType;
import ca.uqac.lif.mtnp.plot.gnuplot.GnuPlot;
import ca.uqac.lif.tui.AnsiPrinter;

public class LabpalPlatform {

	AnsiPrinter m_printer;

	Laboratory lab;

	LabAssistant assistant;

	private List<Experiment> lstExp = new ArrayList<>();


	public LabpalPlatform(Laboratory m_lab, LabAssistant m_assistant, AnsiPrinter m_printer) {
		super();
		this.m_printer = m_printer;
		this.lab = m_lab;
		this.assistant = m_assistant;
	}

	boolean isExpStillRunning() {
		for (Experiment e : lstExp) {
			if (!e.finished) {
				return true;

			}
		}
		return false;
	}
	public List<Experiment> getLstExp() {
		if (lstExp.isEmpty())
			setLstExp(getListExperiment());
		return lstExp;
	}

	public void setLstExp(List<Experiment> lstExp) {
		this.lstExp = lstExp;
	}

	protected Set<Integer> getExperimentIds() {
		return lab.getExperimentIds();
	}

	protected Experiment getExperiment(int id) {
		return lab.getExperiment(id);
	}

	private List<Experiment> getListExperiment() {
		List<Experiment> lst = new ArrayList<>();
		for (int id : getExperimentIds()) {
			lst.add(getExperiment(id));
		}
		return lst;
	}

	protected void addExperimentsToQeue() {
		getLstExp();
		for (Experiment e : lstExp) {
			assistant.queue(e);
			boolean b = true;
			while (b) {
				if (e.getQueueStatus().equals(QueueStatus.QUEUED))
					b = false;
			}
		}

	}

	protected void displayRuningExperiments() {

		int num_ex = 0, num_q = 0, num_failed = 0, num_done = 0, num_warn = 0;
		for (int id : getExperimentIds()) {
			num_ex++;
			Experiment ex = getExperiment(id);
			switch (ex.getStatus()) {
			case RUNNING:
				long seconds = System.currentTimeMillis() - ex.getStartTime();
				m_printer.print("\n Running experiment : #" + ex.getId() + " for " + seconds + " seconds [######-----]"
						+ num_done + "/" + getLstExp().size());

				break;
			case DONE:
				num_done++;
				break;
			case FAILED:
				num_failed++;
				break;
			case DONE_WARNING:
				num_warn++;
				break;
			default:
				if (assistant.isQueued(id)) {
					num_q++;
				}
				break;
			}
		}

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
	protected byte[] exportTo(int plot_id, String format) {
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

	protected void checkLibDependency() {
		if (!GnuPlot.isGnuplotPresent()) {
			m_printer.print("\nWarning: Gnuplot was not found on your system");
		}
		if (lab.isEnvironmentOk() != null) {
			m_printer.print("\nError: some of the environment requirements for this lab are not met");
			m_printer.print("\nThis means you are missing something to run the experiments.");
		}

	}

	protected void runStarting() {
		m_printer.print("\nStart");

	}

	protected void runFinishing() {
		m_printer.print("\nDone");

	}

	protected void checkExprimentsRunning() throws Exception {

		do {
			displayRuningExperiments();
			Thread.sleep(5000);
		} while (isExpStillRunning());

		displayRuningExperiments();
	}

	protected void startLab() {
		lab.start();
	}

	public void run() {

		try {
			runStarting();

			checkLibDependency();

			addExperimentsToQeue();

			startLab();

			checkExprimentsRunning();
			export();
			runFinishing();
		} catch (Exception e) {

		}
	}

	protected void export() {

	}

}
