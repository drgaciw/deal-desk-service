package com.aciworldwide.dealdesk.rules.impl;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.exception.InvalidDealStateException;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Component
public class DealStatusRules {

    @Rule(name = "submit-deal", description = "Rules for submitting a deal", priority = 1)
    public static class SubmitDealRule {
        
        @Condition
        public boolean evaluate(
                @Fact("deal") Deal deal,
                @Fact("targetStatus") DealStatus targetStatus) {
            return targetStatus == DealStatus.SUBMITTED &&
                   deal.getStatus() == DealStatus.DRAFT;
        }

        @Action
        public void execute(@Fact("deal") Deal deal) {
            deal.setStatus(DealStatus.SUBMITTED);
            log.info("Deal {} transitioned to SUBMITTED state", deal.getId());
        }
    }

    @Rule(name = "approve-deal", description = "Rules for approving a deal", priority = 1)
    public static class ApproveDealRule {
        
        @Condition
        public boolean evaluate(
                @Fact("deal") Deal deal,
                @Fact("targetStatus") DealStatus targetStatus,
                @Fact("approverUserId") String approverUserId) {
            return targetStatus == DealStatus.APPROVED &&
                   deal.getStatus() == DealStatus.SUBMITTED &&
                   approverUserId != null;
        }

        @Action
        public void execute(
                @Fact("deal") Deal deal,
                @Fact("approverUserId") String approverUserId) {
            deal.setStatus(DealStatus.APPROVED);
            deal.setApprovedBy(approverUserId);
            deal.setApprovedAt(ZonedDateTime.now(ZoneId.systemDefault()));
            log.info("Deal {} approved by user {}", deal.getId(), approverUserId);
        }
    }

    @Rule(name = "reject-deal", description = "Rules for rejecting a deal", priority = 1)
    public static class RejectDealRule {
        
        @Condition
        public boolean evaluate(
                @Fact("deal") Deal deal,
                @Fact("targetStatus") DealStatus targetStatus,
                @Fact("rejectorUserId") String rejectorUserId,
                @Fact("reason") String reason) {
            return targetStatus == DealStatus.REJECTED &&
                   deal.getStatus() == DealStatus.SUBMITTED &&
                   rejectorUserId != null &&
                   reason != null && !reason.trim().isEmpty();
        }

        @Action
        public void execute(
                @Fact("deal") Deal deal,
                @Fact("reason") String reason) {
            deal.setStatus(DealStatus.REJECTED);
            deal.setNotes(reason);
            log.info("Deal {} rejected. Reason: {}", deal.getId(), reason);
        }
    }

    @Rule(name = "cancel-deal", description = "Rules for cancelling a deal", priority = 1)
    public static class CancelDealRule {
        
        @Condition
        public boolean evaluate(
                @Fact("deal") Deal deal,
                @Fact("targetStatus") DealStatus targetStatus,
                @Fact("reason") String reason) {
            return targetStatus == DealStatus.CANCELLED &&
                   deal.getStatus() != DealStatus.CLOSED_WON &&
                   deal.getStatus() != DealStatus.CLOSED_LOST &&
                   reason != null && !reason.trim().isEmpty();
        }

        @Action
        public void execute(
                @Fact("deal") Deal deal,
                @Fact("reason") String reason) {
            deal.setStatus(DealStatus.CANCELLED);
            deal.setNotes(reason);
            log.info("Deal {} cancelled. Reason: {}", deal.getId(), reason);
        }
    }

    @Rule(name = "validate-status-change", description = "Validates if status change is allowed", priority = 0)
    public static class ValidateStatusChangeRule {
        
        @Condition
        public boolean evaluate(
                @Fact("deal") Deal deal,
                @Fact("targetStatus") DealStatus targetStatus) {
            // Always evaluate this rule
            return true;
        }

        @Action
        public void execute(
                @Fact("deal") Deal deal,
                @Fact("targetStatus") DealStatus targetStatus) {
            // Check for invalid transitions
            if (deal.getStatus() == DealStatus.APPROVED && 
                targetStatus != DealStatus.CANCELLED) {
                throw new InvalidDealStateException(deal.getStatus(), targetStatus);
            }
            
            if ((deal.getStatus() == DealStatus.CLOSED_WON || 
                 deal.getStatus() == DealStatus.CLOSED_LOST) &&
                targetStatus != DealStatus.CANCELLED) {
                throw new InvalidDealStateException(deal.getStatus(), targetStatus);
            }
            
            log.info("Status change from {} to {} validated for deal {}", 
                    deal.getStatus(), targetStatus, deal.getId());
        }
    }
}