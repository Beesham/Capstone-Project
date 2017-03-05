# BeerAC

## Description

BeerAC helps estimate your BAC(Blood Alcohol Content). It provides a way to search for information about any given beer
and allows you to set that as your preferred beer. 

## Key Concepts applied

* Data persistence through `Data Provider` and `SharedPreferences`
* Leverage's `BreweryDB` third party API to query data
* Provides a `widget`

## Pre-requisites

* Android SDK v25
* Android Build Tools v25.0.2
* Android Support Repository v25.1.0

## Getting Started

This project uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

### BreweryDb
An API key is required from BeweryDB in order for the app to work. Go get one from [HERE](http://www.brewerydb.com/developers "BreweryDB")  
The key is placed in `gradle.properties` file

### Admob
May require the test device ID to ensure test ads are displayed. Search `TODO`'s 

### Analytics
May require a tracking ID for analytics to work

## Third Party Libraries
* OkHttp
* Picasso
* Butterknife
* Simonvt Schematic Content Provider

## License

Copyright 2016 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
