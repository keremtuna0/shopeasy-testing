package shopeasy;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 4 – Property-Based Testing (Chapter 5)
 *
 * <p>Target classes: {@link PriceCalculator}, {@link ShoppingCart}
 *
 * <p>Using jqwik, define and test at least <strong>3 distinct properties</strong>.
 * You must use at least one custom {@code @Provide} method.
 *
 * <h3>Suggested properties (you may use these or design your own)</h3>
 * <ul>
 *   <li><b>Monotonicity</b> – For any fixed base and tax, increasing the discount
 *       rate never increases the final price.</li>
 *   <li><b>Identity</b> – A 0% discount and 0% tax returns exactly the base price.</li>
 *   <li><b>Boundedness</b> – The result is always &gt;= 0.</li>
 *   <li><b>Cart commutativity</b> – Adding product A then B yields the same total
 *       as adding B then A.</li>
 *   <li><b>Discount transitivity</b> – Applying a 10% then another 10% discount via
 *       {@code applyDiscount} is equivalent to a single call with the compounded rate
 *       (think carefully: is this actually true for this implementation?).</li>
 * </ul>
 *
 * <h3>For each property, include a comment that answers:</h3>
 * <ol>
 *   <li>What does this property mean in plain English?</li>
 *   <li>What class of bugs would this property catch?</li>
 * </ol>
 *
 * <h3>If jqwik finds a failing case</h3>
 * Do not just fix the test. Investigate the root cause and explain it in your
 * reflection report (include the counterexample jqwik printed).
 */
class ShopEasyPropertyTest {

    // identity: indirim ve vergi 0 ise fiyat base ile ayni olmali
    // bug: formul yanlis yazilirsa base donmez
    @Property
    void identityNoDiscountNoTax(
            @ForAll @DoubleRange(min = 0, max = 5000) double base) {
        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(base, 0, 0);
        assertThat(result).isCloseTo(base, within(0.001));
    }

    // monotonicity: indirim artinca fiyat dusmeli veya esit kalmali
    // bug: indirim ters uygulanirsa yuksek indirimde fiyat artabilir
    @Property
    void discountMonotonic(
            @ForAll @DoubleRange(min = 0, max = 2000) double base,
            @ForAll @DoubleRange(min = 0, max = 100) double disc1,
            @ForAll @DoubleRange(min = 0, max = 100) double disc2,
            @ForAll @DoubleRange(min = 0, max = 100) double tax) {
        Assume.that(disc1 <= disc2);

        PriceCalculator calc = new PriceCalculator();
        double price1 = calc.calculate(base, disc1, tax);
        double price2 = calc.calculate(base, disc2, tax);
        assertThat(price1).isGreaterThanOrEqualTo(price2);
    }

    // boundedness: sonuc 0 ile ust sinir arasinda
    // bug: negatif fiyat veya cok buyuk hatali sonuc
    @Property
    void resultIsBounded(
            @ForAll @DoubleRange(min = 0, max = 3000) double base,
            @ForAll @DoubleRange(min = 0, max = 100) double discount,
            @ForAll @DoubleRange(min = 0, max = 100) double tax) {
        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(base, discount, tax);
        double maxPossible = base * (1 + tax / 100.0);

        assertThat(result).isGreaterThanOrEqualTo(0);
        assertThat(result).isLessThanOrEqualTo(maxPossible + 0.01);
    }

    // cart commutativity: A sonra B = B sonra A
    // bug: ekleme sirasi totali degistiriyorsa
    @Property
    void cartOrderDoesNotMatter(
            @ForAll("products") Product p1,
            @ForAll("products") Product p2,
            @ForAll @IntRange(min = 1, max = 5) int qty1,
            @ForAll @IntRange(min = 1, max = 5) int qty2) {
        Assume.that(!p1.getId().equals(p2.getId()));

        ShoppingCart cartAB = new ShoppingCart();
        cartAB.addItem(p1, qty1);
        cartAB.addItem(p2, qty2);

        ShoppingCart cartBA = new ShoppingCart();
        cartBA.addItem(p2, qty2);
        cartBA.addItem(p1, qty1);

        assertThat(cartAB.total()).isCloseTo(cartBA.total(), within(0.001));
    }

    @Provide
    Arbitrary<Product> products() {
        return Arbitraries.of(
                new Product("PX1", "Elma", 2.5, 20),
                new Product("PX2", "Ekmek", 5.0, 30),
                new Product("PX3", "Su", 1.0, 100),
                new Product("PX4", "Kitap", 45.0, 10)
        );
    }

}
