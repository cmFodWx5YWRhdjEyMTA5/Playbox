project(PROJECT_NAME)
cmake_minimum_required(VERSION 2.6 FATAL_ERROR)

find_package(OpenCV REQUIRED)

# Project Executable
add_executable (VideoFusion ./src/VideoFusion.cpp)
target_link_libraries(VideoFusion ${OpenCV_LIBS})
