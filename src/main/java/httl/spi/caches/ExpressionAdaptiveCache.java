package httl.spi.caches;

/**
 * ExpressionAdaptiveCache. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setExpressionCache(java.util.Map)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ExpressionAdaptiveCache<K, V> extends AdaptiveCache<K, V> {

	/**
	 * httl.properties: expression.cache.capacity=1000
	 */
	public void setExpressionCacheCapacity(int capacity) {
		super.setCacheCapacity(capacity);
	}

}
