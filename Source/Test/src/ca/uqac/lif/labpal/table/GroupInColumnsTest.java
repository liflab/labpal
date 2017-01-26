package ca.uqac.lif.labpal.table;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.provenance.AggregateFunction;
import ca.uqac.lif.labpal.provenance.DirectValue;
import ca.uqac.lif.labpal.provenance.NodeFunction;

public class GroupInColumnsTest
{
	@Test
	public void testValues() 
	{
		DataTable in_table = DataTable.read(new Scanner(FileHelper.internalFileToStream(GroupInColumnsTest.class, "table2.csv")), ",");
		BoxTransformation bt = new BoxTransformation();
		TempTable tt = bt.transform(in_table.getDataTable());
		assertEquals(6, tt.getColumnCount());
		assertEquals(2, tt.getRowCount());		
	}

	@Test
	public void testProvenance1() 
	{
		DataTable in_table = DataTable.read(new Scanner(FileHelper.internalFileToStream(GroupInColumnsTest.class, "table2.csv")), ",");
		GroupInColumns bt = new GroupInColumns("Market", "Share");
		TempTable tt = bt.transform(in_table.getDataTable());
		NodeFunction nf = tt.dependsOn(0, 1);
		assertTrue(nf instanceof TableCellNode);
		TableCellNode tcn = (TableCellNode) nf;
		assertEquals(1, tcn.getCol());
		assertEquals(0, tcn.getRow());
		int parameter_column = in_table.getColumnPosition("Market");
		int value_column = in_table.getColumnPosition("Share");
		for (int row = 0; row < in_table.getRowCount(); row++)
		{
			NodeFunction nf_dep = tt.getDependency(row, 1);
			assertTrue(nf_dep instanceof DirectValue);
			DirectValue af = (DirectValue) nf_dep;
			List<NodeFunction> deps = af.getDependencyNodes();
			assertEquals(2, deps.size());
			{
				TableCellNode tcn_dep = new TableCellNode(in_table, row, parameter_column);
				if (!deps.contains(tcn_dep))
				{
					fail("Dependencies don't contain " + tcn_dep);
				}
			}
			{
				TableCellNode tcn_dep = new TableCellNode(in_table, row, value_column);
				if (!deps.contains(tcn_dep))
				{
					fail("Dependencies don't contain " + tcn_dep);
				}
			}
		}
	}
}
