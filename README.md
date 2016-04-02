# image_processing_tests
Image processing tests for autonomous drone landing  
  
How to get OpenCV working (Windows)  
- Download OpenCV from https://sourceforge.net/projects/opencvlibrary/files/ 
- Extract to a folder
- Add the path to the bin folder eg. C:\Users\Wesley\Downloads\opencv\build\x64\vc14\bin onto the java path environmental variable
- Copy the .dll file in your java bin folder (eg. C:\Users\Wesley\Downloads\opencv\build\java\x64) into the previous bin folder 
- Don’t forget to load the dll in the java code (System.loadLibrary( Core.NATIVE_LIBRARY_NAME )
