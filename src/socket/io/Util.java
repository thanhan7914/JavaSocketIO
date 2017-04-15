package socket.io;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Util {
	public static byte[] toByteArray(String src) {
		return src.getBytes();
	}
	
	public static byte[] stringToByteArray(String src, String charset){
		try {
			return src.getBytes(charset);
		}
		catch(UnsupportedEncodingException exc)
		{
			return new byte[]{};
		}
	}
	
	public static byte[] stringToByteArrayWithUtf8(String src) {
		return stringToByteArray(src, "UTF-8");
	}
	
	public static byte[] toByteArray(long... longs) {
		int len = longs.length;
		ByteBuffer bbuf = ByteBuffer.allocate(len * 8);
		
		for(long value:longs)
			bbuf.putLong(value);
		
		return bbuf.array();
	}
	
	public static byte[] toByteArray(double... doubles) {
		int len = doubles.length;
		ByteBuffer bbuf = ByteBuffer.allocate(len * 8);
		
		for(double value:doubles)
			bbuf.putDouble(value);
		
		return bbuf.array();
	}
	
	public static byte[] toByteArray(float... floats) {
		int len = floats.length;
		ByteBuffer bbuf = ByteBuffer.allocate(len * 4);
		
		for(float value:floats)
			bbuf.putFloat(value);
		
		return bbuf.array();
	}
	
	public static byte[] toByteArray(int... integers) {
		byte[] data = new byte[integers.length * 4];
		
		for(int i = 0; i < integers.length; i++)
		{
			int offset = i * 4;
			data[offset] = (byte)(integers[i] & 0xff);
			data[offset + 1] = (byte)((integers[i] << 8) & 0xff);
			data[offset + 2] = (byte)((integers[i] << 16) & 0xff);
			data[offset + 3] = (byte)((integers[i] << 24) & 0xff);
		}
		
		return data;
	}
	
	public static long[] byteArrayToLongArray(byte[] data) {
		int len = data.length / 8;
		long[] larray = new long[len];
		
		for(int i = 0; i < len; i++)
		{
			int offset = i * 8;
			larray[i] = byteArrayToLong(new byte[]{
					data[offset], data[offset + 1], data[offset + 2], data[offset + 3],
					data[offset + 4], data[offset + 5], data[offset + 6], data[offset + 7]
					});
		}
		
		return larray;
	}
	
	public static double[] byteArrayToDoubleArray(byte[] data) {
		int len = data.length / 8;
		double[] darray = new double[len];
		
		for(int i = 0; i < len; i++)
		{
			int offset = i * 8;
			darray[i] = byteArrayToDouble(new byte[]{
					data[offset], data[offset + 1], data[offset + 2], data[offset + 3],
					data[offset + 4], data[offset + 5], data[offset + 6], data[offset + 7]
					});
		}
		
		return darray;
	}
	
	public static float[] byteArrayToFloatArray(byte[] data) {
		int len = data.length / 4;
		float[] farray = new float[len];
		
		for(int i = 0; i < len; i++)
		{
			int offset = i * 4;
			farray[i] = byteArrayToFloat(new byte[]{data[offset], data[offset + 1], data[offset + 2], data[offset + 3]});
		}
		
		return farray;
	}
	
	public static int[] byteArrayToIntArray(byte[] data) {
		int len = data.length / 4;
		int[] iarray = new int[len];
		
		for(int i = 0; i < len; i++)
		{
			int offset = i * 4;
			iarray[i] = byteArrayToInt(new byte[]{data[offset], data[offset + 1], data[offset + 2], data[offset + 3]});
		}
		
		return iarray;
	}
	
	public static byte[] toByteArray (boolean value) {
		return new byte[]{(byte)(value ? 1 : 0)};
	}
	
	public static byte booleanToByte (boolean value) {
		return (byte)(value ? 1 : 0);
	}
	
	public static boolean byteToBoolean(byte b) {
		return b != 0;
	}
	
	public static byte[] floatToByteArray (float value) {  
	     return ByteBuffer.allocate(4).putFloat(value).array();
	}
	
	public static float byteArrayToFloat (byte[] bytes) {
		return ByteBuffer.wrap(bytes).getFloat();
	}
	
	public static byte[] doubleToByteArray (double value) {  
	     return ByteBuffer.allocate(8).putDouble(value).array();
	}
	
	public static double byteArrayToDouble (byte[] bytes) {
		return ByteBuffer.wrap(bytes).getDouble();
	}
	
	public static byte[] longToByteArray(long value) {
		return ByteBuffer.allocate(8).putLong(value).array();
	}
	
	public static long byteArrayToLong(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getLong();
	}
	
	public static byte[] intToByteArray(int value) {
		byte[] data= new byte[4];
		data[0] = (byte) (value & 0xff);
		data[1] = (byte) ((value << 8) & 0xff);
		data[2] = (byte) ((value << 16) & 0xff);
		data[3] = (byte) ((value << 24) & 0xff);
		
		return data;
	}
	
	public static int byteArrayToInt(byte[] data) {
		return data[0] | (data[1] >> 8) | (data[2] >> 16) | (data[3] >> 24);
	}
	
	public static byte[] byteArrayConcat(byte[] a1, byte[] a2) {
		int len = a1.length + a2.length;
		byte[] data = new byte[len];
		System.arraycopy(a1, 0, data, 0, a1.length);
		System.arraycopy(a2, 0, data, a1.length, a2.length);
		
		return data;
	}
	
	public static String byteArrayToString(byte[] data, String charset) {
		try {
			return new String(data, charset);
		}
		catch (Exception exc) {
			return null;
		}
	}
}
