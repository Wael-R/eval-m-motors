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