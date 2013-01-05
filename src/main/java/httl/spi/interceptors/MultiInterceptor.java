package httl.spi.interceptors;

import httl.Context;
import httl.spi.Interceptor;
import httl.spi.Rendition;

import java.io.IOException;
import java.text.ParseException;

public class MultiInterceptor implements Interceptor {

	private static final String RENDITION_KEY = "__rendition__";

	private Rendition interceptorRendition;

	public void setInterceptors(Interceptor[] interceptors) {
		Rendition last = null;
		for (int i = interceptors.length - 1; i >= 0; i--) {
			final Interceptor current = interceptors[i];
			final Rendition next = last;
			last = new Rendition() {
				public void render(Context context) throws IOException, ParseException {
					if (next == null) {
						Rendition rendition = (Rendition) context.get(RENDITION_KEY);
						if (rendition != null) {
							current.render(context, rendition);
						}
					} else {
						current.render(context, next);
					}
				}
			};
		}
		this.interceptorRendition = last;
	}

	public void render(Context context, Rendition rendition)
			throws IOException, ParseException {
		if (interceptorRendition != null) {
			Object old = context.put(RENDITION_KEY, rendition);
			try {
				interceptorRendition.render(context);
			} finally {
				if ( old != null) {
					context.put(RENDITION_KEY, old);
				} else {
					context.remove(RENDITION_KEY);
				}
			}
		} else {
			rendition.render(context);
		}
	}

}
