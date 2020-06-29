# CodeEdit (Android)

![Android build](https://github.com/Kwasow/CodeEdit-Android/workflows/Android%20CI/badge.svg)

This project aims to create a full IDE for Android, that's based on a server
 implementation. The code would be stored on a self-hosted server and accesed
 through ssh and other networking tools via the app.

This allows to build and deploy projects directly from your Android phone and
gives you the power of a full OS on the go.

The app might get more useful when Android's desktop mode is made available to
everyone by Google in some version of Android in the future.

## Tested server distibutions

 - Ubuntu server 20.04 LTS

*Most linux distributions should work without issues*

The app requires the `ls`, `file` and `uname` commands to work on your remote system.
This means that macOS should also work.

## Current feature set

|                | Mobile             | Desktop |
|:---------------|:------------------:|:-------:|
| Server manager | :heavy_check_mark: |   :x:   |
| File Browser   | :heavy_check_mark: |   :x:   |
| Editor         |    `In progress`   |   :x:   |
| Terminal       | :heavy_check_mark: |   :x:   |
