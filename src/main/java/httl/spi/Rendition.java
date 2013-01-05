package httl.spi;

import httl.Context;

import java.io.IOException;
import java.text.ParseException;

public interface Rendition {

	void render(Context context) throws IOException, ParseException;

}
