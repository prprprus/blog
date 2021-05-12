from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from config import MySQLConfig


class DatabaseEngineAttributeError(Exception):
    pass


class Database:
    def __init__(self, url, echo=False, pool_size=5, max_overflow=10, pool_recycle=3600, pool_pre_ping=True):
        self.__url = url
        self.__echo = echo
        self.__pool_size = pool_size
        self.__max_overflow = max_overflow
        self.__pool_recycle = pool_recycle
        self.__pool_pre_ping = pool_pre_ping

        self.__engine = create_engine(
            url=self.__url,
            echo=self.__echo,
            pool_size=self.__pool_size,
            max_overflow=self.__max_overflow,
            pool_recycle=self.__pool_recycle,
            pool_pre_ping=self.__pool_pre_ping
        )

    @property
    def engine(self):
        return self.__engine

    @engine.setter
    def engine(self, _):
        raise DatabaseEngineAttributeError

    def generate_session(self):
        session = sessionmaker(bind=self.__engine)
        return session()


# MySQL
mysql_config = MySQLConfig(username="tiger", password="hzz2956195", host="10.211.55.39")
# mysql_config = MySQLConfig(username="xxx", password="xxx", host="xxx")
mysql_session = Database(url=mysql_config.connection_url).generate_session()
