package net.clayrobot.delivery;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import net.clayrobot.delivery.entities.Box;
import net.clayrobot.delivery.entities.House;
import net.clayrobot.delivery.entities.Player;
public class MyContactListener implements ContactListener {
	private Object object1;
	private Object object2;
	private Fixture fixture1;
	private Fixture fixture2;
	private Level level;
	private Delivery game;
	private final Sound sound = Gdx.audio.newSound(Gdx.files.internal("guncock2.wav"));
	public MyContactListener(Level level) {
		this.level = level;
		game = Delivery.getGame();
	}
	public boolean setObjects(Contact contact) {
		object1 = contact.getFixtureA().getBody().getUserData();
		fixture1 = contact.getFixtureA();
		object2 = contact.getFixtureB().getBody().getUserData();
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
		Player player = (Player) playerFixture.getBody().getUserData();
		Array<Fixture> fixturesTouching = player.fixturesTouching;
		int index = -1;
		boolean matchFound = false;
		for (int i = 0; i < fixturesTouching.size; i++) {
			if (fixturesTouching.get(i).equals(playerFixture)) {
				matchFound = true;
				index = i;
				break;
			}
		}
		if (beginContact) {
			if (!game.displayText.equals(String.valueOf(box.address))) {
				game.displayText = String.valueOf(box.address);
				sound.play();
			}
			
			if (!matchFound) fixturesTouching.add(playerFixture);
		}
		else {
			if (matchFound) fixturesTouching.removeIndex(index);
			if (fixturesTouching.isEmpty()) game.displayText = "";
		}
	}
	@Override
	public void beginContact (Contact contact) {
		if (!setObjects(contact)) return;
		if (testThenOrderPair(Box.class, House.class)) {
			BoxHouseAction(true, (Box) object1, (House) object2);
		}
		else if (testThenOrderPair(Box.class, Player.class)) {
			BoxPlayerAction(true, (Box) object1, fixture2);
		}
	}
	@Override
	public void endContact (Contact contact) {
		if (!setObjects(contact)) return;
		if (testThenOrderPair(Box.class, House.class)) {
			BoxHouseAction(false, (Box) object1, (House) object2);
		}
		else if (testThenOrderPair(Box.class, Player.class)) {
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
