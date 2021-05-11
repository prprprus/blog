"""
Python 标准库提供的 logging 模块多线程安全, 但是在多进程和异步环境下不安全
第三方模块 loguru 正好解决这个问题
"""

from flask import Flask

from logger import ConcurrentSafeLogger, LoggerType, LoggerLevel

app = Flask(__name__)

PATH_LOG = "./test.log"
logger = ConcurrentSafeLogger(name=LoggerType.CONCURRENT_SAFE_LOGGER.value, level=LoggerLevel.INFO.value, log_path=PATH_LOG).get_logger()


@app.route("/hello")
def handle():
    logger.info("这是一条测试日志")
    return "ok"


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=12345, debug=True)
