//============================================================================
// Name        : VideoFusion.cpp
// Author      : darkerwaters LTD
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <opencv/cv.h>
#include <opencv/highgui.h>
#include <opencv2/opencv.hpp>

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>

using namespace std;
using namespace cv;

int main(int argc, char* argv[])
{
	VideoCapture vcap(0);
	  if(!vcap.isOpened()){
			 cout << "Error opening video stream or file" << endl;
			 return -1;
	  }

   int frame_width=   vcap.get(CV_CAP_PROP_FRAME_WIDTH);
   int frame_height=   vcap.get(CV_CAP_PROP_FRAME_HEIGHT);
   VideoWriter video("out.avi",CV_FOURCC('M','J','P','G'),10, Size(frame_width,frame_height),true);

   for(;;){

	   Mat frame;
	   vcap >> frame;
	   video.write(frame);
	   imshow( "Frame", frame );
	   char c = (char)waitKey(33);
	   if( c == 27 ) break;
	}
   return 0;
/*	VideoCapture cap(0); // open the default camera
	if(!cap.isOpened())  // check if we succeeded
		return -1;

	Mat edges;
	namedWindow("edges",1);
	for(;;)
	{
		Mat frame;
		cap >> frame; // get a new frame from camera
		cvtColor(frame, edges, CV_BGR2GRAY);
		GaussianBlur(edges, edges, Size(7,7), 1.5, 1.5);
		Canny(edges, edges, 0, 30, 3);
		imshow("edges", edges);
		if(waitKey(30) >= 0) break;
	}
	// the camera will be deinitialized automatically in VideoCapture destructor
	return 0;

	VideoCapture cap(0); // open the video camera no. 0

	if (!cap.isOpened())  // if not success, exit program
	{
		cout << "ERROR: Cannot open the video file" << endl;
		return -1;
	}

	namedWindow("MyVideo",CV_WINDOW_AUTOSIZE); //create a window called "MyVideo"

	double dWidth = cap.get(CV_CAP_PROP_FRAME_WIDTH); //get the width of frames of the video
	double dHeight = cap.get(CV_CAP_PROP_FRAME_HEIGHT); //get the height of frames of the video

	cout << "Frame Size = " << dWidth << "x" << dHeight << endl;

	Size frameSize(static_cast<int>(dWidth), static_cast<int>(dHeight));

	// Setup output video
	VideoWriter oVideoWriter("out.avi",
			CV_FOURCC('P','I','M','1'),
			20,
			Size(cap.get(CV_CAP_PROP_FRAME_WIDTH), cap.get(CV_CAP_PROP_FRAME_HEIGHT)));
	//VideoWriter oVideoWriter ("~/MyVideo.avi", CV_FOURCC('P','I','M','1'), 20, frameSize, true); //initialize the VideoWriter object

	if ( !oVideoWriter.isOpened() ) //if not initialize the VideoWriter successfully, exit the program
	{
		cout << "ERROR: Failed to write the video" << endl;
		return -1;
	}

	while (1)
	{

		Mat frame;

		bool bSuccess = cap.read(frame); // read a new frame from video

		if (!bSuccess) //if not success, break loop
		{
			cout << "ERROR: Cannot read a frame from video file" << endl;
			break;
		}

		oVideoWriter.write(frame); //writer the frame into the file

		imshow("MyVideo", frame); //show the frame in "MyVideo" window

		if (waitKey(10) == 27) //wait for 'esc' key press for 30ms. If 'esc' key is pressed, break loop
		{
			cout << "esc key is pressed by user" << endl;
			break;
		}
	}

	cap.release();
	oVideoWriter.release();

	return 0;*/
}

int versionThree()
{
    // Load input video
    cv::VideoCapture input_cap(0);
    if (!input_cap.isOpened())
    {
        std::cout << "!!! Input video could not be opened" << std::endl;
        return -1;
    }

    // Setup output video
    cv::VideoWriter output_cap("~/out.avi",
                               input_cap.get(CV_CAP_PROP_FOURCC),
                               input_cap.get(CV_CAP_PROP_FPS),
                               cv::Size(input_cap.get(CV_CAP_PROP_FRAME_WIDTH), input_cap.get(CV_CAP_PROP_FRAME_HEIGHT)));
    if (!output_cap.isOpened())
    {
        std::cout << "!!! Output video could not be opened" << std::endl;
        return -1;
    }

    // Loop to read from input and write to output
    cv::Mat frame;
    int counter = 0;
    while (counter++ < 200)
    {
        if (!input_cap.read(frame))
        {
        	std::cout << "!!! failed to read a frame" << std::endl;
            break;
        }

        output_cap.write(frame);
    }

    input_cap.release();
    output_cap.release();

    return 0;
}

int versionOne()
{
	VideoCapture cap(0); // open the video camera no. 0

	if (!cap.isOpened())  // if not success, exit program
	{
		cout << "ERROR: Cannot open the video file" << endl;
		return -1;
	}

	namedWindow("MyVideo",CV_WINDOW_AUTOSIZE); //create a window called "MyVideo"

	double dWidth = cap.get(CV_CAP_PROP_FRAME_WIDTH); //get the width of frames of the video
	double dHeight = cap.get(CV_CAP_PROP_FRAME_HEIGHT); //get the height of frames of the video
	double dFps = cap.get(CV_CAP_PROP_FPS);
	double dFourCC = cap.get(CV_CAP_PROP_FOURCC); //get the height of frames of the video

	cout << "Frame Size = " << dWidth << "x" << dHeight << " FPS:" << dFps << " in:" << dFourCC << endl;

	Size frameSize(static_cast<int>(dWidth), static_cast<int>(dHeight));

	// Setup output video
	VideoWriter oVideoWriter("out.avi",
			0,//CV_FOURCC('M','J','P','G'),
			25,
			Size(cap.get(CV_CAP_PROP_FRAME_WIDTH), cap.get(CV_CAP_PROP_FRAME_HEIGHT)),
			true);
	//VideoWriter oVideoWriter ("~/MyVideo.avi", CV_FOURCC('P','I','M','1'), 20, frameSize, true); //initialize the VideoWriter object

	if ( !oVideoWriter.isOpened() ) //if not initialize the VideoWriter successfully, exit the program
	{
		cout << "ERROR: Failed to write the video" << endl;
		return -1;
	}

	while (1)
	{

		Mat frame;

		bool bSuccess = cap.read(frame); // read a new frame from video

		if (!bSuccess) //if not success, break loop
		{
			cout << "ERROR: Cannot read a frame from video device" << endl;
			break;
		}

		oVideoWriter.write(frame); //writer the frame into the file

		imshow("MyVideo", frame); //show the frame in "MyVideo" window

		if (waitKey(40) == 27) //wait for 'esc' key press for 30ms. If 'esc' key is pressed, break loop
		{
			cout << "esc key is pressed by user" << endl;
			break;
		}
	}

	cap.release();

	return 0;


}

int versionTwo()
{

	VideoCapture vcap(0);
	if(!vcap.isOpened()){
		cout << "Error opening video stream or file" << endl;
		return -1;
	}

	int frame_width=   vcap.get(CV_CAP_PROP_FRAME_WIDTH);
	int frame_height=   vcap.get(CV_CAP_PROP_FRAME_HEIGHT);
	VideoWriter video("out.avi",CV_FOURCC('M','J','P','G'),10, Size(frame_width,frame_height),true);

	for(;;){

		Mat frame;
		vcap >> frame;
		video.write(frame);
		imshow( "Frame", frame );
		char c = (char)waitKey(33);
		if( c == 27 ) break;
	}
	return 0;
}

