package ca.uqac.lif.export;

import java.util.List;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.config.Config;
import ca.uqac.lif.labpal.server.LabPalServer;
import ca.uqac.lif.labpal.server.WebCallback;
import ca.uqac.lif.tui.AnsiPrinter;

public class LabpalStandalone extends LabpalPlatform implements IPlatform {
	LabPalServer server = null;

	public LabpalStandalone(Laboratory m_lab, LabAssistant m_assistant, AnsiPrinter m_printer,
			List<WebCallback> callbacks) {
		super(m_lab, m_assistant, m_printer);
		config();
		this.server = new LabPalServer(null, m_lab, m_assistant);
		if (callbacks != null) {
			for (WebCallback cb : callbacks) {
				this.server.registerCallback(0, cb);
			}
		}

	}

	@Override
	protected void config() {
		FileManager.mkdir(Config.getProp("pathOutput"));
	}
	@Override
	public void export() {
		String path = Config.getProp("pathOutput") + Config.getProp("zipName") + ".zip";
		FileManager.mkdir(Config.getProp("pathOutput"));
		server.exportToStaticHtml(path);

	}

}
