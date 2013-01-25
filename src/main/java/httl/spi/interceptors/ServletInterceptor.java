package httl.spi.interceptors;

import httl.Context;
import httl.spi.Listener;
import httl.spi.resolvers.ServletResolver;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletInterceptor extends FirstInterceptor {

	@Override
	protected void doRender(Context context, Listener listener)
			throws IOException, ParseException {
		if (ServletResolver.getRequest() == null) {
			Object request = context.get("request");
			Object response = context.get("response");
			if (request instanceof HttpServletRequest
					&& response instanceof HttpServletResponse) {
				ServletResolver.set((HttpServletRequest) request, (HttpServletResponse) response);
				try {
					listener.render(context);
				} finally {
					ServletResolver.remove();
				}
				return;
			}
		}
		listener.render(context);
	}

}
