//package work.racka.notekeeperjava.reticker;
//
//import android.annotation.SuppressLint;
//import android.app.KeyguardManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.PixelFormat;
//import android.graphics.drawable.Drawable;
//import android.os.Handler;
//import android.os.IBinder;
//import android.service.notification.NotificationListenerService;
//import android.service.notification.StatusBarNotification;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.view.WindowManager.LayoutParams;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
////import com.android.keyguard.KeyguardUpdateMonitor;
////import com.android.keyguard.KeyguardUpdateMonitorCallback;
////import com.android.systemui.Dependency;
////import com.android.systemui.statusbar.phone.NotificationPanelViewController;
////import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
////import com.android.systemui.statusbar.phone.PanelExpansionListener;
//
//import work.racka.notekeeperjava.R;
//
//public class ReTicker extends NotificationListenerService {
//
//    private static final String TAG = "ReTicker";
//    //private boolean mIsKeyguard;
//    private final Context mContext;
//    private Drawable mIcon;
//    private float mQSExpansion;
//    private ImageView mReTickerComebackIcon;
//    //private final KeyguardUpdateMonitor mUpdateMonitor;
//    private final KeyguardManager mKeyguardMan = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//    private LinearLayout mReTickerComeback;
//    private PendingIntent mNotificationIntent;
//    private String mAppName;
//    private String mNotificationContent;
//    private String mPackageName;
//    private TextView mReTickerContentTV;
//    private final Handler mHandler = new Handler();
//    private final boolean DEBUG = true;
//    //private final NotificationPanelViewController mNotificationPanelViewController;
//    private View mReTickerViewInflated;
//    private LayoutParams mParams;
//    private final WindowManager mWindowManager;
//    /**
//     * Keyguard listener
//     */
//
////    private final KeyguardUpdateMonitorCallback mMonitorCallback = new KeyguardUpdateMonitorCallback() {
////        @Override
////        public void onKeyguardVisibilityChanged(boolean showing) {
////            mIsKeyguard = showing;
////        }
////    };
//
//    public ReTicker(Context ctx, NotificationShadeWindowView nswv, NotificationPanelViewController npvc) {
//        mContext = ctx;
////        mUpdateMonitor = Dependency.get(KeyguardUpdateMonitor.class);
////        mUpdateMonitor.registerCallback(mMonitorCallback);
//        mNotificationPanelViewController = npvc;
//        mNotificationPanelViewController.addExpansionListener(this);
//        mWindowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
//        assignRetickerView();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return super.onBind(intent);
//    }
//
//    @Override
//    public void onNotificationPosted(StatusBarNotification sbn) {
//        if (DEBUG) Log.d(TAG, "onNotificationPosted");
//        if (mKeyguardMan.isKeyguardLocked()) return;
//        mAppName = sbn.getNotification().extras.getString("android.title");
//        mPackageName = sbn.getPackageName();
//        mIcon = null;
//        mNotificationIntent = sbn.getNotification().contentIntent;
//        try {
//            if (mPackageName.contains("systemui")) {
//                mIcon = mContext.getDrawable(sbn.getNotification().icon);
//            } else {
//                mIcon = mContext.getPackageManager().getApplicationIcon(mPackageName);
//            }
//        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
//        }
//        if (sbn.getNotification().extras.getString("android.text") != null) {
//            mNotificationContent = sbn.getNotification().extras.getString("android.text");
//        }
//        if (DEBUG) logAll();
//        fillAndShowRetickerView();
//    }
//
//    @Override
//    public void onNotificationRemoved(StatusBarNotification sbn) {
//    }
//
//    private void assignRetickerView() {
//        mReTickerViewInflated = LayoutInflater.from(mContext).inflate(R.layout.reticker, null);
//        //Add the view to the window.
//        mParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT,
//                LayoutParams.TYPE_PHONE,
//                LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT);
//        mParams.gravity = Gravity.TOP | Gravity.CENTER;
//        mWindowManager.addView(mReTickerViewInflated, mParams);
//        mReTickerViewInflated.setVisibility(View.GONE);
//        mReTickerComeback = mReTickerViewInflated.findViewById(R.id.ticker_comeback);
//        mReTickerComebackIcon = mReTickerViewInflated.findViewById(R.id.ticker_comeback_icon);
//        mReTickerContentTV = mReTickerViewInflated.findViewById(R.id.ticker_content);
//    }
//
//    private void fillAndShowRetickerView() {
//        mReTickerComebackIcon.setImageDrawable(mIcon);
//        mReTickerContentTV.setText(mAppName + " " + mNotificationContent);
//        mReTickerContentTV.setSelected(true);
//        showReticker();
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private void showReticker() {
//        if (DEBUG) Log.d(TAG, "********** showReticker");
//        if (mQSExpansion == 1) return;
//        mHandler.removeCallbacksAndMessages(null);
//        mReTickerComeback.setOnTouchListener(new View.OnTouchListener() {
//            private int lastAction;
//            private int initialX;
//            private int initialY;
//            private float initialTouchX;
//            private float initialTouchY;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        Log.d(TAG, "***** DOWN: last action ->" + lastAction);
//                        //remember the initial position.
//                        initialX = mParams.x;
//                        initialY = mParams.y;
//
//                        //get the touch location
//                        initialTouchX = event.getRawX();
//                        initialTouchY = event.getRawY();
//
//                        lastAction = event.getAction();
//                        return true;
//                    case MotionEvent.ACTION_UP:
//                        Log.d(TAG, "***** UP: last action ->" + lastAction);
//                        //As we implemented on touch listener with ACTION_MOVE,
//                        //we have to check if the previous action was ACTION_DOWN
//                        //to identify if the user clicked the view or not.
//                        if (lastAction == MotionEvent.ACTION_DOWN) {
//                            try {
//                                if (mNotificationIntent != null)
//                                    mNotificationIntent.send();
//                            } catch (PendingIntent.CanceledException e) {
//                            }
//                            if (mNotificationIntent != null) {
//                                ReTickerAnimations.doBounceAnimationOut(mReTickerViewInflated);
//                            }
//                        } else if (lastAction == MotionEvent.ACTION_MOVE) {
//                            reTickerDismissal(true);
//                        }
//                        lastAction = event.getAction();
//                        return true;
//                    case MotionEvent.ACTION_MOVE:
//                        mHandler.removeCallbacksAndMessages(null);
//                        //Calculate the X and Y coordinates of the view.
//                        mParams.x = initialX + (int) (event.getRawX() - initialTouchX);
//                        mParams.y = initialY + (int) (event.getRawY() - initialTouchY);
//
//                        //Update the layout with new X & Y coordinate
//                        mWindowManager.updateViewLayout(mReTickerComeback, mParams);
//                        lastAction = event.getAction();
//                        return true;
//                }
//                return false;
//            }
//        });
//        ReTickerAnimations.doBounceAnimationIn(mReTickerViewInflated);
//        reTickerDismissal(true);
//    }
//
//    public void reTickerDismissal(boolean scheduled) {
//        if (DEBUG)
//            Log.d(TAG, "********** retickerDismissal is scheduled: " + scheduled);
//        if (scheduled) {
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    ReTickerAnimations.doBounceAnimationOut(mReTickerViewInflated);
//                }
//            }, 5000);
//        } else {
//            ReTickerAnimations.doBounceAnimationOut(mReTickerViewInflated);
//        }
//    }
//
//    /**
//     * Vars handling
//     */
//    private boolean retickerSpawnable() {
//        return mIcon != null && mAppName != null && mNotificationContent != null;
//    }
//
//    /**
//     * Panel listeners
//     */
////    @Override
////    public void onQsExpansionChanged(float expansion) {
////    }
////
////    @Override
////    public void onPanelExpansionChanged(float expansion, boolean tracking) {
////        if (DEBUG) Log.d(TAG, "********** onPanelExpansionChanged: " + expansion);
////        mQSExpansion = expansion;
////        if (expansion > 0) {
////            mReTickerComeback.setVisibility(View.GONE);
////        }
////    }
//
//    /**
//     * Debug methods
//     */
//
//    private void logAll() {
//        Log.d(TAG, "********** notification debug data");
//        Log.d(TAG, "********** icon is null:" + (mIcon == null));
//        Log.d(TAG, "********** package name is:" + mPackageName);
//        Log.d(TAG, "********** app name is:" + mAppName);
//        Log.d(TAG, "********** notification content is:" + mNotificationContent);
//        Log.d(TAG, "********** pending intent is:" + mNotificationIntent);
//    }
//
//}
