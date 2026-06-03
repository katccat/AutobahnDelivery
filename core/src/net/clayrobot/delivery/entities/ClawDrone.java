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
import net.clayrobot.delivery.AutobahnDelivery;
import net.clayrobot.delivery.Shapes;
import static net.clayrobot.delivery.DrawTools.fillBody;
import net.clayrobot.delivery.levels.Level;
import com.badlogic.gdx.graphics.g2d.Animation;

public class ClawDrone extends Entity {
	private static final Texture idleTex = new Texture("drone/sketch_idle.png");
	private static final Texture disabledTex = new Texture("drone/off.png");
	private static final Texture[] flight_frames = {
		new Texture("drone/sketch_fly.png"),
	};
	private final Animation<Texture> anim;
	private final Sprite droneSprite = new Sprite(idleTex);
	private final Sound rotorSound = Gdx.audio.newSound(Gdx.files.internal("rotor2.wav"));
	private final boolean ENABLE_ARMS = true;
	
	private final Sprite clawSprite = new Sprite(new Texture("drone/claw.png"));
	private float stateTime = 0;
	
	
	private final Body body;
	private Body leftArm;
	private Body rightArm;
	private final FixtureDef fixtureDef = new FixtureDef();
	private RevoluteJoint leftArmJoint;
	private RevoluteJoint rightArmJoint;
	public Vector2 position;
	private float angle, workingAngle, sine, cosine, torque;
	private final float CLAW_SPEED = 3;
	private final float RETRACT_SPEED = 1;
	private final float MAX_TORQUE = 250;
	private final float GRIP_THRESHOLD = 99;
	private final float RELEASE_THRESHOLD = 1;
	private final float TILT_IMPULSE = 18f;
	private final float THRUST_IMPULSE = 55;
	private boolean alive = true;
	private boolean gripping = false;
	private boolean propelling = false;
	private boolean AlreadyPropelling = false;
	private boolean clawing = false;
	private boolean tiltingLeft = false;
	private boolean tiltingRight = false;
	private boolean autoClaw = true;
	private final Vector2 impulseVector = new Vector2();
	private final Vector2 push = new Vector2(0, 0);
	private float targetAngle = 0; // used for tilt controls
	private float angleDifferenceRatio; // used for tilt controls
	private float deltaTime;
	public int holding = 0;
	private final Sound cock = Gdx.audio.newSound(Gdx.files.internal("guncock2.wav"));
	public static final Vector2 spawn = Vector2.Zero; // where player will be created (level sets the spawn)
	public static enum Arm {
		RIGHT,
		LEFT
	}
	public static enum ArmType {
		RECTANGLE,
		CLAW1
	}
	public static ArmType EnabledArmType = ArmType.RECTANGLE; // level can change this property
	public ClawDrone() {
		this(spawn.x, spawn.y, true);
	}
	public ClawDrone(float x, float y, boolean alive) {
		anim = new Animation<>(0.05f, flight_frames);
		anim.setPlayMode(Animation.PlayMode.LOOP);
		this.alive = alive;
		position = new Vector2(x, y);
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
		body.setAngularDamping(0.3f); // to dampen uncontrollable spinning
		circle.dispose();
		square.dispose();
		if (ENABLE_ARMS) setupArms(x, y, EnabledArmType);
		updateState(true);
		
	}
	private void setupArms(float x, float y, ArmType armType) {
		float x_offset = 1f;
		//float y_offset = 1.2f;
		float y_offset = 1f;
		//float legScaleX = 0.45f;
		//float legScaleY = 0.86f;
		float armScaleX = 0.42f;
		float armScaleY = 0.75f;
		game.dynamicBodyDef.position.set(x - x_offset, y - y_offset);
		leftArm = Level.world.createBody(game.dynamicBodyDef);
		leftArm.setUserData(Arm.LEFT);
		game.dynamicBodyDef.position.set(x + x_offset, y - y_offset);
		rightArm = Level.world.createBody(game.dynamicBodyDef);
		rightArm.setUserData(Arm.RIGHT);
		
		PolygonShape armShape = new PolygonShape();
		fixtureDef.shape = armShape;
		fixtureDef.friction = 0.95f;
		fixtureDef.restitution = 0.18f;
		if (armType == ArmType.RECTANGLE) { // creates rectangle arms
			fixtureDef.density = 0.6f;
			armShape.setAsBox(0.15f, 0.5f);
			leftArm.createFixture(fixtureDef);
			rightArm.createFixture(fixtureDef);
		}
		else if (armType == ArmType.CLAW1) { // creates claw arms using custom shapes in Shapes class
			fixtureDef.density = 0.5f;
			armShape.set(Shapes.get("LEFT_ARM_TOP", armScaleX, armScaleY));
			leftArm.createFixture(fixtureDef);
			armShape.set(Shapes.get("LEFT_ARM_BOTTOM", armScaleX, armScaleY));
			leftArm.createFixture(fixtureDef);
			armShape.set(Shapes.get("RIGHT_ARM_TOP", armScaleX, armScaleY));
			rightArm.createFixture(fixtureDef);
			armShape.set(Shapes.get("RIGHT_ARM_BOTTOM", armScaleX, armScaleY));
			rightArm.createFixture(fixtureDef);
		}
		armShape.dispose();
		
		RevoluteJointDef clawJointDef = new RevoluteJointDef();
		clawJointDef.enableMotor = true; // this allows the player to move the arms
		clawJointDef.maxMotorTorque = MAX_TORQUE; // this determines the strength of the arms
		clawJointDef.enableLimit = true; // this enables a limit to the max rotation of the arms
		
		clawJointDef.upperAngle = (float) Math.toRadians(30);
		clawJointDef.lowerAngle = (float) Math.toRadians(-45);
		clawJointDef.initialize(body, leftArm, new Vector2(x - x_offset - 0.05f, y - 0.4f));
		leftArmJoint = (RevoluteJoint) Level.world.createJoint(clawJointDef);
		clawJointDef.upperAngle = (float) Math.toRadians(45);
		clawJointDef.lowerAngle = (float) Math.toRadians(-30);
		clawJointDef.initialize(body, rightArm, new Vector2(x + x_offset + 0.05f, y - 0.4f));
		rightArmJoint = (RevoluteJoint) Level.world.createJoint(clawJointDef);
	}
	// these methods are called by an input processor
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
		float tiltRatio = Gdx.input.getAccelerometerY() / (9.8f); // the ratio of tilt (away from perfectly landscape)
		tiltRatio *= 0.86f; // dampens the ratio
		final int orientation = -1; // orientation is not properly implemented yet (only upright landscape for now)
		
		//final float targetAngle = angle + (float) (Math.PI / 2) * tiltRatio * orientation;
		targetAngle = (float) (Math.PI / 2) * tiltRatio * orientation; // where the player intends to rotate the player to based on the tilt
		angleDifferenceRatio = Math.abs((angle % (float) (Math.PI * 2)) - targetAngle) / (float) (Math.PI / 2); // used to determine strength of rotation to target angle (strong if large, weak if small)
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
		position = body.getPosition();
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
			push.x = position.x - 0.4f * cosine;
			push.y = position.y - 0.5f * sine;
			pollAccelerometer();
		}
		else {
			push.x = position.x - 0.2f * cosine;
			push.y = position.y - 0.5f * sine;
		}
		
		if (propelling) {
			if (!AlreadyPropelling) rotorSound.loop();
			// x and y components multiplied by cosine/sine for direction and deltaTime for consistent speed regardless of framerate
			impulseVector.x = THRUST_IMPULSE * cosine * deltaTime;
			impulseVector.y = THRUST_IMPULSE * sine * deltaTime;
			
			body.applyLinearImpulse(impulseVector, push, true);
			stateTime += deltaTime;
			droneSprite.setTexture(anim.getKeyFrame(stateTime));
			AlreadyPropelling = true;
		}
		else {
			stateTime = 0;
			rotorSound.stop();
			droneSprite.setTexture(idleTex);
			AlreadyPropelling = false;
		}
		if (game.mobilePlatform) { // this block is for tilt controls
			final float dampRatio = 1.2f;//0.5f;
			// multiply by angleDifferenceRatio to determine strenght of pull (strong if big, weak if small)
			if (tiltingLeft) body.applyAngularImpulse(TILT_IMPULSE * angleDifferenceRatio * dampRatio * deltaTime, true);
			if (tiltingRight) body.applyAngularImpulse(-TILT_IMPULSE * angleDifferenceRatio * dampRatio * deltaTime, true);
		}
		else { // this block is for arrow keys
			if (tiltingLeft) body.applyAngularImpulse(TILT_IMPULSE * deltaTime, true);
			if (tiltingRight) body.applyAngularImpulse(-TILT_IMPULSE * deltaTime, true);
		}
		if (ENABLE_ARMS) updateArms();
	}
	private void updateArms() {
		torque = Math.max(leftArmJoint.getMotorTorque(1 / deltaTime), rightArmJoint.getMotorTorque(1 / deltaTime));
		if (clawing) {
			leftArmJoint.setMotorSpeed(CLAW_SPEED);
			rightArmJoint.setMotorSpeed(-CLAW_SPEED);
			if (Math.abs(torque) > GRIP_THRESHOLD) gripping = true; // arms experience feedback, so they know they are gripping something
		}
		else if (gripping && Math.abs(torque) > RELEASE_THRESHOLD) { // maintain zero motor speed as long as torque is above release threshold
			leftArmJoint.setMotorSpeed(0);
			rightArmJoint.setMotorSpeed(0);
		}
		else { 
			if (gripping && autoClaw && holding != 0) { // if the player is holding something (and it is a box) and autoClaw is true
				leftArmJoint.setMotorSpeed(CLAW_SPEED);
				rightArmJoint.setMotorSpeed(-CLAW_SPEED);
			}
			else { // if autoClaw is false and the box slips too much, the claws retract to their retracted position (like a claw machine)
				gripping = false;
				leftArmJoint.setMotorSpeed(-RETRACT_SPEED);
				rightArmJoint.setMotorSpeed(RETRACT_SPEED);
			}
		}
	}
	@Override
	public void draw(float deltaTime) {
		this.deltaTime = deltaTime;
		boolean doUpdate = deltaTime > 0;
		updateState(doUpdate);
		if (doUpdate && alive) {
			if (game.platform != ApplicationType.WebGL) rotorSound.resume();
			update();
		}
		else if (AlreadyPropelling) {
			rotorSound.pause();
			//AlreadyPropelling = false;
		}
		if (ENABLE_ARMS) {
			//fillBody(rightArm, Color.LIME);
			//fillBody(leftArm, Color.LIME);
		}
		updateState(false);
		float texWidth = droneSprite.getTexture().getWidth();
		float texHeight = droneSprite.getTexture().getHeight();
		float aspect = texWidth / texHeight;
		float targetHeight = 3.6f;
		float targetWidth = targetHeight * aspect;
		float clawScale = 2.5f;
		clawSprite.setBounds(position.x - 1, position.y + 1, 0.15f * clawScale, 0.5f * clawScale);
		//x - x_offset - 0.05f, y - 0.4f
		clawSprite.setOriginCenter();
		clawSprite.setRotation((float) Math.toDegrees(leftArm.getAngle()));
		clawSprite.draw(game.batch);
		clawSprite.setBounds(position.x + 1, position.y + 1, 0.15f * clawScale, 0.5f * clawScale);
		//x - x_offset - 0.05f, y - 0.4f
		clawSprite.setOriginCenter();
		clawSprite.setRotation((float) Math.toDegrees(rightArm.getAngle()));
		clawSprite.draw(game.batch);
		droneSprite.setBounds(position.x - targetWidth / 2, position.y - targetHeight / 2, targetWidth, targetHeight);
		droneSprite.setOriginCenter();
		droneSprite.setRotation((float) Math.toDegrees(angle));
		droneSprite.draw(game.batch);
		if (game.drawDebug && alive) game.shapeDrawer.polygon(push.x, push.y, 6, 0.1f);
	}
	public void setHolding(int addr) {
		cock.play(); // sound plays when contact listener notes player is holding box
		holding = addr;
	}
	public void clearHolding() {
		holding = 0;
	}
	public void kill() {
		alive = false;
		droneSprite.setTexture(disabledTex);
		if (ENABLE_ARMS) {
			leftArmJoint.enableMotor(false);
			rightArmJoint.enableMotor(false);
		} 
	}
	@Override
	public void delete() {
		rotorSound.stop();
		rotorSound.dispose();
		Level.world.destroyBody(body);
		if (ENABLE_ARMS) {
			Level.world.destroyBody(leftArm);
			Level.world.destroyBody(rightArm);
		}
	}
	protected static void dispose() {
		idleTex.dispose();
		disabledTex.dispose();
		for (Texture texture : flight_frames) {
			texture.dispose();
		}
	}
}