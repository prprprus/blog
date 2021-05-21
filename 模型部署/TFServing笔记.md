# TFServing 笔记

TFServing 全称是 Tensorflow Serving，是谷歌开源的一个用于模型部署的服务组件，功能丰富、生产就绪。主要用于 Tensorflow 训练的模型，听说 pytorch 的也行

TFServing 提供的核心功能：

- 支持多模型、多版本部署
- 提供 gRPC、HTTP 两种服务形式
- 支持 GPU 批处理
- 支持不中断服务的模型热加载

使用 TFServing 的系统，对应的拓扑结构一般如下：

![]()

## 安装

官方提供 Docker 镜像，安装比较方便：

```BASH
docker pull tensorflow/serving
```

也支持自定义编译安装，通过配置某些 CPU 指令集，可以在一定程度上提升 TFServing 的性能

## 配置文件

TFServing 提供了规范的配置文件，需要按照它的格式要求才能正确启动服务

```BASH
# models.config

model_config_list:{
    config:{                              // 每一个算法模型对应一个 config 配置块
        name:'din',                       // 模型名称
        base_path:'/models/din',          // 模型在容器内的路径
        model_platform:'tensorflow',      // 模型的训练框架
        model_version_policy:{            // 模型的版本
            specific:{
                versions:100000,          // 版本号 100000
                versions:100001
            }
        }
        version_labels:{                  // 模型额外的标签名称, 需要和版本号对应, 这个不是必须的
            key:'canary',                 
            value:100000
        }
        version_labels:{
            key:'stable',
            value:100001
        }
    },
    config:{                            // 另外一个算法模型配置
        name:'sdm',
        base_path:'/models/sdm',
        model_platform:'tensorflow',
        model_version_policy:{
            specific:{
                versions:100000,
                versions:100001,
                versions:100002,
                versions:100003
            }
        }
    },
}
```

## 启动

```BASH
docker run \
    -t --rm \
    -p 8501:8501 \
    -p 8500:8500 \
    --name tf_serving \
    -v /local_model_path:/models \
    tensorflow/serving \
    --model_config_file=/models/models.config \
    --model_config_file_poll_wait_seconds=60
```

参数说明：

- name：容器名字
- v：目录映射
- p：端口映射（8501 是 HTTP 服务，8500 是 gRPC 服务，建议一并开启）
- model_config_file：配置文件路径
- model_config_file_poll_wait_seconds：自动热加载的时间间隔

> 除了这些基本参数外还有其他参数
