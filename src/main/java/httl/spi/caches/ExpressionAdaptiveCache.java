package httl.spi.caches;

public class ExpressionAdaptiveCache<K, V> extends AdaptiveCache<K, V> {
	
	public void setExpressionCacheCapacity(int capacity) {
		super.setCacheCapacity(capacity);
	}

}
