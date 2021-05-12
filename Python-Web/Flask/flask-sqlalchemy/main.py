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


def _to_dict(res):
    _data = []
    for item in res:
        d = dict()
        d["factory_name"] = str(item[0])
        d["product_name"] = str(item[1])
        _data.append(d)

    return _data


@app.route("/hello")
def handle():
    try:
        # 普通查询
        res = mysql_session.query(Factory).all()

        # 基于一对多关系的连表查询
        # res = mysql_session.query(Factory.name, Product.name).join(Product, Factory.factory_id == Product.factory_id).all()

        # 基于多对多关系的连表查询
        # 1. 查这张订单 23ff2f2a-bc09-4444-b415-121bd79df5df 包含的商品名称
        # res = mysql_session.query(OrdersProduct.product_id, Product.name)\
        #                    .join(Product, OrdersProduct.product_id == Product.product_id)\
        #                    .filter(OrdersProduct.order_id == "23ff2f2a-bc09-4444-b415-121bd79df5df")\
        #                    .all()
        # 2. 查这个商品 a473f8af-bdbd-418d-a986-50e10bd9673c 对应订单的价格
        # res = mysql_session.query(OrdersProduct.order_id, Orders.price)\
        #                    .join(Orders, OrdersProduct.order_id == Orders.order_id)\
        #                    .filter(Product.product_id == "a473f8af-bdbd-418d-a986-50e10bd9673c")\
        #                    .all()

        data = {"code": 0, "message": "success", "data": _to_dict(res)}
        result = json.dumps(data, ensure_ascii=False)
        response = Response(result, content_type="application/json; charset=utf-8")
        return response
    except:
        mysql_session.rollback()
        raise
    finally:
        pass


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=12345, debug=True)
