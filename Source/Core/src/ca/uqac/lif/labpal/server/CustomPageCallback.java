package ca.uqac.lif.labpal.server;

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

public class CustomPageCallback extends TemplatePageCallback
{
  public CustomPageCallback(String prefix, Laboratory lab, LabAssistant assistant)
  {
    super(prefix, lab, assistant);
    m_filename = s_path + "/custom.html";
  }
}
