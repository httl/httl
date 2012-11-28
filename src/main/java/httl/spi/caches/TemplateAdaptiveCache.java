package httl.spi.caches;

/**
 * TemplateAdaptiveCache. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setTemplateCache(java.util.Map)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class TemplateAdaptiveCache<K, V> extends AdaptiveCache<K, V> {

	/**
	 * httl.properties: template.cache.capacity=1000
	 */
	public void setTemplateCacheCapacity(int capacity) {
		super.setCacheCapacity(capacity);
	}

}
