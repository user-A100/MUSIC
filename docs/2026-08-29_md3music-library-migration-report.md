# md3Music 收藏与歌单结构迁移记录

## 范围

在用户授权下，对 md3Music 公共仓库的固定提交 `faf8a3fdcb16f905f9d0cae5ea69b4f41c70c55c` 做只读静态分析。仓库采用 AGPL-3.0；本项目没有复制其源码、资源或私有接口，只迁移经过独立实现的产品结构思路。

## Evidence → Finding → Path

- Evidence：`FavoritesRepository` 保存完整歌曲快照、限制容量并把新收藏放在首位；`FavoritesProvider` 用 ID Set 做快速收藏状态判断；完整播放页把“我喜欢”排除在普通歌单选择器之外。
- Finding：成熟的结构应把“我喜欢”作为系统集合，把自建歌单作为独立实体；加入歌单必须去重。GD Music 是多音源客户端，因此单曲键必须是 `source:id`，不能只比较 `id`。
- Path：搜索结果或播放页操作 → ViewModel → `LibraryRepository` → 本地 JSON 持久化 → `StateFlow` 驱动音乐库、红心状态和歌单详情。

## 独立实现

- `LibraryState`：喜欢列表与自建歌单。
- `LocalPlaylist`：稳定 UUID、名称、创建时间和歌曲快照。
- `LibraryRepository`：切换喜欢、新建/删除歌单、加入/移出歌曲、重复检查和跨重启恢复。
- UI：底部“音乐库”内含“我喜欢 / 歌单 / 队列”；播放页提供心形与加入歌单；歌单删除需要确认。

## 验证

- Kotlin 编译、LibraryModelsTest、Debug APK 构建和 Android Lint 均通过。
- Android 36 模拟器安装 2.3.0 后创建 `Test_List`，结束进程并重新进入音乐库，歌单仍存在。
