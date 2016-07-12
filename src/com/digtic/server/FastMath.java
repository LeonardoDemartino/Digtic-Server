package com.digtic.server;

public class FastMath {
	static float epsilon=0.000001f;
	
	public static float sin(float a){
		return (float)Math.sin(a);
	}
	
	public static float cos(float a){
		return (float)Math.cos(a);
	}
	
	public static float fastAtan2(float y, float x) { 
		float abs_y = Math.abs(y); 
		float angle; 
		if (x >= 0) { 
			float r = (x - abs_y) / (x + abs_y); 
			angle = (float) (3.1415927 / 4) - (float) (3.1415927 / 4) * r; 
		} else { 
			float r = (x + abs_y) / (abs_y - x); 
			angle = 3f * ((float) (3.1415927 / 4)) - (float) (3.1415927 / 4) * r; 
		} 
		return y < 0 ? -angle : angle;
	}

	public static float angleFromTwoVectors(float x1, float y1, float x2, float y2)
	{
		return (float) Math.toDegrees(fastAtan2(x1 - x2, y2 - y1));
	}

	public static float sqrt(float p){
		float x=(float)p;
		return Float.intBitsToFloat(532483686 + (Float.floatToRawIntBits(x) >> 1));
	}

	public static float abs(float x){
		return x>0?x:x*-1;
	}

	public static int floor(double x) {
		int xi = (int)x;
		return (x<xi) ? xi-1 : xi;
	}
	public static int ceil(double x) {
		int xi = (int)x;
		return (x-xi)>0.0F ? xi+1 : xi;
	}
	
	//Always positive modulo
	public static int rem(int a, int b){
		return a&(b-1);
	}
	
	public static boolean floatEquals(float a, float b){
		return Math.abs(a - b) < epsilon;
	}
}
