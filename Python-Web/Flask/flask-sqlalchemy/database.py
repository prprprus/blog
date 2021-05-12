from contextlib import contextmanager

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from config import mysql_config, DatabaseType


class DatabaseEngineAttributeError(Exception):
    pass


class DatabaseTypeError(Exception):
    pass


class DatabaseEngine:
    def __init__(self, url, echo=False, echo_pool=False, pool_size=5, max_overflow=10, pool_recycle=25200, pool_pre_ping=True):
        self.__url = url
        self.__echo = echo
        self.__echo_pool = echo_pool
        self.__pool_size = pool_size
        self.__max_overflow = max_overflow
        self.__pool_recycle = pool_recycle
        self.__pool_pre_ping = pool_pre_ping

        self.__engine = create_engine(
            url=self.__url,
            echo=self.__echo,
            echo_pool=self.__echo_pool,
            pool_size=self.__pool_size,
            max_overflow=self.__max_overflow,
            pool_recycle=self.__pool_recycle,
            pool_pre_ping=self.__pool_pre_ping,
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


@contextmanager
def session_factory(database_type=DatabaseType.MYSQL.value):
    if database_type == DatabaseType.MYSQL.value:
        session = DatabaseEngine(url=mysql_config.connection_url).generate_session()
    else:
        raise DatabaseTypeError

    try:
        yield session
        session.commit()
    except:
        session.rollback()
        raise
    finally:
        session.close()
