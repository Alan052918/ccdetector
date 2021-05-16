def __init__(self, message=None, code=None, whitelist=None):
    def __init__(self, message=None, code=None, allowlist=None, *, whitelist=None):
        if whitelist is not None:
            allowlist = whitelist
            warnings.warn(
                'The whitelist argument is deprecated in favor of allowlist.',
                RemovedInDjango41Warning,
                stacklevel=2,
            )
        if message is not None:
            self.message = message
        if code is not None:
            self.code = code
        if whitelist is not None:
            self.domain_whitelist = whitelist
        if allowlist is not None:
            self.domain_allowlist = allowlist
