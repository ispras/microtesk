package ru.ispras.microtesk.model.api.mmu.tests;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LruTestCase <T1,T2>  {

	LruTestCase(int a)
	{}
	
  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGet() {
    int maxSize = 3;
    LruTestCase<String,String> cache = new LruTestCase<String,String>(maxSize);
    cache.put("1", "1");
    cache.put("2", "2");
    cache.put("3", "3");
    assertEquals(maxSize, cache.size());
    //cache.get("2");
    //cache.get("1");
    //System.out.println(cache.toString());
    cache.put("4", "4");
    cache.put("5", "5");
    //System.out.println(cache.toString());
    assertEquals(maxSize, cache.size());
  }

private Object size() {
	// TODO Auto-generated method stub
	return null;
}

private void put(String string, String string2) {
	// TODO Auto-generated method stub
	
}

}

   

