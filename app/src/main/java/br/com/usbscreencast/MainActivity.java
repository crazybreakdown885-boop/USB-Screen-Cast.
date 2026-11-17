package br.com.usbscreencast;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Map;

public class MainActivity extends Activity {
    private static final String DISPLAYLINK_STORE_URL =
            "https://play.google.com/store/apps/details?id=com.displaylink.presenter";

    private TextView statusView;
    private TextView devicesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22), dp(28), dp(22), dp(24));
        content.setBackgroundColor(0xFFF5F7FA);

        TextView title = new TextView(this);
        title.setText("USB Screen Cast");
        title.setTextColor(0xFF14213D);
        title.setTextSize(27);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(8));
        content.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Assistente de conexão USB para DisplayLink");
        subtitle.setTextColor(0xFF526174);
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        content.addView(subtitle, matchWrap());

        statusView = new TextView(this);
        statusView.setTextSize(17);
        statusView.setTextColor(0xFF14213D);
        statusView.setPadding(0, dp(24), 0, dp(12));
        content.addView(statusView, matchWrap());

        devicesView = new TextView(this);
        devicesView.setTextSize(14);
        devicesView.setTextColor(0xFF344054);
        devicesView.setPadding(0, dp(8), 0, dp(18));
        content.addView(devicesView, matchWrap());

        Button refreshButton = new Button(this);
        refreshButton.setText("Verificar conexão USB");
        refreshButton.setOnClickListener(v -> refreshUsbStatus());
        content.addView(refreshButton, matchWrap());

        Button presenterButton = new Button(this);
        presenterButton.setText("Abrir / instalar DisplayLink Presenter");
        presenterButton.setOnClickListener(v -> openDisplayLinkPage());
        content.addView(presenterButton, matchWrap());

        TextView note = new TextView(this);
        note.setText("\nImportante: este aplicativo não substitui o driver DisplayLink. "
                + "É necessário um adaptador DisplayLink USB compatível, conexão OTG "
                + "e o aplicativo oficial DisplayLink Presenter. Um cabo USB-C para HDMI "
                + "comum não adiciona saída de vídeo ao telefone.");
        note.setTextSize(13);
        note.setTextColor(0xFF667085);
        note.setPadding(0, dp(18), 0, 0);
        content.addView(note, matchWrap());

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        setContentView(scroll);
        refreshUsbStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (statusView != null) refreshUsbStatus();
    }

    private void refreshUsbStatus() {
        UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
        if (usbManager == null) {
            statusView.setText("Não foi possível acessar o serviço USB do Android.");
            devicesView.setText("");
            return;
        }

        Map<String, UsbDevice> devices = usbManager.getDeviceList();
        if (devices == null || devices.isEmpty()) {
            statusView.setText("Nenhum dispositivo USB detectado.");
            devicesView.setText("Conecte o adaptador DisplayLink usando um adaptador OTG "
                    + "compatível e toque em Verificar conexão USB.");
            return;
        }

        statusView.setText("Dispositivo(s) USB detectado(s): " + devices.size());
        StringBuilder details = new StringBuilder();
        for (UsbDevice device : devices.values()) {
            details.append("• ").append(device.getDeviceName())
                    .append("\n  Fabricante (VID): ").append(device.getVendorId())
                    .append("\n  Produto (PID): ").append(device.getProductId())
                    .append("\n\n");
        }
        devicesView.setText(details.toString());
    }

    private void openDisplayLinkPage() {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.displaylink.presenter");
            if (launch != null) {
                startActivity(launch);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DISPLAYLINK_STORE_URL)));
            }
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DISPLAYLINK_STORE_URL)));
        }
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
