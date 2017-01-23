/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2017 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.labpal.provenance;

import java.util.ArrayList;
import java.util.List;

public class ProvenanceNode
{
	/**
	 * The owner of the data point
	 */
	protected final DataOwner m_owner;
	
	/**
	 * The ID of the data point
	 */
	protected final String m_dataPointId;
	
	/**
	 * A set of points this data point depends on
	 */
	protected final List<ProvenanceNode> m_parents;
	
	/**
	 * Creates a new provenance node
	 * @param datapoint_id The ID of the data point
	 * @param owner The owner of the data point
	 */
	public ProvenanceNode(String datapoint_id, DataOwner owner)
	{
		super();
		m_owner = owner;
		m_dataPointId = datapoint_id;
		m_parents = new ArrayList<ProvenanceNode>();
	}
	
	/**
	 * Adds a parent to this node
	 * @param p The parent
	 */
	public void addParent(ProvenanceNode p)
	{
		m_parents.add(p);
	}
	
	/**
	 * Replaces a parent node by another
	 * @param position The position where to perform the replacement. The
	 * operation is ignored is this value is out of bounds.
	 * @param p The parent
	 */
	public void replaceParent(int position, ProvenanceNode p)
	{
		m_parents.set(position, p);
	}
	
	public String getDataPointId()
	{
		return m_dataPointId;
	}
	
	public List<ProvenanceNode> getParents()
	{
		return m_parents;
	}
	
	@Override
	public String toString()
	{
		return m_dataPointId;
	}
}