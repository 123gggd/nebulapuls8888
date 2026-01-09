<<<<<<< HEAD
Nebula Commerce 项目完整技术文档

1. 系统架构概览

后端 (nebula-commerce)

技术栈: Java 21, Spring Boot 3.3, Spring Modulith (模块化单体)。

数据层: MySQL 8.0, MyBatis Plus, Redis。

核心特性: 统一响应体 (Result), 全局异常处理, JWT 安全认证, 图片本地存储映射, 多角色权限物理隔离。

运行端口: localhost:8080

前端 - 管理后台 (nebula-admin)

技术栈: Vue 3, TypeScript, Vite, Element Plus。

运行端口: localhost:5173

定位: 商家/管理员使用的运营管理平台。

权限: 仅限 ADMIN (平台管理员) 和 MERCHANT (商家) 登录。

前端 - 商城前台 (nebula-store)

技术栈: Vue 3, TypeScript, Vite, Element Plus。

运行端口: localhost:5174

定位: C端用户浏览、下单、支付的购物平台。

权限: 仅限 USER (普通用户) 登录。

2. 模块功能与代码映射表

2.1 基础设施 (Infrastructure) & 安全认证 (Auth)

负责全局配置、文件服务和安全认证（含权限隔离）。

功能点

描述

后端核心文件

前端相关页面

文件服务

图片上传 (本地存储), 静态资源映射

infrastructure/web/FileController.java

全局通用

安全认证

JWT 拦截, CORS, 异常处理, 登录类型校验

infrastructure/config/SecurityConfig.java



modules/auth/controller/AuthController.java

Store: views/Login.vue



Admin: views/login/index.vue

权限隔离

[新] 强制区分 Admin/Store 登录入口

modules/auth/dto/LoginRequest.java

-

注册功能

[新] 用户/商家/管理员注册接口

modules/auth/controller/AuthController.java

Store: views/Register.vue



Admin: views/login/index.vue

密码管理

[新] 修改密码接口

modules/auth/controller/AuthController.java

-

2.2 商品模块 (Product)

负责核心的商品数据管理、分类及评价。

功能点

描述

后端核心文件

前端相关页面

商品管理

增删改查, 上下架, JSONB 动态规格

modules/product/controller/ProductAdminController.java

Admin: views/product/list.vue



Store: views/ProductDetail.vue

分类管理

无限层级树形结构, 排序

modules/product/controller/CategoryController.java

Admin: views/product/category.vue

商品展示

搜索, 详情页, 库存显示

modules/product/controller/ProductPortalController.java

Store: views/Search.vue



Store: views/Home.vue

商品评价

用户评价提交, 审核, 展示

modules/product/controller/ReviewPortalController.java

Store: views/ProductDetail.vue

2.3 订单与交易模块 (Order)

核心交易链路。

功能点

描述

后端核心文件

前端相关页面

购物车

自动剔除失效商品, 数量联动

modules/order/controller/CartController.java

Store: views/Cart.vue

下单流程

校验库存, 扣减库存, 优惠券核销

modules/order/service/impl/OrderServiceImpl.java

Store: views/Cart.vue

支付

模拟支付, 状态流转

modules/order/controller/OrderController.java

Store: views/Pay.vue

订单管理

列表查询, 发货, 售后申请/审核

modules/order/controller/OrderAdminController.java

Store: views/Order.vue



Admin: views/order/list.vue

2.4 营销模块 (Marketing)

功能点

描述

后端核心文件

前端相关页面

优惠券

发券, 领券, 结算页过滤

modules/marketing/controller/MarketingController.java

Store: views/CouponCenter.vue

秒杀活动

活动创建, 时间冲突校验

modules/marketing/service/impl/MarketingServiceImpl.java

Admin: views/marketing/seckill.vue

2.5 会员模块 (Member)

功能点

描述

后端核心文件

前端相关页面

用户中心

资料修改, 头像上传

modules/member/controller/UserController.java

Store: views/Profile.vue

地址管理

收货地址增删改查

modules/member/controller/AddressController.java

Store: views/Address.vue

会员管理

管理员查询会员列表

modules/member/controller/UserController.java

Admin: views/member/list.vue

2.6 系统模块 (System)

功能点

描述

后端核心文件

前端相关页面

数据看板

真实销售额/订单量统计

modules/system/controller/AnalyticsController.java

Admin: views/dashboard/index.vue

系统基础

公告管理, 操作日志

modules/system/controller/NoticeController.java

Admin: views/system/notice.vue

3. 系统状态更新

✅ 权限隔离升级: 实现了基于 loginType 的强制物理隔离，彻底杜绝了用户登录后台或管理员登录前台的可能性。
✅ 账户功能补全: 补全了商家注册、管理员注册（需邀请码）和修改密码功能。
=======
Nebula Commerce 项目完整技术文档

1. 系统架构概览

后端 (nebula-commerce)

技术栈: Java 21, Spring Boot 3.3, Spring Modulith (模块化单体)。

数据层: MySQL 8.0, MyBatis Plus, Redis (配置了 JSON 序列化)。

核心特性:

统一响应体 (Result): 标准化 API 返回格式。

全局异常处理: 统一处理业务异常与系统错误。

JWT 安全认证: 基于 Spring Security + JWT 的无状态认证。

数据权限隔离 (Data Scope): 基于 MyBatis Plus TenantLineHandler 实现的物理级商家数据隔离。商家只能访问自己的商品、订单和评价，而管理员拥有上帝视角，普通用户不受隔离限制。

登录物理隔离: 强制区分 admin (后台) 和 store (前台) 登录入口，防止越权。

图片存储: 本地文件系统映射 (/uploads -> /images/**)。

运行端口: localhost:8080

前端 - 管理后台 (nebula-admin)

技术栈: Vue 3, TypeScript, Vite, Element Plus, Pinia, ECharts。

运行端口: localhost:5173

定位: 商家与平台管理员的统一运营工作台。

核心特性:

动态路由: 基于角色 (ADMIN/MERCHANT) 的动态菜单渲染。

数据可视化: 集成 ECharts 展示真实销售趋势与核心指标。

交互优化: 完善的表单校验、弹窗交互与图片上传预览。

前端 - 商城前台 (nebula-store)

技术栈: Vue 3, TypeScript, Vite, Element Plus (移动端适配)。

运行端口: localhost:5174

定位: C端用户购物平台。

权限: 仅限 USER (普通用户) 登录。

2. 模块功能与代码映射表

2.1 基础设施 (Infrastructure) & 安全认证 (Auth)

负责全局配置、文件服务、安全认证及权限控制。

功能点

描述

后端核心文件

前端相关页面 (Admin/Store)

文件服务

图片上传 (本地存储, 按日期分目录), 静态资源映射

infrastructure/web/FileController.java



infrastructure/config/WebMvcConfig.java

包含在各表单上传组件中

安全认证

JWT 过滤器, CORS 配置, LoginUser 封装

infrastructure/config/SecurityConfig.java



modules/auth/filter/JwtAuthenticationFilter.java

views/login/index.vue (Admin)



src/views/Login.vue (Store)

数据隔离

[核心] 基于表名白名单与角色的 SQL 拦截

infrastructure/config/mybatis/DataScopeHandler.java

(全局隐式生效)

注册/登录

多角色注册 (普通用户/商家/管理员), 强制登录类型校验

modules/auth/controller/AuthController.java



modules/auth/service/impl/AuthServiceImpl.java

views/login/index.vue (Admin)



src/views/Login.vue (Store)



src/views/Register.vue (Store)

个人中心

修改头像、昵称、密码

modules/member/controller/UserController.java

views/profile/index.vue (Admin)



src/views/Profile.vue (Store)

2.2 商品模块 (Product)

核心商品数据管理，包含复杂的规格与分类逻辑。

功能点

描述

后端核心文件

前端相关页面 (Admin/Store)

商品管理

增删改查, 上下架, 图片上传, 富文本描述

modules/product/controller/ProductAdminController.java



modules/product/service/impl/ProductServiceImpl.java

views/product/list.vue (Admin)



views/product/edit.vue (Admin)

分类管理

无限层级树形结构 (优化递归算法), 排序

modules/product/controller/CategoryController.java

views/product/category.vue (Admin)

商品展示

(前台) 搜索, 详情页, 动态库存扣减 (乐观锁)

modules/product/controller/ProductPortalController.java

src/views/Home.vue (Store)



src/views/Search.vue (Store)



src/views/ProductDetail.vue (Store)

商品评价

评价提交, 商家回复, 审核隐藏

modules/product/controller/ReviewAdminController.java

(暂无独立页面，可扩展)

2.3 订单与交易模块 (Order)

交易全链路闭环，这是业务逻辑最复杂的模块。

功能点

描述

后端核心文件

前端相关页面 (Admin/Store)

购物车

批量查询优化 (解决 N+1), 状态同步

modules/order/controller/CartController.java

src/views/Cart.vue (Store)

下单流程

校验库存, 乐观锁扣减, 优惠券核销, 拆单逻辑

modules/order/service/impl/OrderServiceImpl.java

(后端逻辑)

支付/状态

模拟支付, 状态流转 (待付->待发->已发->完成)

modules/order/controller/OrderController.java



PayController.java

src/views/Pay.vue (Store)

订单管理

多状态筛选, 发货 (录入物流), 详情查看

modules/order/controller/OrderAdminController.java

views/order/list.vue (Admin)



src/views/Order.vue (Store)

售后处理

退款申请审核 (同意/拒绝), 库存回滚

modules/order/service/impl/OrderServiceImpl.java

views/order/list.vue (Admin 弹窗)



src/views/Order.vue (Store 申请退款)

超时关闭

定时任务自动关闭未支付订单

modules/order/task/OrderTask.java

(后台自动运行)

2.4 营销模块 (Marketing)

增强用户粘性与促销能力。

功能点

描述

后端核心文件

前端相关页面 (Admin/Store)

优惠券

创建/发布/下架, 领券 (原子计数防超发)

modules/marketing/controller/MarketingController.java



service/impl/MarketingServiceImpl.java

views/marketing/coupon.vue (Admin)



src/views/CouponCenter.vue (Store)



src/views/MyCoupon.vue (Store)

秒杀活动

活动创建, 商品选择器, 库存/时间冲突校验

modules/marketing/service/impl/MarketingServiceImpl.java

views/marketing/seckill.vue (Admin)



src/views/Home.vue (Store 展示)

2.5 会员模块 (Member)

用户资产与商家管理。

功能点

描述

后端核心文件

前端相关页面 (Admin/Store)

会员管理

列表查询, 状态控制 (封号/解封)

modules/member/controller/UserAdminController.java

views/member/list.vue (Admin)

商家管理

(管理员专用) 创建商家账号, 列表查询

modules/member/controller/MerchantController.java

views/member/merchant.vue (Admin)

地址管理

收货地址增删改查, 默认地址设置

modules/member/controller/AddressController.java

src/views/Address.vue (Store)

2.6 系统模块 (System)

系统监控与日志审计。

功能点

描述

后端核心文件

前端相关页面 (Admin/Store)

数据看板

[真实数据] 销售额(今日/累计), 订单量, 7日趋势图

modules/system/controller/AnalyticsController.java

views/dashboard/index.vue (Admin)

操作日志

AOP 切面记录关键操作, 日志查询

modules/system/aspect/LogAspect.java



modules/system/controller/SystemController.java

views/system/log.vue (Admin)

公告管理

系统公告发布与管理

modules/system/controller/NoticeController.java

src/views/NoticeList.vue (Store)

3. 系统状态更新日志

✅ 核心修复与优化 (已完成)

权限隔离终极修复:

修正了 DataScopeHandler 中的表名白名单 (sys_product 等)，确保商家数据隔离生效。

优化了过滤逻辑，确保普通用户 (User) 和管理员 (Admin) 不受商家隔离限制，仅商家 (Merchant) 受限。

数据真实化:

控制台 (Dashboard) 彻底移除 Mock 数据，对接 AnalyticsController 实时聚合查询数据库。

图表集成 ECharts，动态展示近 7 日销售趋势。

前端功能补全:

个人中心:Admin 端新增 /profile 页面；Store 端新增 Profile.vue，支持头像上传、资料修改和密码修改。

登录页: 集成“商家入驻”注册功能和“忘记密码”提示；Store 端新增 Register.vue 实现用户注册。

布局优化: 增加了顶部 Navbar，包含面包屑和用户下拉菜单（个人中心入口）。

商城前台 (nebula-store) 全面开发:

完成 基础设施: 路由守卫、Request 拦截器、User Store 状态管理。

完成 商品浏览: 首页 (Home.vue)、搜索页 (Search.vue)、详情页 (ProductDetail.vue)。

完成 交易闭环: 购物车 (Cart.vue)、订单列表 (Order.vue)、支付页 (Pay.vue)。

完成 营销中心: 领券中心 (CouponCenter.vue)、我的优惠券 (MyCoupon.vue)。

完成 用户服务: 地址管理 (Address.vue)、系统公告 (NoticeList.vue)。

路由系统修复:

补全了 profile, seckill 等缺失路由。

修复了 404 问题，确保所有功能页面均可正常访问。

依赖修复:

后端: 修复了 LogAspect 的 Bean 冲突和 Servlet API 版本兼容问题。

前端: 修复了 echarts 依赖缺失问题。
>>>>>>> origin/nebula
