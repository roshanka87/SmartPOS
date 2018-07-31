
package com.emv.callback;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.example.admin.smartpos.R;
import com.justtide.osapp.util.CodeConversion;

import java.text.SimpleDateFormat;
import java.util.Date;

import justtide.ContactCard;
import justtide.EmvAidList;
import justtide.EmvLibParam;
import justtide.EmvTermParam;
import justtide.Emvl2;
import justtide.IccReader;
import justtide.PiccReader;
import justtide.SystemMiscService;

import static com.justtide.osapp.util.CodeConversion.byteToString;
import static com.justtide.osapp.util.CodeConversion.byteToString_two;


public class EMVCallBack  {
	protected static final String TAG = "EMVTEST";
	protected static final int CHECKCARD = 0;
	protected static final int REFRESH = 1;

	boolean blnCheck = true;// IC检测标志位 ture:检测 false: 不检测//IC card check flag if
	// true: check else false: don't check
	boolean blnEmvPro = true;// emv处理线程标志位 //emv process thread flag
	private Thread mThread = null;
	private Thread EmvThread = null;
	private Handler mHandler = null;
	private TextView show;
	private String strShow = "";
	private byte[] glOnlinePin = new byte[20];

	private int glOnlineTrans;
	private int gRandSelectNum;
	private int gPrintSignLine;
	private int gAmtConfirmed;
	private long AmtAuthBin, AmtNet, AmtTrans, AmtOtherBin;

	EmvTermParam gEmvTermParam = new EmvTermParam();
	EmvLibParam gEmvLibParam = new EmvLibParam();
	Emvl2 emvl2 = Emvl2.getInstance();
	IccReader iccReader = IccReader.getInstance();
	PiccReader piccReader = PiccReader.getInstance();
	SystemMiscService systemMiscService = SystemMiscService.getInstance();
	ContactCard contactCard = null;
	final String FIRST_LOGIN = "FIRST_LOGIN";

	public static String read_CardType="";
	public static String read_CardNo="";
	/*
	 * The following functions all are callback function.
	 */

	/**
	 * wait select app if card have multi-application
	 *
	 * @param tryCnt
	 * @param emvAidList
	 * @param appNum
	 * @return
	 */
	public int waitAppSel(int tryCnt, EmvAidList emvAidList[], int appNum) {
		strShow += "Callback-->waitAppSel()\n";
		return 0;
	}

	/**
	 *
	 * in put trans amount
	 *
	 * @param authAmt
	 *            amount
	 * @param cashBackAmt
	 *            default 0
	 * @return 0 input success -5 input timeout -4 cances input
	 */
	public int inPutAmt(long[] authAmt, long[] cashBackAmt, int mode) {
		/* authAmt: amount，cashBackAmt：default :0 */

		strShow += "Callback-->inPutAmt()\n";
		long[] lauthAmt = { 10 };
		long[] lcashBackAmt = { 0 };
		System.arraycopy(lauthAmt, 0, authAmt, 0, lauthAmt.length);
		System.arraycopy(lcashBackAmt, 0, cashBackAmt, 0, lcashBackAmt.length);

		return 0;
	}


	public int getHolderPwd(int tryFlag, int remainCnt, byte[] pin, int nMode) {
		strShow += "Callback-->getHolderPwd()\n";
		if (pin == null) {

		}

		Log.v("TEST", "++++++++++++++getHolderPwd++++++++++++++");
		Log.v("TEST", "tryFlag = " + tryFlag + "remainCnt=" + remainCnt);
		if (pin != null) {
			String test = "12345678";
			System.arraycopy(test.getBytes(), 0, pin, 0, test.getBytes().length);
			Log.v("PINBLOCK", test);

		}
		return 0;

	}

	/**
	 *
	 */
	public int referProc() {
		strShow += "Callback-->referProc()\n";
		return 1;
	}

	/**
	 *
	 */
	public int getUnknowTLV(short rag, byte[] dat, int len) {
		return 1;
	}

	public void adviceProc() {

	}

	public void verifyPINOK() {

	}

	/**
	 *
	 */
	public int verifyCert() {
		return 0;
	}

	/**
	 *
	 */
	public void checkExceptionFile() {

	}

	/**
	 * setting datetime
	 *
	 * @param datetime
	 *            format（YYMMDDHHMMSS），BCD code
	 * @return
	 */
	public int getDateTime(byte[] datetime) {
		long currentTime = System.currentTimeMillis();
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyMMddHHmmss");
		String str = timeFormat.format(new Date(currentTime));
		byte[] time = CodeConversion.AscToBcd(str.getBytes(), str.length());
		System.arraycopy(time, 0, datetime, 0, time.length);
		Log.v("TIEM", "time:" + byteToString(time, 6));
		System.arraycopy(time, 0, datetime, 0, time.length);
		Log.v("TIEM", "datetime2:" + byteToString(datetime, 6));
		Log.v("TEST", "callback end");
		return 0;
	}

	public int sendDebugChar(byte ch) {
		// Log.v(TAG, "sendDebugChar java:" + ch);
		return 0;
	}


	public int onlineProc(byte[] rspCode, byte[] authCode, int[] authCodeLen,
						  byte[] iAuthData, int[] iAuthDataLen, byte[] script, int[] scriptLen) {
		strShow += "Callback-->onlineProc()\n";
		Log.v("TEST", "onlineProc");
		byte[] track = new byte[120];
		int[] len = new int[2];
		String test = "1234567890";
		int iRet = emvl2.getTLV((short) 0x57, track, len);// track
		Log.v("TEST", "0x57 data = " + byteToString(track, len[0]));
		iRet = emvl2.getTLV((short) 0x9f26, track, len);// track
		Log.v("TEST", "0x9f26 data = " + byteToString(track, len[0]));
		iRet = emvl2.getTLV((short) 0x9f27, track, len);// track
		Log.v("TEST", "0x9f27 data = " + byteToString(track, len[0]));
		iRet = emvl2.getTLV((short) 0x9f10, track, len);// track
		Log.v("TEST", "0x9f10 data = " + byteToString(track, len[0]));
		iRet = emvl2.getTLV((short) 0x5f34, track, len);// track
		Log.v("TEST", "0x5f34 data = " + byteToString(track, len[0]));


		// emvl2.getTLV((short) 0x57, span);// track2
		int iRet1 = emvl2.getTLV((short) 0x5A, track, len);// CardNO
		read_CardNo=byteToString(track, len[0]);
		Log.v("TEST", "0x5A data = " + byteToString(track, len[0]));

		int iRet2 = emvl2.getTLV((short) 0x5F20, track, len);// Card Holder
		read_CardType=byteToString_two(track, len[0]);
		Log.v("TEST", "0x5F20 data = " + byteToString_two(track, len[0]));

		System.arraycopy(test.getBytes(), 0, iAuthData, 0,
				test.getBytes().length);
		int[] intest = new int[1];
		intest[0] = 10;
		System.arraycopy(intest, 0, iAuthDataLen, 0, 1);
		// iAuthDataLen[0] = 10;
		Log.v("TEST", "iAuthDataLen = " + iAuthDataLen[0]);
		Log.v("TEST", "emvl2.getTLV ret = " + iRet);
		Log.v("TEST", "Track 2 data = " + byteToString(track, iRet));
		return 0;
	}

	public int registerCallBack() {
		int ret = -1;
		ret = _emv_registercallback();
		return ret;
	}

	private native int _emv_registercallback();

	static {
		System.load("/system/lib/libposemvl2registercallback.so");
	}
}
