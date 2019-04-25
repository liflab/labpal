package ca.uqac.lif.labpal.test;

import static org.junit.Assert.assertNotNull;

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.labpal.Laboratory;
import org.junit.Test;

public class LaboratoryTest
{
  @Test
  public void testSave1() throws PrintException
  {
    Laboratory lab = new MyLab();
    String s = lab.saveToString();
    assertNotNull(s);
  }
  
  public static class MyLab extends Laboratory
  {

    @Override
    public void setup()
    {
      // TODO Auto-generated method stub
      
    }
    
  }
}
