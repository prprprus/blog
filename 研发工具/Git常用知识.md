# Git 常用知识

Git 的重要性无需多言 🐶

## Git 的基本概念

- 工作区（本地）：还没 add 的文件待的地方
- 缓存区（本地）：add 后的文件待的地方
- Git 仓库（本地）：commit 后的文件待的地方
- Git 仓库（远端）：push 后的文件待的地方

## Git 常用命令

```Bash
# 分支相关
git branch
git branch -a
git checkout <branch>
git checkout -b <branch>

# 查看本地状态
git status

# 一般的推送步骤
git pull
git add <file>/*
git commit -m <commit_message>
git push -u origin <branch>

# 回滚工作区（新建文件）
git rm --cached <file>/*

# 回滚工作区（修改文件）
git checkout -- <file>/*

# 回滚缓存区（修改文件）
git reset HEAD <file>/*
git checkout -- <file>/*

# 回滚本地 Git 仓库
git reset --hard <commit_id>

# 回滚远端 Git 仓库
git reset --hard <commit_id>
git push -f
```

- clone 项目：`git clone $GIT_ADDRESS/$HTTPS_ADDRESS`
- 查看分支：`git branch -a`

## Git 分支管理
