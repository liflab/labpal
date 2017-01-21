package ca.uqac.lif.labpal.provenance;

import java.util.Set;

public interface DataOwner
{
	/**
	 * Gets an instance of the actual object who is encapsulated by this
	 * owner
	 * @return The object. Must not be null.
	 */
	public Object getOwner();
	
	/**
	 * Gets the current value of the data point with given ID
	 * @param id The ID
	 * @return A value
	 */
	public Object getValue(String id);
	
	/**
	 * For a given data point, gets the set of data points it depends on.
	 * @param id The data point
	 * @return A set of other data points
	 */
	public Set<String> dependsOn(String id);
}
