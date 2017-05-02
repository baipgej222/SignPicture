package com.kamesuta.mc.signpic.plugin.search;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.kamesuta.mc.signpic.plugin.SignData;

public abstract class StringFilterElement extends EnumFilterElement<String, StringFilterProperty> {

	public final @Nullable String str;

	public StringFilterElement(final StringFilterProperty property, final @Nullable String src) {
		super(property);
		this.str = src;
	}

	public static class EqualsStringFilterElement extends StringFilterElement {

		public EqualsStringFilterElement(final StringFilterProperty property, final @Nullable String src) {
			super(property, src);
		}

		@Override
		public boolean filter(final SignData data) {
			return StringUtils.equals(this.property.get(data), this.str);
		}

	}

	public static class EqualsIgnoreCaseStringFilterElement extends StringFilterElement {

		public EqualsIgnoreCaseStringFilterElement(final StringFilterProperty property, final @Nullable String src) {
			super(property, src);
		}

		@Override
		public boolean filter(final SignData data) {
			return StringUtils.equalsIgnoreCase(this.property.get(data), this.str);
		}

	}

	public static class ContainsStringFilterElement extends StringFilterElement {

		public ContainsStringFilterElement(final StringFilterProperty property, final @Nullable String src) {
			super(property, src);
		}

		@Override
		public boolean filter(final SignData data) {
			return StringUtils.contains(this.property.get(data), this.str);
		}

	}

	public static class ContainsIgnoreCaseStringFilterElement extends StringFilterElement {

		public ContainsIgnoreCaseStringFilterElement(final StringFilterProperty property, final @Nullable String src) {
			super(property, src);
		}

		@Override
		public boolean filter(final SignData data) {
			return StringUtils.containsIgnoreCase(this.property.get(data), this.str);
		}

	}

}