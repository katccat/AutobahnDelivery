package net.clayrobot.delivery;

import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;

public class Shapes {
	private final static HashMap<String, Vector2[]> shapes = new HashMap<String, Vector2[]>();
	private static final Vector2[] ROCK = {
		new Vector2(1, 0.3f),
		new Vector2(0.4f, 1),
		new Vector2(-0.3f, 1),
		new Vector2(-1, 0.2f),
		new Vector2(-0.4f, 0),
		new Vector2(-1, -0.3f),
		new Vector2(-0.5f, -1),
		new Vector2(0, -0.3f),
		new Vector2(0, -1),
		new Vector2(0, -1),
		new Vector2(0, -1),
		new Vector2(0.6f, -1),
		new Vector2(1, -0.4f)
	};
	private static final Vector2[] ARROW = {
		new Vector2(0, 1),
		new Vector2(-4 / 7f, -1),
		new Vector2(0, -4 / 7f),
		new Vector2(4 / 7f, -1)
	};
	private static final Vector2[] TRIANGLE = {
		new Vector2(-1, -0.866f),
		new Vector2(0, 0.866f),
		new Vector2(1, -0.866f),
		new Vector2(0, -0.5f)
	};
	private static final Vector2[] LEFT_ARM_TOP = {
		new Vector2(-0.75f, 0),
		new Vector2(0, 0),
		new Vector2(0.5f, -0.8f),
		new Vector2(0.5f, -1)
	};
	private static final Vector2[] LEFT_ARM_BOTTOM = {
		new Vector2(-0.5f, 1),
		new Vector2(0.5f, 1),
		new Vector2(0, 0),
		new Vector2(-0.75f, 0)
	};
	private static final Vector2[] RIGHT_ARM_TOP = new Vector2[LEFT_ARM_TOP.length];
	private static final Vector2[] RIGHT_ARM_BOTTOM = new Vector2[LEFT_ARM_BOTTOM.length];
	static {
		for (int vertex = RIGHT_ARM_TOP.length - 1; vertex >= 0; vertex--) {
			RIGHT_ARM_TOP[vertex] = new Vector2(LEFT_ARM_TOP[vertex].x * -1, LEFT_ARM_TOP[vertex].y);
		}
		for (int vertex = RIGHT_ARM_BOTTOM.length - 1; vertex >= 0; vertex--) {
			RIGHT_ARM_BOTTOM[vertex] = new Vector2(LEFT_ARM_BOTTOM[vertex].x * -1, LEFT_ARM_BOTTOM[vertex].y);
		}
		shapes.put("ROCK", ROCK);
		shapes.put("ARROW", ARROW);
		shapes.put("TRIANGLE", TRIANGLE);
		shapes.put("LEFT_ARM_TOP", LEFT_ARM_TOP);
		shapes.put("LEFT_ARM_BOTTOM", LEFT_ARM_BOTTOM);
		shapes.put("RIGHT_ARM_TOP", RIGHT_ARM_TOP);
		shapes.put("RIGHT_ARM_BOTTOM", RIGHT_ARM_BOTTOM);
	}
	
	public static Vector2[] get(String shapeKey, float scale) {
		Vector2[] sourceShape = shapes.get(shapeKey);
		Vector2[] newShape = new Vector2[sourceShape.length];
		for (int vertex = newShape.length - 1; vertex >= 0; vertex--) {
			newShape[vertex] = new Vector2(sourceShape[vertex].x * scale, sourceShape[vertex].y * scale);
		}
		return newShape;
	}
	public static Vector2[] get(String shapeKey, float scaleX, float scaleY) {
		Vector2[] sourceShape = shapes.get(shapeKey);
		Vector2[] newShape = new Vector2[sourceShape.length];
		for (int vertex = newShape.length - 1; vertex >= 0; vertex--) {
			newShape[vertex] = new Vector2(sourceShape[vertex].x * scaleX, sourceShape[vertex].y * scaleY);
		}
		return newShape;
	}
}
