package ca.uqac.lif.labpal.provenance;

/**
 * An arbitrary function applied to a number of provenance nodes
 * @author Sylvain Hallé
 */
public interface NodeFunction
{
	/**
	 * The symbol for separating elements in a datapoint ID
	 */
	public static final String s_separator = ":";
	
	/**
	 * Generates a datapoint ID for this node
	 * @return A datapoint ID, or the empty string if no ID
	 *   can be generated
	 */
	public String getDataPointId();
	
	
	public NodeFunction dependsOn();

}
