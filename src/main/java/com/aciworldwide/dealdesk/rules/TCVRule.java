package com.aciworldwide.dealdesk.rules;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.TCVCalculation;

/**
 * Interface defining rules for Total Contract Value (TCV) calculations.
 */
public interface TCVRule {
    
    /**
     * Calculates the Total Contract Value for a given deal.
     *
     * @param deal The deal for which to calculate TCV
     * @return TCVCalculation object containing the calculation results
     */
    TCVCalculation calculateTCV(Deal deal);
}
