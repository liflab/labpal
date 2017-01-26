package ca.uqac.lif.labpal.table;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.provenance.DirectValue;
import ca.uqac.lif.labpal.provenance.NodeFunction;

public class ExpandAsColumnsTest
{
	@Test
	public void testValues() 
	{
		DataTable in_table = DataTable.read(new Scanner(FileHelper.internalFileToStream(ExpandAsColumnsTest.class, "table2.csv")), ",");
		BoxTransformation bt = new BoxTransformation();
		TempTable tt = bt.transform(in_table.getDataTable());
		assertEquals(6, tt.getColumnCount());
		assertEquals(2, tt.getRowCount());		
	}

	@Test
	public void testProvenance1() 
	{
		DataTable in_table = DataTable.read(new Scanner(FileHelper.internalFileToStream(ExpandAsColumnsTest.class, "table2.csv")), ",");
		ExpandAsColumns bt = new ExpandAsColumns("Market", "Share");
		TempTable tt = bt.transform(in_table.getDataTable());
		NodeFunction nf = tt.dependsOn(0, 1);
		assertTrue(nf instanceof TableCellNode);
		TableCellNode tcn = (TableCellNode) nf;
		assertEquals(1, tcn.getCol());
		assertEquals(0, tcn.getRow());
		int parameter_column = in_table.getColumnPosition("Market");
		int value_column = in_table.getColumnPosition("Share");
		NodeFunction nf_dep = tt.getDependency(0, 1);
		assertTrue(nf_dep instanceof DirectValue);
		DirectValue af = (DirectValue) nf_dep;
		List<NodeFunction> deps = af.getDependencyNodes();
		assertEquals(2, deps.size());
	}
}
