package net.clayrobot.delivery.entities;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import net.clayrobot.delivery.Delivery;
import net.clayrobot.delivery.Shapes;
import static net.clayrobot.delivery.DrawTools.fillBody;
import net.clayrobot.delivery.levels.Level;

public class Player extends Entity {
	private int frame = 0;
	private static final float ANIM_LENGTH = 0.25f * (Delivery.getGame().refreshRate / 60f);
	private static final Texture idleTex = new Texture("drone/idle.png");
	private static final Texture deadTex = new Texture("drone/off.png");
	private final Sound helicopter = Gdx.audio.newSound(Gdx.files.internal("rotor2.wav"));
	private final boolean ENABLE_ARMS = true;
	private static final Texture[] flight_frames = {
		new Texture("drone/flight/1.png"),
		new Texture("drone/flight/2.png"),
		new Texture("drone/flight/3.png"),
		new Texture("drone/flight/4.png"),
		new Texture("drone/flight/5.png"),
		//new Texture("drone/flight/6.png"),
		//new Texture("drone/flight/7.png"),
		//new Texture("drone/flight/8.png"),
		//new Texture("drone/flight/9.png"),
		//new Texture("drone/flight/10.png")
	};
	private final Sprite droneSprite = new Sprite(idleTex);
	private final Body body;
	private Body leftArm;
	private Body rightArm;
	private final FixtureDef fixtureDef = new FixtureDef();
	private RevoluteJoint leftClaw;
	private RevoluteJoint rightClaw;
	public Vector2 pos;
	private float angle, workingAngle, sine, cosine, torque;
	private final float CLAW_SPEED = 3;
	private final float RETRACT_SPEED = 1;
	private final float MAX_TORQUE = 250;
	private final float GRIP_THRESHOLD = 99;
	private final float RELEASE_THRESHOLD = 4;
	private final float TILT_IMPULSE = 18f;
	private final float THRUST_IMPULSE = 50;
	private boolean alive = true;
	private boolean gripping = false;
	private boolean AlreadyPropelling = false;
	private boolean propelling = false;
	private boolean clawing = false;
	private boolean tiltingLeft = false;
	private boolean tiltingRight = false;
	private boolean autoClaw = false;
	private final Vector2 impulseVector = new Vector2();
	private final Vector2 forceVector = new Vector2();
	private final Vector2 push = new Vector2(0, 0);
	private float targetAngle = 0;
	private float angleDifferenceRatio;
	private float deltaTime;
	public int holding = 0;
	private final Sound cock = Gdx.audio.newSound(Gdx.files.internal("guncock2.wav"));
	public static final Vector2 spawn = Vector2.Zero;
	public static enum Arm {
		RIGHT,
		LEFT
	}
	public Player() {
		this(spawn.x, spawn.y, true);
	}
	public Player(float x, float y, boolean alive) {
		this.alive = alive;
		pos = new Vector2(x, y);
		game.dynamicBodyDef.position.set(x, y);
		body = Level.world.createBody(game.dynamicBodyDef);
		// capsule setup
		PolygonShape square = new PolygonShape();
		square.setAsBox(0.5f, 0.8f);
		fixtureDef.shape = square;
		fixtureDef.density = 0.65f;
		fixtureDef.friction = 0.7f;
		fixtureDef.restitution = 0.22f;
		body.createFixture(fixtureDef);
		CircleShape circle = new CircleShape();
		circle.setRadius(0.8f);
		circle.setPosition(new Vector2(0.8f, 0));
		fixtureDef.shape = circle;
		fixtureDef.density = 0.65f;
		body.createFixture(fixtureDef);
		circle.setPosition(new Vector2(-0.8f, 0));
		fixtureDef.shape = circle;
		body.createFixture(fixtureDef);
		body.setAngularDamping(0.3f);
		//body.setUserData(this);
		circle.dispose();
		square.dispose();
		if (ENABLE_ARMS) setupArms(x, y, 0);
		updateState(true);
		
	}
	private void setupArms(float x, float y, int type) {
		float x_offset = 1;
		float y_offset = 1.2f;
		float legScaleX = 0.45f;
		float legScaleY = 0.86f;
		game.dynamicBodyDef.position.set(x - x_offset, y - y_offset);
		leftArm = Level.world.createBody(game.dynamicBodyDef);
		leftArm.setUserData(Arm.LEFT);
		game.dynamicBodyDef.position.set(x + x_offset, y - y_offset);
		rightArm = Level.world.createBody(game.dynamicBodyDef);
		rightArm.setUserData(Arm.RIGHT);
		
		PolygonShape armShape = new PolygonShape();
		fixtureDef.shape = armShape;
		fixtureDef.friction = 0.9f;
		fixtureDef.restitution = 0.2f;
		if (type == 0) {
			fixtureDef.density = 0.6f;
			armShape.setAsBox(0.15f, 0.5f);
			leftArm.createFixture(fixtureDef);
			rightArm.createFixture(fixtureDef);
		}
		else if (type == 1) {
			fixtureDef.density = 0.4f;
			armShape.set(Shapes.get("LEFT_ARM_TOP", legScaleX, legScaleY));
			leftArm.createFixture(fixtureDef);
			armShape.set(Shapes.get("LEFT_ARM_BOTTOM", legScaleX, legScaleY));
			leftArm.createFixture(fixtureDef);
			armShape.set(Shapes.get("RIGHT_ARM_TOP", legScaleX, legScaleY));
			rightArm.createFixture(fixtureDef);
			armShape.set(Shapes.get("RIGHT_ARM_BOTTOM", legScaleX, legScaleY));
			rightArm.createFixture(fixtureDef);
		}
		armShape.dispose();
		
		RevoluteJointDef clawJointDef = new RevoluteJointDef();
		clawJointDef.enableMotor = true;
		clawJointDef.maxMotorTorque = MAX_TORQUE;
		clawJointDef.upperAngle = (float) Math.toRadians(45);
		clawJointDef.lowerAngle = (float) Math.toRadians(-45);
		clawJointDef.enableLimit = true;
		
		clawJointDef.initialize(body, leftArm, new Vector2(x - x_offset, y - 0.4f));
		leftClaw = (RevoluteJoint) Level.world.createJoint(clawJointDef);
		clawJointDef.initialize(body, rightArm, new Vector2(x + x_offset, y - 0.4f));
		rightClaw = (RevoluteJoint) Level.world.createJoint(clawJointDef);
	}
	public void setPropelling(boolean propelling) {
		this.propelling = propelling;
	}
	public void setTiltLeft(boolean tilting) {
		tiltingLeft = tilting;
	}
	public void setTiltRight(boolean tilting) {
		tiltingRight = tilting;
	}
	public void setClawing(boolean clawing) {
		this.clawing = clawing;
	}
	public boolean getGripping() {
		return gripping;
	}
	public void releaseGrip() {
		gripping = false;
	}
	
	private void pollAccelerometer() {
		float tiltRatio = Gdx.input.getAccelerometerY() / (9.8f);
		tiltRatio *= 0.86f;
		final int orientation = -1;
		
		//final float targetAngle = angle + (float) (Math.PI / 2) * tiltRatio * orientation;
		targetAngle = (float) (Math.PI / 2) * tiltRatio * orientation;
		angleDifferenceRatio = Math.abs((angle % (float) (Math.PI * 2)) - targetAngle) / (float) (Math.PI / 2);
		//angleDifferenceRatio = Math.abs(angle - targetAngle) / (float) (Math.PI / 2);
		//scoreText = Math.round((float) Math.toDegrees(angle)) + " : " + Math.round((float) Math.toDegrees(targetAngle));
		
		if (angle < targetAngle) {
			setTiltLeft(true);
			setTiltRight(false);
		}
		else if (angle > targetAngle) {
			setTiltLeft(false);
			setTiltRight(true);
		}
		else {
			setTiltLeft(false);
			setTiltRight(false);
		}
	}
	private void updateState(boolean updateSineCosine) {
		pos = body.getPosition();
		angle = body.getAngle();
		if (updateSineCosine) {
			// transformed by 90 degrees so 0 is up
			workingAngle = angle + (float) (Math.PI / 2);
			sine = (float) (Math.sin(workingAngle));
			cosine = (float) (Math.cos(workingAngle));
		}
	}
	@Override
	protected void update() {
		if (game.mobilePlatform) {
			push.x = pos.x - 0.4f * cosine;
			push.y = pos.y - 0.5f * sine;
			pollAccelerometer();
		}
		else {
			push.x = pos.x - 0.2f * cosine;
			push.y = pos.y - 0.5f * sine;
		}
		
		if (propelling) {
			if (!AlreadyPropelling) helicopter.loop();
			impulseVector.x = THRUST_IMPULSE * cosine * deltaTime;
			impulseVector.y = THRUST_IMPULSE * sine * deltaTime;
			
			body.applyLinearImpulse(impulseVector, push, true);
			
			droneSprite.setTexture(flight_frames[(int) (frame * ANIM_LENGTH)]);
			frame++;
			if (frame > (int) (flight_frames.length - 1) / ANIM_LENGTH) frame = 0;
			AlreadyPropelling = true;
		}
		else {
			helicopter.stop();
			frame = 0;
			droneSprite.setTexture(idleTex);
			AlreadyPropelling = false;
		}
		if (game.mobilePlatform) {
			final float dampRatio = 1.2f;//0.5f;
			if (tiltingLeft) body.applyAngularImpulse(TILT_IMPULSE * angleDifferenceRatio * dampRatio * deltaTime, true);
			if (tiltingRight) body.applyAngularImpulse(-TILT_IMPULSE * angleDifferenceRatio * dampRatio * deltaTime, true);
		}
		else {
			if (tiltingLeft) body.applyAngularImpulse(TILT_IMPULSE * deltaTime, true);
			if (tiltingRight) body.applyAngularImpulse(-TILT_IMPULSE * deltaTime, true);
		}
		if (ENABLE_ARMS) updateArms();
	}
	private void updateArms() {
		torque = Math.max(leftClaw.getMotorTorque(1 / deltaTime), rightClaw.getMotorTorque(1 / deltaTime));
		if (clawing) {
			leftClaw.setMotorSpeed(CLAW_SPEED);
			rightClaw.setMotorSpeed(-CLAW_SPEED);
			if (Math.abs(torque) > GRIP_THRESHOLD) gripping = true;
		}
		else if (gripping && Math.abs(torque) > RELEASE_THRESHOLD) {
			leftClaw.setMotorSpeed(0);
			rightClaw.setMotorSpeed(0);
		}
		else {
			if (gripping && autoClaw) {
				leftClaw.setMotorSpeed(CLAW_SPEED);
				rightClaw.setMotorSpeed(-CLAW_SPEED);
			}
			else {
				gripping = false;
				leftClaw.setMotorSpeed(-RETRACT_SPEED);
				rightClaw.setMotorSpeed(RETRACT_SPEED);
			}
		}
	}
	@Override
	public void draw(float deltaTime) {
		this.deltaTime = deltaTime;
		boolean doUpdate = deltaTime > 0;
		updateState(doUpdate);
		if (doUpdate && alive) {
			if (game.platform != ApplicationType.WebGL) helicopter.resume();
			update();
		}
		else if (AlreadyPropelling) {
			helicopter.pause();
			//AlreadyPropelling = false;
		}
		if (ENABLE_ARMS) {
			fillBody(rightArm, Color.LIME);
			fillBody(leftArm, Color.LIME);
		}
		updateState(false);
		droneSprite.setBounds(pos.x - droneSprite.getWidth() / 2, pos.y - droneSprite.getHeight() / 2, 4f, 2.48f);
		droneSprite.setOriginCenter();
		droneSprite.setRotation((float) Math.toDegrees(angle));
		droneSprite.draw(game.batch);
		if (game.drawDebug && alive) game.shapeDrawer.polygon(push.x, push.y, 6, 0.1f);
	}
	public void setHolding(int addr) {
		cock.play();
		holding = addr;
	}
	public void clearHolding() {
		holding = 0;
	}
	public void kill() {
		alive = false;
		droneSprite.setTexture(deadTex);
		if (ENABLE_ARMS) {
			leftClaw.enableMotor(false);
			rightClaw.enableMotor(false);
		} 
	}
	@Override
	public void delete() {
		helicopter.stop();
		helicopter.dispose();
		Level.world.destroyBody(body);
		if (ENABLE_ARMS) {
			Level.world.destroyBody(leftArm);
			Level.world.destroyBody(rightArm);
		}
	}
	protected static void dispose() {
		idleTex.dispose();
		deadTex.dispose();
		for (Texture texture : flight_frames) {
			texture.dispose();
		}
	}
}