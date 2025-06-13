# 🧊 AutoFreeze Pro - Minecraft 服务器自动休眠插件

[![License](https://img.shields.io/badge/license-GPL--3.0-blue)](https://www.gnu.org/licenses/gpl-3.0.html)  
[![Build Status](https://img.shields.io/badge/status-Stable-brightgreen)](#)  

> 🚀 让你的 Minecraft 服务器在无人在线时自动冻结 & 进入休眠状态，节省系统资源！

---

## 📌 简介

**AutoFreeze Pro** 是一个轻量级的 Bukkit/Spigot 插件，用于在玩家全部离线后：

- 自动冻结游戏时间（tick freeze）  
- 在设定延迟后触发操作系统的 **休眠（Hibernate）**  
- 防止服务器空转浪费资源，适合私人或低活跃度的 Minecraft 服务器  

✅ 支持 Windows / Linux / macOS  
✅ 带倒计时提示，人性化设计  
✅ 可灵活控制是否启用冻结与休眠功能  

---

## ⚙️ 主要功能

| 功能 | 说明 |
|------|------|
| 🧊 自动冻结游戏 | 最后一名玩家退出后自动执行 `tick freeze` |
| 💤 自动休眠服务器 | 可配置延迟后触发操作系统休眠命令 |
| ⏰ 倒计时通知 | 触发休眠前发送倒计时提示给所有玩家 |
| 🔄 实时响应 | 玩家重新上线时自动解冻并取消休眠任务；支持与 WOL-Tool 插件联动，实现节能副服与高性能主服的智能切换 |


---

## 🧠 技术亮点

- 使用 [BukkitRunnable](file://org\bukkit\scheduler\BukkitRunnable.java#L5-L27) 安全管理定时任务 ✅  
- 使用 `ProcessBuilder` 调用系统命令，兼容性更好 🖥️  
- 多平台支持：Windows/Linux/macOS 均可使用 🌍  
- 事件监听机制实时响应玩家上下线 👂  
- 清晰简洁的设计，便于阅读和扩展 🧱  

---

## 📜 开源协议

本项目采用 [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html) 协议。

- 必须保留原作者署名  
- 不允许将此插件用于闭源项目  
- 修改和分发必须保持开源  

---

## 📬 联系我

欢迎提 Issue 或 Pull Request！如果你有想法改进这个插件，欢迎参与开发！

📧 邮箱：ANTmmmmm@outlook.com 
