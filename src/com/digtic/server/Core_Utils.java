package com.digtic.server;

public class Core_Utils {
	static byte[] swapArray(byte s[]){
		byte[] aux = new byte[s.length];

		for(int i=0; i<s.length; i++){
			aux[i]=s[s.length-i-1];
		}
		return aux;
	}
	
    static boolean equalsBytes(byte a[], byte b[]){
    	if(a==null && b==null)return true;
		if(a==null || b==null)return false;
		if(a.length!=b.length)return false;
		
		for(int i=0; i<a.length; i++){
			if(a[i]!=b[i])return false;
		}
		return true;
	}
    
    public static int swapInt(int value) {
        return
            ( ( ( value >> 0 ) & 0xff ) << 24 ) +
            ( ( ( value >> 8 ) & 0xff ) << 16 ) +
            ( ( ( value >> 16 ) & 0xff ) << 8 ) +
            ( ( ( value >> 24 ) & 0xff ) << 0 );
    }
    
    public static byte[] short2byte(short[]src) {
	    int srcLength = src.length;
	    byte[]dst = new byte[srcLength*2];
	    
	    for (int i=0; i<srcLength; i++) {
	        int x = src[i];
	        int j = i * 2;
	        dst[j++] = (byte) ((x >>> 8) & 0xff);
	        dst[j++] = (byte) ((x >>> 0) & 0xff);
	    }
	    return dst;
	}
	
    public static byte[] int2byte(int[]src) {
	    int srcLength = src.length;
	    byte[]dst = new byte[srcLength << 2];
	    
	    for (int i=0; i<srcLength; i++) {
	        int x = src[i];
	        int j = i << 2;
	        dst[j++] = (byte) ((x >>> 24) & 0xff);           
	        dst[j++] = (byte) ((x >>> 16) & 0xff);
	        dst[j++] = (byte) ((x >>> 8) & 0xff);
	        dst[j++] = (byte) ((x >>> 0) & 0xff);
	    }
	    return dst;
	}
    
    public static int toInt(byte[] bytes, int offset) {
		int ret = 0;
		for (int i=0; i<4 && i+offset<bytes.length; i++) {
			ret <<= 8;
			ret |= (int)bytes[i] & 0xFF;
		}
		return ret;
	}
    
	public static byte[] float2byte(float[]src) {
	    int idst[]=new int[src.length];
	    for(int i=0; i<src.length; i++)
	    	idst[i]=Float.floatToIntBits(src[i]);

	    return int2byte(idst);
	}
    static boolean equalsStrings(String a, String b){
    	if(a==null && b==null)return true;
    	if(a==null || b==null)return false;
    	
    	return a.equals(b);
	}
    
    public static String bytesToHex(byte[] bytes) {
		final char[] hexArray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j*2+0] = hexArray[(v&0xF0)>>>4];
			hexChars[j*2+1] = hexArray[(v&0x0F)>>>0];
		}
		return new String(hexChars);
	}
}
