package com.bs.tool_package;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class FastJSON {

     public static List<Map<String, String>> parseJSON2ListString(String jsonStr)
     {  

        JSONArray jsonArr = JSONArray.fromObject(jsonStr);  

        List<Map<String, String>> list = new ArrayList<Map<String,String>>();  

        
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> it = jsonArr.iterator();  

        while(it.hasNext()){  

            JSONObject json2 = it.next();  

            list.add(parseJSON2MapString(json2.toString()));  

        }  

        return list;  

    }


	public static Map<String, String> parseJSON2MapString(String jsonStr)
	{  
	
	   Map<String, String> map = new HashMap<String, String>();  
	
	  
	   JSONObject json = JSONObject.fromObject(jsonStr);  
	
	   for(Object k : json.keySet()){ 
	
	       Object v = json.get(k);   
	
	       if(null!=v)
	    	   map.put(k.toString(), v.toString());        
	
	   }  
	
	   return map;  
	
	}

}
