# 介绍

在介绍 Cython 之前要先看一下 Python。大家平时说的 Python 一般指的是 CPython，也就是 C 实现的 Python。除了该实现之外，还有 Jython（Java 实现）、IronPython（C# 实现）、pypy（Python 实现）。

之所以有这么多非官方实现，很大原因是为了解决性能问题（如消除 GIL、引入 JIT ）。

除了官方的 CPython，其他实现或多或少都有缺陷，要么是比较非主流，生态不行；要么是对 CPython 的兼容性不是很好，导致很多 CPython 下的库无法使用。

> 后续说的 Python 都是指 CPython

而 Cython 的出现正是为了在兼容 CPython 的情况下，尽可能地解决性能问题。Cython 走的路线也不太一样，它致力于将纯 Python 代码静态化、类型化，通过翻译转换成 C/C++ 语言来达到性能提升的目的。

Cython 的用途大致如下：

1. 通过静态化、类型化、转换成 C/C++ 等方法提升 Python 的性能
2. 在 Python 代码中调用 C/C++ 代码

总的来说，Cython 是一个 Python 编译器，语法上则是 Python 的超集。它能够以比较少的代价（相对用 Python/C API、C++ 扩展、换静态语言重构 等方法），快速显著地提升 Python 的性能，特别是对于 CPU 密集型任务 。

当然，它也有短板，比如在实际的开发情境中，一段待优化的代码往往不是单纯的数值运算，一般还会夹杂着各种第三方库，这些库就可能会成为优化效果的障碍，要突破它们就要面临重写这些库的困境；又比如，Cython 中提供的 C++ 库 <vector>，新的 C++ 标准中有一个相对 push_back() 性能更好的方法 emplace_back()，Cython 则没有提供；再比如，Cython 对于 Python2.x 和 Python3.x 的处理不统一，对于 2.x 中的字符串能直接对应到 Cython C++ 库 <string> 中的字符串，但是由于 3.x 中的字符串是一个 Unicode 容器，则需要先 encode 成 bytes，才能对应到 <string> 中的字符串。

后续笔记主要从实用角度出发，着重讨论的是 Cython 语法和所提供的标准库，C/C++ 的原生代码可能不会涉及太多，目的是尽量用相对简单的 Cython 扩展来提升我们 Python 程序的性能。

# 安装

> 以 Mac 系统为例

安装 Cython 需要的东西：pip、C/C++ 编译器

确保有这些之后执行：

```bash
$ pip install cython
```

验证是否安装成功：

```bash
$ cython
Cython (http://cython.org) is a compiler for code written in the
Cython language.  Cython is based on Pyrex by Greg Ewing.

Usage: cython [options] sourcefile.{pyx,py} ...

Options:
  -V, --version                  Display version number of cython compiler
  -l, --create-listing           Write error messages to a listing file
  -I, --include-dir <directory>  Search for include files in named directory
                                 (multiple include directories are allowed).
  -o, --output-file <filename>   Specify name of generated C file
  -t, --timestamps               Only compile newer source files
  -f, --force                    Compile all source files (overrides implied -t)
  -v, --verbose                  Be verbose, print file names on multiple compilation
  -p, --embed-positions          If specified, the positions in Cython files of each
                                 function definition is embedded in its docstring.
  --cleanup <level>              Release interned objects on python exit, for memory debugging.
                                 Level indicates aggressiveness, default 0 releases nothing.
  -w, --working <directory>      Sets the working directory for Cython (the directory modules
                                 are searched from)
  --gdb                          Output debug information for cygdb
  --gdb-outdir <directory>       Specify gdb debug information output directory. Implies --gdb.

  -D, --no-docstrings            Strip docstrings from the compiled module.
  -a, --annotate                 Produce a colorized HTML version of the source.
  --annotate-coverage <cov.xml>  Annotate and include coverage information from cov.xml.
  --line-directives              Produce #line directives pointing to the .pyx source
  --cplus                        Output a C++ rather than C file.
  --embed[=<method_name>]        Generate a main() function that embeds the Python interpreter.
  -2                             Compile based on Python-2 syntax and code semantics.
  -3                             Compile based on Python-3 syntax and code semantics.
  --3str                         Compile based on Python-3 syntax and code semantics without
                                 assuming unicode by default for string literals under Python 2.
  --lenient                      Change some compile time errors to runtime errors to
                                 improve Python compatibility
  --capi-reexport-cincludes      Add cincluded headers to any auto-generated header files.
  --fast-fail                    Abort the compilation on the first error
  --warning-errors, -Werror      Make all warnings into errors
  --warning-extra, -Wextra       Enable extra warnings
  -X, --directive <name>=<value>[,<name=value,...] Overrides a compiler directive
  -E, --compile-time-env name=value[,<name=value,...] Provides compile time env like DEF would do.
```