package ca.uqac.lif.labpal.plot;

/**
 * Manages basic properties of 2D plots.
 * @author Sylvain Hallé
 */
public interface TwoDimensionalPlot
{
	/**
	 * The two axes of a 2D plot
	 */
	public static enum Axis {X, Y};
	
	/**
	 * Sets the caption for one of the axes 
	 * @param axis The axis
	 * @param caption The caption
	 * @return This plot
	 */
	public TwoDimensionalPlot setCaption(Axis axis, String caption);
	
	/**
	 * Sets whether to use a log scale for one of the axes 
	 * @param axis The axis
	 * @return This plot
	 */
	public TwoDimensionalPlot setLogscale(Axis axis);
}
