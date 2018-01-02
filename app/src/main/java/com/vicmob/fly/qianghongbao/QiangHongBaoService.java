package com.vicmob.fly.qianghongbao;

import java.util.List;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

@SuppressLint("NewApi")
public class QiangHongBaoService extends AccessibilityService
{

	static final String TAG = "tgh";

	/** 微信的包名 */
	static final String WECHAT_PACKAGENAME = "com.tencent.mm";
	/** 红包消息的关键字 */
	static final String WX_HONGBAO_TEXT_KEY = "[微信红包]";
	static final String QQ_HONGBAO_TEXT_KEY = "[QQ红包]";
	private boolean caihongbao = false;
	private boolean luckyClicked, hasLucky;
	Handler handler = new Handler();
	public AccessibilityNodeInfo node = null;

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event)
	{
		final int eventType = event.getEventType(); // ClassName:
													// com.tencent.mm.ui.LauncherUI

		// 通知栏事件
		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
		{
			unlockScreen();
			luckyClicked = false;
			List<CharSequence> texts = event.getText();
			if (!texts.isEmpty())
			{
				for (CharSequence t : texts)
				{
					String text = String.valueOf(t);
					if (text.contains(WX_HONGBAO_TEXT_KEY) || text.contains(QQ_HONGBAO_TEXT_KEY))
					{
						Log.i(TAG,"Find Notification");

						openNotify(event);
						break;
					}
				}
			}
		} else if (eventType == AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE)
		{
			// 从微信主界面进入聊天界面
			openHongBao(event);
		} else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
		{
			Log.i("TAG","(5 TYPE_WINDOW_STATE_CHANGED ) ");
			// 处理微信聊天界面
			openHongBao(event);
			Log.i("TAG","(6 TYPE_WINDOW_STATE_CHANGED ) ");
			String clazzName = event.getClassName().toString();
			if (clazzName.equals("com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f")) {
				traverseNode(event.getSource());
			}
		}
	}

	@Override
	public void onInterrupt()
	{
		Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onServiceConnected()
	{
		super.onServiceConnected();
		Toast.makeText(this, "连接抢红包服务", Toast.LENGTH_SHORT).show();
	}

	/** 打开通知栏消息 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void openNotify(AccessibilityEvent event)
	{

		if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification))
		{
			return;
		}
		// 将微信的通知栏消息打开
		Notification notification = (Notification) event.getParcelableData();
		PendingIntent pendingIntent = notification.contentIntent;
		try
		{
			pendingIntent.send();
		} catch (PendingIntent.CanceledException e)
		{
			e.printStackTrace();
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void openHongBao(AccessibilityEvent event)
	{
		CharSequence className = event.getClassName();

		checkScreen(getApplicationContext());

		if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(className))
		{
			// 点中了红包，下一步就是去拆红包
			Log.i("TAG","(1 if)  plugin.luckymoney.ui.LuckyMoneyReceiveUI");
			checkKey1();

		}
		else if ("com.tencent.mm.ui.LauncherUI".equals(className) || "com.tencent.mobileqq.activity.ChatActivity".equals(className))
		{
			// 在聊天界面,去点中红包
			Log.i("TAG","(2 else if)  plugin.luckymoney.ui.LuckyMoneyReceiveUI");
			checkKey2();
		}
		else if ("com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f".equals(className))
		{
			// 在聊天界面,去点中红包
//			checkKey2();
			Log.i("TAG","(3 else if)  plugin.luckymoney.ui.LuckyMoneyReceiveUI");
			traverseNode(event.getSource());
		}
		else
		{
			// 在聊天界面,去点中红包
			Log.i("TAG","(4 else )  plugin.luckymoney.ui.LuckyMoneyReceiveUI");
			checkKey2();
		}
	}

	/**
	 * 
	 * @description: 检查屏幕是否亮着并且唤醒屏幕
	 * @date: 2016-1-29 下午2:08:25
	 * @author: yems
	 */
	private void checkScreen(Context context)
	{
		// TODO Auto-generated method stub
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn())
		{
			wakeUpAndUnlock(context);
		}

	}

	private void wakeUpAndUnlock(Context context)
	{
		KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
		// 解锁
		kl.disableKeyguard();
		// 获取电源管理器对象
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		// 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
		// 点亮屏幕
		wl.acquire();
		// 释放
		wl.release();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void checkKey1()
	{
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null)
			return;

//		List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");
		List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bx4");
		if (list == null || list.size() == 0)
		{
//			list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b2c");
			list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bx4");
		}

		for (AccessibilityNodeInfo n : list)
		{
			n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		//////////////////////////
		List<AccessibilityNodeInfo> listce = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c2i");
		if (listce == null || listce.size() == 0)
		{
			listce = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c2i");
//			list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bx4");
		}

		for (AccessibilityNodeInfo n : listce)
		{
			n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}


	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void checkKey2()
	{
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null)
		{
			Log.w(TAG, "rootWindow为空");
			return;
		}
		List<AccessibilityNodeInfo> wxList = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
		List<AccessibilityNodeInfo> qqList = nodeInfo.findAccessibilityNodeInfosByText("QQ红包");

		if (!qqList.isEmpty())
		{
			qqList = nodeInfo.findAccessibilityNodeInfosByText("QQ红包");
			for (AccessibilityNodeInfo n : qqList)
			{
				n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				break;
			}
		}

		if (!wxList.isEmpty())
		{
			// 界面上的红包总个数
			int totalCount = wxList.size();
			// 领取最近发的红包
			for (int i = totalCount - 1; i >= 0; i--)
			{
				// 如果为领取过该红包，则执行点击、
				AccessibilityNodeInfo parent = wxList.get(i).getParent();
				if (parent != null)
				{
					parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					Log.w(TAG, "点击c2i");
//					Log.w(TAG, "点击bx4");
//					findIdAndClick("bx4", 2);
//					Log.w(TAG, "点击bx4");
//					findIdAndClick("bus", 1);
//					Log.w(TAG, "点击bus");
					break;
				}
			}
		}
		else if (!qqList.isEmpty())
		{

			// 界面上的红包总个数
			int totalCount = qqList.size();
			// 领取最近发的红包
			for (int i = totalCount - 1; i >= 0; i--)
			{
				// 如果为领取过该红包，则执行点击、
				AccessibilityNodeInfo parent = qqList.get(i).getParent();
				Log.i(TAG, "-->领取红包:" + parent);
				if (parent != null)
				{
					parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					break;
				}
			}

		}

	}

	private void traverseNode(AccessibilityNodeInfo node) {
		if (null == node) return;
		final int count = node.getChildCount();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				AccessibilityNodeInfo childNode = node.getChild(i);
				if (null != childNode && childNode.getClassName().equals("android.widget.Button") && childNode.isClickable()) {
					childNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					hasLucky = true;
				}

				traverseNode(childNode);
			}
		}
	}


	private void unlockScreen() {
		KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		final KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("MyKeyguardLock");
		keyguardLock.disableKeyguard();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE, "MyWakeLock");

		wakeLock.acquire();
	}

}
