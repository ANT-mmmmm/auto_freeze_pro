package sub.server.tool.ant_cave.asia.auto_freeze_pro;

// Bukkit核心类导入
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

// Bukkit配置相关导入
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

// 任务调度相关导入
import org.bukkit.scheduler.BukkitRunnable;

// 其他必要的Java类导入
import java.io.File;
import java.util.Objects;

// 注解相关导入
import org.jetbrains.annotations.NotNull;

// 线程相关导入
import static java.lang.Thread.sleep;

/**
 * Auto_freeze_pro 插件主类
 * 实现服务器自动休眠功能，当没有玩家在线时自动进入休眠状态以节省资源
 */
public final class Auto_freeze_pro extends JavaPlugin {

    // 玩家计数器
    public int currentPlayerNumber = 0;

    // 配置
    public String lang = "zh_cn"; // 默认语言设置为中文

    public boolean autoFreeze = false;//  是否自动冻结游戏
    public boolean autoHibernate = false;//  是否自动休眠游戏

    public int autoFreezeDelayTick=40;//  默认延迟40tick
    public int autoHibernateDelaySecond = 600;//  默认延迟600秒

    public int autoHibernateWarningSecond=10;//  默认延迟10秒


    // 使用 BukkitRunnable 替代 taskId 来管理休眠任务
    private BukkitRunnable hibernateTask = null;


    /**
     * 插件启用时调用的方法
     * 初始化配置、注册事件监听器和命令，设置定时任务等
     */
    @Override
    public void onEnable() {
        // 初始化配置文件
        File settingsFile = new File(getDataFolder(), "settings.yml");
        if (!settingsFile.exists()) {
            settingsFile.getParentFile().mkdirs();
            saveResource("settings.yml", false);
        }

        FileConfiguration settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);

        // 加载配置项
        lang=settingsConfig.getString("lang","zh_cn");
        autoFreeze = settingsConfig.getBoolean("autoFreeze",  true);
        autoHibernate = settingsConfig.getBoolean("autoHibernate",  true);
        autoFreezeDelayTick=settingsConfig.getInt("autoFreezeDelayTick",  40);
        autoHibernateDelaySecond = settingsConfig.getInt("autoHibernateDelaySecond",  600);
        autoHibernateWarningSecond=settingsConfig.getInt("autoHibernateWarningSecond",  10);


        // 注册玩家事件监听器
        getServer().getPluginManager().registerEvents(new PlayerEventsListener(this), this);

        // 注册/hibernate命令
        Objects.requireNonNull(this.getCommand("hibernate")).setExecutor(this);

        // 如果初始状态下没有玩家在线
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            // 初始冻结游戏
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");
            // 启动定时休眠任务
            if (autoHibernate) {
                this.hibernateTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (currentPlayerNumber == 0) {
                            getLogger().info("已等待 " + autoHibernateDelaySecond + " 秒，无玩家在线，已触发服务器休眠！");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "hibernate");
                        }
                    }
                };
                this.hibernateTask.runTaskLater(this, autoHibernateDelaySecond * 20L);
                // 打印任务 ID（调试用途）
                getLogger().info("已启动服务器休眠任务 id:" + this.hibernateTask.getTaskId());
            }
        }
    }

    /**
     * 插件禁用时调用的方法
     * 清理资源、取消任务等
     */
    @Override
    public void onDisable() {
        getLogger().info("AutoFreeze has been disabled!");
    }

    /**
     * 处理插件命令
     * @param sender 命令发送者
     * @param command 执行的命令
     * @param label 命令标签
     * @param args 命令参数
     * @return 是否成功处理命令
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (command.getName().equalsIgnoreCase("hibernate")) {

            // 只允许控制台执行该命令
            if (!(sender instanceof Player)) {
                startHibernateTask(sender);
            } else {
                sender.sendMessage("§c你没有权限执行此命令！");
            }

            return true;
        }

        return false;
    }

    /**
     * 启动10秒倒计时并执行系统休眠
     * @param sender 命令发送者
     */
    private void startHibernateTask(CommandSender sender) {
        int countdownSeconds = autoHibernateWarningSecond; // 固定倒计时10秒

        // 如果已有任务在运行，先取消它防止多个倒计时并存
        if (this.hibernateTask != null) {
            this.hibernateTask.cancel();
        }

        // 创建新的倒计时任务
        this.hibernateTask = new BukkitRunnable() {
            private int remaining = countdownSeconds;

            @Override
            public void run() {
                if (remaining > 0) {
                    sender.sendMessage("§e服务器将在 " + remaining + " 秒后进入休眠状态...");
                    remaining--;
                } else {
                    sender.sendMessage("§a服务器系统休眠!");

                    // 踢出所有玩家
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kick @a 服务器休眠");

                    // 使用 ProcessBuilder 替代 Runtime.exec()
                    String osName = System.getProperty("os.name").toLowerCase();
                    try {
                        ProcessBuilder processBuilder;
                        if (osName.contains("win")) {
                            // Windows 系统
                            processBuilder = new ProcessBuilder("rundll32.exe", "powrprof.dll,SetSuspendState");
                        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
                            // Linux 或 macOS
                            processBuilder = new ProcessBuilder("systemctl", "hibernate");
                        } else {
                            sender.sendMessage("§c未知操作系统，无法执行休眠。");
                            this.cancel();
                            return;
                        }

                        // 启动进程
                        Process process = processBuilder.start();
                        int exitCode = process.waitFor(); // 可选：等待命令执行完成
                        if (exitCode != 0) {
                            sender.sendMessage("§c系统休眠命令执行失败，错误码: " + exitCode);
                        }
                    } catch (Exception e) {
                        sender.sendMessage("§c执行系统休眠失败: " + e.getMessage());
                    }

                    this.cancel(); // 结束当前任务
                }
            }
        };

        this.hibernateTask.runTaskTimer(this, 0L, 20L); // 每秒执行一次
    }

    /**
     * 玩家事件监听器类
     * 处理玩家加入和退出事件，控制游戏冻结与解冻
     */
    class PlayerEventsListener implements Listener {

        private final Auto_freeze_pro plugin;
        public PlayerEventsListener(Auto_freeze_pro plugin) {
            this.plugin = plugin;
            // 注册事件监听器
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        /**
         * 玩家加入事件处理
         * @param event 玩家加入事件
         * @throws InterruptedException 如果线程被中断
         */
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) throws InterruptedException {
            // 当第一个玩家加入时
            if (Bukkit.getOnlinePlayers().size() == 1) {
                plugin.getLogger().info("第一个玩家 " + event.getPlayer().getName() + " 加入了游戏");
                plugin.getLogger().info("游戏解冻");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick unfreeze");

                // 如果有正在运行的休眠任务
                if (plugin.hibernateTask != null){
                    try {
                        // 取消休眠任务
                        if (plugin.autoHibernate) {
                            plugin.hibernateTask.cancel();
                            plugin.hibernateTask = null;
                            plugin.getLogger().info("已取消服务器休眠任务");
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("AutoFreeze Pro 插件执行错误");
                    }
                }
            }
        }

        /**
         * 玩家退出事件处理
         * @param event 玩家退出事件
         * @throws InterruptedException 如果线程被中断
         */
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) throws InterruptedException {
            // 当最后一个玩家退出时
            if (Bukkit.getOnlinePlayers().size() == 1) {
                plugin.getLogger().info("最后一个玩家 " + event.getPlayer().getName() + " 退出了游戏");
                sleep(1000);
                // 冻结服务器
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");

                // 如果启用了自动休眠功能
                if (plugin.autoHibernate)
                {
                    // 如果有旧的任务，先取消
                    if (plugin.hibernateTask != null) {
                        plugin.hibernateTask.cancel(); // 先取消旧任务
                    }

                    // 创建新的休眠任务
                    plugin.hibernateTask = new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "hibernate");
                        }
                    };

                    // 调度任务
                    plugin.hibernateTask.runTaskLater(plugin, plugin.autoHibernateDelaySecond * 20L);
                    plugin.getLogger().info("已启动服务器休眠任务 id:" + plugin.hibernateTask.getTaskId());
                }
            }
        }
    }
}
