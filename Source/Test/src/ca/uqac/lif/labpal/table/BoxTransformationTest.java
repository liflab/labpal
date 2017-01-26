package ca.uqac.lif.labpal.table;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.provenance.AggregateFunction;
import ca.uqac.lif.labpal.provenance.NodeFunction;

public class BoxTransformationTest
{
	@Test
	public void testValues() 
	{
		DataTable in_table = DataTable.read(new Scanner(FileHelper.internalFileToStream(BoxTransformationTest.class, "table1.csv")), ",");
		BoxTransformation bt = new BoxTransformation();
		TempTable tt = bt.transform(in_table.getDataTable());
		assertEquals(6, tt.getColumnCount());
		assertEquals(2, tt.getRowCount());		
	}

	@Test
	public void testProvenance1() 
	{
		DataTable in_table = DataTable.read(new Scanner(FileHelper.internalFileToStream(BoxTransformationTest.class, "table1.csv")), ",");
		BoxTransformation bt = new BoxTransformation();
		TempTable tt = bt.transform(in_table.getDataTable());
		NodeFunction nf = tt.dependsOn(0, 1);
		assertTrue(nf instanceof TableCellNode);
		TableCellNode tcn = (TableCellNode) nf;
		assertEquals(1, tcn.getCol());
		assertEquals(0, tcn.getRow());
		NodeFunction nf_dep = tt.getDependency(0, 1);
		assertTrue(nf_dep instanceof AggregateFunction);
		AggregateFunction af = (AggregateFunction) nf_dep;
		List<NodeFunction> deps = af.getDependencyNodes();
		assertEquals(in_table.getRowCount(), deps.size());
		for (int i = 0; i < in_table.getRowCount(); i++)
		{
			TableCellNode tcn_dep = new TableCellNode(in_table, i, 0);
			if (!deps.contains(tcn_dep))
			{
				fail("Dependencies don't contain " + tcn_dep);
			}
		}
	}
	
	@Test
	public void testProvenance2() 
	{
		DataTable in_table = DataTable.read(new Scanner(FileHelper.internalFileToStream(BoxTransformationTest.class, "table1.csv")), ",");
		BoxTransformation bt = new BoxTransformation();
		TransformedTable trans_t = new TransformedTable(bt, in_table);
		TempTable tt = trans_t.getDataTable();
		NodeFunction nf = tt.dependsOn(0, 1);
		assertTrue(nf instanceof TableCellNode);
		TableCellNode tcn = (TableCellNode) nf;
		assertEquals(1, tcn.getCol());
		assertEquals(0, tcn.getRow());
		NodeFunction nf_dep = tt.getDependency(0, 1);
		assertTrue(nf_dep instanceof AggregateFunction);
		AggregateFunction af = (AggregateFunction) nf_dep;
		List<NodeFunction> deps = af.getDependencyNodes();
		assertEquals(in_table.getRowCount(), deps.size());
		for (int i = 0; i < in_table.getRowCount(); i++)
		{
			TableCellNode tcn_dep = new TableCellNode(in_table, i, 0);
			if (!deps.contains(tcn_dep))
			{
				fail("Dependencies don't contain " + tcn_dep);
			}
		}
	}
}
