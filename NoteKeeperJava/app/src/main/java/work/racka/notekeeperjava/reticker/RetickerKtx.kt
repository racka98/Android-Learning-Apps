@file:JvmName("ReTicker")

package work.racka.notekeeperjava.reticker

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import work.racka.notekeeperjava.R

class RetickerKtx : NotificationListenerService() {
    private val TAG = "ReTicker"

    //private boolean mIsKeyguard;
    private var mContext: Context? = null
    private var mIcon: Drawable? = null
    private val mQSExpansion = 0f
    private var mReTickerComebackIcon: ImageView? = null

    //private final KeyguardUpdateMonitor mUpdateMonitor;
    private lateinit var mKeyguardMan: KeyguardManager
    private var mReTickerComeback: LinearLayout? = null
    private var mNotificationIntent: PendingIntent? = null
    private var mAppName: String? = null
    private var mNotificationContent: String? = null
    private lateinit var mPackageName: String
    private var mReTickerContentTV: TextView? = null
    private val mHandler = Handler()
    private val DEBUG = true

    //private final NotificationPanelViewController mNotificationPanelViewController;
    private var mReTickerViewInflated: View? = null
    private var mParams: WindowManager.LayoutParams? = null
    private var mWindowManager: WindowManager? = null

    /**
     * Keyguard listener
     */
    //    private final KeyguardUpdateMonitorCallback mMonitorCallback = new KeyguardUpdateMonitorCallback() {
    //        @Override
    //        public void onKeyguardVisibilityChanged(boolean showing) {
    //            mIsKeyguard = showing;
    //        }
    //    };
    fun ReTicker(
        ctx: Context?
    ) {
        mContext = ctx
        mWindowManager = mContext!!.getSystemService(WINDOW_SERVICE) as WindowManager
        mKeyguardMan = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        assignRetickerView()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (DEBUG) Log.d(TAG, "onNotificationPosted")
        if (mKeyguardMan.isKeyguardLocked) return
        mAppName = sbn.notification.extras.getString("android.title")
        mPackageName = sbn.packageName
        mIcon = null
        mNotificationIntent = sbn.notification.contentIntent
        try {
            mIcon = if (mPackageName.contains("systemui")) {
                mContext!!.getDrawable(sbn.notification.icon)
            } else {
                mContext!!.packageManager.getApplicationIcon(mPackageName)
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }
        if (sbn.notification.extras.getString("android.text") != null) {
            mNotificationContent = sbn.notification.extras.getString("android.text")
        }
        if (DEBUG) logAll()
        fillAndShowRetickerView()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}

    private fun assignRetickerView() {
        mReTickerViewInflated = LayoutInflater.from(mContext).inflate(R.layout.reticker, null)
        //Add the view to the window.
        mParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        mParams!!.gravity = Gravity.TOP or Gravity.CENTER
        mWindowManager!!.addView(mReTickerViewInflated, mParams)
        mReTickerViewInflated!!.visibility = View.GONE
        mReTickerComeback = mReTickerViewInflated!!.findViewById(R.id.ticker_comeback)
        mReTickerComebackIcon = mReTickerViewInflated!!.findViewById(R.id.ticker_comeback_icon)
        mReTickerContentTV = mReTickerViewInflated!!.findViewById(R.id.ticker_content)
    }

    private fun fillAndShowRetickerView() {
        mReTickerComebackIcon!!.setImageDrawable(mIcon)
        mReTickerContentTV!!.text = "$mAppName $mNotificationContent"
        mReTickerContentTV!!.isSelected = true
        showReticker()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showReticker() {
        if (DEBUG) Log.d(TAG, "********** showReticker")
        if (mQSExpansion == 1f) return
        mHandler.removeCallbacksAndMessages(null)
        mReTickerComeback!!.setOnTouchListener(object : OnTouchListener {
            private var lastAction = 0
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Log.d(TAG, "***** DOWN: last action ->$lastAction")
                        //remember the initial position.
                        initialX = mParams!!.x
                        initialY = mParams!!.y

                        //get the touch location
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        lastAction = event.action
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        Log.d(TAG, "***** UP: last action ->$lastAction")
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            try {
                                if (mNotificationIntent != null) mNotificationIntent!!.send()
                            } catch (e: CanceledException) {
                            }
                            if (mNotificationIntent != null) {
                                ReTickerAnimations.doBounceAnimationOut(mReTickerViewInflated)
                            }
                        } else if (lastAction == MotionEvent.ACTION_MOVE) {
                            reTickerDismissal(true)
                        }
                        lastAction = event.action
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        mHandler.removeCallbacksAndMessages(null)
                        //Calculate the X and Y coordinates of the view.
                        mParams!!.x = initialX + (event.rawX - initialTouchX).toInt()
                        mParams!!.y = initialY + (event.rawY - initialTouchY).toInt()

                        //Update the layout with new X & Y coordinate
                        mWindowManager!!.updateViewLayout(mReTickerComeback, mParams)
                        lastAction = event.action
                        return true
                    }
                }
                return false
            }
        })
        ReTickerAnimations.doBounceAnimationIn(mReTickerViewInflated)
        reTickerDismissal(true)
    }

    fun reTickerDismissal(scheduled: Boolean) {
        if (DEBUG) Log.d(TAG, "********** retickerDismissal is scheduled: $scheduled")
        if (scheduled) {
            mHandler.postDelayed(
                { ReTickerAnimations.doBounceAnimationOut(mReTickerViewInflated) },
                5000
            )
        } else {
            ReTickerAnimations.doBounceAnimationOut(mReTickerViewInflated)
        }
    }

    /**
     * Vars handling
     */
    private fun retickerSpawnable(): Boolean {
        return mIcon != null && mAppName != null && mNotificationContent != null
    }

    /**
     * Panel listeners
     */
    // TODO: reticker Visibility change
//    @Override
//    public void onQsExpansionChanged(float expansion) {
//    }
//
//    @Override
//    public void onPanelExpansionChanged(float expansion, boolean tracking) {
//        if (DEBUG) Log.d(TAG, "********** onPanelExpansionChanged: " + expansion);
//        mQSExpansion = expansion;
//        if (expansion > 0) {
//            mReTickerComeback.setVisibility(View.GONE);
//        }
//    }

    /**
     * Panel listeners
     */
    //    @Override
    //    public void onQsExpansionChanged(float expansion) {
    //    }
    //
    //    @Override
    //    public void onPanelExpansionChanged(float expansion, boolean tracking) {
    //        if (DEBUG) Log.d(TAG, "********** onPanelExpansionChanged: " + expansion);
    //        mQSExpansion = expansion;
    //        if (expansion > 0) {
    //            mReTickerComeback.setVisibility(View.GONE);
    //        }
    //    }
    /**
     * Debug methods
     */
    private fun logAll() {
        Log.d(TAG, "********** notification debug data")
        Log.d(TAG, "********** icon is null:" + (mIcon == null))
        Log.d(TAG, "********** package name is:$mPackageName")
        Log.d(TAG, "********** app name is:$mAppName")
        Log.d(TAG, "********** notification content is:$mNotificationContent")
        Log.d(TAG, "********** pending intent is:$mNotificationIntent")
    }
}