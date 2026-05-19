package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 1 – Specification-Based Testing (Chapter 2)
 *
 * <p>Target class: {@link PriceCalculator}
 *
 * <p>Your goal is to test {@code PriceCalculator.calculate(basePrice, discountRate, taxRate)}
 * using the domain testing technique from Chapter 2:
 * <ol>
 *   <li>Identify equivalence partitions for each input dimension.</li>
 *   <li>Identify boundary values between partitions (on-point / off-point).</li>
 *   <li>Write at least 10 meaningful test cases that cover both partitions and boundaries.</li>
 *   <li>Use {@code @ParameterizedTest} with {@code @CsvSource} for tests that share structure.</li>
 *   <li>Add a comment above each test method explaining which partition or boundary it covers.</li>
 * </ol>
 *
 * <h3>Input dimensions to consider</h3>
 * <ul>
 *   <li><b>basePrice</b>  – zero, positive, very large</li>
 *   <li><b>discountRate</b> – 0 (no discount), (0,100) typical, 100 (full discount)</li>
 *   <li><b>taxRate</b>    – 0 (no tax), (0,100) typical, 100 (100% tax)</li>
 * </ul>
 */
class PriceCalculatorSpecTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // partition - base price sifir
    @Test
    void zeroBasePrice() {
        assertThat(calculator.calculate(0, 20, 10)).isEqualTo(0);
    }

    // on-point boundary: discount 0, tax 0
    @Test
    void noDiscountNoTax() {
        double r = calculator.calculate(100, 0, 0);
        assertThat(r).isEqualTo(100.0);
    }

    // on-point boundary discount 100
    @Test
    void fullDiscount() {
        assertThat(calculator.calculate(100, 100, 0)).isEqualTo(0.0);
    }

    // on-point boundary tax 100
    @Test
    void taxMax() {
        assertThat(calculator.calculate(100, 0, 100)).isEqualTo(200.0);
    }

    // on-point tax 0
    @Test
    void taxZero() {
        assertThat(calculator.calculate(50, 20, 0)).isEqualTo(40.0);
    }

    // off-point - discount 1 (hemen ust sinir degil ama sifira yakin)
    @Test
    void discountOffPoint1() {
        double r = calculator.calculate(100, 1, 0);
        assertThat(r).isEqualTo(99.0);
    }

    // off-point discount 99
    @Test
    void discountOffPoint99() {
        assertThat(calculator.calculate(100, 99, 0)).isCloseTo(1.0, within(0.001));
    }

    // off-point tax 1
    @Test
    void taxOffPoint1() {
        double r = calculator.calculate(100, 0, 1);
        assertThat(r).isEqualTo(101.0);
    }

    // typical - elle hesapladim 100 10 20 -> 108
    @Test
    void testTypical108() {
        double result = calculator.calculate(100, 10, 20);
        assertThat(result).isCloseTo(108, within(0.01));
    }

    // partition pozitif base sadece vergi
    @Test
    void onlyTax() {
        assertThat(calculator.calculate(50, 0, 10)).isEqualTo(55.0);
    }

    // buyuk base price partition
    @Test
    void largeBase() {
        double result = calculator.calculate(10000, 5, 10);
        assertThat(result).isCloseTo(10450.0, within(1.0));
    }

    // invalid partition - negatif base (kod simdilik hata vermiyor task3te assert var)
    @Test
    void invalidNegativeBase() {
        double r = calculator.calculate(-100, 0, 0);
        assertThat(r).isLessThan(0);
    }

    // invalid partition discount > 100
    @Test
    void invalidDiscountTooHigh() {
        double r = calculator.calculate(100, 110, 0);
        assertThat(r).isLessThan(0);
    }

    // invalid tax negatif
    @Test
    void invalidNegativeTax() {
        double r = calculator.calculate(100, 0, -10);
        assertThat(r).isLessThan(100); // vergi negatif olunca dusuyor
    }

    // parameterized - birkac normal deger + off point
    @ParameterizedTest
    @CsvSource({
        "200.0, 0.0, 10.0, 220.0",
        "80.0, 25.0, 15.0, 69.0",
        "100.0, 50.0, 20.0, 60.0",
        "100.0, 1.0, 0.0, 99.0"
    })
    void typicalAndOffPoint(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(0.01));
    }

}
