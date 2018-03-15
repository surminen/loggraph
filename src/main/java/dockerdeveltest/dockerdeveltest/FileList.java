package dockerdeveltest.dockerdeveltest;

import java.util.List;
import java.util.Map;

public class FileList {

	private final List<Map<String, Object>> data;

	public FileList(List<Map<String, Object>> _data) {
		this.data = _data;
	}

	public List<Map<String, Object>> getData() {
		return data;
	}
}