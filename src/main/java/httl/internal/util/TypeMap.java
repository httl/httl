package httl.internal.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class TypeMap extends MapSupport<String, Class<?>> {
	
	private final Map<String, Object> valueMap;

	public TypeMap(Map<String, Object> valueMap) {
		this.valueMap = valueMap;
	}

	public Class<?> get(Object key) {
		Object value = valueMap == null ? null : valueMap.get(key);
		return value == null ? null : value.getClass();
	}

	@SuppressWarnings("unchecked")
	public Set<String> keySet() {
		return valueMap == null ? Collections.EMPTY_SET : valueMap.keySet();
	}

}
