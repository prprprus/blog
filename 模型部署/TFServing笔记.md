# TFServing 笔记

TFServing 全称是 Tensorflow Serving，是谷歌开源的一个用于模型部署的服务组件，功能丰富、生产就绪。主要用于 Tensorflow 训练的模型，听说 pytorch 的也行，
下面主要讨论 Tensorflow 训练出来的模型

相比用原生的 Tensorflow API 加载模型和预测，TFServing 提供比较丰富的开箱即用功能，省去自行开发的工作量，对开发周期短、需要快速落地的场景很有帮助

TFServing 提供的功能：

- 支持多模型、多版本部署
- 提供 gRPC、HTTP 两种服务形式
- 支持 GPU 批处理
- 支持无需中断服务的模型热加载

使用 TFServing 的系统，对应的拓扑结构一般如下：

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/modelserving-1.png)

## 安装

官方提供 Docker 镜像，安装比较方便：

```BASH
docker pull tensorflow/serving
```

也支持编译安装，通过配置某些 CPU 指令集，可以在一定程度上提升 TFServing 的性能

## 导出模型

这块主要是算法工程师去操作。TFServing 要求模型以 saved_model 格式导出，后台工程师一般需要在模型交付的时候确保模型文件的格式

```BASH
├── model_name
│   └── 100002
│       ├── saved_model.pb
│       └── variables
```

## 配置文件

TFServing 提供了规范的配置文件，需要按照它的格式要求才能正确启动服务

```BASH
# models.config

model_config_list:{
    config:{                              // 每个模型对应一个 config 配置块
        name:'model_name1',               // 模型名称
        base_path:'/models/model_name1',  // 模型在容器内的路径
        model_platform:'tensorflow',      // 模型的训练框架
        model_version_policy:{            // 模型的版本
            specific:{
                versions:100000,          // 版本号 100000, 可以有多个
                versions:100001
            }
        }
        version_labels:{                  // 模型额外的标签, 需要和版本号对应, 这个不是必须的
            key:'canary',                 
            value:100000
        }
        version_labels:{
            key:'stable',
            value:100001
        }
    },
    config:{                              // 另一个模型配置
        name:'model_name2',
        base_path:'/models/model_name2',
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
    -t \
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

## 模型的输入输出结构

在编写 TFServing 客户端之前，还需要确定模型的输入输出格式，不同的训练 API 产出的模型格式可能会不一样，比如 Tensorflow 2.0 原生 API 和 tf.estimator 的不一样，
下面主要讨论原生 API 导出的格式

用 `saved_model_cli` 命令行工具查看格式，可以通过 `pip install tensorflow-serving-api==2.5.1` 来安装

安装好之后，到模型所在的目录下（也就是 saved_model.pb 文件所在的目录），执行 `saved_model_cli show --dir ./ --all` 就可以得到输入输出结构

```BASH
MetaGraphDef with tag-set: 'serve' contains the following SignatureDefs:

signature_def['__saved_model_init_op']:
  The given SavedModel SignatureDef contains the following input(s):
  The given SavedModel SignatureDef contains the following output(s):
    outputs['__saved_model_init_op'] tensor_info:
        dtype: DT_INVALID
        shape: unknown_rank
        name: NoOp
  Method name is:

signature_def['serving_default']:
  The given SavedModel SignatureDef contains the following input(s):
    inputs['input_1'] tensor_info:
        dtype: DT_INT32
        shape: (-1, 1)
        name: serving_default_input_1:0
    inputs['input_2'] tensor_info:
        dtype: DT_INT32
        shape: (-1, 1)
        name: serving_default_input_2:0
    inputs['input_3'] tensor_info:
        dtype: DT_INT32
        shape: (-1, 15)
        name: serving_default_input_3:0
    inputs['input_4'] tensor_info:
        dtype: DT_INT32
        shape: (-1, 1)
        name: serving_default_input_4:0
    inputs['input_5'] tensor_info:
        dtype: DT_INT32
        shape: (-1, 7)
        name: serving_default_input_5:0
  The given SavedModel SignatureDef contains the following output(s):
    outputs['output_1'] tensor_info:
        dtype: DT_FLOAT
        shape: (-1, 1)
        name: StatefulPartitionedCall:0
    outputs['output_2'] tensor_info:
        dtype: DT_FLOAT
        shape: (-1, 1)
        name: StatefulPartitionedCall:1
  Method name is: tensorflow/serving/predict
```

结果解析：

- serving_default：默认的模型签名，可以在训练时自定义
- inputs[input_xxx]：模型的输入参数，`input_xxx` 是参数名称，可以在训练时自定义
    - dtype：参数类型
    - shape：参数的行数和列数。(-1, 1) 代表行数不限制，1 列；(-1, 15) 代表行数不限制，15 列
- outputs[output_xxx]：模型的输出参数，`output_xxx` 是参数名称，可以在训练时自定义

## TFServing 客户端

TFServing 服务启动了，模型输入输出格式清楚了，下一步就是编写 TFServing 的客户端，用来发起调用。TFServing 同时支持 HTTP 和 gRPC 两种协议的服务，
在实测的过程中发现 HTTP 的响应时间 比 gRPC 要更短，下面主要讨论 HTTP 服务

### HTTP 客户端

以上面的输入输出结构为例，假设行数是 10

```python
import requests

# 模拟输入
inputs = {
    'input_1': [[0]] * 10,
    'input_2': [[0]] * 10,
    'input_3': [[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]] * 10,
    'input_4': [[0]] * 10,
    'input_5': [[0, 0, 0, 0, 0, 0, 0]] * 10
}

# 8501 是 TFServing 的 HTTP 服务端口
url = "http://<host>:8501/v1/models/<model_name>/versions/<model_version>:predict"
res = requests.post(url, json={'inputs': inputs}, timeout=1)
print(res.status_code)
print(res.json()["outputs"]["output_1"])
print(res.json()["outputs"]["output_2"])
```

### 附带 gRPC 客户端

TFServing 使用 protobuf 作为序列化协议，tensorflow-serving-api 已经包含编译好的相关 proto 文件，可以借助这个库快速完成 gPRC 客户端。也可以自行编译 proto 文件

```python
from tensorflow_serving.apis import predict_pb2, prediction_service_pb2_grpc
from tensorflow.contrib.util import make_tensor_proto
from tensorflow.core.framework import types_pb2
import grpc

config = {
    "hostport": "<host>:8500",
    "max_message_length": 500 * 1024 * 1024,
    "timeout": 1000,
    "signature_name": "serving_default",
    "model_name": "<model_name>",
    "model_version": "stable"   # gRPC 方式的版本号需要用模型标签
}

channel = grpc.insecure_channel(config['hostport'])
stub = prediction_service_pb2_grpc.PredictionServiceStub(channel)

request = predict_pb2.PredictRequest()
request.model_spec.name = config['model_name']
request.model_spec.signature_name = config['signature_name']
request.model_spec.version_label = config["model_version"]

input_1 = [[0]] * 10,
input_2 = [[0]] * 10,
input_3 = [[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]] * 10,
input_4 = [[0]] * 10,
input_5 = [[0, 0, 0, 0, 0, 0, 0]] * 10

request.inputs['input_1'].CopyFrom(make_tensor_proto(input_1, shape=[10, 1], dtype=types_pb2.DT_INT32))
request.inputs['input_2'].CopyFrom(make_tensor_proto(input_2, shape=[10, 1], dtype=types_pb2.DT_INT32))
request.inputs['input_3'].CopyFrom(make_tensor_proto(input_3, shape=[10, 15], dtype=types_pb2.DT_INT32))
request.inputs['input_4'].CopyFrom(make_tensor_proto(input_4, shape=[10, 1], dtype=types_pb2.DT_INT32))
request.inputs['input_5'].CopyFrom(make_tensor_proto(input_5, shape=[10, 1], dtype=types_pb2.DT_FLOAT))

result = stub.Predict(request, config['timeout'])
channel.close()
```
