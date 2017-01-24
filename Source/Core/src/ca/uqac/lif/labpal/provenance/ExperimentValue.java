package ca.uqac.lif.labpal.provenance;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Laboratory;

public class ExperimentValue implements NodeFunction 
{
	protected final Experiment m_experiment;
	
	protected final String m_parameter;
	
	protected final int m_position;
	
	public ExperimentValue(Experiment e, String key)
	{
		this(e, key, -1);
	}
	
	public ExperimentValue(Experiment e, String key, int position)
	{
		super();
		m_experiment = e;
		m_parameter = key;
		m_position = position;
	}
	
	@Override
	public String toString()
	{
		if (m_position < 0)
		{
			return "Value of " + m_parameter + " in Experiment #" + m_experiment.getId();
		}
		else
		{
			return "Value of " + m_parameter + "[" + m_position + "] in Experiment #" + m_experiment.getId();
		}
	}
	
	@Override
	public String getDataPointId()
	{
		if (m_position < 0)
		{
			return "E" + m_experiment.getId() + s_separator + m_parameter;
		}
		else
		{
			return "E" + m_experiment.getId() + s_separator + m_parameter + s_separator + m_position;
		}
	}
	
	public Experiment getOwner()
	{
		return m_experiment;
	}
	
	public static NodeFunction dependsOn(Experiment e, String datapoint_id)
	{
		// Parse the datapoint ID and call the experiment on the extracted values
		String[] parts = datapoint_id.split(NodeFunction.s_separator);
		if (parts.length >= 2)
		{
			int id = Integer.parseInt(parts[0].substring(1).trim());
			if (id != e.getId())
			{
				// Wrong experiment
				return null;
			}
			String param = parts[1].trim();
			if (parts.length == 2)
			{
				return e.dependsOnKey(param);
			}
			if (parts.length == 3)
			{
				int pos = Integer.parseInt(parts[2].trim());
				return e.dependsOnCell(param, pos);
			}
		}
		return null;
	}
	
	/**
	 * Gets the owner of a datapoint
	 * @param lab
	 * @param datapoint_id
	 * @return The owner, or {@code null} if this object could not
	 * find the owner
	 */
	public static Experiment getOwner(Laboratory lab, String datapoint_id)
	{
		if (!datapoint_id.startsWith("E"))
			return null;
		String[] parts = datapoint_id.split(NodeFunction.s_separator);
		int id = Integer.parseInt(parts[0].substring(1).trim());
		return lab.getExperiment(id);
	}
}
