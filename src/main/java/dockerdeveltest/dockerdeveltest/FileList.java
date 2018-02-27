package dockerdeveltest.dockerdeveltest;

import java.util.List;
import java.util.Map;

public class FileList {

	private final List<Map<String, String>> data;

	public FileList(List<Map<String, String>> _data) {
		this.data = _data;
	}

	public List<Map<String, String>> getData() {
		return data;
	}
}