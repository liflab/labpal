package ca.uqac.lif.labpal.plot;

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
}
