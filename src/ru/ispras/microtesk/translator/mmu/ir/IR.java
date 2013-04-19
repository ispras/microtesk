package ru.ispras.microtesk.translator.mmu.ir;

import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.shared.LetExpr;

public final class IR
{
	private Map<String, AssociativityExpr>    associativity = new LinkedHashMap<String, AssociativityExpr>();
	@SuppressWarnings({ "rawtypes", "unused" })
	private Map<String, BufferExpr> buffer = new LinkedHashMap<String, BufferExpr>();
	private Map<String, SetsExpr> set = new LinkedHashMap<String, SetsExpr>();
	private Map<String, LineExpr> line = new LinkedHashMap<String, LineExpr>();
    private Map<String, IndexExpr> index = new LinkedHashMap<String, IndexExpr>();
	private Map<String, EPolicyType> policy = new LinkedHashMap<String, EPolicyType>();
	private Map<String, AddressExpr> address = new LinkedHashMap<String, AddressExpr>();
	private Map<String, MatchExpr> match = new LinkedHashMap<String, MatchExpr>();
	private Map<String, TagExpr> tag = new LinkedHashMap<String, TagExpr>();
	
	private Map<String, LetExpr>      lets = new LinkedHashMap<String, LetExpr>();

    public IR()
    {}
    
    public void add(String name, IndexExpr value)
    {
        index.put(name, value);
    }
 
	public void add(String name, AssociativityExpr value)
    {
        associativity.put(name, value);
    }

	public void add(String name, SetsExpr value)
    {
        set.put(name, value);
    }
    
	public void add(String name, LineExpr value)
    {
        line.put(name, value);
    }
    
    public void add(String name, EPolicyType value)
    {
        policy.put(name, value);
    }
	
    public void add(String name, AddressExpr value)
    {
        address.put(name, value);
    }
    
    public void add(String name, MatchExpr value) 
    {
    	match.put(name, value);
    }
    
	public void add(String name, TagExpr value) 
    {
    	tag.put(name,value);
	}  
    
    public void add(String name, LetExpr value)
    {
        lets.put(name, value);
    }
    
    public Map<String, LetExpr> getLets()
    {
        return lets;
    }
    
    public Map<String, EPolicyType> getPolicy()
    {
    	return policy;
    }
    
    public Map<String, AddressExpr> getAddress()
    {
    	return address;
    }

	public Map<String, LineExpr> getLine()
    {
    	return line;
    }

	public Map<String, TagExpr> getTag() 
    {
    	return tag;
	} 

	public void add(String name, Object dataExpr) 
	{}

	public void add(String name, @SuppressWarnings("rawtypes") BufferExpr bufferExpr) 
	{}
    
}


