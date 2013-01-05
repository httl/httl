package httl.spi.interceptors;

import java.io.IOException;
import java.text.ParseException;

import httl.Context;
import httl.spi.Interceptor;
import httl.spi.Rendition;

public class RequestInterceptor implements Interceptor {

	public void render(Context context, Rendition rendition)
			throws IOException, ParseException {
		context.get("request");
		rendition.render(context);
	}

}
