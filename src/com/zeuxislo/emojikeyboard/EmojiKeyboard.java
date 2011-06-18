package com.zeuxislo.emojikeyboard;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.view.inputmethod.EditorInfo;

public class EmojiKeyboard extends Keyboard {
	
	private Key mEnterKey;
	
	public EmojiKeyboard(Context context, int xmlLayoutResId) {
		super(context, xmlLayoutResId);
	}

	public EmojiKeyboard(Context context, int layoutTemplateResId, CharSequence characters, int columns, int horizontalPadding) {
		super(context, layoutTemplateResId, characters, columns, horizontalPadding);
	}

	@Override
	protected Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
		Key key = new LatinKey(res, parent, x, y, parser);
        if (key.codes[0] == 10) {
            mEnterKey = key;
        }
        return key;
	}
	
	public void setImeOptions(Resources res, int options) {
		if (mEnterKey == null) {
			return;
		}
		
		switch(options & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
			case EditorInfo.IME_ACTION_GO:		// 2
				this.mEnterKey.iconPreview = null;
		        this.mEnterKey.icon = null;
		        this.mEnterKey.label = res.getText(R.string.label_keyboard_key_go);
				break;
			case EditorInfo.IME_ACTION_SEARCH:	// 3
				this.mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_search);
				this.mEnterKey.label = null;
				break;
			case EditorInfo.IME_ACTION_SEND:	// 4
				this.mEnterKey.iconPreview = null;
				this.mEnterKey.icon = null;
				this.mEnterKey.label = res.getText(R.string.label_keyboard_key_send);
				break;
			case EditorInfo.IME_ACTION_NEXT:	// 5
				this.mEnterKey.iconPreview = null;
				this.mEnterKey.icon = null;
				this.mEnterKey.label = res.getText(R.string.label_keyboard_key_next);
				break;
			default:
				this.mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_return);
				this.mEnterKey.label = null;
				break;
		}
	}
	
	class LatinKey extends Key {
		public LatinKey(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {
			super(res, parent, x, y, parser);
		}

		@Override
		public boolean isInside(int x, int y) {
			return super.isInside(x, codes[0] == KEYCODE_CANCEL ? y - 10 : y);
		}
	}
	
}
