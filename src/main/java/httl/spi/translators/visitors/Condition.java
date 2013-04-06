package httl.spi.translators.visitors;

public class Condition {

	private final boolean status;

	private final Object value;

	public Condition(boolean status, Object value) {
		this.status = status;
		this.value = value;
	}

	public boolean isStatus() {
		return status;
	}

	public Object getValue() {
		return value;
	}

}
