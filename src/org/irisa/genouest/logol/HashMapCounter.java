package org.irisa.genouest.logol;

import java.util.HashMap;


/**
 * Hashmap of counters. If counter is not defined in hashmap, add it in list and initialize it to id*100
 * @author osallou
 *
 */
@SuppressWarnings("unchecked")
public class HashMapCounter extends HashMap {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8219255064282986430L;

	/**
	 * Override of superclass method to init the hashmap if key is not set 
	 */
	public Object get(Object obj) {
		Object data = super.get(obj);
		if(data==null) {
			Integer val = (Integer) obj;
			super.put(val, (val*100)+1);
			return (val*100)+1;
		}
		else { return data; }
	}
	
	public Object clone() {
		return super.clone();
	}

}
