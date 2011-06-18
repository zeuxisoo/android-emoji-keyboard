package com.zeuxislo.emojikeyboard;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public class SoftKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
	
	static final boolean DEBUG = false;
	
	private StringBuilder mComposing;
	private long mLastShiftTime;
	private boolean mCapsLock;
	private String mWordSeparators;
	private boolean mPredictionOn;
	private KeyboardView mInputView;
	private EmojiKeyboard mQwertyKeyboard;
	private EmojiKeyboard mSymbolsKeyboard;
	private EmojiKeyboard mSymbolsShiftedKeyboard;
	private long mMetaState;
	private boolean mCompletionOn;
	private Resources mResources;
	private CandidateView mCandidateView;
	private CompletionInfo[] mCompletions;
	private EmojiKeyboard mCurKeyboard;
	private EmojiKeyboard mEmojiKeyboarda1;
	private EmojiKeyboard mEmojiKeyboarda2;
	private EmojiKeyboard mEmojiKeyboarda3;
	private EmojiKeyboard mEmojiKeyboarda4;
	private EmojiKeyboard mEmojiKeyboardb1;
	private EmojiKeyboard mEmojiKeyboardb2;
	private EmojiKeyboard mEmojiKeyboardc1;
	private EmojiKeyboard mEmojiKeyboardc2;
	private EmojiKeyboard mEmojiKeyboardc3;
	private EmojiKeyboard mEmojiKeyboardc4;
	private EmojiKeyboard mEmojiKeyboardc5;
	private EmojiKeyboard mEmojiKeyboardd1;
	private EmojiKeyboard mEmojiKeyboardd2;
	private EmojiKeyboard mEmojiKeyboardd3;
	private EmojiKeyboard mEmojiKeyboarde1;
	private EmojiKeyboard mEmojiKeyboarde2;
	private EmojiKeyboard mEmojiKeyboarde3;
	private EmojiKeyboard mEmojiKeyboarde4;
	private int mLastDisplayWidth;
	
	public SoftKeyboard() {
		this.mComposing = new StringBuilder();
	}
	
	private void checkToggleCapsLock() {
		long now = System.currentTimeMillis();
		if (this.mLastShiftTime + 800 > now) {
			this.mCapsLock = !this.mCapsLock;
			this.mLastShiftTime = 0;
		} else {
			this.mLastShiftTime = now;
		}
	}
	
	private void commitTyped(InputConnection inputConnection) {
		if (this.mComposing.length() > 0) {
			inputConnection.commitText(this.mComposing, 1);		// mComposing.length()
			mComposing.setLength(0);
			this.updateCandidates();
		}
    }
	
	private String getWordSeparators() {
		return this.mWordSeparators;
	}

	public void handleBackspace() {
		final int length = this.mComposing.length();
		if (length > 1) {
			this.mComposing.delete(length - 1, length);
			this.getCurrentInputConnection().setComposingText(this.mComposing, 1);
			this.updateCandidates();
		} else if (length > 0) {
			this.mComposing.setLength(0);
			this.getCurrentInputConnection().commitText("", 0);
			this.updateCandidates();
		} else {
			this.keyDownUp(KeyEvent.KEYCODE_DEL);
		}
		this.updateShiftKeyState(this.getCurrentInputEditorInfo());
	}
	
	private void handleCharacter(int primaryCode, int[] keyCodes) {
		if (isInputViewShown()) {
			if (this.mInputView.isShifted()) {
				primaryCode = Character.toUpperCase(primaryCode);
			}
		}
		if (this.isAlphabet(primaryCode) && this.mPredictionOn) {
			this.mComposing.append((char) primaryCode);
			this.getCurrentInputConnection().setComposingText(this.mComposing, 1);
            this.updateShiftKeyState(this.getCurrentInputEditorInfo());
            this.updateCandidates();
        } else {
            this.mComposing.append((char) primaryCode);
            this.getCurrentInputConnection().setComposingText(this.mComposing, 1);
        }
    }
	
	private void handleClose() {
		commitTyped(this.getCurrentInputConnection());
		this.requestHideSelf(0);
		this.mInputView.closing();
	}
	
	private void handleShift() {
		if (this.mInputView == null) {
			return;
		}

		Keyboard currentKeyboard = this.mInputView.getKeyboard();
		if (this.mQwertyKeyboard == currentKeyboard) {
        	
			this.checkToggleCapsLock();
			this.mInputView.setShifted(this.mCapsLock || !this.mInputView.isShifted());
            
		} else if (currentKeyboard == this.mSymbolsKeyboard) {
        	
			this.mSymbolsKeyboard.setShifted(true);
			this.mInputView.setKeyboard(this.mSymbolsShiftedKeyboard);
			this.mSymbolsShiftedKeyboard.setShifted(true);
            
		} else if (currentKeyboard == this.mSymbolsShiftedKeyboard) {
        	
			this.mSymbolsShiftedKeyboard.setShifted(false);
			this.mInputView.setKeyboard(this.mSymbolsKeyboard);
			this.mSymbolsKeyboard.setShifted(false);
            
		}
	}
	
	private boolean isAlphabet(int code) {
		return Character.isLetter(code);
    }
	
	private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
	
	private void sendKey(int keyCode) {
		switch (keyCode) {
			case '\n':
				keyDownUp(KeyEvent.KEYCODE_ENTER);
				break;
			default:
				if (keyCode >= '0' && keyCode <= '9') {
					keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
				} else {
					getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
				}
				break;
		}
    }
	
	private void showOptionsMenu() {
		((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
	}
	
	private boolean translateKeyDown(int keyCode, KeyEvent event) {
		this.mMetaState = MetaKeyKeyListener.handleKeyDown(this.mMetaState, keyCode, event);
		int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(this.mMetaState));
        this.mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = this.getCurrentInputConnection();
        
        if (c == 0 || ic == null) {
            return false;
        }

		boolean dead = false;

		if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (this.mComposing.length() > 0) {
            char accent = this.mComposing.charAt(this.mComposing.length() - 1);
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                this.mComposing.setLength(this.mComposing.length() - 1);
            }
        }

        this.onKey(c, null);

        return true;
    }
	
	private void updateCandidates() {
		if (!mCompletionOn) {
			if (mComposing.length() > 0) {
				ArrayList<String> list = new ArrayList<String>();
				list.add(mComposing.toString());
				this.setSuggestions(list, true, true);
			} else {
				this.setSuggestions(null, false, false);
			}
		}
	}
	
	private void updateShiftKeyState(EditorInfo editorInfo) {
		if (editorInfo != null && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
			int caps = 0;
			EditorInfo ei = getCurrentInputEditorInfo();
			if (ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
				caps = getCurrentInputConnection().getCursorCapsMode(editorInfo.inputType);
			}
			mInputView.setShifted(mCapsLock || caps != 0);
		}
	}
	
	public boolean isWordSeparator(int code) {
		String separators = getWordSeparators();
		return separators.contains(String.valueOf((char) code));
    }
	
	public void pickDefaultCandidate() {
		this.pickSuggestionManually(0);
	}
	
	public void pickSuggestionManually(int index) {
		if (this.mCompletionOn && this.mCompletions != null && index >= 0 && index < this.mCompletions.length) {
			CompletionInfo ci = mCompletions[index];
			this.getCurrentInputConnection().commitCompletion(ci);
            
			if (this.mCandidateView != null) {
                this.mCandidateView.clear();
            }
            
			this.updateShiftKeyState(this.getCurrentInputEditorInfo());
        } else if (this.mComposing.length() > 0) {
            this.commitTyped(this.getCurrentInputConnection());
        }
    }
	
	public void setSuggestions(List<String> suggestions, boolean completions, boolean typedWordValid) {
		if (suggestions != null && suggestions.size() > 0) {
			this.setCandidatesViewShown(true);
		} else if (isExtractViewShown()) {
			this.setCandidatesViewShown(true);
		}
		
		if (this.mCandidateView != null) {
			this.mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
		}
    }
	
	public void changeEmojiKeyboard(EmojiKeyboard[] emojiKeyboard) {
		int j = 0;
    	for(int i=0; i<emojiKeyboard.length; i++) {
    		if (emojiKeyboard[i] == this.mInputView.getKeyboard()) {
    			j = i;
    			break;
    		}
    	}
    	
    	if (j + 1 >= emojiKeyboard.length) {
    		this.mInputView.setKeyboard(emojiKeyboard[0]);
    	}else{
    		this.mInputView.setKeyboard(emojiKeyboard[j + 1]);
    	}		
	}
	
	public void changeEmojiKeyboardReverse(EmojiKeyboard[] emojiKeyboard) {
		int j = emojiKeyboard.length - 1;
		for(int i=emojiKeyboard.length - 1; i>=0; i--) {
			if (emojiKeyboard[i] == this.mInputView.getKeyboard()) {
				j = i;
				break;
			}
		}
		
		if (j - 1 < 0) {
    		this.mInputView.setKeyboard(emojiKeyboard[emojiKeyboard.length - 1]);
    	}else{
    		this.mInputView.setKeyboard(emojiKeyboard[j - 1]);
    	}
	}
	
	public void onCreate() {
		super.onCreate();
		this.mResources = getResources();
		this.mWordSeparators = getResources().getString(R.string.word_separators);
	}
	
	public View onCreateCandidatesView() {
		this.mCandidateView = new CandidateView(this);
		this.mCandidateView.setService(this);
		return this.mCandidateView;
	}
	
	public View onCreateInputView() {
		this.mInputView = (KeyboardView) this.getLayoutInflater().inflate(R.layout.input, null);
		this.mInputView.setOnKeyboardActionListener(this);
		this.mInputView.setKeyboard(this.mQwertyKeyboard);
		return this.mInputView;
	}
	
	public void onDisplayCompletions(CompletionInfo[] completions) {
		if (this.mCompletionOn) {
            this.mCompletions = completions;
            if (completions == null) {
                this.setSuggestions(null, false, false);
                return;
            }

            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < (completions != null ? completions.length : 0); i++) {
                CompletionInfo ci = completions[i];
                if ((ci != null) && (ci.getText() != null))
                    stringList.add(ci.getText().toString());
            }
            this.setSuggestions(stringList, true, true);
        }
    }
	
	public void onFinishInput() {
		super.onFinishInput();

        this.mComposing.setLength(0);
		this.updateCandidates();
		this.setCandidatesViewShown(false);

        this.mCurKeyboard = mQwertyKeyboard;
        if (this.mInputView != null) {
            this.mInputView.closing();
        }
    }
	
	public void onInitializeInterface() {
		if (this.mQwertyKeyboard != null) {
			int displayWidth = getMaxWidth();
			
			if (displayWidth == mLastDisplayWidth) {
                return;
			}
			
			mLastDisplayWidth = displayWidth;
		}
		
		this.mQwertyKeyboard = new EmojiKeyboard(this, R.xml.qwerty);
		this.mSymbolsKeyboard = new EmojiKeyboard(this, R.xml.symbols);
		this.mSymbolsShiftedKeyboard = new EmojiKeyboard(this, R.xml.symbols_shift);
		
		this.mEmojiKeyboarda1 = new EmojiKeyboard(this, R.xml.emoji_a1);
		this.mEmojiKeyboarda2 = new EmojiKeyboard(this, R.xml.emoji_a2);
		this.mEmojiKeyboarda3 = new EmojiKeyboard(this, R.xml.emoji_a3);
		this.mEmojiKeyboarda4 = new EmojiKeyboard(this, R.xml.emoji_a4);
		
		this.mEmojiKeyboardb1 = new EmojiKeyboard(this, R.xml.emoji_b1);
		this.mEmojiKeyboardb2 = new EmojiKeyboard(this, R.xml.emoji_b2);
		
		this.mEmojiKeyboardc1 = new EmojiKeyboard(this, R.xml.emoji_c1);
		this.mEmojiKeyboardc2 = new EmojiKeyboard(this, R.xml.emoji_c2);
		this.mEmojiKeyboardc3 = new EmojiKeyboard(this, R.xml.emoji_c3);
		this.mEmojiKeyboardc4 = new EmojiKeyboard(this, R.xml.emoji_c4);
		this.mEmojiKeyboardc5 = new EmojiKeyboard(this, R.xml.emoji_c5);

		this.mEmojiKeyboardd1 = new EmojiKeyboard(this, R.xml.emoji_d1);
		this.mEmojiKeyboardd2 = new EmojiKeyboard(this, R.xml.emoji_d2);
		this.mEmojiKeyboardd3 = new EmojiKeyboard(this, R.xml.emoji_d3);

		this.mEmojiKeyboarde1 = new EmojiKeyboard(this, R.xml.emoji_e1);
		this.mEmojiKeyboarde2 = new EmojiKeyboard(this, R.xml.emoji_e2);
		this.mEmojiKeyboarde3 = new EmojiKeyboard(this, R.xml.emoji_e3);
		this.mEmojiKeyboarde4 = new EmojiKeyboard(this, R.xml.emoji_e4);
	}
	
	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		Log.d("Main", "Primary Code: " + primaryCode);
		
		if (this.isWordSeparator(primaryCode)) {
			if (this.mComposing.length() > 0) {
				this.commitTyped(this.getCurrentInputConnection());
			}
			this.sendKey(primaryCode);
			this.updateShiftKeyState(this.getCurrentInputEditorInfo());
		} else if (primaryCode == Keyboard.KEYCODE_DELETE) {
			this.handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
			this.handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
        	handleClose();
            return;
        } else if (primaryCode == EmojiKeyboardView.KEYCODE_OPTIONS) {
        	this.showOptionsMenu();
        } else if (primaryCode == EmojiKeyboardView.KEYCODE_SYMBOL && this.mInputView != null) {
        	this.mInputView.setKeyboard(this.mSymbolsKeyboard);
        	this.mInputView.setShifted(false);
        } else if (primaryCode == EmojiKeyboardView.KEYCODE_ABC && this.mInputView != null) {
        	this.mInputView.setKeyboard(this.mQwertyKeyboard);
        } else if (primaryCode == EmojiKeyboardView.KEYCODE_EMOJI && this.mInputView != null) {
        	this.mInputView.setKeyboard(this.mEmojiKeyboarda1);
        } else if (primaryCode == EmojiKeyboardView.KEYCODE_EMOJI_1 && this.mInputView != null) {
        	this.changeEmojiKeyboard(new EmojiKeyboard[] {
        		this.mEmojiKeyboarda1,
        		this.mEmojiKeyboarda2,
        		this.mEmojiKeyboarda3,
        		this.mEmojiKeyboarda4
        	});
        } else if (primaryCode == EmojiKeyboardView.KEYCODE_EMOJI_2 && this.mInputView != null) {
        	this.changeEmojiKeyboard(new EmojiKeyboard[] {
            	this.mEmojiKeyboardb1,
            	this.mEmojiKeyboardb2
            });
        } else if (primaryCode == EmojiKeyboardView.KEYCODE_EMOJI_3 && this.mInputView != null) {
        	this.changeEmojiKeyboard(new EmojiKeyboard[] {
            	this.mEmojiKeyboardc1,
            	this.mEmojiKeyboardc2,
            	this.mEmojiKeyboardc3,
            	this.mEmojiKeyboardc4,
            	this.mEmojiKeyboardc5
            });
        } else if (primaryCode == EmojiKeyboardView.KEYCODE_EMOJI_4 && this.mInputView != null) {
        	this.changeEmojiKeyboard(new EmojiKeyboard[] {
            	this.mEmojiKeyboardd1,
            	this.mEmojiKeyboardd2,
            	this.mEmojiKeyboardd3
            });
        } else if (primaryCode == EmojiKeyboardView.KEYCODE_EMOJI_5 && this.mInputView != null) {
        	this.changeEmojiKeyboard(new EmojiKeyboard[] {
            	this.mEmojiKeyboarde1,
            	this.mEmojiKeyboarde2,
            	this.mEmojiKeyboarde3,
            	this.mEmojiKeyboarde4
            });
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE && this.mInputView != null) {
        	Keyboard current = mInputView.getKeyboard();
        	
        	if (current == this.mSymbolsKeyboard || current == this.mSymbolsShiftedKeyboard) {
                current = this.mQwertyKeyboard;
            } else {
                current = this.mSymbolsKeyboard;
            }
        	
        	this.mInputView.setKeyboard(current);
        	
            if (current == mSymbolsKeyboard) {
                current.setShifted(false);
            }
        } else {
			this.handleCharacter(primaryCode, keyCodes);
        }
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:		// 4
				if (event.getRepeatCount() == 0 && this.mInputView != null) {
					if (this.mInputView.handleBack()) {
						return true;
					}
				}
				break;
			case KeyEvent.KEYCODE_DEL:		// 64
				if (this.mComposing.length() > 0) {
					this.onKey(Keyboard.KEYCODE_DELETE, null);
					return true;
				}
				break;
			case KeyEvent.KEYCODE_ENTER:	// 67
				return false;
			default:
				if (this.mPredictionOn && this.translateKeyDown(keyCode, event)) {
					return true;
				}
				break;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (this.mPredictionOn) {
			this.mMetaState = MetaKeyKeyListener.handleKeyUp(this.mMetaState, keyCode, event);
        }

        return super.onKeyUp(keyCode, event);
    }
	
	@Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);

		this.mComposing.setLength(0);
		this.updateCandidates();

        if (!restarting) {
			this.mMetaState = 0;
        }

		this.mPredictionOn = false;
		this.mCompletionOn = false;
		this.mCompletions = null;

        switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
        	case EditorInfo.TYPE_CLASS_NUMBER:		// 2
				this.mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        		break;
        	case EditorInfo.TYPE_CLASS_DATETIME:	// 4
				this.mCurKeyboard = this.mSymbolsKeyboard;
        		break;
        	case EditorInfo.TYPE_CLASS_PHONE:		// 3
        		this.mCurKeyboard = this.mSymbolsKeyboard;
        		break;
        	case EditorInfo.TYPE_CLASS_TEXT:		// 1
        		this.mCurKeyboard = this.mQwertyKeyboard;
        		this.mPredictionOn = true;
        		
        		int variation = attribute.inputType & EditorInfo.TYPE_MASK_VARIATION;
        		if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD || variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
        			this.mPredictionOn = false;
        		}
        		
        		if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS || variation == EditorInfo.TYPE_TEXT_VARIATION_URI || variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
        			this.mPredictionOn = false;
        		}
        		
        		if ((attribute.inputType & EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
					this.mPredictionOn = false;
					this.mCompletionOn = this.isFullscreenMode();
                }
        		
        		updateShiftKeyState(attribute);
        		break;
        	default:
				this.mCurKeyboard = this.mQwertyKeyboard;
				this.updateShiftKeyState(attribute);
        		break;
        }
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		super.onStartInputView(attribute, restarting);
		this.mInputView.setKeyboard(this.mCurKeyboard);
		this.mInputView.closing();
    }
	
	@Override
	public void onText(CharSequence text) {
		InputConnection ic = getCurrentInputConnection();
		
		if (ic == null)
            return;
        
		ic.beginBatchEdit();
        
		if (this.mComposing.length() > 0) {
			this.commitTyped(ic);
		}
		
		ic.commitText(text, 0);
		ic.endBatchEdit();
		
		this.updateShiftKeyState(this.getCurrentInputEditorInfo());
    }
	
	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);

		if (this.mComposing.length() > 0 && (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) {
			this.mComposing.setLength(0);
			this.updateCandidates();
			
			InputConnection ic = getCurrentInputConnection();
			
			if (ic != null) {
				ic.finishComposingText();
			}
		}
	}
	
	@Override
	public void onPress(int primaryCode) {
	}

	@Override
	public void onRelease(int primaryCode) {
	}

	@Override
	public void swipeDown() {
		this.handleClose();	
	}

	@Override
	public void swipeLeft() {
		Log.d("Main", "swipe left");
		this.changeEmojiKeyboard(new EmojiKeyboard[] {
			this.mQwertyKeyboard, this.mSymbolsKeyboard, this.mSymbolsShiftedKeyboard,
        	this.mEmojiKeyboarda1, this.mEmojiKeyboarda2, this.mEmojiKeyboarda3, this.mEmojiKeyboarda4,
        	this.mEmojiKeyboardb1, this.mEmojiKeyboardb2,
        	this.mEmojiKeyboardc1, this.mEmojiKeyboardc2, this.mEmojiKeyboardc3, this.mEmojiKeyboardc4, this.mEmojiKeyboardc5,
        	this.mEmojiKeyboardd1, this.mEmojiKeyboardd2, this.mEmojiKeyboardd3,
        	this.mEmojiKeyboarde1, this.mEmojiKeyboarde2, this.mEmojiKeyboarde3, this.mEmojiKeyboarde4,
        });
	}

	@Override
	public void swipeRight() {
		Log.d("Main", "swipe right");
		this.changeEmojiKeyboardReverse(new EmojiKeyboard[] {
			this.mQwertyKeyboard, this.mSymbolsKeyboard, this.mSymbolsShiftedKeyboard,
	        this.mEmojiKeyboarda1, this.mEmojiKeyboarda2, this.mEmojiKeyboarda3, this.mEmojiKeyboarda4,
	        this.mEmojiKeyboardb1, this.mEmojiKeyboardb2,
	        this.mEmojiKeyboardc1, this.mEmojiKeyboardc2, this.mEmojiKeyboardc3, this.mEmojiKeyboardc4, this.mEmojiKeyboardc5,
	        this.mEmojiKeyboardd1, this.mEmojiKeyboardd2, this.mEmojiKeyboardd3,
	        this.mEmojiKeyboarde1, this.mEmojiKeyboarde2, this.mEmojiKeyboarde3, this.mEmojiKeyboarde4,
		});
	}

	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub
		
	}

}
