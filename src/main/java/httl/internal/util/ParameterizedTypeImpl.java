package httl.internal.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeImpl implements ParameterizedType {

	private Class<?> raw;

	private Class<?>[] arguments;

	public ParameterizedTypeImpl(Class<?> raw, Class<?>[] arguments) {
		super();
		this.raw = raw;
		this.arguments = arguments;
	}

	public Type[] getActualTypeArguments() {
		return arguments;
	}

	public Type getRawType() {
		return raw;
	}

	public Type getOwnerType() {
		return raw;
	}

}
