package com.example.admin.smartpos;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emv.callback.EMVCallBack;
import com.justtide.osapp.util.AudioTrackManager;
import com.justtide.osapp.util.DialogService;
import com.justtide.osapp.util.HttpClient;
import com.justtide.osapp.util.SettingUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;

import justtide.CommandApdu;
import justtide.ContactCard;
import justtide.ContactlessCard;
import justtide.EmvAidList;
import justtide.EmvCapkList;
import justtide.EmvLibParam;
import justtide.EmvTermParam;
import justtide.Emvl2;
import justtide.IccException;
import justtide.IccReader;
import justtide.MagcardReader;
import justtide.MagneticCard;
import justtide.PiccException;
import justtide.PiccInterface;
import justtide.PiccReader;
import justtide.ResponseApdu;
import justtide.SystemMiscService;
import justtide.ThermalPrinter;
import okhttp3.Response;

import static android.net.sip.SipErrorCode.TIME_OUT;
import static com.justtide.osapp.util.CodeConversion.AscToBcd;
import static com.justtide.osapp.util.CodeConversion.byteToString;
import static com.justtide.osapp.util.CodeConversion.convert;
import static justtide.PiccReader.USER_CANCEL;

public class fragment_cardpayment extends Fragment {
	private AlertDialog dialog;

	protected static final String TAG = "EMVTEST";
	protected static final int CHECKCARD = 0;
	protected static final int REFRESH = 1;
	protected static final int CHECK_SUCCESS = 0;
	protected static final int RESPONSE_APDU = 1;
	protected static final int OPEN_FAIL = 2;
	protected static final int REFRESH_EXC = 6;
	protected static final int REFRESH_M = 7;

	boolean isClosed = false;

	boolean blnEmvPro = true;

	private Thread magThread = null;
	private Thread nfcThread = null;
	private Thread mThread = null;
	private Thread icThread = null;

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
	EMVCallBack callBack = new EMVCallBack();
	MagneticCard magneticCard;
	private AudioTrackManager audio = new AudioTrackManager();
	MagcardReader magcardReader = MagcardReader.getInstance();
	private TextView showStateText;
	private String dataShowString = "";
	private TextView showDataText;
	private boolean bThreadflag = true;
	private boolean NfcThreadflag = true;
	String title_data = "";
	private Context mContext;
	protected static final int REFRESH_TITLE = 1;

	protected static final int REFRESHD = 1;
	protected static final int RETURNDATA = 2;
	protected static final int CLEARDATA = 3;
	byte[] apdu = {0x00, (byte) 0xA4, 0x00, 0x00, 0x02, 0x3f, 0x00};
	CommandApdu commandApdu;
	ResponseApdu responseApdu;
	boolean blnCheck = true;

	private ContactlessCard contactlessCard;
	String TransactionId = "";
	String TransactionAmount = "";
	String CardHolder = "";

	private String strResponseApdu = "";

	private boolean bLogo;
	private int ilevel = 0;
	private String ticketString = "";
	private byte[] buffer;
	private Thread PrinterThread;
	ThermalPrinter thermalPrinter = ThermalPrinter.getInstance();

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		if (getActivity() != null) {
			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					if (msg.what == CHECKCARD) {
						if (icThread == null) {
							icThread = new MyICThread();
							icThread.start();
						}
					} else if (msg.what == REFRESH) {
						show.setText(strShow);
					}
					super.handleMessage(msg);
				}
			};
		}
		ConnectDevice();

		if (magThread == null) {
			magThread = new MyMAGThread();
			magThread.start();
		}


//		mHandler = new Handler() {
//			public void handleMessage(Message msg) {
//				if (msg.what == CHECK_SUCCESS) {
//					audio.start(3000);
//					audio.play();
//					for (int i = 0; i < 10; i++) {
//					}
//					audio.stop();
//					String strCardType = "";
//					byte[] serialNo = contactlessCard.getSerialNo();
//					String strSerialNo = byteToString(serialNo, serialNo.length);
//					Log.i(TAG, "serialNo:" + strSerialNo);
//					int state = contactlessCard.getState();
//					Log.i(TAG, "state:" + state);
//					byte type = contactlessCard.getType();
//					Log.i(TAG, "type:" + type);
//					if (type == ContactlessCard.TYPE_A) {
//						strCardType = "A";
//					} else if (type == ContactlessCard.TYPE_B) {
//						strCardType = "B";
//					} else if (type == ContactlessCard.TYPE_MIFARE) {
//						strCardType = "M";
//					} else if (type == ContactlessCard.TYPE_FELICA) {
//						strCardType = "C";
//					} else
//						strCardType = "Unkown";
//					Log.i(TAG, "type:" + strCardType);
//					dataShowString = "type:" + strCardType + "\n" + "serialNo:" + strSerialNo + "\n" + "state:" + state + "\n";
//					//showStateText.setText(stateString);
//					//showStateText.setText(R.string.piccdemo_show_success);
//					showDataText.setText(dataShowString);
//				} else if (msg.what == RESPONSE_APDU) {
//					//snShowText.setText(snShowString);
//					audio.start(3000);
//					audio.play();
//					for (int i = 0; i < 10; i++) {
//					}
//					audio.stop();
//					byte[] responseData = responseApdu.getData();
//					int iSW1 = responseApdu.getSW1();
//					int iSW2 = responseApdu.getSW2();
//					//   Integer.toHexString()
//
//					strResponseApdu = "Send:" + byteToString(apdu, apdu.length);
//					strResponseApdu += "\n" + "Resp:";
//					strResponseApdu += byteToString(responseData, responseData.length) + "\n";
//					strResponseApdu += "SW:" + IntToString(iSW1) + " " + IntToString(iSW2);
//
//					//showStateText.setText(R.string.piccdemo_show_success);
//					//showApduText.setText(strResponseApdu);
//				} else if (msg.what == OPEN_FAIL) {
//					//showStateText.setText(stateString);
//				} else if (msg.what == REFRESH) {
//					//showStateText.setText(stateString);
//				} else if (msg.what == USER_CANCEL) {
//					//showStateText.setText(R.string.piccdemo_user_cancel);
//
//				} else if (msg.what == TIME_OUT) {
//					//showStateText.setText(R.string.piccdemo_time_out);
//				} else if (msg.what == REFRESH_EXC) {
//					//showStateText.setText(stateString);
//				} else if (msg.what == REFRESH_M) {
//					//showApduText.setText(stateString);
//				}
//				super.handleMessage(msg);
//			}
//		};
//
//		if (nfcThread == null) {
//
//			nfcThread = new MyNFCThread();
//			nfcThread.start();
//		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_cardpayment, container, false);
	}

	@Override
	public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		EditText txtAmount = (EditText) getActivity().findViewById(R.id.editText_Amount);
		txtAmount.requestFocus();

		getActivity().setTitle("Card Payment");

		ImageButton relative_CardPaymentBacktoHome = (ImageButton) view.findViewById(R.id.relative_CardPaymentBacktoHome);
		relative_CardPaymentBacktoHome.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.relative_BacktoHome, null);
			}
		});

		Button button_Pay = (Button) view.findViewById(R.id.button_Pay);
		button_Pay.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				AsyncTask_Validation validation = new AsyncTask_Validation();
				validation.execute();

			}
		});


	}

	class AsyncTask_Validation extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {

				EditText txtAmount = (EditText) getActivity().findViewById(R.id.editText_Amount);
				TextView txtCardNo = (TextView) getActivity().findViewById(R.id.textView_CardNo);
				TextView txtCardHolder = (TextView) getActivity().findViewById(R.id.textView_CardHolder);

				CardHolder = txtCardHolder.getText().toString();

				String Card = txtCardNo.getText().toString();
				int TotLen = Card.length();
				String Strt4 = Card.substring(0, 4);
				String End4 = Card.substring(TotLen - 4, TotLen);

				String Hash = "";
				int LoopLen = TotLen - 8;
				for (int l = 0; l < LoopLen; l++) {
					Hash = Hash + "*";
				}

				String FinalCardNo = Strt4 + Hash + End4;

				TransactionAmount = txtAmount.getText().toString();

				String AmtVal = txtAmount.getText().toString();
				if (AmtVal.equals("")) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
							builder.setMessage("Invalied Amount");
							dialog = builder.create();
							dialog.show();
						}
					});

					return null;
				}

				double Amt = 0;
				Amt = Double.valueOf(txtAmount.getText().toString());
				if (Amt == 0) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
							builder.setMessage("Invalied Amount");
							dialog = builder.create();
							dialog.show();
						}
					});

					return null;
				}

				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						DialogService.progressDialog((activity_main) getContext(), true);
					}
				});
				String url = "http://" + getString(R.string.server_name) + "/smartpos/WebAPI.php?Type=TransactionCommit&TerminalId=1&DocumentType=CP&Amount=" + txtAmount.getText().toString() + "&AccountNo=" + FinalCardNo;
				HttpClient client = new HttpClient();
				Response response = client.get(url);

				if (!response.isSuccessful()) {
					return null;
				} else {

					String result = response.body().string();
					JSONObject reader = new JSONObject(result);
					JSONArray nameTable = reader.getJSONArray("Result");
					JSONObject item = nameTable.getJSONObject(0);
					TransactionId = item.getString("TranId");

					printTicket(TransactionId);

					if (!TransactionId.toString().equals("0")) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {

								String Para = TransactionId + "-" + TransactionAmount + "- Card Payment";
								((activity_main) getActivity()).displaySelectedScreen(R.id.button_Pay, Para);
							}
						});


					} else {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
								builder.setCancelable(false);
								builder.setMessage("Some error found");
								dialog = builder.create();
								dialog.show();
							}
						});

					}

					DialogService.progressDialog((activity_main) getContext(), false);
				}
			} catch (IOException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DialogService.progressDialog((activity_main) getContext(), false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("IOException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
					}
				});
			} catch (JSONException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DialogService.progressDialog((activity_main) getContext(), false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("JSONException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
					}
				});
			} catch (NetworkOnMainThreadException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DialogService.progressDialog((activity_main) getContext(), false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("NetworkOnMainThreadException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
					}
				});
			}
			return null;
		}
	}




	//Chip Card Read
	public int ConnectDevice() {
		int ret = -1;
		ret = callBack.registerCallBack();
		Log.v(TAG, "registerCallBack ret:" + ret);
		if (ret == 0) {
			strShow = "RegisterCallBack OK \n";
		} else {
			strShow = "RegisterCallBack Fail \n";
		}

		/**
		 * init() emv init
		 */
		emvl2.appInit(getContext());
		// must add following code when run at firt and only need run this code
		// once
		if (!SettingUtil.getPrefer(getContext()).getBoolean(FIRST_LOGIN, false)) {
			Log.v(TAG, "First Run!");
			emvl2.emvDelAllParaFiles();
			emvl2.emvInit();
			Emvl2.getInstance().emvFirstRunInit();
			SettingUtil.getPrefer(getContext()).putBoolean(FIRST_LOGIN, true);
		}
		ret = emvl2.emvInit();
		if (ret == 0) {
			strShow += "emvl2.init() OK \n";
		} else {
			strShow += "emvl2.init() Fail \n";
			strShow = strShow + "ret = " + ret + "\n";
		}

		/**
		 * if emv init success do some setting
		 */
		if (ret == 0) {
			/* 设置capk和应用参数，需要从服务器下载 */
			/* setting capk and APP param，need download from server */
			/* 每次下载新的IC应用参数前需要清除掉已有的IC应用参数数据 */
			/* if download the new AID data, need delete the all old AID data */
			emvl2.emvEmvDeleteAllAidList();
			InitAllApp();

			/* 每次下载新的IC公钥参数前需要清除掉已有的IC公钥参数数据 */
			/* if download the new CAPK data, need delete the all old CAPK data */
			emvl2.emvEmvDeleteAllCapkList();
			InitAllCapk();

			/* 获取emv库相关参数 */
			/* get emv lib param */
			ret = emvl2.getLibParam(gEmvLibParam);
			Log.v(TAG, "getLibParam ret:" + ret);
			String strMerchName = "test";
			gEmvLibParam.transType = 2;
			gEmvLibParam.merchName = strMerchName.getBytes();// 商户名 //Merchant
			// name
			String strMerchID = "1111";
			gEmvLibParam.merchId = strMerchID.getBytes();// 商户号 //Merchant ID
			String strTermId = "123";
			gEmvLibParam.termId = strTermId.getBytes(); // 终端号 // Terminal ID
			byte[] country = {0x01, 0x68};
			gEmvLibParam.countryCode = country;// 终端国家代码 //Country code
			byte[] transCurr = {0x01, 0x68};
			gEmvLibParam.transCurrCode = transCurr;// 交易货币代码 //Currency Code Of
			// Transaction
			byte[] referCurr = {0x01, 0x68};
			gEmvLibParam.referCurrCode = referCurr; // 参考货币代码 //The reference
			// currency code
			byte[] capability = {(byte) 0xE0, (byte) 0xF1, (byte) 0xC8};
			gEmvLibParam.capability = capability;// 终端性能 //terminal capability

			/* 设置emv相关参数,只需应用第一次启动的时候设置即可 */
			/* setting emv lib param */
			ret = emvl2.setLibParam(gEmvLibParam);
			Log.v(TAG, "setLibParam ret:" + ret);
			/* 获取终端相关参数 */
			/* get terminal param */
			ret = emvl2.getTermParam(gEmvTermParam);
			Log.v(TAG, "getTermParam ret:" + ret);
			gEmvTermParam.getDataPIN = 1;
			gEmvTermParam.supportPSESel = 1;

			/* 设置终端参数 */
			/* setting terminal param */
			ret = emvl2.setTermParam(gEmvTermParam);
			Log.v(TAG, "setTermParam ret:" + ret);
			if (ret == 0) {
				strShow += "setTermParam OK \n";
			} else {
				strShow += "setTermParam Fail \n";
			}

		}
		if (mThread == null) {
			mThread = new CheckIcCardThread();
			mThread.start();

			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setCancelable(false);
			builder.setMessage("Searching Card....\n\nDo you want to end Process");
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
//					if (mThread.isAlive()) {
//						Log.i(TAG, "onClick: mThread thread is alive");
//						mThread.interrupt();
//
//					} else {
//						Log.i(TAG, "onClick: mThread thread was killed");
//					}
//					if (icThread != null) {
//						if (icThread.isAlive()) {
//							Log.i(TAG, "onClick: icThread thread is alive");
//							icThread.interrupt();
//						} else {
//							Log.i(TAG, "onClick: icThread thread was killed");
//						}
//					}
					isClosed = true;
					((activity_main) getActivity()).displaySelectedScreen(R.id.nav_home, null);

				}
			});
			dialog = builder.create();
			dialog.show();


		} else {
			if (!magThread.isAlive())
				magThread.start();
		}

		return 0;


	}

	public void InitAllApp() {
		int ret = -1;
		String aidName = "test";
		String straAid1 = "A000000333010101";
		String straAid2 = "A0000000033010";
		String straAid3 = "A0000000043060";
		String straAid4 = "A00000002501";
		String straAid5 = "A000000333010102";
		String straAid6 = "A000000333010103";
		String straAid7 = "A000000333010106";
		String straAid8 = "A0000001523010";
		String straAid9 = "A0000000031010";
		String straAid10 = "A0000000032010";
		String straAid11 = "A0000000041010";
		String straAid12 = "A0000000651010";

		byte[] aid1 = AscToBcd(straAid1.getBytes(), straAid1.getBytes().length);
		byte[] aid2 = AscToBcd(straAid2.getBytes(), straAid2.getBytes().length);
		byte[] aid3 = AscToBcd(straAid3.getBytes(), straAid3.getBytes().length);
		byte[] aid4 = AscToBcd(straAid4.getBytes(), straAid4.getBytes().length);
		byte[] aid5 = AscToBcd(straAid5.getBytes(), straAid5.getBytes().length);
		byte[] aid6 = AscToBcd(straAid6.getBytes(), straAid6.getBytes().length);
		byte[] aid7 = AscToBcd(straAid7.getBytes(), straAid7.getBytes().length);
		byte[] aid8 = AscToBcd(straAid8.getBytes(), straAid8.getBytes().length);
		byte[] aid9 = AscToBcd(straAid9.getBytes(), straAid9.getBytes().length);
		byte[] aid10 = AscToBcd(straAid10.getBytes(),
				straAid10.getBytes().length);
		byte[] aid11 = AscToBcd(straAid11.getBytes(),
				straAid11.getBytes().length);
		byte[] aid12 = AscToBcd(straAid12.getBytes(),
				straAid12.getBytes().length);

		String strVersion1 = "0020";
		String strVersion2 = "0140";
		String strVersion3 = "0002";
		String strVersion4 = "0001";
		String strVersion5 = "0020";
		String strVersion6 = "0020";
		String strVersion7 = "0020";
		String strVersion8 = "0001";
		String strVersion9 = "14E9";
		String strVersion10 = "14E9";
		String strVersion11 = "0200";
		String strVersion12 = "0200";
		byte[] version1 = AscToBcd(strVersion1.getBytes(),
				strVersion1.getBytes().length);
		byte[] version2 = AscToBcd(strVersion2.getBytes(),
				strVersion2.getBytes().length);
		byte[] version3 = AscToBcd(strVersion3.getBytes(),
				strVersion3.getBytes().length);
		byte[] version4 = AscToBcd(strVersion4.getBytes(),
				strVersion4.getBytes().length);
		byte[] version5 = AscToBcd(strVersion5.getBytes(),
				strVersion5.getBytes().length);
		byte[] version6 = AscToBcd(strVersion6.getBytes(),
				strVersion6.getBytes().length);
		byte[] version7 = AscToBcd(strVersion7.getBytes(),
				strVersion7.getBytes().length);
		byte[] version8 = AscToBcd(strVersion8.getBytes(),
				strVersion8.getBytes().length);
		byte[] version9 = AscToBcd(strVersion9.getBytes(),
				strVersion9.getBytes().length);
		byte[] version10 = AscToBcd(strVersion10.getBytes(),
				strVersion10.getBytes().length);
		byte[] version11 = AscToBcd(strVersion11.getBytes(),
				strVersion11.getBytes().length);
		byte[] version12 = AscToBcd(strVersion12.getBytes(),
				strVersion12.getBytes().length);

		String strTacDenial1 = "0010000000";
		String strTacDenial2 = "0010000000";
		String strTacDenial3 = "0400000000";
		String strTacDenial4 = "0000000000";
		String strTacDenial5 = "0010000000";
		String strTacDenial6 = "0010000000";
		String strTacDenial7 = "0010000000";
		String strTacDenial8 = "0010000000";
		String strTacDenial9 = "0010000000";
		String strTacDenial10 = "0010000000";
		String strTacDenial11 = "0400000000";
		String strTacDenial12 = "0010000000";
		byte[] tacDenial1 = AscToBcd(strTacDenial1.getBytes(),
				strTacDenial1.getBytes().length);// 终端行为代码(拒绝)//Terminal
		// behavior// code (refuse)
		byte[] tacDenial2 = AscToBcd(strTacDenial2.getBytes(),
				strTacDenial2.getBytes().length);
		byte[] tacDenial3 = AscToBcd(strTacDenial3.getBytes(),
				strTacDenial3.getBytes().length);
		byte[] tacDenial4 = AscToBcd(strTacDenial4.getBytes(),
				strTacDenial4.getBytes().length);
		byte[] tacDenial5 = AscToBcd(strTacDenial5.getBytes(),
				strTacDenial5.getBytes().length);
		byte[] tacDenial6 = AscToBcd(strTacDenial6.getBytes(),
				strTacDenial6.getBytes().length);
		byte[] tacDenial7 = AscToBcd(strTacDenial7.getBytes(),
				strTacDenial7.getBytes().length);
		byte[] tacDenial8 = AscToBcd(strTacDenial8.getBytes(),
				strTacDenial8.getBytes().length);
		byte[] tacDenial9 = AscToBcd(strTacDenial9.getBytes(),
				strTacDenial9.getBytes().length);
		byte[] tacDenial10 = AscToBcd(strTacDenial10.getBytes(),
				strTacDenial10.getBytes().length);
		byte[] tacDenial11 = AscToBcd(strTacDenial11.getBytes(),
				strTacDenial11.getBytes().length);
		byte[] tacDenial12 = AscToBcd(strTacDenial12.getBytes(),
				strTacDenial12.getBytes().length);

		String strTacOnline1 = "D84004F800";
		String strTacOnline2 = "D84004F800";
		String strTacOnline3 = "F85058F800";
		String strTacOnline4 = "CC00000000";
		String strTacOnline5 = "D84004F800";
		String strTacOnline6 = "D84004F800";
		String strTacOnline7 = "D84004F800";
		String strTacOnline8 = "D84004F800";
		String strTacOnline9 = "D84004F800";
		String strTacOnline10 = "D84004F800";
		String strTacOnline11 = "F850ACF800";
		String strTacOnline12 = "F860ACF800";
		byte[] tacOnline1 = AscToBcd(strTacOnline1.getBytes(),
				strTacOnline1.getBytes().length);// 终端行为代码(联机)//Terminal
		byte[] tacOnline2 = AscToBcd(strTacOnline2.getBytes(),
				strTacOnline2.getBytes().length);
		byte[] tacOnline3 = AscToBcd(strTacOnline3.getBytes(),
				strTacOnline3.getBytes().length);
		byte[] tacOnline4 = AscToBcd(strTacOnline4.getBytes(),
				strTacOnline4.getBytes().length);
		byte[] tacOnline5 = AscToBcd(strTacOnline5.getBytes(),
				strTacOnline5.getBytes().length);
		byte[] tacOnline6 = AscToBcd(strTacOnline6.getBytes(),
				strTacOnline6.getBytes().length);
		byte[] tacOnline7 = AscToBcd(strTacOnline7.getBytes(),
				strTacOnline7.getBytes().length);
		byte[] tacOnline8 = AscToBcd(strTacOnline8.getBytes(),
				strTacOnline8.getBytes().length);
		byte[] tacOnline9 = AscToBcd(strTacOnline9.getBytes(),
				strTacOnline9.getBytes().length);
		byte[] tacOnline10 = AscToBcd(strTacOnline10.getBytes(),
				strTacOnline10.getBytes().length);
		byte[] tacOnline11 = AscToBcd(strTacOnline11.getBytes(),
				strTacOnline11.getBytes().length);
		byte[] tacOnline12 = AscToBcd(strTacOnline12.getBytes(),
				strTacOnline12.getBytes().length);

		String strTacDefault1 = "0010000000";
		String strTacDefault2 = "D84000A800";
		String strTacDefault3 = "FC5058A000";
		String strTacDefault4 = "CC00000000";
		String strTacDefault5 = "0010000000";
		String strTacDefault6 = "0010000000";
		String strTacDefault7 = "0010000000";
		String strTacDefault8 = "D84000A800";
		String strTacDefault9 = "D84000A800";
		String strTacDefault10 = "D84000A800";
		String strTacDefault11 = "FC50ACA000";
		String strTacDefault12 = "FC6024A800";
		byte[] tacDefault1 = AscToBcd(strTacDefault1.getBytes(),
				strTacDefault1.getBytes().length);// 终端行为代码(缺省)//Terminal// //
		// behavior code (default)
		byte[] tacDefault2 = AscToBcd(strTacDefault2.getBytes(),
				strTacDefault2.getBytes().length);
		byte[] tacDefault3 = AscToBcd(strTacDefault3.getBytes(),
				strTacDefault3.getBytes().length);
		byte[] tacDefault4 = AscToBcd(strTacDefault4.getBytes(),
				strTacDefault4.getBytes().length);
		byte[] tacDefault5 = AscToBcd(strTacDefault5.getBytes(),
				strTacDefault5.getBytes().length);
		byte[] tacDefault6 = AscToBcd(strTacDefault6.getBytes(),
				strTacDefault6.getBytes().length);
		byte[] tacDefault7 = AscToBcd(strTacDefault7.getBytes(),
				strTacDefault7.getBytes().length);
		byte[] tacDefault8 = AscToBcd(strTacDefault8.getBytes(),
				strTacDefault8.getBytes().length);
		byte[] tacDefault9 = AscToBcd(strTacDefault9.getBytes(),
				strTacDefault9.getBytes().length);
		byte[] tacDefault10 = AscToBcd(strTacDefault10.getBytes(),
				strTacDefault10.getBytes().length);
		byte[] tacDefault11 = AscToBcd(strTacDefault11.getBytes(),
				strTacDefault11.getBytes().length);
		byte[] tacDefault12 = AscToBcd(strTacDefault12.getBytes(),
				strTacDefault12.getBytes().length);

		byte[] acquierId = {0x00, 0x00, 0x00, 0x12, 0x34, 0x56};//
		byte[] dDOL = {0x03, (byte) 0x9F, 0x37, 0x04};// 终端缺省DDOL //default
		// DDOL
		byte[] tDOL = {0x0F, (byte) 0x9F, 0x02, 0x06, 0x5F, 0x2A, 0x02,
				(byte) 0x9A, 0x03, (byte) 0x9C, 0x01, (byte) 0x95, 0x05,
				(byte) 0x9F, 0x37, 0x04}; // 终端缺省TDOL
		byte[] riskManData = null; //
		EmvAidList emvAidList1 = new EmvAidList(aidName.getBytes(), aid1,
				(byte) aid1.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial1, tacOnline1,
				tacDefault1, acquierId, dDOL, tDOL, version1, null, (byte) 0,
				0, 0, 0, 0);
		EmvAidList emvAidList2 = new EmvAidList(aidName.getBytes(), aid2,
				(byte) aid2.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial2, tacOnline2,
				tacDefault2, acquierId, dDOL, tDOL, version2, null, (byte) 0,
				0, 0, 0, 0);
		EmvAidList emvAidList3 = new EmvAidList(aidName.getBytes(), aid3,
				(byte) aid3.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial3, tacOnline3,
				tacDefault3, acquierId, dDOL, tDOL, version3, null, (byte) 0,
				0, 0, 0, 0);
		EmvAidList emvAidList4 = new EmvAidList(aidName.getBytes(), aid4,
				(byte) aid4.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial4, tacOnline4,
				tacDefault4, acquierId, dDOL, tDOL, version4, null, (byte) 0,
				0, 0, 0, 0);
		EmvAidList emvAidList5 = new EmvAidList(aidName.getBytes(), aid5,
				(byte) aid5.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial5, tacOnline5,
				tacDefault5, acquierId, dDOL, tDOL, version5, null, (byte) 0,
				0, 0, 0, 0);
		EmvAidList emvAidList6 = new EmvAidList(aidName.getBytes(), aid6,
				(byte) aid6.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial6, tacOnline6,
				tacDefault6, acquierId, dDOL, tDOL, version6, null, (byte) 0,
				0, 0, 0, 0);
		EmvAidList emvAidList7 = new EmvAidList(aidName.getBytes(), aid7,
				(byte) aid7.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial7, tacOnline7,
				tacDefault7, acquierId, dDOL, tDOL, version7, null, (byte) 0,
				0, 0, 0, 0);
		EmvAidList emvAidList8 = new EmvAidList(aidName.getBytes(), aid8,
				(byte) aid8.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial8, tacOnline8,
				tacDefault8, acquierId, dDOL, tDOL, version8, null, (byte) 0,
				0, 0, 0, 0);
		EmvAidList emvAidList9 = new EmvAidList(aidName.getBytes(), aid9,
				(byte) aid9.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial9, tacOnline9,
				tacDefault9, acquierId, dDOL, tDOL, version9, null, (byte) 0,
				0, 0, 0, 0);
		EmvAidList emvAidList10 = new EmvAidList(aidName.getBytes(), aid10,
				(byte) aid10.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial10,
				tacOnline10, tacDefault10, acquierId, dDOL, tDOL, version10,
				null, (byte) 0, 0, 0, 0, 0);
		EmvAidList emvAidList11 = new EmvAidList(aidName.getBytes(), aid11,
				(byte) aid11.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial11,
				tacOnline11, tacDefault11, acquierId, dDOL, tDOL, version11,
				null, (byte) 0, 0, 0, 0, 0);
		EmvAidList emvAidList12 = new EmvAidList(aidName.getBytes(), aid12,
				(byte) aid12.length, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 1, (byte) 1, (byte) 1, 2000, 0, tacDenial12,
				tacOnline12, tacDefault12, acquierId, dDOL, tDOL, version12,
				null, (byte) 0, 0, 0, 0, 0);
		/**
		 * addAidList() 添加应用列表 add ic card app list
		 */
		ret = emvl2.addAidList(emvAidList1);
		Log.v(TAG, "addAidList1 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList1) OK \n";
		} else {
			strShow += "addAidList(emvAidList1) Fail \n";
		}
		ret = emvl2.addAidList(emvAidList2);
		Log.v(TAG, "addAidList2 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList2) OK \n";
		} else {
			strShow += "addAidList(emvAidList2) Fail \n";
		}
		ret = emvl2.addAidList(emvAidList3);
		Log.v(TAG, "addAidList3 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList3) OK \n";
		} else {
			strShow += "addAidList(emvAidList3) Fail \n";
		}
		ret = emvl2.addAidList(emvAidList4);
		Log.v(TAG, "addAidList4 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList4) OK \n";
		} else {
			strShow += "addAidList(emvAidList4) Fail \n";
		}
		ret = emvl2.addAidList(emvAidList5);
		Log.v(TAG, "addAidList5 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList5) OK \n";
		} else {
			strShow += "addAidList(emvAidList5) Fail \n";
		}
		ret = emvl2.addAidList(emvAidList6);
		Log.v(TAG, "addAidList6 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList6) OK \n";
		} else {
			strShow += "addAidList(emvAidList6) Fail \n";
		}
		ret = emvl2.addAidList(emvAidList7);
		Log.v(TAG, "addAidList7 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList7) OK \n";
		} else {
			strShow += "addAidList(emvAidList7) Fail \n";
		}
		ret = emvl2.addAidList(emvAidList8);
		Log.v(TAG, "addAidList8 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList8) OK \n";
		} else {
			strShow += "addAidList(emvAidList8) Fail \n";
		}
		ret = emvl2.addAidList(emvAidList9);
		Log.v(TAG, "addAidList9 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList9) OK \n";
		} else {
			strShow += "addAidList(emvAidList9) Fail \n";
		}
		ret = emvl2.addAidList(emvAidList10);
		Log.v(TAG, "addAidList10 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList10) OK \n";
		} else {
			strShow += "addAidList(emvAidList10) Fail \n";
		}
		ret = emvl2.addAidList(emvAidList11);
		Log.v(TAG, "addAidList11 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList11) OK \n";
		} else {
			strShow += "addAidList(emvAidList11) Fail \n";
		}
		ret = emvl2.addAidList(emvAidList12);
		Log.v(TAG, "addAidList12 ret=" + ret);
		if (ret == 0) {
			strShow += "addAidList(emvAidList12) OK \n";
		} else {
			strShow += "addAidList(emvAidList12) Fail \n";
		}
	}

	public void InitAllCapk() {
		int ret = -1;
		// VISA 896 bits Test Key 98
		String strRid1 = "A000000333";
		String strRid2 = "A000000004";
		String strRid3 = "A000000025";
		String strRid4 = "A000000065";
		String strRid5 = "A000000152";
		String strRid6 = "A000000003";
		byte[] rid1 = AscToBcd(strRid1.getBytes(), strRid1.getBytes().length);// 应用注册服务商ID
		// //Application
		// registration
		// services
		// id
		byte[] rid2 = AscToBcd(strRid2.getBytes(), strRid2.getBytes().length);
		byte[] rid3 = AscToBcd(strRid3.getBytes(), strRid3.getBytes().length);
		byte[] rid4 = AscToBcd(strRid4.getBytes(), strRid4.getBytes().length);
		byte[] rid5 = AscToBcd(strRid5.getBytes(), strRid5.getBytes().length);
		byte[] rid6 = AscToBcd(strRid6.getBytes(), strRid6.getBytes().length);

		byte keyID1 = (byte) 2;// 密钥索引 //key index
		byte keyID2 = (byte) 4;// 密钥索引
		byte keyID3 = (byte) 5;// 密钥索引
		byte keyID4 = (byte) 6;// 密钥索引 //key index
		byte keyID5 = (byte) 3;// 密钥索引
		byte keyID6 = (byte) 14;// 密钥索引
		byte keyID7 = (byte) 15;// 密钥索引 //key index
		byte keyID8 = (byte) 16;// 密钥索引
		byte keyID9 = (byte) 9;// 密钥索引
		byte keyID10 = (byte) 16;// 密钥索引 //key index
		byte keyID11 = (byte) 3;// 密钥索引
		byte keyID12 = (byte) 18;// 密钥索引
		byte keyID13 = (byte) 20;// 密钥索引 //key index
		byte keyID14 = (byte) 1;// 密钥索引
		byte keyID15 = (byte) 3;// 密钥索引
		byte keyID16 = (byte) 4;// 密钥索引 //key index
		byte keyID17 = (byte) 5;// 密钥索引
		byte keyID18 = (byte) 4;// 密钥索引
		byte keyID19 = (byte) 1;// 密钥索引 //key index
		byte keyID20 = (byte) 7;// 密钥索引
		byte keyID21 = (byte) 7;// 密钥索引
		byte keyID22 = (byte) 8;// 密钥索引 //key index
		byte keyID23 = (byte) 9;// 密钥索引

		String strExpDate1 = "211230";
		String strExpDate2 = "171231";
		String strExpDate3 = "241231";
		String strExpDate4 = "241231";
		String strExpDate5 = "101231";
		String strExpDate6 = "161231";
		String strExpDate7 = "171231";
		String strExpDate8 = "181231";
		String strExpDate9 = "121231";
		String strExpDate10 = "121231";
		String strExpDate11 = "211230";
		String strExpDate12 = "141231";
		String strExpDate13 = "161231";
		String strExpDate14 = "101231";
		String strExpDate15 = "171231";
		String strExpDate16 = "241231";
		String strExpDate17 = "241231";
		String strExpDate18 = "211230";
		String strExpDate19 = "091231";
		String strExpDate20 = "121231";
		String strExpDate21 = "121231";
		String strExpDate22 = "141231";
		String strExpDate23 = "161231";

		byte[] expDate1 = AscToBcd(strExpDate1.getBytes(),
				strExpDate1.getBytes().length);// 有效期(YYMMDD) //Expiry Date
		byte[] expDate2 = AscToBcd(strExpDate2.getBytes(),
				strExpDate2.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate3 = AscToBcd(strExpDate3.getBytes(),
				strExpDate3.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate4 = AscToBcd(strExpDate4.getBytes(),
				strExpDate4.getBytes().length);// 有效期(YYMMDD) //Expiry Date
		byte[] expDate5 = AscToBcd(strExpDate5.getBytes(),
				strExpDate5.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate6 = AscToBcd(strExpDate6.getBytes(),
				strExpDate6.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate7 = AscToBcd(strExpDate7.getBytes(),
				strExpDate7.getBytes().length);// 有效期(YYMMDD) //Expiry Date
		byte[] expDate8 = AscToBcd(strExpDate8.getBytes(),
				strExpDate8.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate9 = AscToBcd(strExpDate9.getBytes(),
				strExpDate9.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate10 = AscToBcd(strExpDate10.getBytes(),
				strExpDate10.getBytes().length);// 有效期(YYMMDD) //Expiry Date
		byte[] expDate11 = AscToBcd(strExpDate11.getBytes(),
				strExpDate11.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate12 = AscToBcd(strExpDate12.getBytes(),
				strExpDate12.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate13 = AscToBcd(strExpDate13.getBytes(),
				strExpDate13.getBytes().length);// 有效期(YYMMDD) //Expiry Date
		byte[] expDate14 = AscToBcd(strExpDate14.getBytes(),
				strExpDate14.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate15 = AscToBcd(strExpDate15.getBytes(),
				strExpDate15.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate16 = AscToBcd(strExpDate16.getBytes(),
				strExpDate16.getBytes().length);// 有效期(YYMMDD) //Expiry Date
		byte[] expDate17 = AscToBcd(strExpDate17.getBytes(),
				strExpDate17.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate18 = AscToBcd(strExpDate18.getBytes(),
				strExpDate18.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate19 = AscToBcd(strExpDate19.getBytes(),
				strExpDate19.getBytes().length);// 有效期(YYMMDD) //Expiry Date
		byte[] expDate20 = AscToBcd(strExpDate20.getBytes(),
				strExpDate20.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate21 = AscToBcd(strExpDate21.getBytes(),
				strExpDate21.getBytes().length);// 有效期(YYMMDD)
		byte[] expDate22 = AscToBcd(strExpDate22.getBytes(),
				strExpDate22.getBytes().length);// 有效期(YYMMDD) //Expiry Date
		byte[] expDate23 = AscToBcd(strExpDate23.getBytes(),
				strExpDate23.getBytes().length);// 有效期(YYMMDD)
		byte hashInd = 0x01;// HASH算法标志 //Hash index
		byte arithInd = 0x01;// RSA算法标志//RSA index

		String strModul1 = "A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57";
		String strModul2 = "A6DA428387A502D7DDFB7A74D3F412BE762627197B25435B7A81716A700157DDD06F7CC99D6CA28C2470527E2C03616B9C59217357C2674F583B3BA5C7DCF2838692D023E3562420B4615C439CA97C44DC9A249CFCE7B3BFB22F68228C3AF13329AA4A613CF8DD853502373D62E49AB256D2BC17120E54AEDCED6D96A4287ACC5C04677D4A5A320DB8BEE2F775E5FEC5";
		String strModul3 = "B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597";
		String strModul4 = "CB26FC830B43785B2BCE37C81ED334622F9622F4C89AAE641046B2353433883F307FB7C974162DA72F7A4EC75D9D657336865B8D3023D3D645667625C9A07A6B7A137CF0C64198AE38FC238006FB2603F41F4F3BB9DA1347270F2F5D8C606E420958C5F7D50A71DE30142F70DE468889B5E3A08695B938A50FC980393A9CBCE44AD2D64F630BB33AD3F5F5FD495D31F37818C1D94071342E07F1BEC2194F6035BA5DED3936500EB82DFDA6E8AFB655B1EF3D0D7EBF86B66DD9F29F6B1D324FE8B26CE38AB2013DD13F611E7A594D675C4432350EA244CC34F3873CBA06592987A1D7E852ADC22EF5A2EE28132031E48F74037E3B34AB747F";
		String strModul5 = "B0C2C6E2A6386933CD17C239496BF48C57E389164F2A96BFF133439AE8A77B20498BD4DC6959AB0C2D05D0723AF3668901937B674E5A2FA92DDD5E78EA9D75D79620173CC269B35F463B3D4AAFF2794F92E6C7A3FB95325D8AB95960C3066BE548087BCB6CE12688144A8B4A66228AE4659C634C99E36011584C095082A3A3E3";
		String strModul6 = "AA94A8C6DAD24F9BA56A27C09B01020819568B81A026BE9FD0A3416CA9A71166ED5084ED91CED47DD457DB7E6CBCD53E560BC5DF48ABC380993B6D549F5196CFA77DFB20A0296188E969A2772E8C4141665F8BB2516BA2C7B5FC91F8DA04E8D512EB0F6411516FB86FC021CE7E969DA94D33937909A53A57F907C40C22009DA7532CB3BE509AE173B39AD6A01BA5BB85";
		String strModul7 = "C8D5AC27A5E1FB89978C7C6479AF993AB3800EB243996FBB2AE26B67B23AC482C4B746005A51AFA7D2D83E894F591A2357B30F85B85627FF15DA12290F70F05766552BA11AD34B7109FA49DE29DCB0109670875A17EA95549E92347B948AA1F045756DE56B707E3863E59A6CBE99C1272EF65FB66CBB4CFF070F36029DD76218B21242645B51CA752AF37E70BE1A84FF31079DC0048E928883EC4FADD497A719385C2BBBEBC5A66AA5E5655D18034EC5";
		String strModul8 = "CF98DFEDB3D3727965EE7797723355E0751C81D2D3DF4D18EBAB9FB9D49F38C8C4A826B99DC9DEA3F01043D4BF22AC3550E2962A59639B1332156422F788B9C16D40135EFD1BA94147750575E636B6EBC618734C91C1D1BF3EDC2A46A43901668E0FFC136774080E888044F6A1E65DC9AAA8928DACBEB0DB55EA3514686C6A732CEF55EE27CF877F110652694A0E3484C855D882AE191674E25C296205BBB599455176FDD7BBC549F27BA5FE35336F7E29E68D783973199436633C67EE5A680F05160ED12D1665EC83D1997F10FD05BBDBF9433E8F797AEE3E9F02A34228ACE927ABE62B8B9281AD08D3DF5C7379685045D7BA5FCDE58637";
		String strModul9 = "B72A8FEF5B27F2B550398FDCC256F714BAD497FF56094B7408328CB626AA6F0E6A9DF8388EB9887BC930170BCC1213E90FC070D52C8DCD0FF9E10FAD36801FE93FC998A721705091F18BC7C98241CADC15A2B9DA7FB963142C0AB640D5D0135E77EBAE95AF1B4FEFADCF9C012366BDDA0455C1564A68810D7127676D493890BD";
		String strModul10 = "99B63464EE0B4957E4FD23BF923D12B61469B8FFF8814346B2ED6A780F8988EA9CF0433BC1E655F05EFA66D0C98098F25B659D7A25B8478A36E489760D071F54CDF7416948ED733D816349DA2AADDA227EE45936203CBF628CD033AABA5E5A6E4AE37FBACB4611B4113ED427529C636F6C3304F8ABDD6D9AD660516AE87F7F2DDF1D2FA44C164727E56BBC9BA23C0285";
		String strModul11 = "B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33D";
		String strModul12 = "ADF05CD4C5B490B087C3467B0F3043750438848461288BFEFD6198DD576DC3AD7A7CFA07DBA128C247A8EAB30DC3A30B02FCD7F1C8167965463626FEFF8AB1AA61A4B9AEF09EE12B009842A1ABA01ADB4A2B170668781EC92B60F605FD12B2B2A6F1FE734BE510F60DC5D189E401451B62B4E06851EC20EBFF4522AACC2E9CDC89BC5D8CDE5D633CFD77220FF6BBD4A9B441473CC3C6FEFC8D13E57C3DE97E1269FA19F655215B23563ED1D1860D8681";
		String strModul13 = "AEED55B9EE00E1ECEB045F61D2DA9A66AB637B43FB5CDBDB22A2FBB25BE061E937E38244EE5132F530144A3F268907D8FD648863F5A96FED7E42089E93457ADC0E1BC89C58A0DB72675FBC47FEE9FF33C16ADE6D341936B06B6A6F5EF6F66A4EDD981DF75DA8399C3053F430ECA342437C23AF423A211AC9F58EAF09B0F837DE9D86C7109DB1646561AA5AF0289AF5514AC64BC2D9D36A179BB8A7971E2BFA03A9E4B847FD3D63524D43A0E8003547B94A8A75E519DF3177D0A60BC0B4BAB1EA59A2CBB4D2D62354E926E9C7D3BE4181E81BA60F8285A896D17DA8C3242481B6C405769A39D547C74ED9FF95A70A796046B5EFF36682DC29";
		String strModul14 = "BBE9066D2517511D239C7BFA77884144AE20C7372F515147E8CE6537C54C0A6A4D45F8CA4D290870CDA59F1344EF71D17D3F35D92F3F06778D0D511EC2A7DC4FFEADF4FB1253CE37A7B2B5A3741227BEF72524DA7A2B7B1CB426BEE27BC513B0CB11AB99BC1BC61DF5AC6CC4D831D0848788CD74F6D543AD37C5A2B4C5D5A93B";
		String strModul15 = "BF321241BDBF3585FFF2ACB89772EBD18F2C872159EAA4BC179FB03A1B850A1A758FA2C6849F48D4C4FF47E02A575FC13E8EB77AC37135030C5600369B5567D3A7AAF02015115E987E6BE566B4B4CC03A4E2B16CD9051667C2CD0EEF4D76D27A6F745E8BBEB45498ED8C30E2616DB4DBDA4BAF8D71990CDC22A8A387ACB21DD88E2CC27962B31FBD786BBB55F9E0B041";
		String strModul16 = "8EEEC0D6D3857FD558285E49B623B109E6774E06E9476FE1B2FB273685B5A235E955810ADDB5CDCC2CB6E1A97A07089D7FDE0A548BDC622145CA2DE3C73D6B14F284B3DC1FA056FC0FB2818BCD7C852F0C97963169F01483CE1A63F0BF899D412AB67C5BBDC8B4F6FB9ABB57E95125363DBD8F5EBAA9B74ADB93202050341833DEE8E38D28BD175C83A6EA720C262682BEABEA8E955FE67BD9C2EFF7CB9A9F45DD5BDA4A1EEFB148BC44FFF68D9329FD";
		String strModul17 = "E1200E9F4428EB71A526D6BB44C957F18F27B20BACE978061CCEF23532DBEBFAF654A149701C14E6A2A7C2ECAC4C92135BE3E9258331DDB0967C3D1D375B996F25B77811CCCC06A153B4CE6990A51A0258EA8437EDBEB701CB1F335993E3F48458BC1194BAD29BF683D5F3ECB984E31B7B9D2F6D947B39DEDE0279EE45B47F2F3D4EEEF93F9261F8F5A571AFBFB569C150370A78F6683D687CB677777B2E7ABEFCFC8F5F93501736997E8310EE0FD87AFAC5DA772BA277F88B44459FCA563555017CD0D66771437F8B6608AA1A665F88D846403E4C41AFEEDB9729C2B2511CFE228B50C1B152B2A60BBF61D8913E086210023A3AA499E423";
		String strModul18 = "BC853E6B5365E89E7EE9317C94B02D0ABB0DBD91C05A224A2554AA29ED9FCB9D86EB9CCBB322A57811F86188AAC7351C72BD9EF196C5A01ACEF7A4EB0D2AD63D9E6AC2E7836547CB1595C68BCBAFD0F6728760F3A7CA7B97301B7E0220184EFC4F653008D93CE098C0D93B45201096D1ADFF4CF1F9FC02AF759DA27CD6DFD6D789B099F16F378B6100334E63F3D35F3251A5EC78693731F5233519CDB380F5AB8C0F02728E91D469ABD0EAE0D93B1CC66CE127B29C7D77441A49D09FCA5D6D9762FC74C31BB506C8BAE3C79AD6C2578775B95956B5370D1D0519E37906B384736233251E8F09AD79DFBE2C6ABFADAC8E4D8624318C27DAF1";
		String strModul19 = "C696034213D7D8546984579D1D0F0EA519CFF8DEFFC429354CF3A871A6F7183F1228DA5C7470C055387100CB935A712C4E2864DF5D64BA93FE7E63E71F25B1E5F5298575EBE1C63AA617706917911DC2A75AC28B251C7EF40F2365912490B939BCA2124A30A28F54402C34AECA331AB67E1E79B285DD5771B5D9FF79EA630B75";
		String strModul20 = "A89F25A56FA6DA258C8CA8B40427D927B4A1EB4D7EA326BBB12F97DED70AE5E4480FC9C5E8A972177110A1CC318D06D2F8F5C4844AC5FA79A4DC470BB11ED635699C17081B90F1B984F12E92C1C529276D8AF8EC7F28492097D8CD5BECEA16FE4088F6CFAB4A1B42328A1B996F9278B0B7E3311CA5EF856C2F888474B83612A82E4E00D0CD4069A6783140433D50725F";
		String strModul21 = "A89F25A56FA6DA258C8CA8B40427D927B4A1EB4D7EA326BBB12F97DED70AE5E4480FC9C5E8A972177110A1CC318D06D2F8F5C4844AC5FA79A4DC470BB11ED635699C17081B90F1B984F12E92C1C529276D8AF8EC7F28492097D8CD5BECEA16FE4088F6CFAB4A1B42328A1B996F9278B0B7E3311CA5EF856C2F888474B83612A82E4E00D0CD4069A6783140433D50725F";
		String strModul22 = "D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0B";
		String strModul23 = "9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41";

		byte[] modul1 = AscToBcd(strModul1.getBytes(),
				strModul1.getBytes().length);// 模
		byte[] modul2 = AscToBcd(strModul2.getBytes(),
				strModul2.getBytes().length);// 模
		byte[] modul3 = AscToBcd(strModul3.getBytes(),
				strModul3.getBytes().length);// 模
		byte[] modul4 = AscToBcd(strModul4.getBytes(),
				strModul4.getBytes().length);// 模
		byte[] modul5 = AscToBcd(strModul5.getBytes(),
				strModul5.getBytes().length);// 模
		byte[] modul6 = AscToBcd(strModul6.getBytes(),
				strModul6.getBytes().length);// 模
		byte[] modul7 = AscToBcd(strModul7.getBytes(),
				strModul7.getBytes().length);// 模
		byte[] modul8 = AscToBcd(strModul8.getBytes(),
				strModul8.getBytes().length);// 模
		byte[] modul9 = AscToBcd(strModul9.getBytes(),
				strModul9.getBytes().length);// 模
		byte[] modul10 = AscToBcd(strModul10.getBytes(),
				strModul10.getBytes().length);// 模
		byte[] modul11 = AscToBcd(strModul11.getBytes(),
				strModul11.getBytes().length);// 模
		byte[] modul12 = AscToBcd(strModul12.getBytes(),
				strModul12.getBytes().length);// 模
		byte[] modul13 = AscToBcd(strModul13.getBytes(),
				strModul13.getBytes().length);// 模
		byte[] modul14 = AscToBcd(strModul14.getBytes(),
				strModul14.getBytes().length);// 模
		byte[] modul15 = AscToBcd(strModul15.getBytes(),
				strModul15.getBytes().length);// 模
		byte[] modul16 = AscToBcd(strModul16.getBytes(),
				strModul16.getBytes().length);// 模
		byte[] modul17 = AscToBcd(strModul17.getBytes(),
				strModul17.getBytes().length);// 模
		byte[] modul18 = AscToBcd(strModul18.getBytes(),
				strModul18.getBytes().length);// 模
		byte[] modul19 = AscToBcd(strModul19.getBytes(),
				strModul19.getBytes().length);// 模
		byte[] modul20 = AscToBcd(strModul20.getBytes(),
				strModul20.getBytes().length);// 模
		byte[] modul21 = AscToBcd(strModul21.getBytes(),
				strModul21.getBytes().length);// 模
		byte[] modul22 = AscToBcd(strModul22.getBytes(),
				strModul22.getBytes().length);// 模
		byte[] modul23 = AscToBcd(strModul23.getBytes(),
				strModul23.getBytes().length);// 模

		byte modulLen1 = (byte) modul1.length;// 模长度
		byte modulLen2 = (byte) modul2.length;// 模长度
		byte modulLen3 = (byte) modul3.length;// 模长度
		byte modulLen4 = (byte) modul4.length;// 模长度
		byte modulLen5 = (byte) modul5.length;// 模长度
		byte modulLen6 = (byte) modul6.length;// 模长度
		byte modulLen7 = (byte) modul7.length;// 模长度
		byte modulLen8 = (byte) modul8.length;// 模长度
		byte modulLen9 = (byte) modul9.length;// 模长度
		byte modulLen10 = (byte) modul10.length;// 模长度
		byte modulLen11 = (byte) modul11.length;// 模长度
		byte modulLen12 = (byte) modul12.length;// 模长度
		byte modulLen13 = (byte) modul13.length;// 模长度
		byte modulLen14 = (byte) modul14.length;// 模长度
		byte modulLen15 = (byte) modul15.length;// 模长度
		byte modulLen16 = (byte) modul16.length;// 模长度
		byte modulLen17 = (byte) modul17.length;// 模长度
		byte modulLen18 = (byte) modul18.length;// 模长度
		byte modulLen19 = (byte) modul19.length;// 模长度
		byte modulLen20 = (byte) modul20.length;// 模长度
		byte modulLen21 = (byte) modul21.length;// 模长度
		byte modulLen22 = (byte) modul22.length;// 模长度
		byte modulLen23 = (byte) modul23.length;// 模长度

		byte exponentLen = 0x01;// 指数长度
		byte[] exponent = {0x03};// 指数

		String strCheckSum1 = "03BB335A8549A03B87AB089D006F60852E4B8060";
		String strCheckSum2 = "381A035DA58B482EE2AF75F4C3F2CA469BA4AA6C";
		String strCheckSum3 = "EBFA0D5D06D8CE702DA3EAE890701D45E274C845";
		String strCheckSum4 = "F910A1504D5FFB793D94F3B500765E1ABCAD72D9";
		String strCheckSum5 = "8708A3E3BBC1BB0BE73EBD8D19D4E5D20166BF6C";
		String strCheckSum6 = "A7266ABAE64B42A3668851191D49856E17F8FBCD";
		String strCheckSum7 = "A73472B3AB557493A9BC2179CC8014053B12BAB4";
		String strCheckSum8 = "C729CF2FD262394ABC4CC173506502446AA9B9FD";
		String strCheckSum9 = "4410C6D51C2F83ADFD92528FA6E38A32DF048D0A";
		String strCheckSum10 = "C75E5210CBE6E8F0594A0F1911B07418CADB5BAB";
		String strCheckSum11 = "87F0CD7C0E86F38F89A66F8C47071A8B88586F26";
		String strCheckSum12 = "874B379B7F607DC1CAF87A19E400B6A9E25163E8";
		String strCheckSum13 = "C0D15F6CD957E491DB56DCDD1CA87A03EBE06B7B";
		String strCheckSum14 = "E881E390675D44C2DD81234DCE29C3F5AB2297A0";
		String strCheckSum15 = "CA1E9099327F0B786D8583EC2F27E57189503A57";
		String strCheckSum16 = "17F971CAF6B708E5B9165331FBA91593D0C0BF66";
		String strCheckSum17 = "12BCD407B6E627A750FDF629EE8C2C9CC7BA636A";
		String strCheckSum18 = "F527081CF371DD7E1FD4FA414A665036E0F5E6E5";
		String strCheckSum19 = "D34A6A776011C7E7CE3AEC5F03AD2F8CFC5503CC";
		String strCheckSum20 = "B4BC56CC4E88324932CBC643D6898F6FE593B172";
		String strCheckSum21 = "B4BC56CC4E88324932CBC643D6898F6FE593B172";
		String strCheckSum22 = "20D213126955DE205ADC2FD2822BD22DE21CF9A8";
		String strCheckSum23 = "1FF80A40173F52D7D27E0F26A146A1C8CCB29046";
		byte[] checkSum1 = AscToBcd(strCheckSum1.getBytes(),
				strCheckSum1.getBytes().length);// 密钥校验值 //key checksum
		byte[] checkSum2 = AscToBcd(strCheckSum2.getBytes(),
				strCheckSum2.getBytes().length);// 密钥校验值
		byte[] checkSum3 = AscToBcd(strCheckSum3.getBytes(),
				strCheckSum3.getBytes().length);// 密钥校验值
		byte[] checkSum4 = AscToBcd(strCheckSum4.getBytes(),
				strCheckSum4.getBytes().length);// 密钥校验值 //key checksum
		byte[] checkSum5 = AscToBcd(strCheckSum5.getBytes(),
				strCheckSum5.getBytes().length);// 密钥校验值
		byte[] checkSum6 = AscToBcd(strCheckSum6.getBytes(),
				strCheckSum6.getBytes().length);// 密钥校验值 //key checksum
		byte[] checkSum7 = AscToBcd(strCheckSum7.getBytes(),
				strCheckSum7.getBytes().length);// 密钥校验值
		byte[] checkSum8 = AscToBcd(strCheckSum8.getBytes(),
				strCheckSum8.getBytes().length);// 密钥校验值
		byte[] checkSum9 = AscToBcd(strCheckSum9.getBytes(),
				strCheckSum9.getBytes().length);// 密钥校验值 //key checksum
		byte[] checkSum10 = AscToBcd(strCheckSum10.getBytes(),
				strCheckSum10.getBytes().length);// 密钥校验值
		byte[] checkSum11 = AscToBcd(strCheckSum11.getBytes(),
				strCheckSum11.getBytes().length);// 密钥校验值
		byte[] checkSum12 = AscToBcd(strCheckSum12.getBytes(),
				strCheckSum12.getBytes().length);// 密钥校验值 //key checksum
		byte[] checkSum13 = AscToBcd(strCheckSum13.getBytes(),
				strCheckSum13.getBytes().length);// 密钥校验值
		byte[] checkSum14 = AscToBcd(strCheckSum14.getBytes(),
				strCheckSum14.getBytes().length);// 密钥校验值
		byte[] checkSum15 = AscToBcd(strCheckSum15.getBytes(),
				strCheckSum15.getBytes().length);// 密钥校验值 //key checksum
		byte[] checkSum16 = AscToBcd(strCheckSum16.getBytes(),
				strCheckSum16.getBytes().length);// 密钥校验值
		byte[] checkSum17 = AscToBcd(strCheckSum17.getBytes(),
				strCheckSum17.getBytes().length);// 密钥校验值
		byte[] checkSum18 = AscToBcd(strCheckSum18.getBytes(),
				strCheckSum18.getBytes().length);// 密钥校验值 //key checksum
		byte[] checkSum19 = AscToBcd(strCheckSum19.getBytes(),
				strCheckSum19.getBytes().length);// 密钥校验值
		byte[] checkSum20 = AscToBcd(strCheckSum20.getBytes(),
				strCheckSum20.getBytes().length);// 密钥校验值
		byte[] checkSum21 = AscToBcd(strCheckSum21.getBytes(),
				strCheckSum21.getBytes().length);// 密钥校验值 //key checksum
		byte[] checkSum22 = AscToBcd(strCheckSum22.getBytes(),
				strCheckSum22.getBytes().length);// 密钥校验值
		byte[] checkSum23 = AscToBcd(strCheckSum23.getBytes(),
				strCheckSum23.getBytes().length);// 密钥校验值

		EmvCapkList emvCapkList1 = new EmvCapkList(rid1, keyID1, hashInd,
				arithInd, modulLen1, modul1, exponentLen, exponent, expDate1,
				checkSum1);
		EmvCapkList emvCapkList2 = new EmvCapkList(rid2, keyID2, hashInd,
				arithInd, modulLen2, modul2, exponentLen, exponent, expDate2,
				checkSum2);
		EmvCapkList emvCapkList3 = new EmvCapkList(rid2, keyID3, hashInd,
				arithInd, modulLen3, modul3, exponentLen, exponent, expDate3,
				checkSum3);
		EmvCapkList emvCapkList4 = new EmvCapkList(rid2, keyID4, hashInd,
				arithInd, modulLen4, modul4, exponentLen, exponent, expDate4,
				checkSum4);
		EmvCapkList emvCapkList5 = new EmvCapkList(rid3, keyID5, hashInd,
				arithInd, modulLen5, modul5, exponentLen, exponent, expDate5,
				checkSum5);
		EmvCapkList emvCapkList6 = new EmvCapkList(rid3, keyID6, hashInd,
				arithInd, modulLen6, modul6, exponentLen, exponent, expDate6,
				checkSum6);
		EmvCapkList emvCapkList7 = new EmvCapkList(rid3, keyID7, hashInd,
				arithInd, modulLen7, modul7, exponentLen, exponent, expDate7,
				checkSum7);
		EmvCapkList emvCapkList8 = new EmvCapkList(rid3, keyID8, hashInd,
				arithInd, modulLen8, modul8, exponentLen, exponent, expDate8,
				checkSum8);
		EmvCapkList emvCapkList9 = new EmvCapkList(rid4, keyID9, hashInd,
				arithInd, modulLen9, modul9, exponentLen, exponent, expDate9,
				checkSum9);
		EmvCapkList emvCapkList10 = new EmvCapkList(rid4, keyID10, hashInd,
				arithInd, modulLen10, modul10, exponentLen, exponent,
				expDate10, checkSum10);
		EmvCapkList emvCapkList11 = new EmvCapkList(rid1, keyID11, hashInd,
				arithInd, modulLen11, modul11, exponentLen, exponent,
				expDate11, checkSum11);
		EmvCapkList emvCapkList12 = new EmvCapkList(rid4, keyID12, hashInd,
				arithInd, modulLen12, modul12, exponentLen, exponent,
				expDate12, checkSum12);
		EmvCapkList emvCapkList13 = new EmvCapkList(rid4, keyID13, hashInd,
				arithInd, modulLen13, modul13, exponentLen, exponent,
				expDate13, checkSum13);
		EmvCapkList emvCapkList14 = new EmvCapkList(rid1, keyID14, hashInd,
				arithInd, modulLen14, modul14, exponentLen, exponent,
				expDate14, checkSum14);
		EmvCapkList emvCapkList15 = new EmvCapkList(rid5, keyID15, hashInd,
				arithInd, modulLen15, modul15, exponentLen, exponent,
				expDate15, checkSum15);
		EmvCapkList emvCapkList16 = new EmvCapkList(rid5, keyID16, hashInd,
				arithInd, modulLen16, modul16, exponentLen, exponent,
				expDate16, checkSum16);
		EmvCapkList emvCapkList17 = new EmvCapkList(rid5, keyID17, hashInd,
				arithInd, modulLen17, modul17, exponentLen, exponent,
				expDate17, checkSum17);
		EmvCapkList emvCapkList18 = new EmvCapkList(rid1, keyID18, hashInd,
				arithInd, modulLen18, modul18, exponentLen, exponent,
				expDate18, checkSum18);
		EmvCapkList emvCapkList19 = new EmvCapkList(rid6, keyID19, hashInd,
				arithInd, modulLen19, modul19, exponentLen, exponent,
				expDate19, checkSum19);
		EmvCapkList emvCapkList20 = new EmvCapkList(rid6, keyID20, hashInd,
				arithInd, modulLen20, modul20, exponentLen, exponent,
				expDate20, checkSum20);
		EmvCapkList emvCapkList21 = new EmvCapkList(rid6, keyID21, hashInd,
				arithInd, modulLen21, modul21, exponentLen, exponent,
				expDate21, checkSum21);
		EmvCapkList emvCapkList22 = new EmvCapkList(rid6, keyID22, hashInd,
				arithInd, modulLen22, modul22, exponentLen, exponent,
				expDate22, checkSum22);
		EmvCapkList emvCapkList23 = new EmvCapkList(rid6, keyID23, hashInd,
				arithInd, modulLen23, modul23, exponentLen, exponent,
				expDate23, checkSum23);

		/**
		 * 添加CAPK addCapkList()
		 */
		ret = emvl2.addCapkList(emvCapkList1);
		Log.v(TAG, "addCapkList1 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList1) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList1) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList2);
		Log.v(TAG, "addCapkList2 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList2) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList2) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList3);
		Log.v(TAG, "addCapkList3 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList3) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList3) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList4);
		Log.v(TAG, "addCapkList4 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList4) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList4) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList5);
		Log.v(TAG, "addCapkList5 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList5) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList5) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList6);
		Log.v(TAG, "addCapkList6 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList6) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList6) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList7);
		Log.v(TAG, "addCapkList7 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList7) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList7) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList8);
		Log.v(TAG, "addCapkList8 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList8) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList8) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList9);
		Log.v(TAG, "addCapkList9 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList9) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList9) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList10);
		Log.v(TAG, "addCapkList10 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList10) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList10) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList11);
		Log.v(TAG, "addCapkList11 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList11) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList11) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList12);
		Log.v(TAG, "addCapkList12 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList12) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList12) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList13);
		Log.v(TAG, "addCapkList13 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList13) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList13) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList14);
		Log.v(TAG, "addCapkList14 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList14) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList14) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList15);
		Log.v(TAG, "addCapkList15 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList15) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList15) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList16);
		Log.v(TAG, "addCapkList16 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList16) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList16) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList17);
		Log.v(TAG, "addCapkList17 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList17) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList17) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList18);
		Log.v(TAG, "addCapkList18 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList18) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList18) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList19);
		Log.v(TAG, "addCapkList19 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList19) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList19) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList20);
		Log.v(TAG, "addCapkList20 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList20) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList20) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList21);
		Log.v(TAG, "addCapkList21 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList21) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList21) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList22);
		Log.v(TAG, "addCapkList22 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList22) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList22) Fail \n";
		}
		ret = emvl2.addCapkList(emvCapkList23);
		Log.v(TAG, "addCapkList23 ret=" + ret);
		if (ret == 0) {
			strShow += "addCapkList(emvCapkList23) OK \n";
		} else {
			strShow += "addCapkList(emvCapkList23) Fail \n";
		}

	}

	public class CheckIcCardThread extends Thread {
		public void run() {
			System.out.println("iccCheck....");

			while (blnCheck) {

				if (isClosed == true) {
					Log.i(TAG, "Loop break ");
					break;
				}
				boolean bret = false;

				try {
					bret = iccReader.check(IccReader.ICCARD_SLOT, true);
					Log.i(TAG, "Reader success: ");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					Log.i(TAG, "Reader error: " + e1.getMessage());
					e1.printStackTrace();
				} catch (IccException e1) {
					Log.i(TAG, "Reader error: " + e1.toString());
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (bret == true) {
					blnCheck = false;
					try {


						emvl2.emvSetReadCardType((byte) 0x02);
						contactCard = iccReader.enableCard(
								IccReader.ICCARD_SLOT, IccReader.CARD_VCC_5V,
								true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IccException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Message msg = new Message();
					msg.what = CHECKCARD;
					mHandler.sendMessage(msg);

				}
			}
//			mThread = null;
		}
	}

	public class MyICThread extends Thread {
		public void run() {
			int i = 1;
			while (true) {
				if (isClosed == true) {
					Log.i(TAG, "Loop break ");
					break;
				}
				// unsigned char buff[80];
				strShow = "";
				byte[] buff = new byte[80];
				int ret = -1;
				glOnlineTrans = 0;
				gAmtConfirmed = 0;
				glOnlinePin[0] = 0;
				// gEmvTermParam.authorCode[0] = 0;
				// gEmvTermParam.authRespCode[0] = 0;
				// gEmvTermParam.posEntryMode = 0x02;
				gPrintSignLine = 0;


				ret = emvl2.appSel(IccReader.ICCARD_SLOT, 1);
				if (ret < 0) {
					Log.e(TAG, "appSel fail");
					strShow += "appSel() Fail \n" + i + "    ";
					strShow = strShow + "ret= " + ret + "\n";
				} else {
					strShow += "appSel() OK \n" + i + "    ";
				}

				/**
				 * 读取应用数据 readAppData()
				 */
				ret = emvl2.readAppData();
				if (ret < 0) {
					Log.e(TAG, "readAppData fail");
					strShow += "readAppData() Fail \n";

				} else {
					strShow += "readAppData() OK \n";
				}
				if (gEmvTermParam.exceptionFile == 0x01)
					callBack.checkExceptionFile(); // callback function process exception
				// file

				ret = emvl2.cardAuth();
				if (ret < 0) {
					Log.e(TAG, "cardAuth fail");
					strShow += "cardAuth() Fail \n";
				} else {
					strShow += "cardAuth() OK \n";
				}

				ret = emvl2.procTrans();
				if (ret <= 0) {
					Log.e(TAG, "procTrans fail ret=" + ret);
					strShow += "procTrans() Fail \n";
					if (glOnlineTrans == 1) {
						strShow += "glOnlineTrans == 1 \n";
						Log.e(TAG, "glOnlineTrans == 1");


						ret = emvl2.getScriptResult(buff);
						if (ret < 0) {
							strShow += "getScriptResult() Fail \n";
							Log.e(TAG, "getScriptResult fail");
							if (gEmvTermParam.batchCapture == 0x00) {
								SendReversal();
							}
						} else {
							strShow += "getScriptResult() OK \n";
						}
					} else {
						strShow += "glOnlineTrans != 1 \n";
						if (getActivity() != null) {
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {

									String CardNo = EMVCallBack.read_CardNo;
									TextView txtCardNo = (TextView) getActivity().findViewById(R.id.textView_CardNo);
									txtCardNo.setText(CardNo);

									String CardType = EMVCallBack.read_CardType;
									TextView txtCardHolder = (TextView) getActivity().findViewById(R.id.textView_CardHolder);
									txtCardHolder.setText(CardType);

									LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.liner_amount);
									linearLayout.setVisibility(1);

									isClosed = true;

									dialog.dismiss();


								}
							});
							break;
						}
					}

				} else {
					strShow += "procTrans() OK \n";
				}
				// emv process finish

				if (contactCard != null) {
					try {


						iccReader.disableCard(contactCard);
					} catch (IOException e) {

						e.printStackTrace();
					} catch (IccException e) {

						e.printStackTrace();
					}
				}


//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}


				i++;
				try {
					if (iccReader != null) {
						contactCard = iccReader.enableCard(IccReader.ICCARD_SLOT, IccReader.CARD_VCC_5V, true);

					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IccException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// EmvThread = null;
		}
	}

	int SendReversal() {
		//
		return 1;
	}



	//Magnetic Card Read ==========================================================================================================
	public class MyMAGThread extends Thread {
		public void run() {
			int i = 0;
			boolean bRet;

			try {
				magcardReader.open();
			} catch (IOException e) {
				DialogService.progressDialog((activity_main) getContext(), false);
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setMessage("IOException\nPlease check the connection.");
				dialog = builder.create();
				dialog.show();
				e.printStackTrace();
			}


			while (bThreadflag) {

				if (isClosed == true) {
					Log.i(TAG, "Loop break ");
					break;
				}
				bRet = magcardReader.check();
				if (bRet) {
					try {
						magneticCard = magcardReader.read();
					} catch (IOException e) {
						DialogService.progressDialog((activity_main) getContext(), false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("IOException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
						e.printStackTrace();
					}

					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {

								String CardNo = magneticCard.getTrack2();
								String[] separatedCardNo = CardNo.split("=");
								TextView txtCardNo = (TextView) getActivity().findViewById(R.id.textView_CardNo);
								txtCardNo.setText(separatedCardNo[0]);

								String CardType = magneticCard.getTrack1().replace("^", "-");
								String[] separatedHolder = CardType.split("-");
								TextView txtCardHolder = (TextView) getActivity().findViewById(R.id.textView_CardHolder);
								txtCardHolder.setText(separatedHolder[1]);

								LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.liner_amount);
								linearLayout.setVisibility(1);

								isClosed = true;

								dialog.dismiss();
							} catch (Exception e) {
								DialogService.progressDialog((activity_main) getContext(), false);
								AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
								builder.setMessage("Exception\nPlease check the connection.");
								dialog = builder.create();
								dialog.show();
								e.printStackTrace();
							}


						}
					});
				}
			}
			try {
				magcardReader.close();
			} catch (IOException e) {
				DialogService.progressDialog((activity_main) getContext(), false);
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setMessage("Exception\nPlease check the connection.");
				dialog = builder.create();
				dialog.show();
				e.printStackTrace();
			}
		}
	}



	//NfC Card Read ===============================================================================================================
	public static String toHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}

	private String IntToString(int arg) {
		String str = "", strTemp = "";
		int temp;

		temp = (int) arg & 0xff;
		if (temp <= 0xf) {
			strTemp = "0";
			strTemp += Integer.toHexString(arg & 0xff);
		} else {
			strTemp = Integer.toHexString(arg & 0xff);
		}
		str = str + strTemp;

		return str;
	}

	public class MyNFCThread extends Thread {

//		PiccInterface piccdemo = new PiccInterface() {
//			@Override
//			public void getContactlessCard(int i, ContactlessCard contactlessCard) {
//
//			}
//		};

		public void run() {
			System.out.println("Open PICC....");
			int a = 0;
			try {
				piccReader.open();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (PiccException e) {
			}
			;
			Log.i("PICCDemo", "++++begin search++++");
			try {

				while (NfcThreadflag) {
					piccReader.search(ContactlessCard.TYPE_FELICA, 60000, new PiccInterface() {
						@Override
						public void getContactlessCard(int i, ContactlessCard contactlessCard) {

						}
					});

					NfcThreadflag=false;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (PiccException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Log.i("PICCDemo", "++++End search++++");
			if (contactlessCard != null) {
				Log.i("PICCDemo", "++++begin A card reset++++");
				if (contactlessCard.getType() == ContactlessCard.TYPE_A) {
					try {
						piccReader.reset(contactlessCard);

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (PiccException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}


				Log.i("PICCDemo", "++++begin A B Card transmit++++");
				if (contactlessCard.getType() == ContactlessCard.TYPE_A || contactlessCard.getType() == ContactlessCard.TYPE_B) {

					try {
						responseApdu = piccReader.transmit(commandApdu);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (PiccException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (responseApdu != null) {

						}


					}
				}
				if (contactlessCard.getType() == ContactlessCard.TYPE_MIFARE) {

					byte[] pw = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
					//byte[] pw = {(byte) 0x65,(byte) 0x70,(byte) 0x6c,(byte) 0x69,(byte) 0x6e,(byte) 0x6b};
					byte[] serialNo = contactlessCard.getSerialNo();
					byte[] BlkValue = new byte[16];
					byte[] writeData = new byte[16];
					byte[] test = new byte[20];
					int ret = -1;
					int[] nValue = new int[2];
					test[0] = (byte) serialNo.length;
					System.arraycopy(serialNo, 0, test, 1, serialNo.length);
					Log.v(TAG, "m1Authority begin ");
					ret = piccReader.m1Authority('a', (char) 4, pw, test);
					Log.v(TAG, "m1Authority ret = " + ret);


					ret = piccReader.m1ReadBlock((char) 4, BlkValue);
					Log.v(TAG, "m1ReadBlock ret = " + ret);

					/*piccReader.m1WriteBlock((char)4, writeData);
					Log.v(TAG, "m1WriteBlock ret = "+ret);*/

					ret = piccReader.m1ReadValue((char) 4, nValue);
					Log.v(TAG, "readValue ret = " + ret);

					ret = piccReader.m1Operate('-', (char) 4, 100, (char) 4);
					Log.v(TAG, "m1Operate - ret = " + ret);

					ret = piccReader.m1ReadValue((char) 4, nValue);
					Log.v(TAG, "readValue - ret = " + ret);

					ret = piccReader.m1Operate('+', (char) 4, 100, (char) 4);
					Log.v(TAG, "m1Operate + ret = " + ret);

					ret = piccReader.m1ReadValue((char) 4, nValue);
					Log.v(TAG, "readValue + ret = " + ret);
				}

				if (contactlessCard.getType() == ContactlessCard.TYPE_FELICA) {
					byte[] reData = piccReader.felicaCardSendData(apdu, apdu.length);

				}

			}

//            try {
//                piccReader.close();
//                Log.i("Piccdemo", "picc close");
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (PiccException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//
//            }

		}
	}

	public void getContactlessCard(int arg0, ContactlessCard arg1) {
		// TODO Auto-generated method stub
		if (arg0 == 0) {
//            stateString = "Successful";
//            this.contactlessCard = arg1;
//            Log.i(TAG,"Successful");
            Message msg = new Message();
            msg.what = CHECK_SUCCESS;
//            mHandler.sendMessage(msg);
		} else if (arg0 == PiccReader.TIMEOUT_ERROR) {
//            stateString = "Time out";
//            Log.e(TAG,"Time out");
//            Message msg = new Message();
//            msg.what = TIME_OUT;
//            mHandler.sendMessage(msg);
		} else if (arg0 == USER_CANCEL) {
//            stateString = "User cancel";
//            Log.e(TAG,"User cancel");
//            Message msg = new Message();
//            msg.what = USER_CANCEL;
//            mHandler.sendMessage(msg);
		}
	}




	//Printer
	private int printTicket(String TransactionId) {

		int i, state = 0;
		int ret = -1;
		Double Amount;
		String TransactionDateTime = "";
		String TransactionAccountNo = "";
		try {
			HttpClient clientRec = new HttpClient();
			String url = "http://" + getString(R.string.server_name) + "/smartpos/WebAPI.php?Type=Receipt&TransactionId=" + TransactionId + "";

			Response responseRec = clientRec.get(url);

			if (!responseRec.isSuccessful()) {
				return state;
			} else {

				String result = responseRec.body().string();
				JSONObject reader = new JSONObject(result);
				JSONArray nameTable = reader.getJSONArray("Result");
				JSONObject item = nameTable.getJSONObject(0);

				Amount = Double.valueOf(item.getString("Amount"));
				TransactionDateTime = item.getString("TransactionDateTime");
				TransactionAccountNo = item.getString("TransactionAccountNo");


			}
		} catch (IOException e) {
			return state;
		} catch (JSONException e) {
			return state;
		}
		bLogo = true;

		if (bLogo == true) {
			thermalPrinter.initBuffer();
			thermalPrinter.setGray(ilevel);
			Resources res = getResources();
			Bitmap bitmap1 = BitmapFactory.decodeResource(res, R.drawable.cargills);
			thermalPrinter.printLogo(0, 0, bitmap1);
			thermalPrinter.setStep(10);
			thermalPrinter.printStart();
			state = thermalPrinter.waitForPrintFinish();

		}

		thermalPrinter.initBuffer();

		if (Build.BOARD.equals("msm8909")) {
			boolean paper = thermalPrinter.isPaperOut();
			if (paper) {
				thermalPrinter.feedPaper(52);
				thermalPrinter.waitForPrintFinish();
				paper = thermalPrinter.isPaperOut();
				if (paper) {
					PrinterThread = null;
					return thermalPrinter.NO_PAPER;
				}
			}
		}

		thermalPrinter.setGray(ilevel);
		thermalPrinter.setHeightAndLeft(0, 0);
		thermalPrinter.setLineSpacing(5);
		thermalPrinter.setDispMode(ThermalPrinter.UMODE);

		thermalPrinter.setFont(ThermalPrinter.ASC8X16_DEF, ThermalPrinter.HZK12);
		String Address = "Cargills Bank\n34 Maitland Cres, Colombo 00700\n";
		thermalPrinter.print(Address);

		String Phone = "Tel : 0117 514 555\n\n";
		thermalPrinter.print(Phone);


		thermalPrinter.setFont(ThermalPrinter.ASC12X24, ThermalPrinter.HZK12);
		String Header = "PAYMENT RECEIPT\n";
		thermalPrinter.print(Header);


		buffer = new byte[48];
		thermalPrinter.setFont(ThermalPrinter.ASC8X16_DEF, ThermalPrinter.HZK12);
		for (i = 0; i < 48; i++) {
			buffer[i] = (byte) 0xfc;
		}
		thermalPrinter.printLine(buffer);
		thermalPrinter.printLine(buffer);
		thermalPrinter.setStep(12);


		String PTransactionDateTime = "     Date " + String.format("%38s", TransactionDateTime);
		thermalPrinter.print(PTransactionDateTime);

		String PTransactionId = "     Bill No " + String.format("%35s", TransactionId);
		thermalPrinter.print(PTransactionId);

		String PTransactionAccountNo = "     Acc No" + String.format("%37s", TransactionAccountNo);
		thermalPrinter.print(PTransactionAccountNo);

		String PType = String.format("%48s", "CARD PAYMENT");
		thermalPrinter.print(PType);

		thermalPrinter.setStep(15);

		thermalPrinter.setFont(ThermalPrinter.ASC12X24, ThermalPrinter.HZK12);
		String DAmount = String.format("%,.2f", Amount);
		String PAmount = "    Paid : " + String.format("%21s", "LKR " + DAmount);
		thermalPrinter.print(PAmount);

		thermalPrinter.setStep(150);

		buffer = new byte[24];
		thermalPrinter.setFont(ThermalPrinter.ASC8X16_DEF, ThermalPrinter.HZK12);
		for (i = 0; i < 24; i++) {
			buffer[i] = (byte) 0xfc;
		}
		thermalPrinter.printLine(buffer);
		thermalPrinter.printLine(buffer);

//		thermalPrinter.setFont(ThermalPrinter.ASC8X16_DEF, ThermalPrinter.HZK12);
//		String Signature = "     Signature\n";
//		thermalPrinter.print(Signature);

		String PHolder = CardHolder + "\n\n";
		thermalPrinter.print(PHolder);

		thermalPrinter.setFont(ThermalPrinter.ASC12X24, ThermalPrinter.HZK12);
		String EndOne = "            THANK YOU\n";
		thermalPrinter.print(EndOne);

		String EndTwo = "    WELCOME TO CARGILLS BANK\n\n";
		thermalPrinter.print(EndTwo);

		thermalPrinter.setStep(15);

		buffer = new byte[48];
		thermalPrinter.setFont(ThermalPrinter.ASC8X16_DEF, ThermalPrinter.HZK12);
		for (i = 0; i < 48; i++) {
			buffer[i] = (byte) 0xfc;
		}
		thermalPrinter.printLine(buffer);
		thermalPrinter.printLine(buffer);


		thermalPrinter.setStep(200);
		ret = thermalPrinter.printStart();
		state = thermalPrinter.waitForPrintFinish();
		return state;


	}

}
