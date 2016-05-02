package ca.uqac.lif.parkbench;

public abstract class TwoDeePlot extends Plot
{
	/**
	 * The underlying table of values for this plot
	 */
	protected Table m_table;
	
	/**
	 * The label used for the x-axis
	 */
	protected String m_xLabel;
	
	/**
	 * The label used for the y-axis
	 */
	protected String m_yLabel;
	
	/**
	 * Whether the x-axis has a logarithmic scale
	 */
	protected boolean m_logscaleX = false;
	
	/**
	 * Whether the y-axis has a logarithmic scale
	 */
	protected boolean m_logscaleY = false;
	
	/**
	 * Creates an empty 2D plot
	 */
	public TwoDeePlot()
	{
		super();
		m_table = new Table();
	}
	
	@Override
	public TwoDeePlot add(Experiment e)
	{
		m_table.addExperiment(e);
		return this;
	}
	
	/**
	 * Tells the plot to draw the x axis with a logarithmic scale
	 * @return This plot
	 */
	public TwoDeePlot setLogscaleX()
	{
		m_logscaleX = true;
		return this;
	}
	
	/**
	 * Tells the plot to draw the y axis with a logarithmic scale
	 * @return This plot
	 */
	public TwoDeePlot setLogscaleY()
	{
		m_logscaleY = true;
		return this;
	}
	
	/**
	 * Tells the plot to group experiment results into data series, according
	 * to an input parameter present in the experiments
	 * @param param The input parameters in an experiment used to determine
	 * to which data series it belongs
	 * @return This plot
	 */
	public TwoDeePlot groupBy(String ... param)
	{
		m_table.groupBy(param);
		return this;
	}
	
	/**
	 * Tells the plot what input parameter of the experiments to use as the
	 * "x" value 
	 * @param param The output parameter to use for the "x" value
	 * @param label The label for the x axis in the resulting plot
	 * @return This plot
	 */
	public TwoDeePlot useForX(String param, String label)
	{
		m_table.useForX(param);
		m_xLabel = label;
		return this;
	}
	
	/**
	 * Tells the plot what input parameter of the experiments to use as the
	 * "x" value 
	 * @param param The output parameter to use for the "y" value
	 * @param label The label for the y axis in the resulting plot
	 * @return This plot
	 */
	public TwoDeePlot useForY(String param, String label)
	{
		m_table.useForY(param);
		m_yLabel = label;
		return this;
	}
	
	@Override
	public StringBuilder getHeader(Terminal term)
	{
		StringBuilder out = super.getHeader(term);
		out.append("set xlabel \"").append(m_xLabel).append("\"\n");
		out.append("set ylabel \"").append(m_yLabel).append("\"\n");
		return out;
	}

}
