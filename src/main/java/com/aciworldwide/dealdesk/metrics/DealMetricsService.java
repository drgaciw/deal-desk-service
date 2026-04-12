package com.aciworldwide.dealdesk.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.repository.DealRepository;

/**
 * Central service that registers and records custom Micrometer metrics for deal operations.
 *
 * <p><b>Counters</b>
 * <ul>
 *   <li>{@code deals.created}  – incremented each time a new deal is created</li>
 *   <li>{@code deals.approved} – incremented each time a deal is approved</li>
 *   <li>{@code deals.rejected} – incremented each time a deal is rejected</li>
 * </ul>
 *
 * <p><b>Timers</b>
 * <ul>
 *   <li>{@code deals.tcv.calculation.duration}   – duration of TCV rule execution</li>
 *   <li>{@code deals.salesforce.sync.duration}   – duration of a Salesforce sync operation</li>
 * </ul>
 *
 * <p><b>Gauges</b>
 * <ul>
 *   <li>{@code deals.active} (tag: {@code status}) – live count of deals per {@link DealStatus},
 *       queried from the database each time Micrometer polls the gauge</li>
 * </ul>
 *
 * <p>All metrics are exposed through the standard Spring Boot Actuator
 * {@code /actuator/metrics} endpoint.
 */
@Service
public class DealMetricsService {

    private final Counter dealsCreatedCounter;
    private final Counter dealsApprovedCounter;
    private final Counter dealsRejectedCounter;
    private final Timer tcvCalculationTimer;
    private final Timer salesforceSyncTimer;
    private final MeterRegistry meterRegistry;

    public DealMetricsService(MeterRegistry meterRegistry, DealRepository dealRepository) {
        this.meterRegistry = meterRegistry;

        this.dealsCreatedCounter = Counter.builder("deals.created")
                .description("Total number of deals created")
                .register(meterRegistry);

        this.dealsApprovedCounter = Counter.builder("deals.approved")
                .description("Total number of deals approved")
                .register(meterRegistry);

        this.dealsRejectedCounter = Counter.builder("deals.rejected")
                .description("Total number of deals rejected")
                .register(meterRegistry);

        this.tcvCalculationTimer = Timer.builder("deals.tcv.calculation.duration")
                .description("Time spent executing TCV calculation rules")
                .register(meterRegistry);

        this.salesforceSyncTimer = Timer.builder("deals.salesforce.sync.duration")
                .description("Time spent synchronising a deal with Salesforce")
                .register(meterRegistry);

        for (DealStatus status : DealStatus.values()) {
            Gauge.builder("deals.active", dealRepository, repo -> repo.countByStatus(status))
                    .tag("status", status.name())
                    .description("Number of deals in a given status (live count)")
                    .register(meterRegistry);
        }
    }

    /** Increments the {@code deals.created} counter. */
    public void recordDealCreated() {
        dealsCreatedCounter.increment();
    }

    /** Increments the {@code deals.approved} counter. */
    public void recordDealApproved() {
        dealsApprovedCounter.increment();
    }

    /** Increments the {@code deals.rejected} counter. */
    public void recordDealRejected() {
        dealsRejectedCounter.increment();
    }

    /**
     * Starts a {@link Timer.Sample} for measuring TCV calculation duration.
     *
     * @return a started sample that must be stopped via {@link #stopTcvCalculationTimer(Timer.Sample)}
     */
    public Timer.Sample startTcvCalculationTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stops the given TCV calculation timer sample and records the measurement.
     *
     * @param sample the sample returned by {@link #startTcvCalculationTimer()}
     */
    public void stopTcvCalculationTimer(Timer.Sample sample) {
        sample.stop(tcvCalculationTimer);
    }

    /**
     * Starts a {@link Timer.Sample} for measuring Salesforce sync duration.
     *
     * @return a started sample that must be stopped via {@link #stopSalesforceSyncTimer(Timer.Sample)}
     */
    public Timer.Sample startSalesforceSyncTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stops the given Salesforce sync timer sample and records the measurement.
     *
     * @param sample the sample returned by {@link #startSalesforceSyncTimer()}
     */
    public void stopSalesforceSyncTimer(Timer.Sample sample) {
        sample.stop(salesforceSyncTimer);
    }
}
