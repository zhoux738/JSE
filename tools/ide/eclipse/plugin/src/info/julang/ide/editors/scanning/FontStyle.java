package info.julang.ide.editors.scanning;

import info.julang.ide.themes.PluginColor;

public enum FontStyle {

	KEYWORD(PluginColor.KEYWORD, true),
	COMMENT(PluginColor.COMMENT, false),
	LITERAL(PluginColor.LITERAL, false),
	REGEX(PluginColor.REGEX, false)
	
	;
	
	private FontStyle(PluginColor color, boolean isBold) {
		this.color = color;
		this.isBold = isBold;
	}
	
	private PluginColor color;
	private boolean isBold;
	
	public PluginColor getColor() {
		return color;
	}
	
	public boolean isBold() {
		return isBold;
	}
}
