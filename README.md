# NetworkRouteTool
可用于双网卡分流使用，常见于内外网双网卡流量分离使用。

### Update

#### v1.1

​		1、新增白名单模式。

​		2、修复路由控制不够精细问题。

​		3、强制管理员运行。



#### V1.0

​		1、实现双网口同时使用。



### F&Q

#### 	软件运行环境？

​			本软件仅支持在Windows操作系统运行，中文或英文环境下使用。



#### 	网卡的默认网关为什么是0.0.0.0？

​			1、IP为手动配置，而没有配置默认网关，进入对应网卡进行设置即可。

​			2、IP为DHCP获取，网线刚刚插上，仅获取到了IP，网关还没来得及获取，等待15秒到1分钟后刷新即可获取。



#### 	为什么执行之后提示 “请求的操作需要提升”或“The requested operation requires elevation”？

​		以管理员权限执行软件即可。



#### 	白名单如何设置？

​		配置文件命名：WhiteList.config。

​		文件存放位置：与软件同级目录即可检测。

​		如何打开：文本编辑器即可直接打开编辑。

​		配置内容：IP和子网掩码以空格隔开，多条名单内容则以回车隔开，#为单行注释。



### 反馈

如遇到其他BUG或问题，欢迎将问题描述和软件工具下存放路径下的error.log邮箱至zhouh1105@163.com进行反馈。
