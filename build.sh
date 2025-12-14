#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'
BOLD='\033[1m'

SOURCE_DIR="source"

print_msg() {
    echo -e "${2}${1}${NC}"
}

print_header() {
    echo -e "\n${BOLD}${CYAN}========================================${NC}"
    echo -e "${BOLD}${CYAN}  $1${NC}"
    echo -e "${BOLD}${CYAN}========================================${NC}\n"
}

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

install_jdk() {
    print_header "Checking Java Development Kit (JDK)"
    
    if command_exists java && command_exists javac; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
        print_msg "✓ JDK is installed (version: $JAVA_VERSION)" "$GREEN"
    else
        print_msg "✗ JDK not found, installing..." "$YELLOW"
        
        if command_exists apt-get; then
            sudo apt-get update
            sudo apt-get install -y openjdk-17-jdk
        elif command_exists yum; then
            sudo yum install -y java-17-openjdk-devel
        elif command_exists dnf; then
            sudo dnf install -y java-17-openjdk-devel
        else
            print_msg "✗ Unsupported package manager, please install JDK manually" "$RED"
            exit 1
        fi
        
        if [ $? -eq 0 ]; then
            print_msg "✓ JDK installed successfully" "$GREEN"
        else
            print_msg "✗ JDK installation failed" "$RED"
            exit 1
        fi
    fi
    
    export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
    print_msg "JAVA_HOME set to: $JAVA_HOME" "$BLUE"
}

install_android_sdk() {
    print_header "Checking Android SDK"
    
    ANDROID_HOME="$HOME/Android/Sdk"
    
    if [ -d "$ANDROID_HOME" ] && [ -f "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
        print_msg "✓ Android SDK is installed" "$GREEN"
        
        print_msg "Checking and installing required SDK packages..." "$CYAN"
        yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses >/dev/null 2>&1
        "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;android-34" "build-tools;34.0.0" "platforms;android-36" "build-tools;35.0.0"
        print_msg "✓ SDK packages updated" "$GREEN"
    else
        print_msg "✗ Android SDK not found, installing..." "$YELLOW"
        
        CURRENT_DIR=$(pwd)
        mkdir -p "$ANDROID_HOME/cmdline-tools"
        cd "$ANDROID_HOME/cmdline-tools"
        
        SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip"
        print_msg "Downloading Android SDK command-line tools..." "$CYAN"
        wget -q --show-progress "$SDK_URL" -O commandlinetools.zip
        
        if [ $? -ne 0 ]; then
            print_msg "✗ Android SDK download failed" "$RED"
            cd "$CURRENT_DIR"
            exit 1
        fi
        
        unzip -q commandlinetools.zip
        mv cmdline-tools latest
        rm commandlinetools.zip
        
        print_msg "✓ Android SDK command-line tools installed" "$GREEN"
        
        print_msg "Accepting SDK licenses..." "$CYAN"
        yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses >/dev/null 2>&1
        
        print_msg "Installing SDK packages..." "$CYAN"
        "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;android-34" "build-tools;34.0.0" "platforms;android-36" "build-tools;35.0.0"
        
        cd "$CURRENT_DIR"
        print_msg "✓ Android SDK packages installed" "$GREEN"
    fi
    
    export ANDROID_HOME
    export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"
    print_msg "ANDROID_HOME set to: $ANDROID_HOME" "$BLUE"
}

install_gradle() {
    print_header "Checking Gradle"
    
    if command_exists gradle; then
        GRADLE_VERSION=$(gradle -v | grep "Gradle" | awk '{print $2}')
        print_msg "✓ Gradle is installed (version: $GRADLE_VERSION)" "$GREEN"
    else
        print_msg "✗ Gradle not found, installing..." "$YELLOW"
        
        GRADLE_VERSION="8.5"
        GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
        
        CURRENT_DIR=$(pwd)
        cd /tmp
        print_msg "Downloading Gradle ${GRADLE_VERSION}..." "$CYAN"
        wget -q --show-progress "$GRADLE_URL" -O gradle.zip
        
        if [ $? -ne 0 ]; then
            print_msg "✗ Gradle download failed" "$RED"
            cd "$CURRENT_DIR"
            exit 1
        fi
        
        sudo mkdir -p /opt/gradle
        sudo unzip -q gradle.zip -d /opt/gradle
        sudo ln -sf /opt/gradle/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle
        rm gradle.zip
        
        cd "$CURRENT_DIR"
        print_msg "✓ Gradle installed successfully" "$GREEN"
    fi
}

build_apk() {
    print_header "Building APK"
    
    if [ ! -d "$SOURCE_DIR" ]; then
        print_msg "✗ Source directory '$SOURCE_DIR' not found!" "$RED"
        exit 1
    fi
    
    cd "$SOURCE_DIR"
    print_msg "Building from directory: $(pwd)" "$BLUE"
    
    if [ -f "gradlew" ]; then
        chmod +x gradlew
        print_msg "✓ gradlew set as executable" "$GREEN"
    else
        print_msg "✗ gradlew not found!" "$RED"
        exit 1
    fi
    
    print_msg "\nRunning Gradle clean..." "$CYAN"
    ./gradlew clean
    
    print_msg "\nBuilding APK (this may take some time)..." "$MAGENTA"
    ./gradlew assembleDebug --stacktrace
    
    BUILD_RESULT=$?
    
    if [ $BUILD_RESULT -eq 0 ]; then
        print_msg "\n✓ APK build succeeded!" "$GREEN"
        
        APK_PATH=$(find . -name "*.apk" -type f 2>/dev/null)
        if [ -n "$APK_PATH" ]; then
            print_msg "\n${BOLD}📦 APK files:${NC}" "$CYAN"
            for apk in $APK_PATH; do
                APK_SIZE=$(du -h "$apk" | cut -f1)
                print_msg "  → $apk ($APK_SIZE)" "$GREEN"
            done
        fi
    else
        print_msg "\n✗ APK build failed!" "$RED"
        print_msg "Please check the error messages above." "$YELLOW"
        print_msg "\nYou can also try:" "$YELLOW"
        print_msg "  cd $SOURCE_DIR && ./gradlew assembleDebug --info" "$BLUE"
        exit 1
    fi
}

main() {
    print_header "Android APK Build Script"
    print_msg "Starting build process..." "$MAGENTA"
    
    install_jdk
    install_android_sdk
    install_gradle
    build_apk
    
    print_header "Build Complete! 🎉"
    print_msg "All tasks completed successfully!" "$GREEN"
}

main