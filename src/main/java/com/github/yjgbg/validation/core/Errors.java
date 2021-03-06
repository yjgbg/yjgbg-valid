package com.github.yjgbg.validation.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Error的构建，以及相关运算，包括Error的加法，以及Error与string的运算
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Errors {
	private static final Errors NONE = new Errors(null, Set.of(), Map.of());
	Object rejectValue;
	Set<String> messages;
	Map<String, Errors> fieldErrors;

	public static Errors none() {
		return NONE;
	}

	@Contract(pure = true)
	public static Errors of(@Nullable final Object rejectValue, @NotNull final String message) {
		if (rejectValue == null && message.isBlank()) return none();
		return new Errors(rejectValue, Set.of(message), Map.of());
	}

	/**
	 * 定义了与字符串的wrapper运算
	 */
	@Contract(pure = true)
	public static Errors wrapper(@NotNull final String key, @Nullable final Errors errors) {
		if (errors == null) return none();
		if (errors == none()) return none();
		return new Errors(null, Collections.emptySet(), Map.of(key, errors));
	}

	/**
	 * 定义了两个Error的加法
	 */
	@NotNull
	@Contract(pure = true)
	public static Errors plus(@Nullable final Errors errors0, @Nullable final Errors errors1) {
		if (errors0 == null) return plus(none(), errors1);
		if (errors1 == null) return plus(errors0, none());
		if (errors0 == none()) return errors1;
		if (errors1 == none()) return errors0;
		final var reject0 = errors0.getRejectValue();
		final var reject1 = errors1.getRejectValue();
		if (reject0 != null && reject1 != null && reject0 != reject1)
			throw new IllegalArgumentException(String.format("不同数据对象的校验结果不可相加(%s,%s)", reject0, reject1));
		// 并集
		final var messages = new HashSet<>(errors0.getMessages());
		messages.addAll(errors1.getMessages());
		final var errors = new HashMap<>(errors0.getFieldErrors());
		errors1.getFieldErrors().forEach((k, v) -> errors.put(k, plus(errors.get(k), v)));
		return new Errors(reject0 != null ? reject0 : reject1, messages, errors);
	}

	public boolean hasError() {
		return this != none();
	}
}
