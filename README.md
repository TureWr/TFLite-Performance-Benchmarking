# Summer of code 2024

This project aims to compare the performance of certain models on different hardware platforms: the Edge TPU USB Accelerator, CPU, mobile GPU, and mobile CPU. The testing is conducted using an Android application to evaluate performance on an Android device. Two models have been tested: an image classification model and an anomaly detection model.

## Table of Contents

1. [Application](#application)
   - [Description](#description)
   - [Running the Application](#running-the-application)
   - [Adding More Models](#adding-more-models)
   - [Adding More Images for Classification](#adding-more-images-for-classification)
2. [Hardware](#hardware)
3. [Image Classification](#image-classification)
   - [Description](#description)
   - [Performance Results](#performance-results)
   - [Installation & Setup](#installation--setup)
     - [Libraries & Frameworks](#libraries-&-frameworks)
     - [Setup & Run](#setup-&-run)
4. [Anomaly Detection](#anomaly-detection)
   - [Description](#description-1)
   - [Installation & Setup](#installation--setup-1)
     - [Libraries & Frameworks](#libraries-&-frameworks-1)
     - [Setup](#setup)
     - [Train model](#train-model)
5. [Discussion & Future Work](#discussion--future-work)
   - [Pytorch & GPU](#pytorch--gpu)
   - [TCN anomaly detection & TFLite](#tcn-anomaly-detection--tflite)
   - [Conversion from Pytorch to Tensorflow](#conversion-from-pytorch-to-tensorflow)

## Application

### Description

This project includes an Android application designed to benchmark the performance of TFLite models on mobile phones. The application is based on TensorFlow's [example image classification app](https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification), utilizing a pretrained MobileNetV2 model for image classification. When performing benchmarks, the app processes 10 images and records the time taken for each prediction to evaluate the hardware performance.

Currently, the application only supports image classification, as anomaly detection has not yet been fully implemented. Although a trained anomaly detection model from this project can be imported into the application, it cannot be used for predictions or benchmarking at this time.

### Running the Application

To run the application, the easiest method is using Android Studio. By opening the `application` folder in Android Studio, the project can be run directly on a virtual device. The app provides options to select the hardware for running the benchmark and the model to use (MobileNetV2 is the default). Once the selections are made, pressing the "Benchmark" button will display the results.

### Adding More Models

To add additional models to the application, download the desired models and place them in the `application/app/src/main/assets` directory. The application will recognize the new models if they are placed in this folder.

### Adding More Images for Classification

To add more images for classification, download the images and place them in the `application/app/src/main/assets/images` directory. The application supports JPEG format, but other formats may also work.


## Hardware 

 - CPU: AMD Ryzen™ 5 3500U with Radeon™ Vega Mobile Gfx × 8
 - Edge TPU: Google Edge TPU ML accelerator
 - GPU: NVIDIA GeForce RTX 4070 Ti
 - mobile GPU: Mali-G77 MP11 
 - mobile CPU: Octa-core (2x2.73 GHz Mongoose M5 & 2x2.50 GHz Cortex-A76 & 4x2.0 GHz Cortex-A55)

## Image classification

### Description

The image classification model used in this project is *MobileNet V2*, a lightweight convolutional neural network designed for efficient execution on mobile and edge devices. The tflite model was retrieved from the [Coral AI Model Zoo ](https://coral.ai/models/image-classification/) for testing across the various hardware platforms.


### Performance results

The results from running the model on 10 images, and the average time per image:

 
| Metric | Edge TPU | CPU | GPU | mobile GPU | mobile CPU | 
| --------------------- | --- | -------- | --- | ---------- | --- |
Average time per image |  25.75ms | 24.31ms | 20.33ms | 20.50 ms | 26.00 ms |

### Installation & Setup

#### Libraries and Frameworks

| Library/Framework | Version |
| --- | ---|
| **Python** | **3.9.19** |
| Tensorflow | 2.14.0|
| Pycoral  | 2.0.0 |
| Pillow | 10.3.0 |
| TFLite-runtime | 2.5.0.post1 |


#### Setup & Run
      
      cd img_classification
      pip install -r requirements.txt
      pip install https://github.com/google-coral/pycoral/releases/download/v2.0.0/tflite_runtime-2.5.0.post1-cp39-cp39-linux_x86_64.whl
      pip install https://github.com/google-coral/pycoral/releases/download/v2.0.0/pycoral-2.0.0-cp39-cp39-linux_x86_64.whl
      cd src
      python run_imgclass.py [HARDWARE]

Choose hardware from [cou, gpu, tpu].


## Anomaly detection

### Description
This project builds upon an existing anomaly detection model, specifically a TCN (Temporal Convolutional Network) autoencoder. The original model, developed by Markus Thill, can be found in this repository: [bioma-tcn-ae GitHub Repo](https://github.com/MarkusThill/bioma-tcn-ae/tree/main/src). The model is based on the research presented in the paper titled ["Bioma2020: TCN Autoencoder for Anomaly Detection"](https://www.gm.th-koeln.de/ciopwebpub/Thill20a.d/bioma2020-tcn.pdf). For training and testing, the previoius project utilizes the Mackey-Glass Anomaly Benchmark, with the corresponding source code available here: [MGAB GitHub Repo.](https://github.com/MarkusThill/MGAB)

The primary enhancements made in this project include updating the requirements to ensure compatibility and adding functionality to convert and save the model into TensorFlow Lite format, enabling it to run efficiently on the target application.

### Installation & Setup


#### Libraries and Frameworks

| Library/Framework | Version |
| --- | --- |
| **Python** | **3.9.19** |
| Numpy | 1.23.5 |
| Matplotlib | 3.3.1 |
| Tensorflow | 2.14.0 |
| Keras-tcn | 3.5.0 |
| Pandas | 2.2.2 |
| Scikit-learn | 1.3.0 |
| TFLite-runtime | 2.5.0.post1 |

#### Setup

      cd anomaly_detection
      pip install -r requirements.txt
      pip install https://github.com/google-coral/pycoral/releases/download/v2.0.0/tflite_runtime-2.5.0.post1-cp39-cp39-linux_x86_64.whl


#### Train model

      cd src
      python train.py [EPOCHS]

Choose epochs as an integer. 40 gives good estimate and took around 389 seconds to train using GPU.
The model will be saved in the folder `anomaly_detection/src/models`.


## Discussion & Future Work


| Objects achived | Description |
|---|---
 | Application | Created an application that can take a TfLite model and run on mobile GPU and CPU. |
|Image Classification | Run MobileNetV2 benchmark on TPU, GPU, CPU as well as on the application. |
 | Anomaly Detection | Train and run anomaly detection with similar result as linked repo. |
 

 | Future Work | Description |
 |---|---|
 |Convert Tensorflow model to TfLite | Anomaly detection model needs to be saved as a Tensorflow Lite for it to be able to run on the application. Investigate what layers in the model that needs modification for a conversion.|
 | PyTorch for mobile GPU | Investigate if there is more support for running on mobile GPU. |

### PyTorch & GPU

Throughout this project, we have mainly used TensorFlow models to maintain compatibility with GPUs on Android devices. PyTorch Mobile has not had the same support for running on mobile GPUs. However, there seem to exist prototypes for running on Android GPUs that might be interesting to investigate in the future.

### TCN Anomaly Detection & TFLite

In this project, we trained a Temporal Convolutional Network (TCN) model for anomaly detection on time-series data and tried to convert it to TensorFlow Lite format for deployment on mobile devices. However, we encountered issues with the TFLite model and could not run it properly, even though the original model performed correctly before conversion. This suggests a potential corruption in the TFLite conversion process. Some layers and functions are not convertable from the keras model into the TFLite format. To solve this, one would need to identify and rewrite the aspects of the model that cannot be transformed for the model to be able to run on edgetpu and on the application.

### Conversion from PyTorch to TensorFlow

While converting models from PyTorch to TensorFlow is theoretically feasible, our initial attempts revealed that this process is not straightforward and would likely require significant time and effort. Given these challenges, we decided to prioritize other research areas in the project. Future work could revisit this approach to leverage the flexibility of both PyTorch and TensorFlow ecosystems.
