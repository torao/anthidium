cmake_minimum_required(VERSION 3.10)
project(vsdb)

set(CMAKE_CXX_STANDARD 11)

add_executable(vsdb main.cpp src/block.h src/core.h src/core.cpp src/vsdb.h src/database.cpp src/errno_messages.cpp)