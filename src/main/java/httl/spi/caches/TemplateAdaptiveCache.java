package httl.spi.caches;

public class TemplateAdaptiveCache<K, V> extends AdaptiveCache<K, V> {
	
	public void setTemplateCacheCapacity(int capacity) {
		super.setCacheCapacity(capacity);
	}

}
