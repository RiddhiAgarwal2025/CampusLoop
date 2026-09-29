package com.campusloop.service;

/**
 * The single, transparent CampusLoop Impact Point rule.
 *
 * Every COMPLETED resale awards a fixed number of points to BOTH the buyer and
 * the seller, because both chose reuse over buying new. Points are awarded in
 * the same database transaction as the payment and are never random.
 *
 * Points measure participation in reuse on campus. They are NOT a measurement
 * of carbon, waste or any other environmental quantity.
 */
public final class ImpactRules {

    public static final int POINTS_PER_COMPLETED_RESALE = 10;

    public static final String RULE_SUMMARY =
            "Every completed resale earns " + POINTS_PER_COMPLETED_RESALE
            + " Impact Points for the buyer and " + POINTS_PER_COMPLETED_RESALE + " for the seller.";

    private ImpactRules() {
    }
}
