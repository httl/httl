package httl.internal.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeImpl implements ParameterizedType {

	private Type owner;

	private Type raw;

	private Type[] arguments;

	public ParameterizedTypeImpl(Type raw, Type[] arguments) {
		this(raw, raw, arguments);
	}

	public ParameterizedTypeImpl(Type owner, Type raw, Type[] arguments) {
		super();
		this.owner = owner;
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
		return owner;
	}

}
