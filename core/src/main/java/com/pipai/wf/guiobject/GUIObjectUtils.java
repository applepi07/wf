package com.pipai.wf.guiobject;

public class GUIObjectUtils {
	
	public static boolean isInCircle(float centerX, float centerY, float radius, float x, float y) {
		return Math.pow(x - centerX, 2.0) + Math.pow(y - centerY, 2.0) <= radius*radius;
	}
	
	public static boolean isInRectangle(float blX, float blY, float width, float height, float x, float y) {
		return x >= blX && x <= blX + width && y >= blY && y <= blY + height;
	}
	
	public static boolean isInBoundingBox(float centerX, float centerY, float width, float height, float x, float y) {
		return isInRectangle(centerX - width/2, centerY - height/2, width, height, x, y);
	}
	
}