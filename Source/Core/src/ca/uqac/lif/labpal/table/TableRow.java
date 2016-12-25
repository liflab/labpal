package ca.uqac.lif.labpal.table;

import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.Row;

public class TableRow extends Row
{
	Object[] m_data;

	/**
	 * Dummy UID
	 */
	private static final long serialVersionUID = -8176898827781404921L;

	public TableRow(DataSource source, int row)
	{
		super(source, row);
	}	

}
