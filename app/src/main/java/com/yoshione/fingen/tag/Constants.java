/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.tag;

import android.graphics.Color;

class Constants {

	//use dp and sp, not px
	
	//----------------- separator TagView-----------------//
	static final float DEFAULT_LINE_MARGIN = 4;
	static final float DEFAULT_TAG_MARGIN = 4;
	static final float DEFAULT_TAG_TEXT_PADDING_LEFT = 8;
	static final float DEFAULT_TAG_TEXT_PADDING_TOP = 3.5f;
	static final float DEFAULT_TAG_TEXT_PADDING_RIGHT = 8;
	static final float DEFAULT_TAG_TEXT_PADDING_BOTTOM = 4;
	
	static final float LAYOUT_WIDTH_OFFSET = 2;
	
	//----------------- separator Tag Item-----------------//
	static final float DEFAULT_TAG_TEXT_SIZE = 12.5f;
	static final float DEFAULT_TAG_DELETE_INDICATOR_SIZE = 14f;
	static final float DEFAULT_TAG_LAYOUT_BORDER_SIZE = 0f;
	static final float DEFAULT_TAG_RADIUS = 100;
	static final int DEFAULT_TAG_LAYOUT_COLOR = Color.parseColor("#AED374");
	static final int DEFAULT_TAG_LAYOUT_COLOR_PRESS = Color.parseColor("#88363636");
	static final int DEFAULT_TAG_TEXT_COLOR = Color.parseColor("#ffffff");
	static final int DEFAULT_TAG_DELETE_INDICATOR_COLOR = Color.parseColor("#ffffff");
	static final int DEFAULT_TAG_LAYOUT_BORDER_COLOR = Color.parseColor("#ffffff");
	static final String DEFAULT_TAG_DELETE_ICON = "×";
	static final boolean DEFAULT_TAG_IS_DELETABLE = false;
}
