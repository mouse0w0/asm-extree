# asm-extree
[![Download](https://api.bintray.com/packages/mouse0w0/maven/asm-extree/images/download.svg)](https://bintray.com/mouse0w0/maven/asm-extree/_latestVersion) [![](https://jitpack.io/v/Mouse0w0/asm-extree.svg)](https://jitpack.io/#Mouse0w0/asm-extree)

A new tree implementation for asm (A Java bytecode manipulation and analysis framework).

## How to use it
### Maven
Step 1. Add the JitPack repository to your build file
```xml
	<repositories>
		<repository>
		    <id>jcenter</id>
		    <url>https://jcenter.bintray.com</url>
		</repository>
	</repositories>
```
Step 2. Add the dependency
```xml
	<dependency>
	    <groupId>com.github.mouse0w0</groupId>
	    <artifactId>asm-extree</artifactId>
	    <version>8.0.4</version>
	</dependency>
```
### Gradle
Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:
```gradle
	allprojects {
		repositories {
			jcenter()
		}
	}
```
Step 2. Add the dependency
```gradle
	dependencies {
	        implementation 'com.github.mouse0w0:asm-extree:8.0.4'
	}
```
