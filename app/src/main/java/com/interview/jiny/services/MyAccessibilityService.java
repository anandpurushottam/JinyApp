package com.interview.jiny.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.interview.jiny.Constants;
import com.interview.jiny.R;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Marijuana Monster on 3/12/2017.
 */

public class MyAccessibilityService extends AccessibilityService {
    static final String TAG = "ExampleAccessibily";
    private WindowManager wm;
    private boolean isSpoken;
    ImageView simpleOverlay, simpleOverlay2;
    TextToSpeech t1;
    private Rect searchBarPostion, buyNowPostion;


    LinearLayout containor;

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                }
            }
        });


        containor = new LinearLayout(this);

        WindowManager.LayoutParams topButtonParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, //The width of the screen
                ViewGroup.LayoutParams.MATCH_PARENT, //The height of the screen
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        topButtonParams.alpha = 100;

        containor.setLayoutParams(topButtonParams);

        wm.addView(containor, topButtonParams);
        simpleOverlay = new ImageView(this);
        simpleOverlay.setImageDrawable(getResources().getDrawable(R.drawable.searcher));
        containor.addView(simpleOverlay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        simpleOverlay.setVisibility(View.GONE);

        simpleOverlay2 = new ImageView(this);
        simpleOverlay2.setImageDrawable(getResources().getDrawable(R.drawable.searcher));
        containor.addView(simpleOverlay2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        simpleOverlay2.setVisibility(View.GONE);

    }


    private void drawOverApp(Rect pos, int type) {
        if (type == 1) {
            simpleOverlay.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    pos.width(),
                    pos.height());
            params.setMargins(pos.left, (pos.top - pos.height()) + 48, pos.right, pos.bottom);
            simpleOverlay.setLayoutParams(params);
            simpleOverlay.invalidate();
        } else {
            simpleOverlay2.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    pos.width(),
                    pos.height());
            params.setMargins(pos.left, (pos.top - pos.height()) + 48, pos.right, pos.bottom);
            simpleOverlay2.setLayoutParams(params);
            simpleOverlay2.invalidate();
        }


    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        switch (event.getEventType()) {

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                if (event.getPackageName() != null) {
                    if (event.getPackageName().equals(Constants.FLIPKART_PACKAGE_NAME)) {
                        respondToFlipKartApp(event);
                    } else if (event.getPackageName().equals(Constants.PAYTM_PACKAGE)) {
                        respondToPaytmApp(event);
                    }
                }

            }
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
                if (event.getPackageName() != null) {
                    if (event.getPackageName().equals(Constants.FLIPKART_PACKAGE_NAME)) {
                        respondToFlipKartApp(event);
                    } else {
                        simpleOverlay.setVisibility(View.GONE);
                        simpleOverlay2.setVisibility(View.GONE);
                    }
                    if (event.getPackageName().equals(Constants.PAYTM_PACKAGE)) {
                        respondToPaytmApp(event);
                    }

                }


            }
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED: {
                if (event.getPackageName() != null) {
                    clearAllFlags(event.getPackageName().toString());
                }


            }


        }
    }


    private void respondToFlipKartApp(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        try {
            AccessibilityNodeInfo node = nodeInfo.getChild(0).getChild(0).getChild(5);
            if (node != null) {
                //SEARCH _BAR DETECt and DRAW a overlay
                if (node.getViewIdResourceName().equals("com.flipkart.android:id/search_widget")) {
                    Rect temp = searchBarPostion;
                    searchBarPostion = new Rect();
                    node.getBoundsInScreen(searchBarPostion);
                    drawOverApp(searchBarPostion, 1);

                } else {
                    simpleOverlay.setVisibility(View.GONE);
                }


            }
        } catch (Exception e) {
            // e.printStackTrace();
            simpleOverlay.setVisibility(View.GONE);
        }
        //Buy now button
        try {
            AccessibilityNodeInfo node = nodeInfo.getChild(0).getChild(0).getChild(0).getChild(5).getChild(1);
            if (node != null) {

                if (node.getViewIdResourceName().equals("com.flipkart.android:id/product_buynow_1")) {
                    Log.i("asnfsnbabfksabjkf", "Found");
                    Rect temp = buyNowPostion;
                    buyNowPostion = new Rect();
                    node.getBoundsInScreen(buyNowPostion);
                    drawOverApp(buyNowPostion, 2);


                } else {
                    simpleOverlay2.setVisibility(View.GONE);
                }

            }
        } catch (Exception e) {
            // e.printStackTrace();
            simpleOverlay2.setVisibility(View.GONE);
        }
        //   dumpNode(getRootInActiveWindow(), 0);


    }


    private void respondToPaytmApp(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo == null) {
            return;
        }
        boolean payExists = nodeInfo.findAccessibilityNodeInfosByText("Pay").size() > 0;
        boolean addMoneyExists = nodeInfo.findAccessibilityNodeInfosByText("Add Money").size() > 0;
        boolean passbookExists = nodeInfo.findAccessibilityNodeInfosByText("Passbook").size() > 0;
        boolean nearbyExists = nodeInfo.findAccessibilityNodeInfosByText("Nearby").size() > 0;
        if (payExists && addMoneyExists && passbookExists && nearbyExists) {
            if (!isSpoken) {
                textToSpeech("Welcome To Paytm : You can Pay Add Money Passbook AcceptPayment Nearby");
                isSpoken = true;
            }
        }
    }

    private void clearAllFlags(String packageName) {
        if (!packageName.equals(Constants.PAYTM_PACKAGE)) {
            isSpoken = false;
        }


    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.v(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY |
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);

        Toast.makeText(this, "Service has been started", Toast.LENGTH_SHORT).show();
    }

    private void textToSpeech(String toSpeak) {
        Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String utteranceId = this.hashCode() + "";
            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, map);
        }
    }

    @Override
    public void onInterrupt() {
    }

}

