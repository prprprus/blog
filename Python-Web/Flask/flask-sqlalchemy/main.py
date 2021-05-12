import json

from flask import Flask, Response

from database import mysql_session
from model import (
    to_dict,
    Factory,
    Product,
    Orders,
    OrdersProduct
)

app = Flask(__name__)


@app.route("/hello")
def handle():
    try:
        res = mysql_session.query(Factory).all()

        _data = {"code": 0, "message": "success", "data": to_dict(res)}
        data = json.dumps(_data, ensure_ascii=False)
        response = Response(data, content_type="application/json; charset=utf-8")
        return response
    except:
        mysql_session.rollback()
        raise
    finally:
        pass


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=12345, debug=True)
