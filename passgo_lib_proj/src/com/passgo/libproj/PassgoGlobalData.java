/*
 * Copyright (C) 2015 PassGo Technology, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.passgo.libproj;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PassgoGlobalData {
	
	public static final String PASSGO_NOTIFICATION_STRING = "com.passgo.libproj.NOTIFICATION_LISTENER";
	public static final String PASSGO_PHONECALLSTATE_STRING = "android.intent.action.PHONE_STATE";
	public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
	public static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
	
	public static final String PASSGO_SECURITY_SETTINGS = "android.settings.SETTINGS";
	
	public static List<String> TempStopProtectPacknames=new ArrayList<String>(); 
	public static int ClearDelay=10; 
	
	public static final int MAIN_SERVICE = 1001;
    public static final int CHANGE_GRID_SIZE = 1002;
    public static final int PATTERN_CALL_REFRESH = 1003;
    public static final int PASSGO_CHANGED = 1006;
    public static final int PIN_CHANGED = 1010;
    public static final int PASSGO_PIN_CHANGED = 1011;
    public static final int INCOMING_CALL = 1007;
    public static final int CALL_IDLE = 1008;
    public static final int BACKGROUNDCOLOR_SERVICE = 1009;
    
    public static final int PASSGO_FAILURE_TRIAL = 1010;
    
    public final static String INCOMING_CALL_NUMBER = "incoming_call_number";
    
    public static final int PASSGO_DOT_COLOR = 1004;
    public static final int PASSGO_LINE_COLOR = 1005;
    
    public static int Wrong_PassGo_Count = 0;
    public static int Wrong_PIN_Count = 0;
    
	public static String AllowedProcessname="";
	public static String AllowedProcessname_1="";
	public static String AllowedProcessname_2="";

     
	public final static String KEY_FIRST_START = "key_first_start";
	public final static String KEY_PASSGO_CANCEL = "key_passgo_cancel";
	public final static String KEY_PASSGO_DIALOG = "key_passgo_dialog";
	
	public final static String KEY_TIMER_TRIGGER = "key_timer_trigger";
	public final static String KEY_ALARM_ENABLED = "key_alarm_enabled";
	//
	public final static String KEY_IS_OFFICIAL_PATTERN = "key_is_official_pattern";
	//
	public final static String KEY_OFFICIAL_PATTERN_NUM_ROW = "key_official_pattern_num_row";
	public final static String KEY_OFFICIAL_PATTERN_NUM_COL = "key_official_pattern_num_col";
	public final static String KEY_OFFICIAL_PATTERN_PWD = "key_official_pattern_pwd";
	public final static String KEY_OFFICIAL_PATTERN_BACKUP_PIN_PWD = "key_official_pattern_backup_pin_pwd";
	//
	public final static String KEY_UNOFFICIAL_PATTERN_NUM_ROW = "key_unofficial_pattern_num_row";
	public final static String KEY_UNOFFICIAL_PATTERN_NUM_COL = "key_unofficial_pattern_num_col";
	public final static String KEY_UNOFFICIAL_PATTERN_PWD = "key_unofficial_pattern_pwd";
	
	//
	public final static String KEY_OFFICIAL_PIN_PWD = "key_official_pin_pwd";
	
	public final static String KEY_OPTIMIZE_MEMORY = "key_optimize_memory";
	public final static String KEY_LOCK_SCREEN_SERVICE = "key_lock_screen_service";
	public final static String KEY_APP_LOCK_SERVICE = "key_app_lock_service";
	
	//
	public final static String KEY_CUR_LOCK_SCREEN_TYPE = "key_cur_lock_screen_type";
	
	//timer
	public final static String KEY_LOCK_SCREEN_INSTANTLOCK_POWERKEY = "key_lock_screen_instantlocker_powerkey";
	
	//Weather
	public final static String KEY_WEATHER_TEMP = "key_weather_temp";
	public final static String KEY_WEATHER_LOCATION = "key_weather_location";
	
	//Pattern Settings
	public final static String KEY_LOCK_SCREEN_PATTERN_VIBRATE = "key_lock_screen_pattern_vibrate";
	public final static String KEY_LOCK_SCREEN_PATTERN_HIDE_LINE = "key_lock_screen_pattern_hide_line";
	public final static String KEY_LOCK_SCREEN_PATTERN_HIDE_ARROW = "key_lock_screen_pattern_hide_arrow";
	
	public final static String KEY_LOCK_SCREEN_PATTERN_CLEAR_LAST = "key_lock_screen_pattern_clear_last";
	
	public final static String KEY_LOCK_SCREEN_PATTERN_HIDE_OUTLINE = "key_lock_screen_pattern_hide_outline";

	public final static String KEY_LOCK_SCREEN_PATTERN_RETRY_COUNT = "key_lock_screen_pattern_retry_count";
	public final static String KEY_LOCK_SCREEN_PASSGO_LINE_COLOR = "key_lock_screen_passgo_line_color";
	public final static String KEY_LOCK_SCREEN_PASSGO_DOT_COLOR = "key_lock_screen_passgo_dot_color";
	
	
	public final static String KEY_LOCK_SCREEN_PATTERN_LINE_THICKNESS = "key_lock_screen_pattern_line_thickness";
	public final static String KEY_LOCK_SCREEN_GRID_LINE_THICKNESS = "key_lock_screen_grid_line_thickness";
	public final static String KEY_LOCK_SCREEN_PATTERN_OUTLINE_COLOR = "key_lock_screen_pattern_outline_color";
	public final static String KEY_LOCK_SCREEN_BACK_GROUND_COLOR = "key_lock_screen_back_ground_color";
	
	public final static String KEY_LOCK_SCREEN_GRID_OUTLINE_COLOR = "key_lock_screen_grid_outline_color";
	
	public final static String KEY_LOCK_SCREEN_TIMER_SETTING = "key_lock_screen_alarm_timer_setting";
	
	//Pattern Outline Color
	public final static int KEY_COLOR_ORANGE = 1;
	public final static int KEY_COLOR_YELLOW = 2;
	public final static int KEY_COLOR_GREEN = 3;
	public final static int KEY_COLOR_DARK_GREEN = 4;
	public final static int KEY_COLOR_SKY_BLUE = 5;
	public final static int KEY_COLOR_BLUE = 6;
	public final static int KEY_COLOR_DARK_BLUE = 7;
	public final static int KEY_COLOR_PURPLE = 8;
	public final static int KEY_COLOR_PINK = 9;
	public final static int KEY_COLOR_WHITE = 10;
	
	public final static int KEY_COLOR_BLACK = 11;
	public final static int KEY_COLOR_GRAY = 12;
	public final static int KEY_COLOR_NAVY = 13;
	public final static int KEY_COLOR_RED = 14;
	public final static int KEY_COLOR_MAROON = 15;
	public final static int KEY_COLOR_FUCHSIA = 16;
	
	//Lock Screen Decoration
	public final static String KEY_LOCK_SCREEN_DECORATION_AMPM_COLOR = "key_lock_screen_decoration_ampm_color";
	public final static String KEY_LOCK_SCREEN_DECORATION_TIME_COLOR = "key_lock_screen_decoration_time_color";
	public final static String KEY_LOCK_SCREEN_DECORATION_DATE_COLOR = "key_lock_screen_decoration_date_color";
	public final static String KEY_LOCK_SCREEN_DECORATION_ALARM_COLOR = "key_lock_screen_decoration_alarm_color";
	public final static String KEY_LOCK_SCREEN_DECORATION_CONTACT_COLOR = "key_lock_screen_decoration_contact_color";
	public final static String KEY_LOCK_SCREEN_DECORATION_HINT_COLOR = "key_lock_screen_decoration_hint_color";
	public final static String KEY_LOCK_SCREEN_DECORATION_BACKGROUND = "key_lock_screen_decoration_background";
	
	//Decorate Options
	public final static String KEY_LOCK_SCREEN_DECORATION_AMPM_IS_HIDE = "key_lock_screen_decoration_ampm_is_hide";
	public final static String KEY_LOCK_SCREEN_DECORATION_TIME_IS_HIDE = "key_lock_screen_decoration_time_is_hide";
	public final static String KEY_LOCK_SCREEN_DECORATION_DATE_IS_HIDE = "key_lock_screen_decoration_date_is_hide";
	public final static String KEY_LOCK_SCREEN_DECORATION_WEATHER_IS_HIDE = "key_lock_screen_decoration_weather_is_hide";
	public final static String KEY_LOCK_SCREEN_DECORATION_CAMERA_IS_HIDE = "key_lock_screen_decoration_camera_is_hide";
	public final static String KEY_LOCK_SCREEN_DECORATION_ALARM_IS_HIDE = "key_lock_screen_decoration_alarm_is_hide";
	public final static String KEY_LOCK_SCREEN_DECORATION_CONTACT_IS_HIDE = "key_lock_screen_decoration_contact_is_hide";
	public final static String KEY_LOCK_SCREEN_DECORATION_HINT_IS_HIDE = "key_lock_screen_decoration_hint_is_hide";
	
	public final static String ACTION_LOCK_SCREEN = "FIRE_LCOK_SCREEN";
	public final static String ACTION_REBOOT_RESET_ALARM = "RESET_ALARM_AFTER_REBOOT";
	public final static String ACTION_TRIGGER_ALARM = "FIRE_ALARM";
	
	//QA setting
	public final static String KEY_LOCK_SCREEN_QUESTION = "key_lock_screen_question";
	public final static String KEY_LOCK_SCREEN_ANSWER = "key_lock_screen_answer";
	//Hint setting
	public final static String KEY_LOCK_SCREEN_HINT = "key_lock_screen_hint";
	//Phone setting
	public final static String KEY_LOCK_SCREEN_PHONE = "key_lock_screen_phone";
	
	//Additional Lock
	public final static String KEY_LOCK_STATUS_BAR = "key_lock_status_bar";
	public final static String KEY_LOCK_OUTCALL = "key_lock_outcall";
	public final static String KEY_LOCK_INCALL = "key_lock_incall";
	public final static String INCOMING_CALL_FLAG = "incoming_call_flag";
	public final static String KEY_LOCK_WIFI = "key_lock_wifi";
	public final static String KEY_LOCK_IS_MY_WIFI_OPEN = "key_lock_is_my_wifi_open";
	public final static String KEY_LOCK_MOBILE_DATA = "key_lock_mobile_data";
	public final static String KEY_LOCK_IS_MY_MOBILE_DATA_OPEN = "key_lock_is_my_mobile_data_open";
	public final static String KEY_LOCK_BLUETOOTH = "key_lock_bluetooth";
	public final static String KEY_LOCK_IS_MY_BLUETOOTH_OPEN = "key_lock_is_my_bluetooth_open";
	public final static String KEY_LOCK_RECENT_APPS = "key_lock_recent_apps";
	
	//app lock
	public final static String KEY_LOCK_APP_PACKAGE = "key_lock_app_package";
	public final static String KEY_LOCK_APP_TASK = "key_lock_app_task";
	public final static String KEY_LOCK_APP_TASK_COUNT = "key_lock_app_task_count";
	
	//Security
	public final static String KEY_ENABLE_DEVICE_ADMINISTRATOR = "key_enable_device_administrator";
	
	//
	public final static String KEY_LOCK_SCREEN_BACKGROUND_TYPE = "key_lock_screen_background_type";
	public final static int KEY_BG_NONE = 0;
	public final static int KEY_BG_WALLPAPER = 1;
	public final static int KEY_BG_GALLERY = 2;
	public final static int KEY_BG_CAMERA = 3;
	public final static int KEY_BG_PURECOLOR = 4;

	public static String KEY_IS_LOCKED = "key_is_locked";
	public static String KEY_IS_APP_LOCKED = "key_is_app_locked";
	public static String KEY_IS_CALLING = "key_is_calling";
	public static String KEY_IS_USING_CAMERA = "key_is_using_camera";
	public static boolean IsTalkingIncomingCall=false;
	public static int Just_Unlock_Screen=0;
	public static int current_rotate=1;
	public static boolean Changing_PassGo=false;
	public static boolean ScreenOff=false;
	
	
	public final static String TESTING_FLAG = "testing_flag";
	public static final String KEY_IS_LOCKSCREEN_DESTROYED = "key_is_lockscreen_destroyed";
	
	
	public static void setDataStr(Context context, String key, String value) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = preference.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static void setDataInt(Context context, String key, int value) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = preference.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public static void setDataBool(Context context, String key, boolean value) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = preference.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public static String getDataStr(Context context, String key, String defValue) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		String str = preference.getString(key, defValue);
		return str;
	}
	
	public static int getDataInt(Context context, String key, int defValue) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		int str = preference.getInt(key, defValue);
		return str;
	}
	
	public static boolean getDataBool(Context context, String key, boolean defValue) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		boolean str = preference.getBoolean(key, defValue);
		return str;
	}
}
