function jsonRequest(url, method, body, onSuccess, onError, asText = false)
{
	const csrfToken = $("meta[name='csrf']").attr("content");
	const csrfHead = $("meta[name='csrf_head']").attr("content");

	let failed = false;

	fetch(url, {
		method: method,
		headers: {
			"Content-Type": "application/json",
			[csrfHead]: csrfToken
		},
		body: body ? JSON.stringify(body) : undefined})
	.then((res) => {
		if(!res.ok)
			failed = true;

		if(asText)
			return res.text();
		else
			return res.json();
	}).then((res) => {
		if(failed)
			onError(res);
		else
			onSuccess(res);
	});
}

function formRequest(url, method, files, params, onSuccess, onError, onProgress, asText = false)
{
	const csrfToken = $("meta[name='csrf']").attr("content");
	const csrfHead = $("meta[name='csrf_head']").attr("content");

	const data = new FormData();

	for(const file of files)
		data.append(`files`, file);

	for(const [k,v] of Object.entries(params))
	{
		if(v != null && v != undefined)
			data.append(k, v);
	}

	const req = new XMLHttpRequest();

	req.onreadystatechange = (ev) => {
		if(req.readyState != XMLHttpRequest.DONE)
			return;

		if(req.status >= 200 && req.status < 400)
		{
			if(onSuccess)
			{
				if(asText)
					onSuccess(req.responseText);
				else
					onSuccess(req.response);
			}
		}
		else
		{
			if(onError)
			{
				if(asText)
					onError(req.responseText);
				else
					onError(req.response);
			}
		}
	};

	req.upload.onprogress = (ev) => {
		if(!onProgress)
			return;

		if(ev.lengthComputable)
			onProgress(ev.loaded, ev.total);
		else
			onProgress(0, 0);
	}

	req.open(method, url);
	req.setRequestHeader(csrfHead, csrfToken);
	req.send(data);

	return req;
}

function displayAlert(success, message, div)
{
	div.toggleClass("alert-warning", !success);
	div.toggleClass("alert-success", success);

	div.toggleClass("d-none", message.length <= 0);
	div.text(message);
}