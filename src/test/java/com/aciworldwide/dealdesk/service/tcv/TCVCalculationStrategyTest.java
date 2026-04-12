package com.aciworldwide.dealdesk.service.tcv;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.aciworldwide.dealdesk.model.tcv.ContingentRevenue;
import com.aciworldwide.dealdesk.model.tcv.ContractTerm;
import com.aciworldwide.dealdesk.model.tcv.DiscountsAndAdjustments;
import com.aciworldwide.dealdesk.model.tcv.TCVComponent;
import com.aciworldwide.dealdesk.model.tcv.TCVComponent.ComponentType;

@DisplayName("TCV Calculation Model Tests")
class TCVCalculationStrategyTest {

    // -------------------------------------------------------------------------
    // TCVCalculationStrategy.applyDiscounts (default method)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("applyDiscounts default method")
    class ApplyDiscounts {

        private TCVCalculationStrategy strategy;

        @BeforeEach
        void setUp() {
            // Minimal implementation to test the default method
            strategy = new TCVCalculationStrategy() {
                @Override
                public BigDecimal calculateTCV(
                        List<com.aciworldwide.dealdesk.model.tcv.TCVComponent> components,
                        com.aciworldwide.dealdesk.model.tcv.PricingModel pricingModel) {
                    return BigDecimal.ZERO;
                }

                @Override
                public BigDecimal calculateNPV(
                        com.aciworldwide.dealdesk.model.tcv.TCVCalculation calculation,
                        BigDecimal discountRate) {
                    return BigDecimal.ZERO;
                }

                @Override
                public void validateComponents(
                        List<com.aciworldwide.dealdesk.model.tcv.TCVComponent> components) {
                    // no-op for tests
                }
            };
        }

        @ParameterizedTest(name = "applyDiscounts({0}, {1}) = {2}")
        @CsvSource({
            "100.00, 0.10, 90.00",
            "200.00, 0.25, 150.00",
            "1000.00, 0.00, 1000.00",
            "500.00, 1.00, 0.00"
        })
        @DisplayName("applyDiscounts returns correct reduced amount")
        void applyDiscounts_ValidRate_ReturnsReducedAmount(
                BigDecimal amount, BigDecimal rate, BigDecimal expected) {
            BigDecimal result = strategy.applyDiscounts(amount, rate);
            assertThat(result).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("applyDiscounts with negative rate throws IllegalArgumentException")
        void applyDiscounts_NegativeRate_Throws() {
            assertThatThrownBy(() -> strategy.applyDiscounts(
                    new BigDecimal("100.00"), new BigDecimal("-0.01")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between 0 and 1");
        }

        @Test
        @DisplayName("applyDiscounts with rate > 1 throws IllegalArgumentException")
        void applyDiscounts_RateGreaterThanOne_Throws() {
            assertThatThrownBy(() -> strategy.applyDiscounts(
                    new BigDecimal("100.00"), new BigDecimal("1.01")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between 0 and 1");
        }
    }

    // -------------------------------------------------------------------------
    // TCVComponent.calculateValue
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("TCVComponent calculations")
    class TCVComponentCalculations {

        @Test
        @DisplayName("calculateValue returns quantity multiplied by unitPrice")
        void calculateValue_ValidQuantityAndPrice_ReturnsProduct() {
            TCVComponent component = new TCVComponent();
            component.setQuantity(new BigDecimal("3"));
            component.setUnitPrice(new BigDecimal("1500.00"));

            assertThat(component.calculateValue()).isEqualByComparingTo("4500.00");
        }

        @Test
        @DisplayName("calculateValue returns ZERO when quantity is null")
        void calculateValue_NullQuantity_ReturnsZero() {
            TCVComponent component = new TCVComponent();
            component.setUnitPrice(new BigDecimal("1000.00"));

            assertThat(component.calculateValue()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("calculateValue returns ZERO when unitPrice is null")
        void calculateValue_NullUnitPrice_ReturnsZero() {
            TCVComponent component = new TCVComponent();
            component.setQuantity(new BigDecimal("5"));

            assertThat(component.calculateValue()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("calculateValue returns ZERO when both fields are null")
        void calculateValue_BothNull_ReturnsZero() {
            TCVComponent component = new TCVComponent();
            assertThat(component.calculateValue()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("TCVComponent stores all component types correctly")
        void componentType_AllTypesAvailable() {
            assertThat(ComponentType.values()).containsExactlyInAnyOrder(
                    ComponentType.PRODUCT,
                    ComponentType.SERVICE,
                    ComponentType.SUBSCRIPTION,
                    ComponentType.IMPLEMENTATION,
                    ComponentType.MAINTENANCE,
                    ComponentType.SUPPORT);
        }
    }

    // -------------------------------------------------------------------------
    // DiscountsAndAdjustments
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DiscountsAndAdjustments validation")
    class DiscountsAndAdjustmentsTests {

        @Test
        @DisplayName("validate succeeds with no discounts set")
        void validate_NoDiscounts_Succeeds() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder().build();
            assertThatCode(discounts::validate).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validate with valid volume discount percentage succeeds")
        void validate_ValidVolumeDiscountPercentage_Succeeds() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder()
                    .volumeDiscountPercentage(new BigDecimal("15"))
                    .build();
            assertThatCode(discounts::validate).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validate with negative volume discount percentage throws")
        void validate_NegativeVolumeDiscountPercentage_Throws() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder()
                    .volumeDiscountPercentage(new BigDecimal("-5"))
                    .build();
            assertThatThrownBy(discounts::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between 0 and 100");
        }

        @Test
        @DisplayName("validate with volume discount percentage > 100 throws")
        void validate_VolumeDiscountOver100_Throws() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder()
                    .volumeDiscountPercentage(new BigDecimal("101"))
                    .build();
            assertThatThrownBy(discounts::validate)
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("validate with price adjustment but no reason throws")
        void validate_PriceAdjustmentWithoutReason_Throws() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder()
                    .priceAdjustmentAmount(new BigDecimal("500.00"))
                    .priceAdjustmentEffectiveDate(LocalDate.now())
                    .build();
            assertThatThrownBy(discounts::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reason");
        }

        @Test
        @DisplayName("validate with price adjustment but no date throws")
        void validate_PriceAdjustmentWithoutDate_Throws() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder()
                    .priceAdjustmentAmount(new BigDecimal("500.00"))
                    .priceAdjustmentReason("Special pricing")
                    .build();
            assertThatThrownBy(discounts::validate)
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("validate with approved discount but no approver throws")
        void validate_ApprovedWithoutApprover_Throws() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder()
                    .approved(true)
                    .approvedDate(LocalDate.now())
                    .build();
            assertThatThrownBy(discounts::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Approver");
        }

        @Test
        @DisplayName("validate with approved discount but no approval date throws")
        void validate_ApprovedWithoutDate_Throws() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder()
                    .approved(true)
                    .approvedBy("manager")
                    .build();
            assertThatThrownBy(discounts::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("date");
        }

        @Test
        @DisplayName("getTotalDiscountAmount sums all discount amounts")
        void getTotalDiscountAmount_SumsAllDiscounts() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder()
                    .volumeDiscountAmount(new BigDecimal("100.00"))
                    .earlyPaymentDiscountAmount(new BigDecimal("50.00"))
                    .otherDiscountAmount(new BigDecimal("25.00"))
                    .build();

            assertThat(discounts.getTotalDiscountAmount()).isEqualByComparingTo("175.00");
        }

        @Test
        @DisplayName("getTotalDiscountAmount returns ZERO when no discounts set")
        void getTotalDiscountAmount_NoDiscounts_ReturnsZero() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder().build();
            assertThat(discounts.getTotalDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("getTotalAdjustmentAmount returns adjustment when set")
        void getTotalAdjustmentAmount_WithAdjustment_ReturnsAmount() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder()
                    .priceAdjustmentAmount(new BigDecimal("200.00"))
                    .build();
            assertThat(discounts.getTotalAdjustmentAmount()).isEqualByComparingTo("200.00");
        }

        @Test
        @DisplayName("getTotalAdjustmentAmount returns ZERO when no adjustment set")
        void getTotalAdjustmentAmount_NoAdjustment_ReturnsZero() {
            DiscountsAndAdjustments discounts = DiscountsAndAdjustments.builder().build();
            assertThat(discounts.getTotalAdjustmentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // -------------------------------------------------------------------------
    // ContingentRevenue
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ContingentRevenue validation")
    class ContingentRevenueTests {

        @Test
        @DisplayName("validate succeeds with minimal valid data")
        void validate_MinimalValidData_Succeeds() {
            ContingentRevenue revenue = ContingentRevenue.builder()
                    .amount(new BigDecimal("50000.00"))
                    .triggerDate(LocalDate.now().plusMonths(6))
                    .build();
            assertThatCode(revenue::validate).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validate throws when amount is null")
        void validate_NullAmount_Throws() {
            ContingentRevenue revenue = ContingentRevenue.builder()
                    .triggerDate(LocalDate.now().plusMonths(3))
                    .build();
            assertThatThrownBy(revenue::validate).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("validate throws when amount is zero")
        void validate_ZeroAmount_Throws() {
            ContingentRevenue revenue = ContingentRevenue.builder()
                    .amount(BigDecimal.ZERO)
                    .triggerDate(LocalDate.now().plusMonths(3))
                    .build();
            assertThatThrownBy(revenue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("greater than zero");
        }

        @Test
        @DisplayName("validate throws when triggerDate is null")
        void validate_NullTriggerDate_Throws() {
            ContingentRevenue revenue = ContingentRevenue.builder()
                    .amount(new BigDecimal("1000.00"))
                    .build();
            assertThatThrownBy(revenue::validate).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("validate throws when achieved but no achievedDate")
        void validate_AchievedWithoutDate_Throws() {
            ContingentRevenue revenue = ContingentRevenue.builder()
                    .amount(new BigDecimal("1000.00"))
                    .triggerDate(LocalDate.now().minusMonths(1))
                    .achieved(true)
                    .build();
            assertThatThrownBy(revenue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Achieved date");
        }

        @Test
        @DisplayName("validate throws when achievedDate is before triggerDate")
        void validate_AchievedDateBeforeTrigger_Throws() {
            LocalDate triggerDate = LocalDate.now().minusMonths(1);
            ContingentRevenue revenue = ContingentRevenue.builder()
                    .amount(new BigDecimal("1000.00"))
                    .triggerDate(triggerDate)
                    .achieved(true)
                    .achievedDate(triggerDate.minusDays(1))
                    .build();
            assertThatThrownBy(revenue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("before trigger date");
        }

        @Test
        @DisplayName("isVerified returns true when verificationStatus is VERIFIED")
        void isVerified_WhenStatusIsVerified_ReturnsTrue() {
            ContingentRevenue revenue = ContingentRevenue.builder()
                    .amount(new BigDecimal("1000.00"))
                    .triggerDate(LocalDate.now())
                    .verificationStatus("VERIFIED")
                    .build();
            assertThat(revenue.isVerified()).isTrue();
            assertThat(revenue.isPending()).isFalse();
            assertThat(revenue.isRejected()).isFalse();
        }

        @Test
        @DisplayName("isPending returns true when verificationStatus is PENDING")
        void isPending_WhenStatusIsPending_ReturnsTrue() {
            ContingentRevenue revenue = ContingentRevenue.builder()
                    .amount(new BigDecimal("1000.00"))
                    .triggerDate(LocalDate.now())
                    .verificationStatus("PENDING")
                    .build();
            assertThat(revenue.isPending()).isTrue();
            assertThat(revenue.isVerified()).isFalse();
        }

        @Test
        @DisplayName("isRejected returns true when verificationStatus is REJECTED")
        void isRejected_WhenStatusIsRejected_ReturnsTrue() {
            ContingentRevenue revenue = ContingentRevenue.builder()
                    .amount(new BigDecimal("1000.00"))
                    .triggerDate(LocalDate.now())
                    .verificationStatus("REJECTED")
                    .build();
            assertThat(revenue.isRejected()).isTrue();
        }

        @Test
        @DisplayName("includedInTCV defaults to false")
        void includedInTCV_Default_IsFalse() {
            ContingentRevenue revenue = ContingentRevenue.builder()
                    .amount(new BigDecimal("1000.00"))
                    .triggerDate(LocalDate.now())
                    .build();
            assertThat(revenue.isIncludedInTCV()).isFalse();
        }
    }

    // -------------------------------------------------------------------------
    // ContractTerm
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ContractTerm validation and calculations")
    class ContractTermTests {

        @Test
        @DisplayName("validate succeeds with valid contract term")
        void validate_ValidContractTerm_Succeeds() {
            ContractTerm term = ContractTerm.builder()
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .monthlyValue(new BigDecimal("5000.00"))
                    .build();
            assertThatCode(term::validate).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validate throws when startDate is null")
        void validate_NullStartDate_Throws() {
            ContractTerm term = ContractTerm.builder()
                    .endDate(LocalDate.now().plusYears(1))
                    .monthlyValue(new BigDecimal("5000.00"))
                    .build();
            assertThatThrownBy(term::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("required");
        }

        @Test
        @DisplayName("validate throws when endDate is before startDate")
        void validate_EndDateBeforeStartDate_Throws() {
            ContractTerm term = ContractTerm.builder()
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().minusDays(1))
                    .monthlyValue(new BigDecimal("5000.00"))
                    .build();
            assertThatThrownBy(term::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be after end date");
        }

        @Test
        @DisplayName("validate throws when monthlyValue is zero")
        void validate_ZeroMonthlyValue_Throws() {
            ContractTerm term = ContractTerm.builder()
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .monthlyValue(BigDecimal.ZERO)
                    .build();
            assertThatThrownBy(term::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("greater than zero");
        }

        @Test
        @DisplayName("validate throws when early termination is allowed but fee is null")
        void validate_EarlyTerminationWithoutFee_Throws() {
            ContractTerm term = ContractTerm.builder()
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .monthlyValue(new BigDecimal("5000.00"))
                    .earlyTerminationAllowed(true)
                    .earlyTerminationFee(null)
                    .build();
            assertThatThrownBy(term::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("fee");
        }

        @Test
        @DisplayName("validate succeeds when early termination allowed with valid fee")
        void validate_EarlyTerminationWithFee_Succeeds() {
            ContractTerm term = ContractTerm.builder()
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .monthlyValue(new BigDecimal("5000.00"))
                    .earlyTerminationAllowed(true)
                    .earlyTerminationFee(new BigDecimal("10000.00"))
                    .build();
            assertThatCode(term::validate).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("calculateValues computes durationMonths and totalValue")
        void calculateValues_ValidDates_CalculatesCorrectly() {
            ContractTerm term = ContractTerm.builder()
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 7, 1))
                    .monthlyValue(new BigDecimal("1000.00"))
                    .build();

            term.calculateValues();

            assertThat(term.getDurationMonths()).isGreaterThan(0);
            assertThat(term.getTotalValue()).isNotNull().isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("earlyTerminationAllowed defaults to false")
        void earlyTerminationAllowed_Default_IsFalse() {
            ContractTerm term = ContractTerm.builder().build();
            assertThat(term.isEarlyTerminationAllowed()).isFalse();
        }
    }
}
