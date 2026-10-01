package com.rwael.mmotors;

import jakarta.servlet.http.HttpServletResponse;
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

	public static ApiResult Get(boolean success, String message, HttpServletResponse res)
	{
		return Get(success, message, null, res);
	}

	public static ApiResult Get(boolean success, String message, Object data, HttpServletResponse res)
	{
		if(!success)
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		
		return new ApiResult(success, message, data);
	}
}
