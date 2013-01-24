package httl.spi.interceptors;

import java.io.IOException;
import java.text.ParseException;

import httl.Context;
import httl.spi.Interceptor;
import httl.spi.Listener;

public abstract class FirstInterceptor implements Interceptor {

	public void render(Context context, Listener listener)
			throws IOException, ParseException {
		if (context.getLevel() > 1 // 只处理一级自动布局，防止递归
				|| context.getTemplate().isMacro()) { 
			listener.render(context);
			return;
		}
		doRender(context, listener);
	}

	protected abstract void doRender(Context context, Listener listener)
			throws IOException, ParseException;

}
