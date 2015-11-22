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
import java.util.Arrays;
import java.util.List;

import com.passgo.libproj.PassgoGlobalData;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PassgoPatternUtils {
	private Context mContext;
	private PassgoPatternView mLockPatternView;
	private SharedPreferences preference;
	
	private int mRow;
	private int mCol;
	
	
	 public PassgoPatternUtils(Context context, PassgoPatternView passgoPatternView) {
	        mContext = context;
	        preference = PreferenceManager.getDefaultSharedPreferences(mContext);
	        
	        mLockPatternView = passgoPatternView;
	        mRow = mLockPatternView.getRow();
	        mCol = mLockPatternView.getColumn();
	 }
	
	 /**
     * Deserialize a pattern.
     * @param string The pattern serialized with {@link #patternToString}
     * @return The pattern.
     */
    public List<Cell> stringToPattern(String string) {
        List<Cell> result = new ArrayList<Cell>();

        final byte[] bytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            result.add(mLockPatternView.getCell(b / mRow, b % mCol));
        }
        return result;
    }

    /**
     * Serialize a pattern.
     * @param pattern The pattern.
     * @return The pattern in string form.
     */
    public String patternToString(List<Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * mRow + cell.getColumn());
        }

        return Arrays.toString(res);
        
    }
    
    public void saveLockPattern(String password, boolean isOfficial){
    	Editor editor = preference.edit();
    	if(isOfficial) {
    		editor.putString(PassgoGlobalData.KEY_OFFICIAL_PATTERN_PWD, password);
    	}
    	else {
    		editor.putString(PassgoGlobalData.KEY_UNOFFICIAL_PATTERN_PWD, password);
    	}
    	editor.commit();
    }
    
    public String getLockPaternString(boolean isOfficial){
    	if(isOfficial) {
    		return preference.getString(PassgoGlobalData.KEY_OFFICIAL_PATTERN_PWD, "");
    	}
    	else {
    		return preference.getString(PassgoGlobalData.KEY_UNOFFICIAL_PATTERN_PWD, "");
    	}
    }
    
    public int checkPattern(String password, boolean isOfficial) {
    	String stored = getLockPaternString(isOfficial);
    	if(stored != null){ 
    		System.out.println(isOfficial + " stored is " + stored);
    		return  password.contains(stored)?1:0;
    	}
    	return -1;
    }
    
    public int checkIfWrongPattern(String password, boolean isOfficial) {
    	String stored = getLockPaternString(isOfficial);

    	if(stored != null && !stored.isEmpty()){ 
    		if (password.length()<=stored.length()){
    			return  password.equals(stored.substring(0,password.length()))?0:1;
    		} else {
    			return  1;
    		}
    	}	
    	return -1;
    }
    

    public void clearLock(boolean isOfficial) {
    	saveLockPattern(null, isOfficial);
    }
  

}
