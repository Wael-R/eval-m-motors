function xhr(url, method, data, params, onSuccess, onError, onProgress, onDownloadProgress)
{
	const csrfToken = $("meta[name='csrf']").attr("content");
	const csrfHead = $("meta[name='csrf_head']").attr("content");

	const req = new XMLHttpRequest();
	const search = new URLSearchParams();

	let body = new FormData();
	let json = false;

	if(data instanceof FileList || !data)
	{
		if(params)
		{
			for(const [k,v] of Object.entries(params))
			{
				if(v != null && v != undefined)
					body.append(k, v);
			}
		}

		if(data)
		{
			for(const file of data)
				body.append(`files`, file);
		}
	}
	else if(data)
	{
		if(params)
		{
			for(const [k,v] of Object.entries(params))
			{
				if(v != null && v != undefined)
					search.append(k, v);
			}
		}

		body = JSON.stringify(data);
		json = true;
	}

	req.onreadystatechange = (ev) => {
		if(req.readyState != XMLHttpRequest.DONE)
			return;

		let json;

		try {
			json = JSON.parse(req.responseText);
		} catch (e) {
			json = null;
		}

		if(req.status >= 200 && req.status < 400)
		{
			if(onSuccess)
				onSuccess(req, json);
		}
		else
		{
			if(onError)
				onError(req, json);
		}
	};

	if(onProgress)
	{
		req.upload.onprogress = (ev) => {
			if(ev.lengthComputable)
				onProgress(req, ev.loaded, ev.total);
			else
				onProgress(req, 0, 1);
		}
	}

	if(onDownloadProgress)
	{
		req.onprogress = (ev) => {
			if(ev.lengthComputable)
				onDownloadProgress(req, ev.loaded, ev.total);
			else
				onDownloadProgress(req, 0, 1);
		}
	}

	req.open(method, url + (search.size > 0 ? "?" + search.toString() : ""));
	req.setRequestHeader(csrfHead, csrfToken);
	if(json)
		req.setRequestHeader("Content-Type", "application/json");
	req.send(body);

	return req;
}

function displayAlert(success, message, div)
{
	div.toggleClass("alert-warning", !success);
	div.toggleClass("alert-success", success);

	div.toggleClass("d-none", message.length <= 0);
	div.text(message);
}