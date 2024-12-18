package net.clayrobot.delivery;
import net.clayrobot.delivery.levels.Level;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import java.util.HashMap;
import net.clayrobot.delivery.entities.Box;
import net.clayrobot.delivery.entities.House;

import static net.clayrobot.delivery.entities.Player.Arm;
public class MyContactListener implements ContactListener {
	private Object object1;
	private Object object2;
	private Fixture fixture1;
	private Fixture fixture2;
	private final Level level;
	private final HashMap<String, int[]> hashmap = new HashMap<>();
	public MyContactListener(Level level) {
		this.level = level;
	}
	public boolean setObjects(Contact contact) {
		try { // to prevent crash
			object1 = contact.getFixtureA().getBody().getUserData();
			object2 = contact.getFixtureB().getBody().getUserData();
		}
		catch(NullPointerException a) {
			return false;
		}
		fixture1 = contact.getFixtureA();
		fixture2 = contact.getFixtureB();
		return true;
	}
	private boolean testThenOrderPair(Class<?> classA, Class<?> classB) { // this method takes two class arguments and orders the object in that pair if there are matches
		if (classA.isInstance(object1) && classB.isInstance(object2)) {
			return true;
		} 
		else if (classA.isInstance(object2) && classB.isInstance(object1)) {
			Object tempObject = object1;
			Fixture tempFixture = fixture1;
			object1 = object2;
			fixture1 = fixture2;
			object2 = tempObject;
			fixture2 = tempFixture;
			return true;
		}
		return false;
	}
	private void BoxHouseAction(boolean beginContact, Box box, House house) { // this method is called when boxes enter/exit house (for scoring)
		if (box.address == house.address) {
			if (beginContact) {
				house.incrementScore();
				level.incrementScore();
			}
			else {
				house.decrementScore();
				level.decrementScore();
			}
		}
	}
	private void BoxPlayerAction(boolean beginContact, Box box, Fixture playerFixture) { // this method is for when the player is grabbing a box with both arms (to display its address on screen)
		Arm side = (Arm) playerFixture.getBody().getUserData();
		String uuid = box.uuid;
		
		if (beginContact && !hashmap.containsKey(uuid)) {
			hashmap.put(uuid, new int[2]); // creates new hashmap entry which uses box UUID as index that points to int[2] (one for each arm)
		}
		if (side == Arm.LEFT) {
			hashmap.get(uuid)[0] += beginContact ? 1 : -1; // increments if beginContact is true, decrements if false
		}
		else if (side == Arm.RIGHT) {
			hashmap.get(uuid)[1] += beginContact ? 1 : -1; // the max value is determined by how many fixtures each arm has that can possibly touch the box
		}
		int[] LeftRight = hashmap.get(uuid);
		
		if (beginContact) {
			if (LeftRight[0] > 0 && LeftRight[1] > 0) {
				if (!box.held) {
					level.game.displayText = String.valueOf(box.address); // since a box is being held, display its address
					level.getPlayer().setHolding(box.address);
				}
				box.held = true;
			}
		}
		else if (box.held) { // if box was being held but contact ended
			if (LeftRight[0] < 1 && LeftRight[1] < 1) { // if no fixtures in either left or right arm are touching this box
				level.game.displayText = ""; // clear display text
				level.getPlayer().clearHolding();
				box.held = false;
			}
		}		
	}
	@Override
	public void beginContact (Contact contact) {
		if (!setObjects(contact)) return;
		if (testThenOrderPair(Box.class, House.class)) {
			BoxHouseAction(true, (Box) object1, (House) object2);
		}
		else if (testThenOrderPair(Box.class, Arm.class)) {
			BoxPlayerAction(true, (Box) object1, fixture2);
		}
	}
	@Override
	public void endContact (Contact contact) {
		if (!setObjects(contact)) return;
		if (testThenOrderPair(Box.class, House.class)) {
			BoxHouseAction(false, (Box) object1, (House) object2);
		}
		else if (testThenOrderPair(Box.class, Arm.class)) {
			BoxPlayerAction(false, (Box) object1, fixture2);
		}
	}
	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		
	}
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		
	}
}
