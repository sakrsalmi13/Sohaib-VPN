package com.sohaib.vpn;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int VPN_REQUEST_CODE = 0x0F;
    private boolean isVpnRunning = false;
    private Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = findViewById(R.id.btnConnect);

        btnConnect.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isVpnRunning) {
						stopVpn();
					} else {
						startVpn();
					}
				}
			});
    }

    private void startVpn() {
        // التحقق مما إذا كان إذن الـ VPN ممنوحاً من قبل المستخدم
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            // إذا كان الإذن غير ممنوح، نطلب الإذن من النظام
            startActivityForResult(intent, VPN_REQUEST_CODE);
        } else {
            // إذا كان الإذن ممنوحاً مسبقاً، نشغل الخدمة فوراً
            onActivityResult(VPN_REQUEST_CODE, Activity.RESULT_OK, null);
        }
    }

    private void stopVpn() {
        Intent intent = new Intent(this, XrayVpnService.class);
        stopService(intent);
        btnConnect.setText("اتصال");
        isVpnRunning = false;
        Toast.makeText(this, "تم فصل الـ VPN", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            // بعد موافقة المستخدم، نشغل كود الـ Service الذي كتبناه سابقاً
            Intent intent = new Intent(this, XrayVpnService.class);
            startService(intent);

            btnConnect.setText("قطع الاتصال");
            isVpnRunning = true;
            Toast.makeText(this, "جاري تشغيل Sohaib VPN...", Toast.LENGTH_SHORT).show();
        }
    }
}

