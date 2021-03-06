package org.stagemonitor.ehcache;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import net.sf.ehcache.statistics.CacheUsageListener;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.codahale.metrics.RatioGauge.Ratio;

public class StagemonitorCacheUsageListener implements CacheUsageListener {

	private final String metricPrefix;
	private final MetricRegistry registry;
	final String allCacheHitsMetricName;
	final String allCacheMissesMetricName;


	public StagemonitorCacheUsageListener(String metricPrefix, MetricRegistry registry) {
		this.metricPrefix = metricPrefix;
		this.registry = registry;
		allCacheHitsMetricName = name(metricPrefix, "access.hit.total");
		allCacheMissesMetricName = name(metricPrefix, "access.miss.total");
	}

	@Override
	public void notifyStatisticsEnabledChanged(boolean enableStatistics) {
	}

	@Override
	public void notifyStatisticsCleared() {
	}

	@Override
	public void notifyCacheHitInMemory() {
		notifyAllCacheHits();
	}

	@Override
	public void notifyCacheHitOffHeap() {
		notifyAllCacheHits();
	}

	@Override
	public void notifyCacheHitOnDisk() {
		notifyAllCacheHits();
	}

	@Override
	public void notifyCacheElementPut() {
	}

	@Override
	public void notifyCacheElementUpdated() {
	}

	@Override
	public void notifyCacheMissedWithNotFound() {
		notifyAllCacheMisses();
	}

	@Override
	public void notifyCacheMissedWithExpired() {
		notifyAllCacheMisses();
	}

	private void notifyAllCacheHits() {
		registry.meter(allCacheHitsMetricName).mark();
	}

	private void notifyAllCacheMisses() {
		registry.meter(allCacheMissesMetricName).mark();
	}

	public Ratio getHitRatio1Min() {
		final Meter hitRate = registry.meter(allCacheHitsMetricName);
		final Meter missRate = registry.meter(allCacheMissesMetricName);
		final double oneMinuteHitRate = hitRate.getOneMinuteRate();
		return Ratio.of(oneMinuteHitRate, oneMinuteHitRate + missRate.getOneMinuteRate());
	}

	@Override
	public void notifyCacheMissInMemory() {
	}

	@Override
	public void notifyCacheMissOffHeap() {
	}

	@Override
	public void notifyCacheMissOnDisk() {
	}

	@Override
	@Deprecated
	public void notifyTimeTakenForGet(long millis) {
	}

	@Override
	public void notifyGetTimeNanos(long nanos) {
		registry.timer(name(metricPrefix, "get")).update(nanos, TimeUnit.NANOSECONDS);
	}

	@Override
	public void notifyCacheElementEvicted() {
		registry.meter(name(metricPrefix, "delete", "eviction")).mark();
	}

	@Override
	public void notifyCacheElementExpired() {
		registry.meter(name(metricPrefix, "delete", "expire")).mark();
	}

	@Override
	public void notifyCacheElementRemoved() {
		registry.meter(name(metricPrefix, "delete", "remove")).mark();
	}

	@Override
	public void notifyRemoveAll() {
	}

	@Override
	public void notifyStatisticsAccuracyChanged(int statisticsAccuracy) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void notifyCacheSearch(long executeTime) {
	}

	@Override
	public void notifyXaCommit() {
	}

	@Override
	public void notifyXaRollback() {
	}
}
