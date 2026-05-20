package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 2 – Structural Testing &amp; Code Coverage (Chapter 3)
 *
 * <p>Target class: {@link ShoppingCart}
 *
 * <h3>Workflow</h3>
 * <ol>
 *   <li>Write an initial test suite based on the specification (Javadoc of ShoppingCart).</li>
 *   <li>Run {@code mvn test} to generate the JaCoCo report:
 *       <pre>  target/site/jacoco/index.html</pre></li>
 *   <li>Open the report, navigate to {@code ShoppingCart}, and identify uncovered branches.</li>
 *   <li>Add tests specifically to cover those branches until branch coverage &gt;= 80%.</li>
 *   <li>Take a screenshot of the final JaCoCo summary and put it in {@code report/jacoco-screenshot.png}.</li>
 * </ol>
 *
 * <h3>Branches to think about</h3>
 * <ul>
 *   <li>{@code addItem}: product already in cart vs. new product</li>
 *   <li>{@code removeItem}: product found vs. not found in cart</li>
 *   <li>{@code updateQuantity}: product found vs. not found, quantity valid vs. invalid</li>
 *   <li>{@code applyDiscount}: zero discount, positive discount</li>
 *   <li>{@code total}: empty cart vs. non-empty cart</li>
 * </ul>
 *
 * <h3>Bonus (PIT Mutation Testing)</h3>
 * Run: {@code mvn org.pitest:pitest-maven:mutationCoverage}
 * <br>Examine the HTML report in {@code target/pit-reports/}. Find two surviving mutants,
 * explain why each survived, and describe a test that would kill it. Add this analysis
 * to your reflection report.
 */
class ShoppingCartStructuralTest {

    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        apple  = new Product("P001", "Apple",  1.50, 100);
        banana = new Product("P002", "Banana", 0.80, 50);
    }

    // bos sepet total
    @Test
    void emptyCart() {
        assertThat(cart.total()).isEqualTo(0);
        assertThat(cart.itemCount()).isEqualTo(0);
    }

    // addItem - yeni urun branch
    @Test
    void addNewProduct() {
        cart.addItem(apple, 2);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(3.0);
    }

    // addItem - urun zaten var branch
    @Test
    void addSameProductTwice() {
        cart.addItem(apple, 1);
        cart.addItem(apple, 2);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(4.5);
    }

    @Test
    void twoDifferentProducts() {
        cart.addItem(apple, 1);
        cart.addItem(banana, 1);
        assertThat(cart.itemCount()).isEqualTo(2);
        assertThat(cart.total()).isCloseTo(2.3, within(0.01));
    }

    // removeItem bulundu
    @Test
    void removeItem() {
        cart.addItem(apple, 1);
        cart.removeItem("P001");
        assertThat(cart.itemCount()).isEqualTo(0);
    }

    // removeItem yok - hicbir sey olmamali
    @Test
    void removeItemNotInCart() {
        cart.addItem(apple, 1);
        cart.removeItem("XXX");
        assertThat(cart.itemCount()).isEqualTo(1);
    }

    // updateQuantity ok
    @Test
    void updateQty() {
        cart.addItem(banana, 1);
        cart.updateQuantity("P002", 3);
        assertThat(cart.total()).isCloseTo(2.4, within(0.01));
    }

    // updateQuantity quantity <= 0
    @Test
    void updateQtyInvalid() {
        cart.addItem(apple, 1);
        assertThatThrownBy(() -> cart.updateQuantity("P001", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // updateQuantity urun yok - bos sepet
    @Test
    void updateQtyNotFound() {
        assertThatThrownBy(() -> cart.updateQuantity("P999", 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // sepet dolu ama yanlis id (loop icinde eslesmeyen branch)
    @Test
    void updateQtyWrongId() {
        cart.addItem(apple, 1);
        assertThatThrownBy(() -> cart.updateQuantity("P002", 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // PIT bonus: qty 0 + wrong id — mutant (< 0) skips cart check, throws "not found" instead
    @Test
    void updateQtyZeroWrongProduct() {
        cart.addItem(apple, 1);
        assertThatThrownBy(() -> cart.updateQuantity("P999", 0))
                .hasMessageContaining("Quantity must be > 0");
    }

    // PIT bonus: CartItem.setQuantity boundary — direct call with qty 0
    @Test
    void cartItemRejectsZeroQuantity() {
        CartItem item = new CartItem(apple, 1);
        assertThatThrownBy(() -> item.setQuantity(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be > 0");
    }

    // applyDiscount 0
    @Test
    void discountZero() {
        cart.addItem(apple, 2);
        assertThat(cart.applyDiscount(0)).isEqualTo(3.0);
    }

    // applyDiscount > 0
    @Test
    void discountTenPercent() {
        cart.addItem(apple, 2);
        assertThat(cart.applyDiscount(10)).isCloseTo(2.7, within(0.01));
    }

    @Test
    void clearCart() {
        cart.addItem(apple, 1);
        cart.clear();
        assertThat(cart.total()).isEqualTo(0);
    }

    @Test
    void getItemsAndToString() {
        cart.addItem(apple, 1);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.toString()).contains("ShoppingCart");
    }

}
