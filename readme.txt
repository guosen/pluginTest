title: Gradle插件自定义
date: 2018-11-28 17:12:42
tags:
---
# 删除java文件夹、res文件夹
# 在main下面新建 groovy 文件夹
# 新建 com.guosen.plugin （名字自己定义）
# 新建resources 文件夹 和groovy同级，在resources里面新建META-INF。gradle-plugins目录，底下有guosen-plugin(插件名称).properties
```java
implementation-class=com.guosen.plugin.MyHelloWorldPlugin
//后面为实现类
```
# 插件model 的build.gradle
```java
apply plugin: 'java-library'
apply plugin: 'java-gradle-plugin'
apply plugin: 'groovy'
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation gradleApi()
    implementation localGroovy()
}




apply plugin: 'maven'
uploadArchives {
    repositories {
        mavenDeployer {
            pom.groupId = 'com.guosen.implePlugin'
            pom.artifactId = 'guosen-plugin'
            pom.version = 1.0
            //本地的Maven地址设置为D:/repos
            repository(url: uri('../repo'))
        }
    }
}

sourceCompatibility = "1.8"
targetCompatibility = "1.8"

```

# 整个项目的build。gradle
```java

// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply plugin: 'guosen-plugin'
buildscript {

    repositories {
        google()
        jcenter()
        maven {
            //cooker-plugin 所在的仓库
            //这里是发布在本地文件夹了
            url uri('./repo')
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.2.1'
        classpath 'com.guosen.implePlugin:guosen-plugin:1.0'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}

```

