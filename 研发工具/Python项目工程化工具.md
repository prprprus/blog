# Python 项目工程化工具

update -> lint -> build -> test -> deploy

当我们需要做一个稍大点的项目，而不是一两个简单的脚本时，可能就会遇到像环境隔离、依赖管理、代码风格、单元测试、部署等一系列工程化问题。下面记录一些好用的，可以帮助完成
Python 项目工程化的相关工具

### 代码管理

- Git（业内标准）

### 代码风格检查

- pylint（轻量级，支持不同的编码风格，比如 PEP8、flask8 等）
- Sonar（重量级）

### 依赖管理

- pip（一般搭配 requirements.txt 使用）
- pip 国内源
  - 豆瓣（推荐）：`-i https://pypi.doubanio.com/simple/`
  - 清华：`-i https://pypi.tuna.tsinghua.edu.cn/simple/`

### 环境隔离

- Docker（业内标准）
- Conda/Miniconda（可以做环境隔离、依赖管理，同时对科学计算工作者友好，内置了比较多相关的库）

### 打包

- Docker（也是一个应用打包工具）
- Pyinstaller
- Nuitka（这个除了打包，还能帮你稍微优化一下性能）

### 单元测试

- [unittest](https://github.com/hsxhr-10/Blog/blob/master/Python%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0/%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95.md) （基本够用了）
- unittest.mock（基本够用了）
- coverage.py（生成覆盖率报告）

### 部署

- Shell（轻量级）
- fabric（轻量级）
- Ansible（规范化的 Shell 脚本，貌似用来做裸机配置比较多）
- k8s（灵活强大的容器编排平台）

### 持续集成

- Git Pre-Commit（轻量级，在 commit 之前先执行自定义脚本）
- GitLab-CI（跟 GitLab 的粘性较好）
- Jenkins（业内标准）

### 进程管理

- Supervisor（除了 Python 服务，其他服务组件或者一些零散的监控脚本也可以用进程管理工具进行管理）

### 接口管理

- YApi
