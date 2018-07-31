package com.justtide.osapp.util;

public class SinWave {
	/** ���Ҳ��ĸ߶� **/
	public static final int HEIGHT = 127;
	/** 2PI **/
	public static final double TWOPI = 2 * 3.1415;

	/**
	 * �������Ҳ�
	 * 
	 * @param wave
	 * @param waveLen
	 *            ÿ�����Ҳ��ĳ���
	 * @param length
	 *            �ܳ���
	 * @return
	 */
	public static byte[] sin(byte[] wave, int waveLen, int length) {
		for (int i = 0; i < length; i++) {
			wave[i] = (byte) (HEIGHT * (1 - Math.sin(TWOPI * ((i % waveLen) * 1.00 / waveLen))));
		}
		return wave;
	}
}
