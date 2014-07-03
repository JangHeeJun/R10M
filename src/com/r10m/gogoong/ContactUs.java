package com.r10m.gogoong;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.r10m.gogoong.R;
import com.r10m.gogoong.resource.around.GMailSender;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ContactUs extends Activity implements OnClickListener {
	
	ProgressDialog dialog;
	EditText contentEt;
	EditText fromEt;
	GMailSender sender;
	CheckBox agreeCheck;
	StringBuffer phoneInfo = new StringBuffer();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contactus);
		Button send = (Button)this.findViewById(R.id.bt_send);
		send.setOnClickListener(this);
		

		 //설정_뒤로
	    Button btn_back = (Button) findViewById(R.id.btn_back);
	    btn_back.setOnClickListener(new OnClickListener(){
		       @Override
		       public void onClick(View v) {
					finish();
		       }
	    });
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	//이메일 유효성
	public static boolean isEmailPattern(String email){
	   if( android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ){
		   return true;
	   }else{
		   return false;
	   }
	}
	   
	//Send 버튼 클릭
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		phoneInfo.append("Phone Number : "+getPhoneNumber()+", \n");
		phoneInfo.append("OperatorName : "+getSimOperatorName()+", \n");
		phoneInfo.append("ModelName : "+getModelName()+", \n");
		phoneInfo.append("FirwareVer : "+getFirmwareVersion()+", \n");
		phoneInfo.append("OSVer : "+getOSVersion()+", \n");
		phoneInfo.append("Internal Storage : "+getInternalStorageSize()+", \n");
		phoneInfo.append("External Storage : "+getExternalStorageSize()+", \n");
		phoneInfo.append("Internal Storage Per: "+getInternalStoragePercent() + "%\n");
		
		contentEt = (EditText) this.findViewById(R.id.editText4);
		String contentStr = contentEt.getText()+"";
		fromEt = (EditText) this.findViewById(R.id.editText2);
		agreeCheck = (CheckBox)this.findViewById(R.id.checkBox1);
		
		if (!(isEmailPattern(fromEt.getText()+""))) {
			Toast toastEmail = Toast.makeText(this, "이메일 주소가 올바르지 않습니다.", Toast.LENGTH_SHORT);
			toastEmail.show();
		}else if(contentStr.equals("")){
			Toast toastContent = Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT);
			toastContent.show();
		}else if (!(agreeCheck.isChecked())) {
			Toast toastContent = Toast.makeText(this, "동의는 필수 항목입니다.", Toast.LENGTH_SHORT);
			toastContent.show();
		}else {
			sender = new GMailSender("drp2pp@gmail.com", "rlagywls"); // SUBSTITUTE ID PASSWORD
			timeThread();
		}
	}

	public void timeThread() {
		
		phoneInfo.append(contentEt.getText().toString());
		dialog = new ProgressDialog(this);
		dialog = new ProgressDialog(this);
		dialog.setTitle("Wait...");
		dialog.setMessage("의견을 보내는 중입니다.");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.show();
		new Thread(new Runnable() {

			public void run() {
				Looper.prepare();
				// TODO Auto-generated method stub
				try {
					sender.sendMail("의견보내기", phoneInfo.toString(),
							fromEt.getText().toString(), "drp2pp@gmail.com"
					);
					sleep(3000);
					okPopup();
					
				} catch (Exception e) {
					Log.e("SendMail", e.getMessage(), e);
					Toast.makeText(ContactUs.this, "신청 실패", Toast.LENGTH_SHORT).show();
				}
				dialog.dismiss();
				Looper.loop();
			}
			
			
			private void sleep(int i) {
				// TODO Auto-generated method stub
			}
			
			private void okPopup(){
				AlertDialog.Builder alert = new AlertDialog.Builder(ContactUs.this);
				alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
					    dialog.dismiss(); //닫기
					    
					    Intent intent = new Intent(ContactUs.this, SettingActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				        startActivity(intent);
				    }
				});
				alert.setMessage("전송이 완료되었습니다.");
				alert.show();
			}
		}).start();
		
	}
	
	public String getPhoneNumber() {
		TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		if (mTelephonyMgr.getLine1Number() == null || mTelephonyMgr.getLine1Number().trim().equals("")) {
			return "EMPTY";
		} else {
			return mTelephonyMgr.getLine1Number();
		}
	}
	
	public String getSimOperatorName() {
		TelephonyManager telephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyMgr.getSimOperatorName() == null || telephonyMgr.getSimOperatorName().equals("")) {
			return "EMPTY";
		} else {
			return telephonyMgr.getSimOperatorName();
		}
	}
	 
	public String getModelName() {
		return Build.MODEL;
	}
	 
	public String getFirmwareVersion() {
		return Build.VERSION.RELEASE;
	}
	 
	public String getOSVersion() {
		return System.getProperty("os.version");
	}
	 
	public String getInternalStorageSize() {
		String[] sizes = getStorageInfo(Environment.getDataDirectory());
		return sizes[0] + "(TOTAL)" + " / " + sizes[1] + "(AVAILABLE)";
	}
	 
	public String getExternalStorageSize() {
		String state = Environment.getExternalStorageState( );
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) || Environment.MEDIA_MOUNTED.equals(state)) {
			File externalPath = Environment.getExternalStorageDirectory();
			
			String[] sizes = getStorageInfo(externalPath);
			 
			Log.d("maluchi", externalPath.getAbsolutePath() + " : " + sizes[0] + " - " + sizes[1]);
			 
			if (sizes != null) {
				return sizes[0] + "(TOTAL)" + " / " + sizes[1] + "(AVAILABLE)";
			} else {
				return "EMPTY";
			}
		}
		return "EMPTY";
	}
	 
	private int getInternalStoragePercent() {
		StatFs stat = new StatFs(Environment.getDataDirectory().getAbsolutePath());
		long blockSize = stat.getBlockSize();
		long totalSize = stat.getBlockCount() * blockSize;
		long availableSize = stat.getAvailableBlocks() * blockSize;
		 
		int percent = (int)((((double)(totalSize - availableSize)) / (double)totalSize) * 100);
		if (percent < 1) percent = 1;
			return percent;
		}
		 
		private String[] getStorageInfo( File path ) {
		if ( path != null ) {
			try {
				StatFs stat = new StatFs( path.getAbsolutePath( ) );
				long blockSize = stat.getBlockSize( );
				 
				String[] info = new String[2];
				info[0] = Formatter.formatFileSize( this, stat.getBlockCount() * blockSize );
				info[1] = Formatter.formatFileSize( this, stat.getAvailableBlocks() * blockSize );
				return info;
			} catch ( Exception e ) {
				Log.d( "maluchi", "Cannot access path: " + path.getAbsolutePath( ), e );
			}
		}
		return null;
	}
}