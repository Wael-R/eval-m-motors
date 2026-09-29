package com.rwael.mmotors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class ApiResult
{
	private final boolean success;
	private final String message;

	private Object data;
}
