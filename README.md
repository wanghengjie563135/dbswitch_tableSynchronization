# -dbswitch_tableSynchronization-DBSwitch阉割版实现异构数据库表结构同步
将dbswitch修改之后，dbswitch只支持表结构同步功能，没有数据同步功能了，主要支持字段类型、主键信息、建表语句等的转换，并生成建表SQL语句。支持基于正则表达式转换的表名与字段名映射转换。

# 一、前言

&#x20;   在公司使用dataX实现异构数据库离线结构同步之后，公司同步数据库数据效率大大提升，但是如果oracle数据库创建了一张test表，想同步到mysql数据库，也需要在mysql数据库中创建test表之后,才能实现数据库表结构同步，DataX本身作为数据同步框架，将不同数据源的同步抽象为从源头数据源读取数据的Reader插件，以及向目标端写入数据的Writer插件，理论上DataX框架可以支持任意数据源类型的数据同步工作，但是就是没有同步表结构的功能，然后在gitee上面，发现了一个叫做dbswitch的项目

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_decQAwYeSa.png)

*   dbswitch开源项目地址：[https://gitee.com/inrgihc/dbswitch](https://gitee.com/inrgihc/dbswitch "https://gitee.com/inrgihc/dbswitch")

dbswitch实现的功能是:**异构数据库迁移同步工具**,dbswitch提供源端数据库向目的端数据的全量与增量迁移同步功能,其实大概功能和datax类似，只是效率和稳定性没有datax好，因为datax毕竟是阿里巴巴开源，并且也是目前国内认为开源中最好的离线数据同步工具，但是dbswitch有一个强大的功能，并且目前开源项目中我认为最完善的异构数据库表结构同步功能（我是在github,gitee上面找了几乎所有的国内表结构同步项目，最后选择了dbswitch）目前dbswitch同步的功能有：

一句话，dbswitch工具提供源端数据库向目的端数据库的**批量**迁移同步功能，支持数据的全量和增量方式同步。包括：

*   **结构迁移**

支持字段类型、主键信息、建表语句等的转换，并生成建表SQL语句。

支持基于正则表达式转换的表名与字段名映射转换。

*   **数据同步**。

基于JDBC的分批次读取源端数据库数据，并基于insert/copy方式将数据分批次写入目的数据库。

支持有主键表的 **增量变更同步** （变化数据计算Change Data Calculate）功能(千万级以上数据量的性能尚需在生产环境验证)

而他的数据同步可以使用datax替代，但是结构迁移目前最好表结构同步方案，然后我经过了大概半个月的时间，进行修改测试整合，目前dbswitch+datax的整合已经进入尾声，在生产环境上能够实现dbswitch实现表结构同步，datax实现数据同步！

# 二、dbswitch阉割版（异构数据库表结构同步工具）简介

### 1、dbswitch阉割版之后功能

&#x20;我将dbswitch修改之后，dbswitch只支持表结构同步功能，没有数据同步功能了，主要支持字段类型、主键信息、建表语句等的转换，并生成建表SQL语句。

支持基于正则表达式转换的表名与字段名映射转换。

### 2、功能设计

这张图是dbswitch开源作者画的，其中阉割版的dbswitch就取消了离线异构数据同步功能

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_nPrD5xEXlf.png)

### 3、表结构同步支持的数据库

目前常见数据库oracle,db2，mysql,sqlserver都全部支持，具体详情请参考：

*   源段oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo/Hive向目的端为Greenplum/PostgreSQL/HighGo的迁移(**支持绝大多数常规类型字段**)

*   源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo/Hive向目的端为Oracle的迁移(**支持绝大多数常规类型字段**)

*   源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo/Hive向目的端为DM的迁移(**支持绝大多数常规类型字段...**)

*   源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo/Hive向目的端为SQLServer的迁移(**字段类型兼容测试中...**)

*   源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo/Hive向目的端为MySQL/MariaDB的迁移(**字段类型兼容测试中...**)

*   源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo/Hive向目的端为DB2的迁移(**字段类型兼容测试中...**)

*   源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo/Hive向目的端为Kingbase8的迁移(**支持绝大多数常规类型字段...**)

### 4、dbswitch模块结构设计

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_8Tv6LH7ndH.png)

### 5、模块结构功能

```bash
└── dbswitch
    ├── dbswitch-common    // dbswitch通用定义模块
    ├── dbswitch-pgwriter  // PostgreSQL的二进制写入封装模块
    ├── dbswitch-dbwriter  // 数据库的通用批量Insert封装模块
    ├── dbswitch-core      // 数据库元数据抽取与建表结构语句转换模块
    ├── dbswitch-sql       // 基于calcite的DML语句转换与DDL拼接模块
    ├── dbswitch-dbcommon  // 数据库操作通用封装模块
    ├── dbswitch-dbchange  // 基于全量比对计算变更（变化量）数据模块
    ├── dbswitch-dbsynch   // 将dbchange模块计算的变更数据同步入库模块
    ├── dbswitch-data      // 工具入口模块，读取配置文件中的参数执行异构迁移同步
    ├── dbswitch-admin     // 在以上模块的基础上引入Quartz的调度服务与接口
    ├── dbswitch-admin-ui  // 基于Vue2的前段WEB交互页面
    ├── package-tool       // 基于maven-assembly-plugin插件的项目打包模块
```

# 三、dbswitch实现异构数据库表结构同步功能

我们可以将gitee仓库的dbswitch拉取到本地开发环境：[https://gitee.com/inrgihc/dbswitch.git](https://gitee.com/inrgihc/dbswitch.git "https://gitee.com/inrgihc/dbswitch.git")

### 1、在dbswitch-data下找到MigrationHandler

拉取下来之后部署，修改MigrationHandler代码如下：

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_eTzim-OpD5.png)

```java

```

### 2、修改dbswitch-admin的配置

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_DARaACbpHM.png)

```.properties
server:
  port: 9088

spring:
  application:
    name: dbswitch-admin
  tomcat:
    uri-encoding: UTF-8
    max-http-header-size: 8096
  mvc:
    throw-exception-if-no-handler-found: true
    static-path-pattern: /statics/dbswitch/**
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/dbswitch?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC
    username: root
    password: root
    validation-query: SELECT 1
    test-on-borrow: true
  flyway:
    locations: classpath:db/migration
    baseline-on-migrate: true
    table: DBSWITCH_SCHEMA_HISTORY
    enabled: true

mybatis:
  configuration:
    lazy-loading-enabled: true
    aggressive-lazy-loading: false
    map-underscore-to-camel-case: true
    #log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

mapper:
  wrap-keyword: "`{0}`"
  enable-method-annotation: true
```

*   注意：我使用的是mysql8所以还得修改maven

```xml
<dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <version>8.0.22</version>
      <scope>runtime</scope>
    </dependency>
```

### 3、如果想实现Oracle相关表结构同步需要添加依赖

在dbswitch-core的maven里面添加依赖，不然最初始的项目不支持oracle，会报错：

```xml
<dependency>
      <groupId>cn.easyproject</groupId>
      <artifactId>orai18n</artifactId>
      <version>12.1.0.2.0</version>
    </dependency>
```

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_7njjE2cW0i.png)

这样之后启动项目，启动项目之前需要在数据库中创建dbswitch数据库，dbswitch可以实现自动导入表，所以只需要创建数据库就可以了，创建数据库的语句：

```sql
drop database if exists `dbswitch`;
#创建dbswitch数据库
create database `dbswitch` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

```

阉割版的dbswitch就可以实现了，启动项目，项目访问地址：[http://127.0.0.1:9088/](http://127.0.0.1:9088/ "http://127.0.0.1:9088/")

账户：admin密码:123456

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_Hb2F4mRum8.png)

### 4、dbswitch添加账户密码

dbswitch本身没有添加账户的功能，如果登录只能admin/123456进行登入，如果想添加账户，需要数据库管理员（DBA）添加

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_E8evToEWC6.png)

*   我们可以看到dbswitch的用户密码是加了盐的

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_c6NyfZio56.png)

我们想要添加账户时需要给密码加盐，java实现dbswitch的密码

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_cGSabtnO77.png)

*   导入maven

```xml
 <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.7.5</version>
 </dependency>
```

*   java,输入密码自动实现加密

```java
package com.tjcu;

import cn.hutool.crypto.digest.BCrypt;
public class PasswordUtils {

    public static void main(String[] args) {
        String password = "whj1234";
        String credentialsSalt = "$2a$10$eUanVjvzV27BBxAb4zuBCu";
        String newPassword = encryptPassword(password, credentialsSalt);
        System.out.println(newPassword);
        System.out.println(credentialsSalt);
    }

    public static String encryptPassword(String password, String credentialsSalt) {
        return BCrypt.hashpw(password, credentialsSalt);
    }


}

```

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_fBzRM65OJM.png)

*   在dbswitch 添加账号sql语句

```sql
# dbswitch 添加账号sql语句
INSERT INTO `dbswitch_system_user` VALUES (2, 'whj', 
'$2a$10$eUanVjvzV27BBxAb4zuBCuK/4evNmPiFwA.XvSOd6Efpw.hQZbST.', 
'$2a$10$eUanVjvzV27BBxAb4zuBCu', 'whj', '123@qq.com', '', 0, NOW(), NOW());
```

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_cUpAy-SiHq.png)

这样之后我们阉割版的dbswitch就全部完成了

# 四、dbswitch实现nginx配置

dbswitch全部完成了,但是项目需要放在生产环境上，需要使用nginx进行转发，如果我们之间转发

[http://127.0.0.1:9088/](http://127.0.0.1:9088/ "http://127.0.0.1:9088/")这个连接的话，影响nginx使用，占用太多的资源了，所以为了节约nginx资源，我们需要在端口号后面增加dbswitch,[http://127.0.0.1:9088/](http://127.0.0.1:9088/ "http://127.0.0.1:9088/")dbswitch访问

### 1、修改dbswtch的前端vue项目

这是dbswitch的前端代码，

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_gtkj6uNdj3.png)

但是我修改前端代码实出现这个报错，而我对vue就会皮毛，改也不会改，然后我就自己通过idea手动修改了打包后的静态资源

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_WWA3p7ASJf.png)

*   我将所有的静态资源的static改为dbswitch,如index.html

修改后的静态资源我放在了gitee上面了，如果自己不想修改的可以参考我的：[https://gitee.com/programmer-wanghengjie/dbswitchTable-synchronization.git](https://gitee.com/programmer-wanghengjie/dbswitchTable-synchronization.git "https://gitee.com/programmer-wanghengjie/dbswitchTable-synchronization.git")

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_I-kMkfzdUP.png)

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_IsxeiknPmx.png)

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_CJD8skHMdm.png)

### 2、修改WebMvcConfig

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image__DdSqDgp4I.png)

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 访问http://localhost:8080/index.html 都会跳转到classpath:/index.html下去找，即resources/index.html
    registry.addResourceHandler("/index.html").addResourceLocations("classpath:/index.html");
    registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/favicon.ico");
    // 访问http://localhost:8080/static/*** 都会跳转到classpath:/static/下去找，即resources/static/
    registry.addResourceHandler("/dbswitch/**").addResourceLocations("classpath:/dbswitch/");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/dbswitch").setViewName("forward:index.html");
  }

}
```

### 3、修改application.yml

```yaml
 static-path-pattern: /statics/dbswitch/**
```

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_OM90cFSENl.png)

然后启动项目之后访问地址：[http://127.0.0.1:9088/dbswitch#/login](http://127.0.0.1:9088/dbswitch#/login "http://127.0.0.1:9088/dbswitch#/login")

### 4、然后部署在开发环境上面配置nginx

我们一般在一台机器上部署项目，部署项目步骤

本工具纯Java语言开发，代码中的依赖全部来自于开源项目。

#### （1）编译打包

* 环境要求:

  **JDK**:>=1.8 （建议用JDK 1.8）

  **maven**:>=3.6

> Maven 仓库默认在国外， 国内使用难免很慢，可以更换为阿里云的仓库。 参考教程： [配置阿里云的仓库教程](https://gitee.com/link?target=https://www.runoob.com/maven/maven-repositories.html "配置阿里云的仓库教程")

**(2) Linux下编译：**

```bash
#最好上传自己修改后的版本
git clone https://gitee.com/inrgihc/dbswitch.git
cd dbswitch/
sh ./build.sh

```

#### （3）当编译打包命令执行完成后，会在dbswitch/target/目录下生成dbswitch-relase-x.x.x.tar.gz的打包文件，将文件拷贝到已安装JRE的部署机器上解压即可。

```bash
tar -zxvf dbswitch-release-1.6.9.tar.gz
```

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_iIWbezDtWL.png)

*   在dbswitch-release-1.6.9的bin中启动项目

```bash
./startup.sh
```

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_gbip0RKvRT.png)

### 5、在nginx中配置

```json
location ~ ^/dbswitch{
            proxy_pass  http://ip:9088;
        }
```

然后启动项目，我在我的虚拟机上面配置的nginx，访问项目，正常访问

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_pUJjEBYSZk.png)

# 五、阉割版的DBSwitch使用帮助手册

### 1、DBSwitch登录

开发环境登录地址：[http://ip:9088/dbswitch](http://ip:9088/dbswitch "http://ip:9088/dbswitch")

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_qgC8vXJGSW.png)

输入网址后进入DBSwitch系统登录页，须填写用户名和密码。

用户名：

密码：

### 2、核心功能

#### （1）数据源连接管理

见左侧“连接管理”🡪“添加”

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_1_GalWCrt9ki.png)

点击“添加之后”：

可以选择数据库名称，选择数据库类型，jdbc连接串和账户密码；

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_2_jWvpSMbgO4.png)

添加成功后示意图：

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_3_zGKznBWSJV.png)

测试是否连接成功

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_4_D89MZO4PRy.png)

#### （2）任务管理

见左侧“任务管理栏”🡪“任务安排”🡪“添加”

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_5_nyqZq9o3XD.png)

完成基本信息配置，点击下一步

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_6_zjtxC9ukTO.png)

完成源数据库配置：点击下一步

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_7_xjBZnUDnJQ.png)

完成目标数据库配置：点击下一步

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_8_zJZrBeGy7d.png)

同步表的关系映射，如果表名映射和字段名映射为空时，表示

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_9_JZcx9T-cAM.png)

如果目标表需要改名，点击添加表映射

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_10_MvsiXRW1vK.png)

添加成功后点击预览表名映射看是否改名成功（必须操作）

注意：这个查看表名映射关系的操作必须操作，有时候大家感觉自己添加映射之后没什么问题，但是有时候添加表名的时候是复制粘贴的，系统没识别，不查看表名关系映射直接操作下一步的话，可能会出现表名修改失败，导致同步的数据库表名和源端表名一样，需要走OA删除建的表导致时间浪费过长

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_11_diGaQjsJzj.png)

如果字段名需要修改，点击添加字段名映射

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_12_m3hH3uZkV0.png)

同样的步骤也要预览字段名

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_13_pvf4V9XKFd.png)

点击下一步之后，点击提交

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_14_jdKYSCiXK0.png)

点击发布也可以点击更多修改配置，但是一旦发布就不能再修改

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_15_VDJXSNRUdo.png)

点击执行同步表结构就开始了！

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_16_Kw0yycRf3x.png)

见左侧“任务管理栏”——》“调度记录”“任务安排列表”

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_17_kms8NjjbdT.png)

点击刚才任务中取的名字，查看任务日志

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_18_OQD2m_TJ5m.png)

如果同步成功以后，可以点击

见左侧“数据目录栏”——》“数据源导航树”——》“查看自己同步目标库的目标表”

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_19_Fbv1Otfxwq.png)

查看目标表的建表和赋权限的sql语句是否正确

注意：重点查看是否赋权限

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_20_mBm1cFK5os.png)

点击字段信息，查看同步后目标表的字段是否正确

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_21_3emQ9EOdJL.png)

点击取样数据主要是查看表中的数据，同步表之后数据为空，如果需要实现同步数据，请使用datax实现，使用datax同步数据之后可以使用dbswitch查看数据是否同步成功

![](https://csdn-image.oss-cn-beijing.aliyuncs.com/img/image_22_KaNzf4IYj6.png)

![image](https://user-images.githubusercontent.com/77565682/169270750-55ed940d-74d7-4464-86c7-da691f880c76.png)
