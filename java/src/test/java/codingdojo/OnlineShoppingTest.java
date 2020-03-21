package codingdojo;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OnlineShoppingTest {

  private static final Consumer<ModelObject> NO_OP_DATABASE_ADAPTER = modelObject -> {
  };

  private Store backaplan;
  private Store nordstan;

  private Item cherryBloom;
  private Item rosePetal;
  private Item blusherBrush;
  private Item eyelashCurler;
  private Item wildRose;
  private Item cocoaButter;
  private Item masterclass;
  private Item makeoverNordstan;
  private Item makeoverBackaplan;

  @BeforeEach
  public void setUp() {
    nordstan = new Store("Nordstan", false);
    backaplan = new Store("Backaplan", true);

    cherryBloom = new Item("Cherry Bloom", "LIPSTICK", 30);
    rosePetal = new Item("Rose Petal", "LIPSTICK", 30);
    blusherBrush = new Item("Blusher Brush", "TOOL", 50);
    eyelashCurler = new Item("Eyelash curler", "TOOL", 100);
    wildRose = new Item("Wild Rose", "PURFUME", 200);
    cocoaButter = new Item("Cocoa Butter", "SKIN_CREAM", 250);

    nordstan.addStockedItems(cherryBloom, rosePetal, blusherBrush, eyelashCurler, wildRose, cocoaButter);
    backaplan.addStockedItems(cherryBloom, rosePetal, eyelashCurler, wildRose, cocoaButter);

    // Store events add themselves to the stocked items at their store
    masterclass = new StoreEvent("Eyeshadow Masterclass", nordstan);
    makeoverNordstan = new StoreEvent("Makeover", nordstan);
    makeoverBackaplan = new StoreEvent("Makeover", backaplan);
  }

  @Test
  void resetStore() {
    final Session session = new Session(NO_OP_DATABASE_ADAPTER);
    final OnlineShopping shopping = new OnlineShopping(session);

    shopping.switchStore(null);

    assertNull(session.get("STORE"));
  }

  @Test
  void resetStoreWithDeliveryInformationSetDeliveryTypeToShipping() {
    final Session session = new Session(NO_OP_DATABASE_ADAPTER);
    session.put("DELIVERY_INFO", new DeliveryInformation("DRONE", null, 0L));
    final OnlineShopping shopping = new OnlineShopping(session);

    shopping.switchStore(null);

    assertEquals(((DeliveryInformation)session.get("DELIVERY_INFO")).getType(), "SHIPPING");
  }

  @Test
  public void switchDeliveryTypeToDroneIfShopSupportsDrones() {
    DeliveryInformation deliveryInfo = new DeliveryInformation("HOME_DELIVERY", nordstan, 60);
    deliveryInfo.setDeliveryAddress("NEARBY");

    Cart cart = new Cart();
    cart.addItem(cherryBloom);
    cart.addItem(blusherBrush);
    cart.addItem(masterclass);
    cart.addItem(makeoverNordstan);

    Session session = new Session(NO_OP_DATABASE_ADAPTER);
    session.put("STORE", nordstan);
    session.put("DELIVERY_INFO", deliveryInfo);
    session.put("CART", cart);
    OnlineShopping shopping = new OnlineShopping(session);

    shopping.switchStore(backaplan);
    assertEquals("DRONE", ((DeliveryInformation) session.get("DELIVERY_INFO")).getType());
  }

  @Test
  void ifWeightExceedsDroneCapacityDoNotSwitchToDroneDelivery() {
    final DeliveryInformation deliveryInfo = new DeliveryInformation("DRONE", backaplan, 0L);
    deliveryInfo.setDeliveryAddress("NEARBY");
    final Item heavyItem = new Item("heavy thing", "HEAVY", 501L);
    backaplan.addStockedItems(heavyItem);
    final Cart cart = new Cart();
    cart.addItem(heavyItem);

    final Session session = new Session(NO_OP_DATABASE_ADAPTER);
    session.put("DELIVERY_INFO", deliveryInfo);
    session.put("CART", cart);
    final OnlineShopping shopping = new OnlineShopping(session);

    shopping.switchStore(backaplan);

    assertEquals("HOME_DELIVERY", ((DeliveryInformation) session.get("DELIVERY_INFO")).getType());
  }

  @Test
  public void switchFromDroneToHomeDeliveryIfShopDoesNotSupportDrones() {
    DeliveryInformation deliveryInfo = new DeliveryInformation("DRONE", backaplan, 60);
    deliveryInfo.setDeliveryAddress("NEARBY");

    Cart cart = new Cart();
    cart.addItem(cherryBloom);
    cart.addItem(blusherBrush);
    cart.addItem(masterclass);
    cart.addItem(makeoverNordstan);

    Session session = new Session(NO_OP_DATABASE_ADAPTER);
    session.put("STORE", backaplan);
    session.put("DELIVERY_INFO", deliveryInfo);
    session.put("CART", cart);
    OnlineShopping shopping = new OnlineShopping(session);

    shopping.switchStore(nordstan);
    assertEquals("HOME_DELIVERY", ((DeliveryInformation) session.get("DELIVERY_INFO")).getType());
  }
}
