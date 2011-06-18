package com.zeuxislo.emojikeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

public class EmojiKeyboardView extends KeyboardView {
	
	static final int KEYCODE_OPTIONS = -100;
	static final int KEYCODE_EMOJI = -10;
	static final int KEYCODE_ABC = -11;
	static final int KEYCODE_SYMBOL = -12;
	
	static final int KEYCODE_EMOJI_1 = -21;
	static final int KEYCODE_EMOJI_2 = -31;
	static final int KEYCODE_EMOJI_3 = -41;
	static final int KEYCODE_EMOJI_4 = -51;
	static final int KEYCODE_EMOJI_5 = -61;

	public EmojiKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public EmojiKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected boolean onLongPress(Key popupKey) {
		if (popupKey.codes[0] == 10) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        }
		
		if (popupKey.codes[0] == KEYCODE_ABC) {
			getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
			return true;
		}
		
		if (popupKey.codes[0] == KEYCODE_SYMBOL) {
			getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
			return true;
		}
		
		if (popupKey.codes[0] == KEYCODE_EMOJI) {
			getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
			return true;
		}
		
		return super.onLongPress(popupKey);
	}

}
