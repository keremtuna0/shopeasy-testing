package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Task 5 – Mocks &amp; Stubs (Chapter 6)
 *
 * <p>Target class: {@link OrderProcessor}
 *
 * <p>Use Mockito to mock {@link InventoryService} and {@link PaymentGateway},
 * then test {@link OrderProcessor#process(String, ShoppingCart)} in isolation.
 *
 * <h3>Required scenarios (at least 4)</h3>
 * <ol>
 *   <li><b>Happy path</b> — inventory available, payment succeeds → non-null {@link Order} returned.</li>
 *   <li><b>Inventory failure</b> — {@code isAvailable()} returns {@code false} for at least one item
 *       → method returns {@code null} AND {@code charge()} is <em>never</em> called.</li>
 *   <li><b>Payment failure</b> — inventory OK, {@code charge()} returns {@code false}
 *       → method returns {@code null}.</li>
 *   <li><b>Partial quantity</b> — define the expected behaviour when only some items
 *       pass the inventory check, and write a test for it.</li>
 * </ol>
 *
 * <h3>Verification</h3>
 * Use {@code verify(paymentGateway, never()).charge(...)} to assert that
 * payment is never attempted when inventory is insufficient.
 *
 * <h3>Reflection (add to your report)</h3>
 * Answer: What does mocking allow you to test that you could not test otherwise?
 * What does it prevent you from testing? When is mocking a bad idea?
 */
@ExtendWith(MockitoExtension.class)
class OrderProcessorMockTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private OrderProcessor orderProcessor;

    private ShoppingCart cart;
    private Product widget;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        widget = new Product("P001", "Widget", 25.0, 100);
    }

    // 1) happy path - stok tamam odeme tamam
    @Test
    void happyPath() {
        cart.addItem(widget, 2);

        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(paymentGateway.charge(eq("kerem-1"), eq(50.0))).thenReturn(true);

        Order order = orderProcessor.process("kerem-1", cart);

        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("kerem-1");
        assertThat(order.getTotal()).isEqualTo(50.0);
        verify(inventoryService).isAvailable(widget, 2);
        verify(paymentGateway).charge("kerem-1", 50.0);
    }

    // 2) inventory failure - stok yok, charge cagrilmamali
    @Test
    void inventoryFail() {
        cart.addItem(widget, 1);
        when(inventoryService.isAvailable(widget, 1)).thenReturn(false);

        Order result = orderProcessor.process("kerem-1", cart);

        assertThat(result).isNull();
        verify(paymentGateway, never()).charge(any(), anyDouble());
    }

    // 3) payment failure
    @Test
    void paymentFail() {
        cart.addItem(widget, 1);

        when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        when(paymentGateway.charge("kerem-1", 25.0)).thenReturn(false);

        Order result = orderProcessor.process("kerem-1", cart);

        assertThat(result).isNull();
        verify(paymentGateway).charge("kerem-1", 25.0);
    }

    // 4) partial quantity
    // OrderProcessor tam miktar ister: 5 adet istenince isAvailable(widget,5) false ise null doner
    // kismi teslim yok, siparis iptal
    @Test
    void partialQtyNotEnoughStock() {
        cart.addItem(widget, 5);

        when(inventoryService.isAvailable(widget, 5)).thenReturn(false);

        Order result = orderProcessor.process("x99", cart);

        assertThat(result).isNull();
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
        verify(inventoryService).isAvailable(widget, 5);
    }

}
