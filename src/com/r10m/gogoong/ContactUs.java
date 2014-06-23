package com.r10m.gogoong;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.r10m.gogoong.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
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
					Intent intent = new Intent(ContactUs.this, SettingActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		            startActivity(intent);
		       }
	    });
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(ContactUs.this, SettingActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
		super.onBackPressed();
	}
	
	
	//이메일 유효성
	   public static boolean isEmailPattern(String email){
		   if( android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ){
			   return true;
		   }else{
			   return false;
		   }
	   }
	   
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
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
					sender.sendMail("의견보내기", contentEt.getText().toString(), 
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
	/*
	public void okPopup(){
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
		alert.setMessage("테스트 메세지");
		alert.show();
	}
*/
}
