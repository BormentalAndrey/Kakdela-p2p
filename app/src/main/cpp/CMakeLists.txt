cmake_minimum_required(VERSION 3.18.1)
project("kakdelap2p")

# WebRTC is a prebuilt AAR – nothing to build here
# This file just satisfies the externalNativeBuild requirement
add_library(kakdela-p2p SHARED dummy.cpp)

# Create a dummy source file so CMake is happy
file(WRITE ${CMAKE_BINARY_DIR}/dummy.cpp "// dummy")
