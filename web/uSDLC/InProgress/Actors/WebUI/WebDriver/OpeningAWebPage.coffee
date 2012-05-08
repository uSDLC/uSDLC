browse = (path) -> client.browse "http://localhost:9000/?#{path}"
browse "Sandbox"
check 'title', 'Sandbox'