package net.clayrobot.delivery;
import net.clayrobot.delivery.levels.Level;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import net.clayrobot.delivery.entities.Box;
import net.clayrobot.delivery.entities.House;
import net.clayrobot.delivery.entities.Player;

import static net.clayrobot.delivery.entities.Player.Arm;
public class MyContactListener implements ContactListener {
	private Object object1;
	private Object object2;
	private Fixture fixture1;
	private Fixture fixture2;
	private Level level;
	private Delivery game;
	private HashMap<String, int[]> hashmap = new HashMap<>();
	public MyContactListener(Level level) {
		this.level = level;
		game = Delivery.getGame();
	}
	public boolean setObjects(Contact contact) {
		try {
			object1 = contact.getFixtureA().getBody().getUserData();
			object2 = contact.getFixtureB().getBody().getUserData();
		}
		catch(NullPointerException a) {
			return false;
		}
		fixture1 = contact.getFixtureA();
		fixture2 = contact.getFixtureB();
		if (object1 == null || object2 == null) return false;
		return true;
	}
	private boolean testThenOrderPair(Class<?> classA, Class<?> classB) {
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
	private void BoxHouseAction(boolean beginContact, Box box, House house) {
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
	private void BoxPlayerAction(boolean beginContact, Box box, Fixture playerFixture) {
		Arm side = (Arm) playerFixture.getBody().getUserData();
		String uuid = box.uuid;
		
		if (beginContact && !hashmap.containsKey(uuid)) {
			hashmap.put(uuid, new int[2]);
		}
		if (side == Arm.LEFT) {
			hashmap.get(uuid)[0] += beginContact ? 1 : -1;
		}
		else if (side == Arm.RIGHT) {
			hashmap.get(uuid)[1] += beginContact ? 1 : -1;
		}
		int[] LeftRight = hashmap.get(uuid);
		
		if (beginContact) {
			if (LeftRight[0] > 0 && LeftRight[1] > 0) {
				if (!box.held) {
					game.displayText = String.valueOf(box.address);
					level.player.setHolding(box.address);
				}
				box.held = true;
			}
		}
		else if (box.held) {
			if (LeftRight[0] < 1 && LeftRight[1] < 1) {
				game.displayText = "";
				level.player.clearHolding();
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
