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
		if (valueMap != null) {
			Object value = valueMap.get(key);
			if (value != null) {
				return value.getClass();
			}
			value = valueMap.get(key + ".class");
			if (value instanceof Class) {
				return (Class<?>) value;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Set<String> keySet() {
		return valueMap == null ? Collections.EMPTY_SET : valueMap.keySet();
	}

}
