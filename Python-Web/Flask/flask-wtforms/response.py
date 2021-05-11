from enum import Enum

from flask import make_response, jsonify


class ResponseStatus:
    class StatusCode(Enum):
        SUCCESS = 0
        ARGS_VALIDATE_ERROR = 400000

    class StatusMessage(Enum):
        SUCCESS = "success"
        ARGS_VALIDATE_ERROR = "arguments validate error"


def generate_response(code, message, data=None, headers=None):
    response = make_response(jsonify({"code": code, "message": message, "data": data}))

    _headers = {"content_type": "application/json; charset=utf-8"}
    if headers is not None:
        _headers.update(headers)
    for k, v in _headers.items():
        response.headers.add(k, v)

    return response
