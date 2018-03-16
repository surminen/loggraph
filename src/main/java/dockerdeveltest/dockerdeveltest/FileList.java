package dockerdeveltest.dockerdeveltest;

import java.util.HashMap;
import java.util.Map;

public class FileList {

	private Map<String, Map<String, Object>> datamap = new HashMap<>();;

	public Map<String, Map<String, Object>> getDatamap() {
		return datamap;
	}

	public void addToMap(String key, HashMap<String, Object> value) {
		if(datamap.containsKey(key))
		{
			datamap.get(key).putAll(value);
		}
		else
		{
			datamap.put(key, value);
		}
	}
}